package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class DataPirTask {

    private Long id;

    private Long taskId;

    private String providerOrganName;

    private String resourceId;

    private String resourceName;

    private String retrievalId;

    @JsonIgnore
    private Integer isDel;

    @JsonIgnore
    private Date createDate;

    @JsonIgnore
    private Date updateDate;

}
