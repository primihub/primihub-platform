package com.primihub.biz.entity.data.req;

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
     * 授权类型 1.公开 2.私有 3.指定机构可见
     */
    private Integer resourceAuthType;
    /**
     * 资源来源 1.文件上传 2.数据库链接
     */
    private Integer resourceSource;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 授权类型为指定机构可见 传入
     */
    private String publicOrganId;

    private String tag;
    /**
     * 标签条件状态  0:选择 1:输入
     */
    private Integer selectTag = 0;

    /**
     * 文件
     */
    private Long fileId;

    private List<DataResourceFieldReq> fieldList;

    private List<DataSourceOrganReq> fusionOrganList;

    private DataSourceReq dataSource;

    private String userName;

    private Integer derivation = 0;

    private Integer fileContainsY;

}
