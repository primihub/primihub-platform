package com.yyds.biz.service.sys;

import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.sys.param.AlterAuthNodeStatusParam;
import com.yyds.biz.entity.sys.param.CreateAuthNodeParam;
import com.yyds.biz.entity.sys.po.SysAuth;
import com.yyds.biz.entity.sys.vo.SysAuthNodeVO;
import com.yyds.biz.repository.primarydb.sys.SysAuthPrimarydbRepository;
import com.yyds.biz.repository.primaryredis.sys.SysAuthPrimaryRedisRepository;
import com.yyds.biz.repository.secondarydb.sys.SysAuthSecondarydbRepository;
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
        if(sysAuth.getAuthUrl()==null)
            sysAuth.setAuthUrl("");
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
        if(sysAuth==null||sysAuth.getAuthId()==null)
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_ALTER,"不存在该数据");
        if(sysAuth.getIsEditable().equals(0))
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_ALTER,"该记录是不可编辑状态");
        sysAuthPrimarydbRepository.updateSysAuthExplicit(alterAuthNodeStatusParam);
        sysAuthPrimaryRedisRepository.deleteSysAuthForBfs();
        return BaseResultEntity.success();
    }

    public BaseResultEntity deleteAuthNode(Long authId){
        SysAuth sysAuth=sysAuthSecondarydbRepository.selectSysAuthByAuthId(authId);
        if(sysAuth==null||sysAuth.getAuthId()==null)
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"不存在该数据");
        if(sysAuth.getIsEditable().equals(0))
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"该记录是不可编辑状态");
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

}
