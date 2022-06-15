package com.yyds.biz.service.sys;

import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.PageParam;
import com.yyds.biz.entity.sys.param.AlterOrganNodeStatusParam;
import com.yyds.biz.entity.sys.param.CreateOrganNodeParam;
import com.yyds.biz.entity.sys.param.FindOrganPageParam;
import com.yyds.biz.entity.sys.po.SysOrgan;
import com.yyds.biz.entity.sys.vo.SysOrganListVO;
import com.yyds.biz.repository.primarydb.sys.SysOrganPrimarydbRepository;
import com.yyds.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysOrganService {

    @Autowired
    private SysOrganPrimarydbRepository sysOrganPrimarydbRepository;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;

    public BaseResultEntity findOrganPage(FindOrganPageParam findOrganPageParam, Integer pageNum,Integer pageSize){
        PageParam pageParam=new PageParam(pageNum,pageSize);
        Map paramMap=new HashMap(){
            {
                put("pOrganId",findOrganPageParam.getPOrganId());
                put("organName",findOrganPageParam.getOrganName());
                put("pageIndex",pageParam.getPageIndex());
                put("pageSize",pageParam.getPageSize()+1);
            }
        };
        List<SysOrganListVO> sysOrganList=sysOrganSecondarydbRepository.selectOrganListByParam(paramMap);
        Long count=sysOrganSecondarydbRepository.selectOrganListCountByParam(paramMap);
        pageParam.isLoadMore(sysOrganList);
        pageParam.initItemTotalCount(count);
        Map map=new HashMap<>();
        map.put("sysOrganList",sysOrganList);
        map.put("pageParam",pageParam);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity createOrganNode(CreateOrganNodeParam createOrganNodeParam){
        SysOrgan parentSysOrgan=createOrganNodeParam.getPOrganId()==0?null:sysOrganSecondarydbRepository.selectSysOrganByOrganId(createOrganNodeParam.getPOrganId());
        SysOrgan sysOrgan=new SysOrgan();
        BeanUtils.copyProperties(createOrganNodeParam,sysOrgan);
        if(sysOrgan.getPOrganId().equals(0L)){
            sysOrgan.setROrganId(0L);
            sysOrgan.setOrganDepth(0);
        }else{
            sysOrgan.setROrganId(parentSysOrgan.getROrganId());
            sysOrgan.setOrganDepth(parentSysOrgan.getOrganDepth()+1);
        }
        sysOrgan.setFullPath("");
        sysOrgan.setIsDel(0);
        sysOrganPrimarydbRepository.insertSysOrgan(sysOrgan);
        if(parentSysOrgan!=null) {
            sysOrgan.setFullPath(new StringBuilder().append(parentSysOrgan.getFullPath()).append(",").append(sysOrgan.getOrganId()).toString());
        }else{
            sysOrgan.setFullPath(sysOrgan.getOrganId().toString());
        }
        if(sysOrgan.getROrganId().equals(0L)) {
            sysOrganPrimarydbRepository.updateROrganIdAndFullPath(sysOrgan.getOrganId(),sysOrgan.getOrganId(),sysOrgan.getFullPath());
        }else{
            sysOrganPrimarydbRepository.updateROrganIdAndFullPath(sysOrgan.getOrganId(),null,sysOrgan.getFullPath());
        }
        Map map=new HashMap<>();
        map.put("sysOrgan",sysOrgan);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity alterOrganNodeStatus(AlterOrganNodeStatusParam alterOrganNodeStatusParam){
        Map paramMap=new HashMap(){
            {
                put("organId",alterOrganNodeStatusParam.getOrganId());
                put("organName",alterOrganNodeStatusParam.getOrganName());
            }
        };
        sysOrganPrimarydbRepository.updateSysOrganExplicit(paramMap);
        return BaseResultEntity.success();
    }

    public BaseResultEntity deleteOrganNode(@Param("organId") Long organId){
        sysOrganPrimarydbRepository.updateOrganDelStatus(organId);
        return BaseResultEntity.success();
    }

    public Map<Long,SysOrgan> getSysOrganMap(Set<Long> organIdSet){
        if (organIdSet==null||organIdSet.size()==0){
            return new HashMap<>();
        }
        List<SysOrgan> sysOrgans = this.sysOrganSecondarydbRepository.selectSysOrganByBatchOrganId(organIdSet);
        if (sysOrgans.size()>0){
            return sysOrgans.stream().collect(Collectors.toMap(SysOrgan::getOrganId, Function.identity(),(key1,key2)->key2));
        }
        return new HashMap<>();
    }
}
