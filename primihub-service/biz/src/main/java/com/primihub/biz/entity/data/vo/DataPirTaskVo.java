package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DataPirTaskVo {

    private String serverAddress;

    private Long taskId;

    private String organId;

    private String organName;

    private String resourceId;

    private String resourceName;

    private Integer resourceRowsCount;

    private Integer resourceColumnCount;

    private Integer resourceContainsY;

    private Integer resourceYRowsCount;

    private BigDecimal resourceYRatio;

    private String retrievalId;

    private Integer available;


    /**
     * 任务状态(0未开始 1成功 2查询中 3失败)
     */
    private Integer taskState;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createDate;
}
