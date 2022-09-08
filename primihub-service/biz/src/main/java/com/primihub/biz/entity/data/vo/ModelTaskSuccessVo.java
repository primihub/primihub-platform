package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.primihub.biz.util.crypt.DateUtil;
import lombok.Data;

import java.util.Date;

@Data
public class ModelTaskSuccessVo {
    private Long taskId;
    private String taskIdName;
    private String taskName;
    @JsonIgnore
    private Long taskEndTime;
    private String taskEndDate;
    private Long modelId;
    private Long projectId;
    private String modelName;
    private String projectName;
    private String createdOrgan;
    @JsonIgnore
    private String serverAddress;
    @JsonIgnore
    private String providerOrganNames;
    private String[] providerOrgans;
    private Integer resourceNum;

    public String getTaskEndDate() {
        return (taskEndTime!=null && taskEndTime!=0)? DateUtil.formatDate(new Date(taskEndTime),DateUtil.DateStyle.TIME_FORMAT_NORMAL.getFormat()) :null;
    }
}
