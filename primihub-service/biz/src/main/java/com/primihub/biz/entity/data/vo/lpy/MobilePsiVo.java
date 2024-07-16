package com.primihub.biz.entity.data.vo.lpy;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobilePsiVo {
    private String phoneNum;

    public MobilePsiVo() {
    }

    public MobilePsiVo(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
