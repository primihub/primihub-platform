package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.ScoreModel;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface ScoreModelRepository {
    Set<ScoreModel> selectAll();

    ScoreModel selectScoreModelByScoreTypeValue(@Param("type") String scoreTypeValue);
}
