package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class DataProjectDetailsVo {
    /**
     * 本地真实ID
     */
    private Long id;
    /**
     * 项目id
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
     * 是否创建者
     */
    private Boolean creator = false;
    /**
     * 创建者名称
     */
    private String userName;
    /**
     * 项目状态 0审核中 1可用 2关闭
     */
    private Integer status;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;
    /**
     * 机构信息
     */
    private List<DataProjectOrganVo> organs;
}
