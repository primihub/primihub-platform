package com.primihub.biz.entity.data.req;


import lombok.Data;

@Data
public class DataPsiReq {

    /**
     * psi 数字ID
     */
    private Long id;

    /**
     * 本机构id
     */
    private String ownOrganId;

    /**
     * 本机构资源id
     * 对应 data_resource 中的 resource_fusion_id
     */
    private String ownResourceId;

    /**
     * 本机构资源关键字
     */
    private String ownKeyword;

    /**
     * 其他机构id
     */
    private String otherOrganId;

    /**
     * 其他机构资源id
     */
    private String otherResourceId;

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
     * 备注
     */
    private String remarks;

    /**
     * 0、ECDH
     * 1、KKRT
     * 2、TEE
     */
    private Integer psiTag;


    private String taskName;

    private String teeOrganId;

}
