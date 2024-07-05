package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class DataExamTask {

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

    private String targetField;

}
