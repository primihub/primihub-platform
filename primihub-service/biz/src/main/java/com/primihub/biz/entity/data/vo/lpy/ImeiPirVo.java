package com.primihub.biz.entity.data.vo.lpy;

import com.primihub.biz.entity.data.po.lpy.DataImei;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImeiPirVo {
    private String imei;
    private String score;
    private String scoreModelType;

    public ImeiPirVo() {
    }

    public ImeiPirVo(String imei) {
        this.imei = imei;
    }

    public ImeiPirVo(String imei, String score, String scoreModelType) {
        this.imei = imei;
        this.score = score;
        this.scoreModelType = scoreModelType;
    }

    public ImeiPirVo(DataImei imei) {
        new ImeiPirVo(imei.getImei(), String.valueOf(imei.getScore()), imei.getScoreModelType());
    }
}
