package com.primihub.biz.repository.primarydb.data;


import com.primihub.biz.entity.data.po.DataVisitingUsers;
import org.springframework.stereotype.Repository;

@Repository
public interface DataMarketPrimarydbRepository {
    void saveDataVisitingUsers(DataVisitingUsers dataVisitingUsers);
}