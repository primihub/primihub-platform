package com.primihub.biz.entity.data.vo.lpy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImeiResultVo {
    private String imei;

    public ImeiResultVo() {
    }

    public ImeiResultVo(String imei) {
        this.imei = imei;
    }
}
