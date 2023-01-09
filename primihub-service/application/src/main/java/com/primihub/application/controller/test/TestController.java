package com.primihub.application.controller.test;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.config.test.TestConfiguration;
import com.primihub.biz.config.test.TestYamlConfiguration;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.base.ResourceFileData;
import com.primihub.biz.repository.primaryredis.sys.SysAuthPrimaryRedisRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.test.TestService;
import com.primihub.biz.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.List;
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
    @Autowired
    private DataResourceService dataResourceService;

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

    @RequestMapping("/testFormatResources")
    public BaseResultEntity formatResources(String tag){
        testService.formatResources(tag);
        return BaseResultEntity.success();
    }

    @RequestMapping("/testFile")
    public BaseResultEntity testFile(String filePath,Integer severalLines) throws Exception {
        if (StringUtils.isBlank(filePath))
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL);
        ResourceFileData resourceFileData = dataResourceService.getResourceFileData(filePath);
        return BaseResultEntity.success(resourceFileData);
    }

    @RequestMapping("/testFileMd5")
    public BaseResultEntity testFileMd5(String filePath){
        if (StringUtils.isBlank(filePath))
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL);
        return BaseResultEntity.success(FileUtil.md5HashCode(new File(filePath)));
    }




}
