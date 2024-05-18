package com.primihub.biz.entity.data.dataenum;

import java.util.HashMap;
import java.util.Map;

public enum ProjectTypeEnum {
    MPC("MPC", "多方安全计算"),
    HFL("HFL", "横向联邦"),
    VFL("VFL", "纵向联邦");

    private final String code;
    private final String name;

    ProjectTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public static Map<String, ProjectTypeEnum> PROJECT_TYPE_MAP = new HashMap() {
        {
            for (ProjectTypeEnum e : ProjectTypeEnum.values()) {
                put(e.code, e);
            }
        }
    };
}
