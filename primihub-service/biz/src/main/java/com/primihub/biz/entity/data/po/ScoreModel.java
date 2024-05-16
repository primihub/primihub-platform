package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.primihub.biz.entity.data.req.ScoreModelReq;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ScoreModel {
    private Long id;
    /**
     * url 代码
     */
    private String scoreModelCode;
    /**
     * 中文
     */
    private String scoreModelName;
    /**
     * 英文
     */
    private String scoreModelType;
    /**
     * 键
     */
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

    public ScoreModel() {
    }

    public ScoreModel(ScoreModelReq req) {
        this.scoreModelCode = req.getScoreModelCode();
        this.scoreModelName = req.getScoreModelName();
        this.scoreModelType = req.getScoreModelType();
        this.scoreKey = req.getScoreKey();
        this.isDel = 0;
        this.createDate = new Date();
        this.updateDate = new Date();
    }
}
