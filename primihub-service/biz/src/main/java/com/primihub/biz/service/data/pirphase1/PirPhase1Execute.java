package com.primihub.biz.service.data.pirphase1;

import com.primihub.biz.entity.data.req.DataPirCopyReq;
import org.springframework.stereotype.Service;

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
@Service
public interface PirPhase1Execute {
    void processPirPhase1(DataPirCopyReq req);
}
