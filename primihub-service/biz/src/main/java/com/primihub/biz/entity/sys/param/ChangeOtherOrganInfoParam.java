package com.primihub.biz.entity.sys.param;

import lombok.Data;

@Data
public class ChangeOtherOrganInfoParam {
    private String organId;
    private String organName;
    private String gatewayAddress;
    private String publicKey;
    private String applyId;
}
