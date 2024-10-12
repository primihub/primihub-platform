package com.primihub.biz.entity.sys.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SysOrgan {
    /**
     * 主键
     */
    private Long id;
    /**
     * 申请ID
     */
    private String applyId;
    /**
     * 机构id
     */
    private String organId;
    /**
     * 机构名称
     */
    private String organName;
    /**
     * 机构网关
     */
    private String organGateway;
    /**
     * 机构公钥
     */
    private String publicKey;
    /**
     * node 节点状态
     */
    private Integer nodeState;
    /**
     * fusion 服务状态
     */
    private Integer fusionState;
    /**
     * platform 服务状态
     */
    private Integer platformState;
    /**
     * 地方
     */
    private String country;
    private BigDecimal lat;
    private BigDecimal lon;

    /**
     * 申请状态 0 审核中 1 通过 2 拒绝
     */
    private Integer examineState;
    private String examineMsg;
    /**
     * 服务是否可用 0 不可用 1 可用
     */
    private Integer enable;
    /**
     * 是否是本方 0 否 1 是
     */
    private Integer identity = 1;

    private Integer isDel;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uTime;
}
