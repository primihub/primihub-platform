package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataPsiResourceReq {
    private Long id;
    private Long resourceId;
    /**
     * 表结构模板
     */
    private String tableStructureTemplate;

    /**
     * 机构类型 0 机构资源 默认0
     */
    private Integer organType;

    /**
     * 是否允许结果出现在对方节点上 0允许 1不允许
     */
    private Integer resultsAllowOpen;

    /**
     * 关键字 逗号间隔,
     */
    private String keywordList;

    /**
     * 描述
     */
    private String psiResourceDesc;


}
