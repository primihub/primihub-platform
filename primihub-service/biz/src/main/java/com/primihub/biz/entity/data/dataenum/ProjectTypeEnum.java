package com.primihub.biz.entity.data.dataenum;

import java.util.HashMap;
import java.util.Map;

public enum ProjectTypeEnum {
    MPC("MPC", "多方安全计算", "start,dataSet,jointStatistical"),
    HFL("HFL", "横向联邦", "start,dataSet,fitTransform,model"),
    VFL("VFL", "纵向联邦", "start,dataSet,dataAlign,fitTransform,model");

    private final String code;
    private final String name;

    private final String componentNodes;

    ProjectTypeEnum(String code, String name, String componentNodes) {
        this.code = code;
        this.name = name;
        this.componentNodes = componentNodes;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getComponentNodes() {
        return componentNodes;
    }

    public static Map<String, ProjectTypeEnum> PROJECT_TYPE_MAP = new HashMap() {
        {
            for (ProjectTypeEnum e : ProjectTypeEnum.values()) {
                put(e.code, e);
            }
        }
    };
}
