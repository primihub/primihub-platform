package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class DataPsiResourceVo {
    /**
     * psi资源id
     */
    private Long id;

    private Long resourceId;

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
    private String[] keywordList;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;
}
