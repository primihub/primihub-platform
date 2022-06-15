package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class DataProjectListVo {
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
     * 机构id
     */
    private Long organId;
    /**
     * 机构数
     */
    private Integer organNum;
    /**
     * 资源数
     */
    private Integer resourceNum;
    /**
     * 机构数
     */
    private Integer resourceOrganNum;
    /**
     * 已授权资源数
     */
    private Integer authResourceNum;
    /**
     * 模型数量
     */
    private Integer modelNum;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户名称
     */
    private String userName = "";
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createDate;
}
