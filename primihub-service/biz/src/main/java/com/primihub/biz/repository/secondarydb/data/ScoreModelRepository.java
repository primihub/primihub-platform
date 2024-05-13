package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.ScoreModel;

import java.util.Set;

public interface ScoreModelRepository {
    Set<ScoreModel> selectAll();
}
