package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.SaveOrUpdateRoleParam;
import com.primihub.biz.service.sys.SysRoleService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Api(value = "角色接口",tags = "角色接口")
@RequestMapping("role")
@RestController
public class RoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping("saveOrUpdateRole")
    public BaseResultEntity saveOrUpdateRole(SaveOrUpdateRoleParam saveOrUpdateRoleParam){
        if(saveOrUpdateRoleParam.getRoleId()!=null){
            if(saveOrUpdateRoleParam.getRoleId()<=0L) {
                return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"roleId");
            }
        }
        return sysRoleService.saveOrUpdateRole(saveOrUpdateRoleParam);
    }

    @GetMapping("deleteSysRole")
    public BaseResultEntity deleteSysRole(Long roleId){
        if(roleId==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"roleId");
        }
        return sysRoleService.deleteSysRole(roleId);
    }

    @GetMapping("getRoleAuthTree")
    public BaseResultEntity getRoleAuthTree(Long roleId){
        if(roleId==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"roleId");
        }
        return sysRoleService.getRoleAuthTree(roleId);
    }

    @GetMapping("findRolePage")
    public BaseResultEntity findRolePage(String roleName,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10")Integer pageSize){
        return sysRoleService.findRolePage(roleName, pageNum, pageSize);
    }
}
