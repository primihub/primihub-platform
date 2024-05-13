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
    private String scoreModelCode;
    private String scoreModelName;
    private String scoreModelType;
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
