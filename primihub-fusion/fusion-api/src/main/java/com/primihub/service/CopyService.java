package com.primihub.service;

import com.alibaba.fastjson.JSON;
import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.copy.dto.DataFusionCopyDto;
import com.primihub.entity.copy.enumeration.CopyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@Service
public class CopyService implements ApplicationContextAware {

    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context=applicationContext;
    }

    public BaseResultEntity batchSave(String globalId,DataFusionCopyDto copyDto){
        CopyEnum enu=CopyEnum.FUSION_COPY_MAP.get(copyDto.getTableName());
        if(enu==null)
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"没有"+copyDto.getTableName());
        Object object = context.getBean(enu.getBeanName());
        Class<? extends Object> clazz = object.getClass();
        try {
            Method method = clazz.getMethod(enu.getFunctionName(), String.class,List.class);
            BaseResultEntity result = (BaseResultEntity)method.invoke(object,globalId,JSON.parseArray(copyDto.getCopyPart(),enu.getClazz()));
            return result;
        } catch (Exception e) {
            log.info("handleFusionCopyTask", e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
    }

}
