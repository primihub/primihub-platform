package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataVisitingUsers;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface DataMarketRepository {

    Map<String,Long> selectDataVisitingUsers();

}