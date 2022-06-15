package com.primihub.biz.entity.sys.param;

import lombok.Data;

@Data
public class SaveOrUpdateRoleParam {
    /**
     * 角色id
     */
    private Long roleId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 授权的权限id
     */
    private Long[] grantAuthArray;
    /**
     * 取消授权的权限id
     */
    private Long[] cancelAuthArray;
}
