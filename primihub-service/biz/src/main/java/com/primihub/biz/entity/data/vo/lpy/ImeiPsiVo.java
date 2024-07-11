package com.primihub.biz.entity.data.vo.lpy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImeiPsiVo {
    private String imei;

    public ImeiPsiVo() {
    }

    public ImeiPsiVo(String imei) {
        this.imei = imei;
    }
}
