package com.yyds.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

@Data
public class DataScript {

    /**
     * 脚本id
     */
    private Long scriptId;

    /**
     * 文件名称或文件夹名称
     */
    private String name;

    /**
     * 是否目录 0否 1是
     */
    private Integer catalogue;

    /**
     * 上级id
     */
    private Long pScriptId;

    /**
     * 脚本类型 0sql 1python
     */
    private Integer scriptType;

    /**
     * 脚本状态 0打开 1关闭 默认打开
     */
    private Integer scriptStatus;

    /**
     * 脚本内容
     */
    private String scriptContent;

    private Long userId;
    private Long organId;

    /**
     * 是否删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 修改时间
     */
    private Date updateDate;


}
