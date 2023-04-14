package com.primihub.biz.entity.sys.enumeration;

import lombok.Getter;

@Getter
public enum AuthTypeEnum {

    MENU(1,"菜单"),
    LIST(2,"列表"),
    BUTTON(3,"按钮");
    ;

    private Integer typeCode;
    private String typeDesc;

    AuthTypeEnum(Integer typeCode, String typeDesc) {
        this.typeCode = typeCode;
        this.typeDesc = typeDesc;
    }

    public static AuthTypeEnum findByCode(Integer code){
        for(AuthTypeEnum e: AuthTypeEnum.values()){
            if(e.getTypeCode().equals(code)){
                return e;
            }
        }
        return null;
    }

}
