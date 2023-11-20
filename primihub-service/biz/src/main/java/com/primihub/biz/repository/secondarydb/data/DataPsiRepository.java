package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataPsi;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.entity.data.req.DataPsiQueryReq;
import com.primihub.biz.entity.data.vo.DataOrganPsiTaskVo;
import com.primihub.biz.entity.data.vo.DataPsiTaskVo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataPsiRepository {

    List<DataPsiTaskVo> selectPsiTaskPage(DataPsiQueryReq req);

    DataPsiTask selectPsiTaskById(Long id);

    DataPsiTask selectPsiTaskByTaskId(String id);

    DataPsi selectPsiById(Long id);

    Long selectPsiTaskPageCount(DataPsiQueryReq req);

    List<DataOrganPsiTaskVo> selectOrganPsiTaskPage(Map<String,Object> map);

    Long selectOrganPsiTaskPageCount(Map<String,Object> map);







}
