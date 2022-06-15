package com.primihub.service;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.fusion.param.RegisterConnectionParam;
import com.primihub.entity.fusion.po.FusionOrgan;
import com.primihub.repository.FusionRepository;
import com.primihub.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FusionService {

    @Autowired
    private FusionRepository fusionRepository;

    public BaseResultEntity registerConnection(RegisterConnectionParam registerConnectionParam){
        FusionOrgan fusionOrgan=fusionRepository.getFusionOrganByGlobalId(registerConnectionParam.getGlobalId());
        if(fusionOrgan!=null&&fusionOrgan.getId()!=null)
            return BaseResultEntity.success();
        fusionOrgan=new FusionOrgan();
        fusionOrgan.setGlobalName(registerConnectionParam.getGlobalName());
        fusionOrgan.setGlobalId(registerConnectionParam.getGlobalId());
        fusionOrgan.setPinCodeMd(SignUtil.getMD5ValueUpperCaseByDefaultEncode(registerConnectionParam.getPinCode()));
        fusionOrgan.setRegisterTime(new Date());
        fusionOrgan.setIsDel(0);
        fusionRepository.insertFusionOrgan(fusionOrgan);
        return BaseResultEntity.success();
    }

    public BaseResultEntity changeConnection(String globalId,String globalName) {
        FusionOrgan fusionOrgan=fusionRepository.getFusionOrganByGlobalId(globalId);
        if (fusionOrgan==null)
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_ALTER,"无机构信息");
        fusionOrgan.setGlobalName(globalName);
        fusionRepository.updateFusionOrganSpeByGlobalId(fusionOrgan);
        return BaseResultEntity.success();
    }

    public BaseResultEntity findOrganByGlobalId(String[] globalIdArray){
        Map result=new HashMap<>();
        result.put("organList",fusionRepository.selectFusionOrganByGlobalIds(Arrays.stream(globalIdArray).collect(Collectors.toSet())));
        return BaseResultEntity.success(result);
    }
}
