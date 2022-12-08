package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class PsiTaskVo {

    /**
     * psi任务id
     */
    private Long taskId;

    /**
     * psi id
     */
    private Long psiId;

    /**
     * 对外展示的任务uuid 同时也是文件名称
     */
    private String taskIdName;

    /**
     * 运行状态 0未运行 1完成 2运行中 3失败 默认0
     */
    private Integer taskState;

    /**
     * 结果归属
     */
    private String ascription;

    /**
     * 结果归属类型 0一方 1双方
     */
    private Integer ascriptionType;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;


}
