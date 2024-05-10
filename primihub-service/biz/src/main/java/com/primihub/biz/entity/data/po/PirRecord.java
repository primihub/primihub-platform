package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PirRecord {
    private Long id;
    private String pirName;
    private Long pirTaskId;
    // 任务状态(0未开始 1成功 2运行中 3失败 4取消)
    private Integer taskState;

    private Date startTime;
    private Integer commitRowsNum;
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
