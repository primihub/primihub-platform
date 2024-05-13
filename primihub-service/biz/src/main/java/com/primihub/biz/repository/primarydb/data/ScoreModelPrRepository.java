package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.ScoreModel;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreModelPrRepository {
    void saveScoreModel(ScoreModel scoreModel);
}
