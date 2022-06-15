package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DataProjectVo {
    /**
     * 项目id
     */
    private Long projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目描述
     */
    private String projectDesc;
    /**
     * 创建机构id
     */
    private Long organId;
    /**
     * 创建机构名称
     */
    private String organName;
    /**
     * 创建时间
     */
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createDate;
    /**
     * 机构列表
     */
    private List<String> organNames;

    /**
     * 数据资源信息
     */
    private List<DataResourceRecordVo> resources;
    /**
     * 模型信息
     */
    private List<ProjectModelVo> models;

}
