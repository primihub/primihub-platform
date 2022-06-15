package com.primihub.biz.entity.data.vo;

import lombok.Data;

@Data
public class ComponentResourceYVo {
    public ComponentResourceYVo() {
    }

    public ComponentResourceYVo(String yId, String yName) {
        this.yId = yId;
        this.yName = yName;
    }

    private String yId;
    private String yName;
}
