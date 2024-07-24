package com.primihub.biz.entity.data.po.lpy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataMap {
    private String id;
    private String idNum;
    private String phoneNum;

    public DataMap() {
    }

    public DataMap(String idNum, String phoneNum) {
        this.idNum = idNum;
        this.phoneNum = phoneNum;
    }
}
