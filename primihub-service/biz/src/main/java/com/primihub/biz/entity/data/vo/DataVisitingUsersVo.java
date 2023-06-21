package com.primihub.biz.entity.data.vo;

import lombok.Data;

@Data
public class DataVisitingUsersVo {
    private String label;
    private String value;
    private String key;

    public DataVisitingUsersVo(String label, String value, String key) {
        this.label = label;
        this.value = value;
        this.key = key;
    }

    public DataVisitingUsersVo() {
    }
}