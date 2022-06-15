package com.yyds.biz.repository.secondarydb.data;

import com.yyds.biz.entity.data.po.DataPsi;
import com.yyds.biz.entity.data.po.DataPsiResource;
import com.yyds.biz.entity.data.po.DataPsiTask;
import com.yyds.biz.entity.data.vo.DataOrganPsiTaskVo;
import com.yyds.biz.entity.data.vo.DataPsiResourceAllocationVo;
import com.yyds.biz.entity.data.vo.DataPsiTaskVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataPsiRepository {

    DataPsiResource selectPsiResourceByResourceId(Long resourceId);
    DataPsiResource selectPsiResourceById(Long id);

//    List<DataPsiResourceAllocationVo> selectPsiResourceAllocation(Map<String,Object> map);

    Long selectPsiResourceAllocationCount(Map<String,Object> map);

    List<DataPsiTaskVo> selectPsiTaskPage(Map<String,Object> map);

    DataPsiTask selectPsiTaskById(Long id);

    DataPsi selectPsiById(Long id);

    Long selectPsiTaskPageCount(Map<String,Object> map);

    List<DataOrganPsiTaskVo> selectOrganPsiTaskPage(Map<String,Object> map);

    Long selectOrganPsiTaskPageCount(Map<String,Object> map);







}
