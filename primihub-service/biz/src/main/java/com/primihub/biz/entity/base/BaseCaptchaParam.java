package com.primihub.biz.entity.base;

import com.anji.captcha.model.vo.CaptchaVO;
import lombok.Data;

@Data
public class BaseCaptchaParam extends CaptchaVO {
    private String tokenKey;
}
