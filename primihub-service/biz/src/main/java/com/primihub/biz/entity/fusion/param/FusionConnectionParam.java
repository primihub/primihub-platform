package com.primihub.biz.entity.fusion.param;

import lombok.Data;

@Data
public class FusionConnectionParam {
    private String globalId;
    private String globalName;
    private String gatewayAddress;
    private String publicKey;
    private String privateKey;
}
