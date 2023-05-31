package com.primihub.controller;

import com.alibaba.fastjson.JSON;
import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.copy.dto.DataFusionCopyDto;
import com.primihub.service.CopyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("copy")
@RestController
public class CopyController {

    @Autowired
    private CopyService copyService;

    @PostMapping("batchSave")
    public BaseResultEntity batchSave(String globalId,String copyPart){
        DataFusionCopyDto copyDto=JSON.parseObject(copyPart, DataFusionCopyDto.class);
        if(copyDto.getTableName()==null|| "".equals(copyDto.getTableName())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"tableName");
        }
        if(copyDto.getMaxOffset()==null|| "".equals(copyDto.getMaxOffset())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"maxOffset");
        }
        if(copyDto.getCopyPart()==null|| "".equals(copyDto.getCopyPart())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"copyPart");
        }
        return copyService.batchSave(globalId,copyDto);
    }

}
