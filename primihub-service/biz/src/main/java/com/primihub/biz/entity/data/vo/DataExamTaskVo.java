package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DataExamTaskVo {
    private Long id;

    private String taskId;

    private String taskName;

    private String originOrganId;

    private String targetOrganId;

    private String originResourceId;

    private String targetResourceId;

    /**
     * 运行状态 0未运行 1完成 2运行中 3失败 4取消 默认0
     */
    private Integer taskState;

    @JsonIgnore
    private Integer isDel;

    @JsonIgnore
    private Date createDate;

    @JsonIgnore
    private Date updateDate;
}
