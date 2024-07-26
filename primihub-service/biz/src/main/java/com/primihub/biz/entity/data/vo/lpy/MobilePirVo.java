package com.primihub.biz.entity.data.vo.lpy;


import com.primihub.biz.entity.data.po.lpy.DataMobile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobilePirVo {
    private String phoneNum;
    private String score;
    private String scoreModelType;

    public MobilePirVo() {
    }

    public MobilePirVo(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public MobilePirVo(String phoneNum, String score, String scoreModelType) {
        this.phoneNum = phoneNum;
        this.score = score;
        this.scoreModelType = scoreModelType;
    }

    public MobilePirVo(DataMobile mobile) {
        this(mobile.getPhoneNum(), String.valueOf(mobile.getScore()), mobile.getScoreModelType());
    }
}
