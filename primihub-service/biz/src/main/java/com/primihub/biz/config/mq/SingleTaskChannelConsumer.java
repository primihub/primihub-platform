package com.primihub.biz.config.mq;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
@EnableBinding(value = {SingleTaskChannel.class})
public class SingleTaskChannelConsumer implements ApplicationContextAware {

    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context=applicationContext;
    }

    @StreamListener(SingleTaskChannel.INPUT)
    public void receive(String message) {
        if(message!=null&&!message.trim().equals("")) {
            BaseFunctionHandleEntity baseFunctionHandleEntity=JSON.parseObject(message,BaseFunctionHandleEntity.class);
            if(baseFunctionHandleEntity.getHandleType()!=null){
                BaseFunctionHandleEnum enu=BaseFunctionHandleEnum.FUNCTION_MAP.get(baseFunctionHandleEntity.getHandleType());
                StringBuilder logBuilder=new StringBuilder().append("-----").append("beanName:").append(enu.getBeanName()).append("functionName").append(enu.getFunctionName());
                log.info(new StringBuilder().append(logBuilder).append(" is ready to run").append("-----").toString());
                if(enu!=null) {
                    Object object = context.getBean(enu.getBeanName());
                    Class<? extends Object> clazz = object.getClass();
                    try {
                        Method method = clazz.getMethod(enu.getFunctionName(), String.class);
                        method.invoke(object, baseFunctionHandleEntity.getParamStr());
                    } catch (Exception e) {
                        log.info("handleMessage", e);
                    }
                }
                log.info(new StringBuilder().append(logBuilder).append(" has finished").append("-----").toString());
            }
        }
    }

}
