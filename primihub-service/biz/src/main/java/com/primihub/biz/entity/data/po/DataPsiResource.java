package com.primihub.biz.entity.data.po;

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
public class DataPsiResource {

    /**
     * psi资源id
     */
    private Long id;

    private Long resourceId;

    /**
     * psi 资源描述
     */
    private String psiResourceDesc;

    /**
     * 表结构模板
     */
    private String tableStructureTemplate;

    /**
     * 机构类型
     */
    private Integer organType;

    /**
     * 是否允许结果出现在对方节点上
     */
    private Integer resultsAllowOpen;

    /**
     * 关键字 关键字:类型,关键字:类型.....
     */
    private String keywordList;

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
