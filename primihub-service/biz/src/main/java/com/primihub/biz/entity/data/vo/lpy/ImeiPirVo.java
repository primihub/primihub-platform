package com.primihub.biz.entity.data.vo.lpy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImeiPirVo {
    private String imei;

    public ImeiPirVo() {
    }

    public ImeiPirVo(String imei) {
        this.imei = imei;
    }
}
