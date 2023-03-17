package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 资源表
 */
@Data
public class DataResourceRecordVo {
    /**
     * 资源id
     */
    private Long resourceId;
    /**
     * 资源名称
     */
    private String resourceName;
    /**
     * 文件大小
     */
    private Integer fileSize;
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName="";
    /**
     * 机构id
     */
    private Long organId;
    /**
     * 机构名称
     */
    private String organName = "";
    /**
     * 是否授权
     */
    private Integer isAuthed;
    /**
     * 创建时间
     */
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;
}
