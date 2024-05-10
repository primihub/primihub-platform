package com.primihub.biz.entity.data.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemoteRespVo {

    private RespHead head;
    private String response;
    private RespResp respBody;

    @Getter
    @Setter
    public class RespHead {
        private String requestRefId;
        private String responseRefId;
        private String result;
        private String responseCode;
        private String responseMsg;
    }

    @Getter
    @Setter
    public class RespResp {
        private String truth_score;
    }

}
