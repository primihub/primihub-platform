package com.primihub.biz.entity.data.vo;

import lombok.Data;

@Data
public class DataFileHandleFieldVo {
    public DataFileHandleFieldVo() {
    }

    public DataFileHandleFieldVo(String name,boolean isCheck) {
        this.name = name;
        this.isCheck = isCheck;
        this.desc = "";
    }

    public DataFileHandleFieldVo(String name) {
        this.name = name;
        this.desc = "";
    }

    public DataFileHandleFieldVo(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    private String name;
    private String desc;
    private boolean isCheck = false;
}
