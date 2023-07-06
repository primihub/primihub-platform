package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.primihub.biz.util.crypt.DateUtil;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class ModelTaskSuccessVo {
    private Long taskId;
    private String taskIdName;
    private String taskName;
    @JsonIgnore
    private Long taskEndTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskEndDate;
    private Long modelId;
    private Long projectId;
    private String modelName;
    private String projectName;
    private String createdOrganId;
    private String createdOrgan;
//    @JsonIgnore
    private String serverAddress;
    @JsonIgnore
    private String providerOrganNames;
    private List<Map<String,String>> providerOrgans;
    private Integer resourceNum;

    private Integer isCooperation;
    private Integer trainType;
}
