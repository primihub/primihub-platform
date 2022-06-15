package com.yyds.biz.entity.data.req;

import lombok.Data;
import java.util.List;

@Data
public class DataResourceReq extends PageReq{
    /**
     * 资源id
     */
    private Long resourceId;
    /**
     * 资源名称
     */
    private String resourceName;
    /**
     * 资源描述
     */
    private String resourceDesc;
    /**
     * 资源分类 1.银行 2.电商 3.媒体 4.运营商 5.保险
     */
    private Integer resourceSortType;
    /**
     * 授权类型 1.公开 2.私有 3.已授权
     */
    private Integer resourceAuthType;
    /**
     * 资源来源 1.文件上传 2.数据库链接
     */
    private Integer resourceSource;

    private List<String> tags;

    private Long fileId;

}
