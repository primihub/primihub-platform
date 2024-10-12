package com.primihub.biz.entity.data.dataenum;

/**
 * 机构项目中参与身份 1发起者 2协作者
 */
public enum ModelProjectPartyRoleEnum {
    HOST(1),
    GUEST(2),
    ;

    private final Integer code;

    ModelProjectPartyRoleEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
