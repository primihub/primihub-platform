package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PirRecord {
    private Long id;
    private String recordId;
    private String pirName;

    private Long pirTaskId;
    // 任务状态(0未开始 1成功 2运行中 3失败 4取消)
    private Integer taskState;

    private String originOrganId;
    private String targetOrganId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    private Integer commitRowsNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    private Integer resultRowsNum;


    /**
     * 是否删除
     */
    @JsonIgnore
    private Integer isDel;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Date createDate;

    /**
     * 修改时间
     */
    @JsonIgnore
    private Date updateDate;
}
