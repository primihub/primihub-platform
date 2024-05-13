package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

// todo sql ddl 和 dql
@Getter
@Setter
public class ScoreModel {
    private Long id;
    /** url 代码 */
    private String scoreModelCode;
    /** 中文 */
    private String scoreModelName;
    /** 英文 */
    private String scoreModelType;
    /** 键 */
    private String scoreKey;
    /**
     * 是否删除
     */
    @JsonIgnore
    private Integer isDel;
    /**
     * 创建时间
     */
    @JsonIgnore
    private Date createDate;
    /**
     * 修改时间
     */
    @JsonIgnore
    private Date updateDate;
}
