package com.primihub.biz.service.sys;

import com.primihub.biz.repository.primarydb.sys.SysRolePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysRoleSecondarydbRepository;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageParam;
import com.primihub.biz.entity.sys.param.SaveOrUpdateRoleParam;
import com.primihub.biz.entity.sys.po.SysRa;
import com.primihub.biz.entity.sys.po.SysRole;
import com.primihub.biz.entity.sys.vo.SysAuthNodeVO;
import com.primihub.biz.entity.sys.vo.SysRoleListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SysRoleService {

    @Autowired
    private SysRolePrimarydbRepository sysRolePrimarydbRepository;
    @Autowired
    private SysRoleSecondarydbRepository sysRoleSecondarydbRepository;
    @Autowired
    private SysAuthService sysAuthService;

    public BaseResultEntity saveOrUpdateRole(SaveOrUpdateRoleParam saveOrUpdateRoleParam) {
        Long roleId=saveOrUpdateRoleParam.getRoleId();
        SysRole sysRole;
        if(roleId==null){
            if(saveOrUpdateRoleParam.getRoleName()==null) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"roleId");
            }
            sysRole=new SysRole();
            sysRole.setRoleName(saveOrUpdateRoleParam.getRoleName());
            sysRole.setIsEditable(1);
            sysRole.setIsDel(0);
            sysRolePrimarydbRepository.insertSysRole(sysRole);
            roleId=sysRole.getRoleId();
        }else{
            sysRole=sysRoleSecondarydbRepository.selectSysRoleByRoleId(roleId);
            if(sysRole==null||sysRole.getRoleId()==null) {
                return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_ALTER,"不存在该数据");
            }
            if(sysRole.getIsEditable().equals(0)) {
                return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_ALTER,"该记录是不可编辑状态");
            }
            Map paramMap=new HashMap(){
                {
                    put("roleId",saveOrUpdateRoleParam.getRoleId());
                    put("roleName",saveOrUpdateRoleParam.getRoleName());
                }
            };
            sysRolePrimarydbRepository.updateSysRole(paramMap);
        }

        Long[] grantAuthArray=saveOrUpdateRoleParam.getGrantAuthArray();
        if(grantAuthArray!=null&&grantAuthArray.length!=0){
            sysRolePrimarydbRepository.deleteSysRaBatch(grantAuthArray,roleId);
            List<SysRa> raList=new ArrayList<>();
            for(Long authId:grantAuthArray){
                SysRa sysRa=new SysRa();
                sysRa.setRoleId(roleId);
                sysRa.setAuthId(authId);
                sysRa.setIsDel(0);
                raList.add(sysRa);
            }
            sysRolePrimarydbRepository.insertSysRaBatch(raList);
        }

        Long[] cancelAuthArray=saveOrUpdateRoleParam.getCancelAuthArray();
        if(cancelAuthArray!=null&&cancelAuthArray.length!=0){
            sysRolePrimarydbRepository.deleteSysRaBatch(cancelAuthArray,roleId);
        }

        Map map=new HashMap<>();
        map.put("sysRole",sysRole);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity deleteSysRole(Long roleId){
        SysRole sysRole=sysRoleSecondarydbRepository.selectSysRoleByRoleId(roleId);
        if(sysRole==null||sysRole.getRoleId()==null) {
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"不存在该数据");
        }
        if(sysRole.getIsEditable().equals(0)) {
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"该记录是不可编辑状态");
        }
        Long userCount = sysRoleSecondarydbRepository.selectUserCountByRole(roleId);
        if (userCount>0) {
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"该记录下存在用户");
        }
        sysRolePrimarydbRepository.deleteSysRole(roleId);
        sysRolePrimarydbRepository.deleteSysRa(roleId);
        return BaseResultEntity.success();
    }

    public BaseResultEntity getRoleAuthTree(Long roleId){
        SysRole sysRole=sysRoleSecondarydbRepository.selectSysRoleByRoleId(roleId);
        Set<Long> authSet=sysRoleSecondarydbRepository.selectRaByRoleId(roleId);
        List<SysAuthNodeVO> roleAuthRootList=sysAuthService.getSysAuthTree(authSet);
        Map map=new HashMap<>();
        map.put("roleAuthRootList",roleAuthRootList);
        map.put("sysRole",sysRole);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity findRolePage(String roleName,Integer pageNum,Integer pageSize){
        PageParam pageParam=new PageParam(pageNum,pageSize);
        Map paramMap=new HashMap(){
            {
                put("roleName",roleName);
                put("pageIndex",pageParam.getPageIndex());
                put("pageSize",pageParam.getPageSize()+1);
            }
        };
        List<SysRoleListVO> sysRoleList=sysRoleSecondarydbRepository.selectRoleListByParam(paramMap);
        Long count=sysRoleSecondarydbRepository.selectRoleListCountByParam(paramMap);
        pageParam.isLoadMore(sysRoleList);
        pageParam.initItemTotalCount(count);
        Map map=new HashMap<>();
        map.put("sysRoleList",sysRoleList);
        map.put("pageParam",pageParam);
        return BaseResultEntity.success(map);
    }

}
