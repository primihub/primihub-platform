package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2022-04-25
 */
@Getter
@Setter
public class DataPsiTask {

    /**
     * psi任务id
     */
    private Long id;

    /**
     * psi id
     */
    private Long psiId;

    /**
     * 对外展示的任务uuid 同时也是文件名称
     */
    private String taskId;

    /**
     * 运行状态 0未运行 1完成 2运行中 3失败 4取消 默认0
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

    private Integer fileRows;

    /**
     * 文件路径
     */
    @JsonIgnore
    private String filePath;

    /**
     * 文件内容
     */
    @JsonIgnore
    private String fileContent;

    /**
     * 是否删除
     */
    @JsonIgnore
    private Integer isDel;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 修改时间
     */
    @JsonIgnore
    private Date updateDate;


}
