package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class DataPsiTaskVo {
    /**
     * psi id
     */
    private Long dataPsiId;
    /**
     * 返回结果名称
     */
    private String resultName;

    private String taskName;

    private String otherOrganId;

    private String otherOrganName;

    private Integer psiTag;
    /**
     * 真实任务id
     */
    private Long taskId;
    /**
     * 任务id名称
     */
    private String taskIdName;
    /**
     * 运行状态 0未运行 1完成 2运行中 3失败 默认0
     */
    private Integer taskState;
    /**
     * 归属
     */
    private String ascription;
    /**
     * 创建日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    private Long consuming;
    @JsonIgnore
    private Long taskStart;
    @JsonIgnore
    private Long taskEnd;

    public Long getConsuming() {
        if (taskStart==null) {
            return 0L;
        }
        if (taskEnd==null||taskEnd==0) {
            return (System.currentTimeMillis() - taskStart)/1000;
        }
        return (taskEnd - taskStart)/1000;
    }
}
