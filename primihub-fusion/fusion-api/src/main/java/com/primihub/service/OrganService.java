package com.primihub.service;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.fusion.FusionOrgan;
import com.primihub.repository.FusionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class OrganService {
    @Autowired
    private FusionRepository fusionRepository;

    public BaseResultEntity organData(String organId, String organName){
        FusionOrgan fusionOrgan = fusionRepository.getFusionOrganByGlobalId(organId);
        if (fusionOrgan == null) {
            fusionOrgan = new FusionOrgan();
            fusionOrgan.setGlobalId(organId);
            fusionOrgan.setGlobalName(organName);
            fusionOrgan.setRegisterTime(new Date());
            fusionOrgan.setIsDel(0);
            fusionRepository.insertFusionOrgan(fusionOrgan);
        }else {
            fusionOrgan.setGlobalName(organName);
            fusionRepository.updateFusionOrganSpeByGlobalId(fusionOrgan);
        }
        return BaseResultEntity.success();
    }

}
