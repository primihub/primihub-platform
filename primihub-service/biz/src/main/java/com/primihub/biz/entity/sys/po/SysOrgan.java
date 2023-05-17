package com.primihub.biz.entity.sys.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SysOrgan {
    private Long id;
    private String applyId;
    private String organId;
    private String organName;
    private String organGateway;
    private String publicKey;
    private Integer examineState;
    private String examineMsg;
    private Integer nodeState;
    private Integer fusionState;
    private Integer platformState;
    private String country;
    private BigDecimal lat;
    private BigDecimal lon;
    private Integer enable;
    private Integer identity = 1;
    private Integer isDel;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uTime;
}
