package com.primihub.application.controller.test;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.config.test.TestConfiguration;
import com.primihub.biz.config.test.TestYamlConfiguration;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import com.primihub.biz.entity.base.BaseJsonParam;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.repository.primaryredis.sys.SysAuthPrimaryRedisRepository;
import com.primihub.biz.service.test.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private TestConfiguration testConfiguration;
    @Autowired
    private TestYamlConfiguration testYamlConfiguration;
    @Autowired
    private TestService testService;
    @Autowired
    private SysAuthPrimaryRedisRepository sysAuthPrimaryRedisRepository;
    @Autowired
    private SingleTaskChannel singleTaskChannel;

    @RequestMapping("/testPublish")
    public BaseResultEntity testPublish(){
        testService.testPublish();
        return BaseResultEntity.success();
    }

    @RequestMapping("/testTable")
    public BaseResultEntity testTable(){
        return BaseResultEntity.success(testService.testTable());
    }

    @RequestMapping("/testStream")
    public BaseResultEntity testStream(){
        BaseFunctionHandleEntity entity=new BaseFunctionHandleEntity();
        entity.setHandleType(BaseFunctionHandleEnum.Test.getHandleType());
        entity.setParamStr("{\"a\":1,\"b\":2}");
        singleTaskChannel.output().send(MessageBuilder.withPayload(JSON.toJSONString(entity)).build());
        return BaseResultEntity.success();
    }

    @RequestMapping("/testJsonParam")
    public BaseResultEntity testJsonParam(@RequestBody BaseJsonParam<Map> baseJsonParam, HttpServletRequest request){
        return BaseResultEntity.success(baseJsonParam.getParam());
    }

    @RequestMapping("/testDelete")
    public BaseResultEntity testDelete(){
        sysAuthPrimaryRedisRepository.deleteSysAuthForBfs();
        return BaseResultEntity.success();
    }


    @RequestMapping("/runGrpc")
    public BaseResultEntity runGrpc(){
        testService.runGrpc();
        return BaseResultEntity.success();
    }

    @RequestMapping("/runFeign")
    public BaseResultEntity runFeign(){
        return BaseResultEntity.success(testService.runFeign());
    }

    @RequestMapping("/testInfo")
    public BaseResultEntity testInfo(Long testId){

        Map info=new HashMap<>();
        info.put("port",8090);
        info.put("name","platform");
        info.put("first",testConfiguration.getFirst());
        info.put("second",testConfiguration.getSecond());
        info.put("third",testYamlConfiguration.getThird());
        info.put("id",testYamlConfiguration.getId());
        info.put("sql",testService.test());
        info.put("testId",testId);
//        info.put("userId",userId);
//        info.put("organId",organId);
//        info.put("organCode",organCode);
        return BaseResultEntity.success(info);
    }



}
