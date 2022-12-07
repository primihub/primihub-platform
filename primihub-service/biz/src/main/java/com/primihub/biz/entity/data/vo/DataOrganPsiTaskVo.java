package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class DataOrganPsiTaskVo {
    /**
     * psi id
     */
    private Long dataPsiId;
    /**
     * 返回结果名称
     */
    private String resultName;
    /**
     * 总记录数
     */
    private String fileColumns;
    /**
     * 总行数
     */
    private String fileRows;
    /**
     * 真实任务id
     */
    private Long taskId;
    /**
     * 任务id名称
     */
    private String taskIdName;
    /**
     * 创建日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;
    /**
     * 修改日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;
}
