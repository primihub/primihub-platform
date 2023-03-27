package com.primihub.biz.service.sys;

import com.primihub.biz.entity.sys.po.SysRa;
import com.primihub.biz.repository.primarydb.sys.SysAuthPrimarydbRepository;
import com.primihub.biz.repository.primarydb.sys.SysRolePrimarydbRepository;
import com.primihub.biz.repository.primaryredis.sys.SysAuthPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.sys.SysAuthSecondarydbRepository;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.AlterAuthNodeStatusParam;
import com.primihub.biz.entity.sys.param.CreateAuthNodeParam;
import com.primihub.biz.entity.sys.po.SysAuth;
import com.primihub.biz.entity.sys.vo.SysAuthNodeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysAuthService {

    @Autowired
    private SysAuthPrimarydbRepository sysAuthPrimarydbRepository;
    @Autowired
    private SysAuthSecondarydbRepository sysAuthSecondarydbRepository;
    @Autowired
    private SysAuthPrimaryRedisRepository sysAuthPrimaryRedisRepository;
    @Autowired
    private SysRolePrimarydbRepository sysRolePrimarydbRepository;

    public BaseResultEntity createAuthNode(CreateAuthNodeParam createAuthNodeParam){
        SysAuth parentSysAuth=createAuthNodeParam.getPAuthId().equals(0L)?null: sysAuthSecondarydbRepository.selectSysAuthByAuthId(createAuthNodeParam.getPAuthId());
        SysAuth sysAuth=new SysAuth();
        BeanUtils.copyProperties(createAuthNodeParam,sysAuth);
        if(sysAuth.getPAuthId().equals(0L)){
            sysAuth.setRAuthId(0L);
            sysAuth.setAuthDepth(0);
        }else{
            sysAuth.setRAuthId(parentSysAuth.getRAuthId());
            sysAuth.setAuthDepth(parentSysAuth.getAuthDepth()+1);
        }
        sysAuth.setIsEditable(1);
        sysAuth.setIsDel(0);
        sysAuth.setFullPath("");
        if(sysAuth.getAuthUrl()==null) {
            sysAuth.setAuthUrl("");
        }
        sysAuthPrimarydbRepository.insertSysAuth(sysAuth);
        if(parentSysAuth!=null) {
            sysAuth.setFullPath(new StringBuilder().append(parentSysAuth.getFullPath()).append(",").append(sysAuth.getAuthId()).toString());
        }else{
            sysAuth.setFullPath(sysAuth.getAuthId().toString());
        }
        if(sysAuth.getRAuthId().equals(0L)) {
            sysAuthPrimarydbRepository.updateRAuthIdAndFullPath(sysAuth.getAuthId(),sysAuth.getAuthId(),sysAuth.getFullPath());
        }else {
            sysAuthPrimarydbRepository.updateRAuthIdAndFullPath(sysAuth.getAuthId(),null,sysAuth.getFullPath());
        }
        sysAuthPrimaryRedisRepository.deleteSysAuthForBfs();
        Map map=new HashMap<>();
        map.put("sysAuth",sysAuth);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity alterAuthNodeStatus(AlterAuthNodeStatusParam alterAuthNodeStatusParam){
        SysAuth sysAuth=sysAuthSecondarydbRepository.selectSysAuthByAuthId(alterAuthNodeStatusParam.getAuthId());
        if(sysAuth==null||sysAuth.getAuthId()==null) {
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_ALTER,"不存在该数据");
        }
        if(sysAuth.getIsEditable().equals(0)) {
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_ALTER,"该记录是不可编辑状态");
        }
        sysAuthPrimarydbRepository.updateSysAuthExplicit(alterAuthNodeStatusParam);
        sysAuthPrimaryRedisRepository.deleteSysAuthForBfs();
        return BaseResultEntity.success();
    }

    public BaseResultEntity deleteAuthNode(Long authId){
        SysAuth sysAuth=sysAuthSecondarydbRepository.selectSysAuthByAuthId(authId);
        if(sysAuth==null||sysAuth.getAuthId()==null) {
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"不存在该数据");
        }
        if(sysAuth.getIsEditable().equals(0)) {
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"该记录是不可编辑状态");
        }
        sysAuthPrimarydbRepository.deleteSysAuth(authId);
        sysAuthPrimaryRedisRepository.deleteSysAuthForBfs();
        return BaseResultEntity.success();
    }

    public BaseResultEntity getAuthTree(){
        List<SysAuthNodeVO> sysAuthRootList = getSysAuthTree(new HashSet<>());
        Map resultMap=new HashMap<>();
        resultMap.put("sysAuthRootList",sysAuthRootList);
        return BaseResultEntity.success(resultMap);
    }

    public List<SysAuthNodeVO> getSysAuthTree(Set<Long> grantAuthSet) {
        List<SysAuthNodeVO> list = getSysAuthForBfs();
        Map<Long,SysAuthNodeVO> map=new HashMap<>();

        List<SysAuthNodeVO> sysAuthRootList=new ArrayList<>();
        for(SysAuthNodeVO node:list){
            if(node.getPAuthId().equals(0L)){
                sysAuthRootList.add(node);
            }else{
                SysAuthNodeVO parentSysAuthNodeVO=map.get(node.getPAuthId());
                if(parentSysAuthNodeVO.getChildren()==null){
                    parentSysAuthNodeVO.setChildren(new ArrayList<>());
                }
                parentSysAuthNodeVO.getChildren().add(node);
            }

            if(grantAuthSet.contains(node.getAuthId())){
                node.setIsGrant(1);
            }
            map.put(node.getAuthId(),node);
        }
        return sysAuthRootList;
    }

    public Map<String,SysAuthNodeVO> getSysAuthUrlMapping() {
        List<SysAuthNodeVO> list = getSysAuthForBfs();
        return list.stream().collect(Collectors.toMap(SysAuthNodeVO::getAuthUrl,item->item,(x,y)->x));
    }

    public List<SysAuthNodeVO> getSysAuthForBfs() {
        List<SysAuthNodeVO> list = sysAuthPrimaryRedisRepository.getSysAuthForBFS();
        if(list==null||list.size()==0) {
            list = sysAuthSecondarydbRepository.selectAllSysAuthForBFS();
            sysAuthPrimaryRedisRepository.setSysAuthForBFS(list);
        }
        return list;
    }

    public BaseResultEntity generateAllAuth(){
        String[][] authArray=new String[][]{
                {"项目管理","Project","1","","","1","1"},
                {"项目列表","ProjectList","2","Project","/project/getProjectList","1","1"},
                {"项目详情","ProjectDetail","3","Project","/project/getProjectDetails","1","1"},
                {"任务详情","ModelTaskDetail","3","Project","/project/getProjectDetails","1","1"},
                {"新建项目","ProjectCreate","3","ProjectList","/project/saveOrUpdateProject","1","1"},
                {"关闭项目","ProjectDelete","3","ProjectList","/project/closeProject","1","1"},
                {"模型管理","Model","1","","/model/getmodellist","1","1"},
                {"模型列表","ModelList","2","Model","/model/getmodellist","1","1"},
                {"模型详情","ModelDetail","3","ModelList","/model/getdatamodel","1","1"},
                {"模型查看","ModelView","3","ModelList","/model/getdatamodel","1","1"},
                {"添加模型","ModelCreate","3","ModelList","/model/saveModelAndComponent","1","1"},
                {"模型编辑","ModelEdit","3","ModelList","/model/saveModelAndComponent","1","1"},
                {"执行记录列表","ModelTaskHistory","3","ModelList","/task/saveModelAndComponent","1","1"},
                {"模型运行","ModelRun","3","ModelList","/model/runTaskModel","1","1"},
                {"下载结果","ModelResultDownload","3","ModelList","/task/downloadTaskFile","1","1"},
                {"匿踪查询","PrivateSearch","1","","/fusionResource/getResourceList","1","1"},
                {"匿踪查询按钮","PrivateSearchButton","3","PrivateSearch","/pir/pirSubmitTask","1","1"},
                {"匿踪查询列表","PrivateSearchList","2","PrivateSearch","/pir/downloadPirTask","1","1"},
                {"隐私求交","PSI","1","","","1","1"},
                {"求交任务","PSITask","2","PSI","/psi/getPsiResourceAllocationList","1","1"},
                {"求交结果","PSIResult","2","PSI","/psi/getPsiTaskList","1","1"},
                {"资源管理","ResourceMenu","1","","","1","1"},
                {"资源概览","ResourceList","2","ResourceMenu","/resource/getdataresourcelist","1","1"},
                {"资源详情","ResourceDetail","3","ResourceMenu","/resource/getdataresource","1","1"},
                {"上传资源","ResourceUpload","3","ResourceMenu","/resource/saveorupdateresource","1","1"},
                {"编辑资源","ResourceEdit","3","ResourceMenu","/resource/saveorupdateresource","1","1"},
                {"联邦资源","UnionList","2","ResourceMenu","/fusionResource/getResourceList","1","1"},
                {"联邦资源详情","UnionResourceDetail","3","UnionList","/fusionResource/getDataResource","1","1"},
                {"系统设置","Setting","1","","","1","0"},
                {"用户管理","UserManage","2","Setting","/user/findUserPage","1","0"},
                {"用户新增","UserAdd","3","UserManage","/user/saveOrUpdateUser","1","0"},
                {"用户编辑","UserEdit","3","UserManage","/user/saveOrUpdateUser","1","0"},
                {"用户删除","UserDelete","3","UserManage","/user/deleteSysUser","1","0"},
                {"密码重置","UserPasswordReset","3","UserManage","/user/initPassword","1","0"},
                {"角色管理","RoleManage","2","Setting","/role/findRolePage","1","0"},
                {"角色新增","RoleAdd","3","RoleManage","/role/saveOrUpdateRole","1","0"},
                {"角色编辑","RoleEdit","3","RoleManage","/role/saveOrUpdateRole","1","0"},
                {"角色删除","RoleDelete","3","RoleManage","/role/deleteSysRole","1","0"},
                {"菜单管理","MenuManage","2","Setting","/auth/getAuthTree","1","0"},
                {"菜单新增","MenuAdd","3","MenuManage","/auth/createAuthNode","1","0"},
                {"菜单编辑","MenuEdit","3","MenuManage","/auth/alterAuthNodeStatus","1","0"},
                {"菜单编辑","MenuDelete","3","MenuManage","/auth/deleteAuthNode","1","0"},
                {"中心管理","CenterManage","2","Setting","","1","0"},
                {"编辑机构信息","OrganChange","3","CenterManage","/organ/changeLocalOrganInfo","1","0"},
                {"添加中心节点","FusionAdd","3","CenterManage","/fusion/registerConnection","1","0"},
                {"删除中心节点","FusionDelete","3","CenterManage","/fusion/deleteConnection","1","0"},
                {"创建群组","GroupCreate","3","CenterManage","/fusion/createGroup","1","0"},
                {"加入群组","GroupJoin","3","CenterManage","/fusion/joinGroup","1","0"},
                {"退出群组","GroupExit","3","CenterManage","/fusion/exitGroup","1","0"},
                {"项目禁用","closeProject","3","ProjectDetail","/project/closeProject","1","0"},
                {"项目启动","openProject","3","ProjectDetail","/project/openProject","1","0"},
                {"模型任务删除","deleteModelTask","3","ProjectDetail","/task/deleteTask","1","0"},
                {"模型复制","copyModelTask","3","ProjectDetail","","1","0"},
                {"模型推理","ModelReasoning","1","","","1","0"},
                {"模型推理列表","ModelReasoningList","2","ModelReasoning","","1","0"},
                {"模型推理任务","ModelReasoningTask","3","ModelReasoning","","1","0"},
                {"模型推理详情","ModelReasoningDetail","3","ModelReasoning","","1","0"},
                {"日志","Log","1","","","1","0"},
        };

        Map<String,SysAuth> map=new HashMap<>();
        Map<String,Integer> indexMap=new HashMap();
        List<SysRa> adminRaList=new ArrayList<>();
        List<SysRa> anotherRaList=new ArrayList<>();
        for(String[] auth:authArray) {
            SysAuth sysAuth=new SysAuth();
            sysAuth.setAuthName(auth[0]);
            sysAuth.setAuthCode(auth[1]);
            sysAuth.setAuthType(Integer.parseInt(auth[2]));
            sysAuth.setFullPath("");
            sysAuth.setAuthUrl(auth[4]);
            sysAuth.setDataAuthCode("own");
            sysAuth.setIsShow(Integer.parseInt(auth[5]));
            sysAuth.setIsEditable(Integer.parseInt(auth[6]));
            sysAuth.setIsDel(0);

            int index=indexMap.getOrDefault(auth[3],1);
            sysAuth.setAuthIndex(index);
            indexMap.put(auth[3],index+1);

            SysAuth parent=map.get(auth[3]);
            if("".equals(auth[3])) {
                sysAuth.setPAuthId(0L);
                sysAuth.setRAuthId(0L);
                sysAuth.setAuthDepth(0);
            } else {
                sysAuth.setPAuthId(parent.getAuthId());
                sysAuth.setRAuthId(parent.getRAuthId());
                sysAuth.setAuthDepth(parent.getAuthDepth()+1);
            }

            sysAuthPrimarydbRepository.insertSysAuth(sysAuth);

            if(parent!=null) {
                sysAuth.setFullPath(new StringBuilder().append(parent.getFullPath()).append(",").append(sysAuth.getAuthId()).toString());
            }else{
                sysAuth.setFullPath(sysAuth.getAuthId().toString());
            }
            if(sysAuth.getRAuthId().equals(0L)) {
                sysAuth.setRAuthId(sysAuth.getAuthId());
                sysAuthPrimarydbRepository.updateRAuthIdAndFullPath(sysAuth.getAuthId(),sysAuth.getAuthId(),sysAuth.getFullPath());
            }else {
                sysAuthPrimarydbRepository.updateRAuthIdAndFullPath(sysAuth.getAuthId(),null,sysAuth.getFullPath());
            }
            map.put(auth[1],sysAuth);
            SysRa sysRa=new SysRa();
            sysRa.setAuthId(sysAuth.getAuthId());
            sysRa.setRoleId(1L);
            sysRa.setIsDel(0);
            adminRaList.add(sysRa);

            if(sysAuth.getAuthId()<24) {
                SysRa anotherSysRa = new SysRa();
                anotherSysRa.setAuthId(sysAuth.getAuthId());
                anotherSysRa.setRoleId(1000L);
                anotherSysRa.setIsDel(0);
                anotherRaList.add(anotherSysRa);
            }
        }


        sysRolePrimarydbRepository.insertSysRaBatch(adminRaList);
        if(anotherRaList.size()!=0) {
            sysRolePrimarydbRepository.insertSysRaBatch(anotherRaList);
        }
        return BaseResultEntity.success();
    }

}
