package com.primihub.biz.entity.data.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DataProjectReq{
    /**
     * 本地项目ID
     */
    private Long id;
    /**
     * 全局唯一机构ID
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目描述
     */
    private String projectDesc;
    /**
     * 项目机构列表
     */
    private List<DataProjectOrganReq> projectOrgans;

}
