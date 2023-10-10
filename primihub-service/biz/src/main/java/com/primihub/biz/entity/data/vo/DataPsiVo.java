package com.primihub.biz.entity.data.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;


@Data
public class DataPsiVo {

    /**
     * psi 主键
     */
    private Long id;

    /**
     * 本机构id
     */
    private String ownOrganId;

    /**
     * 本机构名称
     */
    private String ownOrganName;

    /**
     * 本机构资源id
     */
    private String ownResourceId;

    /**
     * 本机构资源名称
     */
    private String ownResourceName;

    /**
     * 本机构资源关键字
     */
    private String ownKeyword;

    /**
     * 其他机构id
     */
    private String otherOrganId;
    /**
     * 其他机构名称
     */
    private String otherOrganName;

    /**
     * 其他机构资源id
     */
    private String otherResourceId;
    /**
     * 其他机构资源名称
     */
    private String otherResourceName;

    /**
     * 其他机构资源关键字
     */
    private String otherKeyword;

    /**
     * 文件路径输出类型 0默认 自动生成
     */
    private Integer outputFilePathType;

    /**
     * 输出内容是否不去重 默认0 不去重 1去重
     */
    private Integer outputNoRepeat;

    /**
     * 0、ECDH
     * 1、KKRT
     */
    private Integer tag;

    /**
     * 结果名称
     */
    private String resultName;

    /**
     * 输出内容 默认0 0交集 1差集
     */
    private Integer outputContent;

    /**
     * 输出格式
     */
    private String outputFormat;

    /**
     * 结果获取方 多机构","号间隔
     */
    private String resultOrganIds;

    /**
     * 结果获取方 多机构","号间隔
     */
    private String resultOrganName;

    /**
     * 备注
     */
    private String remarks;


    /**
     * 创建时间
     */
//    @JsonIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 真实任务id
     */
    private Long taskId;
    /**
     * 任务id名称
     */
    private String taskIdName;

    private String taskName;
    /**
     * 运行状态 0未运行 1完成 2运行中 3失败 4取消 默认0
     */
    private Integer taskState;

    private String teeOrganId;
    private String teeOrganName;
    private String taskError;

    private List<LinkedHashMap<String, Object>> dataList;

    private Long taskStartTime;
    private Long taskEndTime;

    public Long getConsuming(){
        if ((taskStartTime!=null&&taskStartTime!=0L)&&(taskEndTime!=null&&taskEndTime!=0L)){
            return (taskEndTime-taskStartTime)/1000;
        }
        return 0L;
    }
}
