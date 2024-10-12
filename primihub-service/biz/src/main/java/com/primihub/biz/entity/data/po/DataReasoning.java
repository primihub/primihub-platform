package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class DataReasoning {
    private Long id;

    private String reasoningId;

    private String reasoningName;

    private String reasoningDesc;

    private Integer reasoningType;

    private Integer reasoningState;

    private Long taskId;

    private Long runTaskId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",  timezone = "GMT+8")
    private Date releaseDate;

    private Long userId;

    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",  timezone = "GMT+8")
    private Date createDate;
    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",  timezone = "GMT+8")
    private Date updateDate;
}
