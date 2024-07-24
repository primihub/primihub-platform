package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.primihub.biz.entity.data.req.ScoreModelReq;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
+----+------------------+-----------------------------+------------------+--------------+--------+---------------------+---------------------+
| id | score_model_code | score_model_name            | score_model_type | score_key    | is_del | create_date         | update_date         |
+----+------------------+-----------------------------+------------------+--------------+--------+---------------------+---------------------+
|  1 | AME000678        | 用户真实性评分              | truth_score      | truth_score  |      0 | 2024-05-14 14:44:03 | 2024-05-14 14:44:03 |
|  2 | AME000815        | 用户购买力评分              | yhgm_score       | yhgm_score   |      0 | 2024-05-14 14:44:03 | 2024-05-16 17:41:59 |
|  3 | AME000816        | 用户消费意愿评分            | yhxf_score       | yhxf_score   |      0 | 2024-05-14 14:44:03 | 2024-05-16 17:42:06 |
|  4 | AME000818        | 用户号码稳定性评分          | yhhhwd_score     | yhhhwd_score |      0 | 2024-05-14 14:44:03 | 2024-05-16 17:42:13 |
+----+------------------+-----------------------------+------------------+--------------+--------+---------------------+---------------------+
 */
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
