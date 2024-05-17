package com.primihub.biz.entity.data.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RemoteRespVo {

    private RespHead head;
    private String response;
    private Map<String, String> respBody;

}
