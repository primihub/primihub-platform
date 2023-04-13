package com.primihub.entity.fusion.param;

import lombok.Data;

@Data
public class FusionConnectionParam {
    private String globalId;
    private String globalName;
    private String pinCode;
    private String gatewayAddress;
    private String publicKey;
    private String privateKey;
    private Integer dispatch = 0;
}
