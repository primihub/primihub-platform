package com.primihub.biz.entity.sys.param;

import lombok.Data;

@Data
public class SaveOrUpdateUserParam {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 账号名称
     */
    private String userAccount;
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 角色id集合
     */
    private Long[] roleIdList;
    /**
     * 是否禁用
     */
    private Integer isForbid;
}
