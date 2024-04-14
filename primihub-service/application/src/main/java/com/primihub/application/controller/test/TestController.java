package com.primihub.application.controller.test;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.base.ResourceFileData;
import com.primihub.biz.repository.primaryredis.sys.SysAuthPrimaryRedisRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.test.TestService;
import com.primihub.biz.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * 测试接口
 */
@Slf4j
@RestController
public class TestController {

    @Autowired
    private TestService testService;
    @Autowired
    private SysAuthPrimaryRedisRepository sysAuthPrimaryRedisRepository;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private DataResourceService dataResourceService;

    /**
     * 检测通信
     * @return
     */
    @GetMapping("/healthConnection")
    public BaseResultEntity healthConnection(){
        return BaseResultEntity.success(System.currentTimeMillis());
    }

    /**
     * stream mq send test
     * @return
     */
    @GetMapping("/testStream")
    public BaseResultEntity testStream(){
        BaseFunctionHandleEntity entity=new BaseFunctionHandleEntity();
        entity.setHandleType(BaseFunctionHandleEnum.Test.getHandleType());
        entity.setParamStr("{\"a\":1,\"b\":2}");
        singleTaskChannel.output().send(MessageBuilder.withPayload(JSON.toJSONString(entity)).build());
        return BaseResultEntity.success();
    }

    /**
     * 删除菜单权限Redis缓存
     * @return
     */
    @GetMapping("/testDelete")
    public BaseResultEntity testDelete(){
        sysAuthPrimaryRedisRepository.deleteSysAuthForBfs();
        return BaseResultEntity.success();
    }

    /**
     * 重新加载资源集
     * @param tag
     * @return
     */
    @GetMapping("/testFormatResources")
    public BaseResultEntity formatResources(String tag){
        testService.formatResources(tag);
        return BaseResultEntity.success();
    }

    /**
     * 加载文件信息
     * @param filePath
     * @return
     * @throws Exception
     */
    @GetMapping("/testFile")
    public BaseResultEntity testFile(String filePath) throws Exception {
        if (StringUtils.isBlank(filePath)) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL);
        }
        ResourceFileData resourceFileData = dataResourceService.getResourceFileData(filePath);
        return BaseResultEntity.success(resourceFileData);
    }

    /**
     * 加载文件MD5
     * @param filePath
     * @return
     */
    @GetMapping("/testFileMd5")
    public BaseResultEntity testFileMd5(String filePath){
        if (StringUtils.isBlank(filePath)) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL);
        }
        return BaseResultEntity.success(FileUtil.md5HashCode(new File(filePath)));
    }

    /**
     * 注册后端node上的测试资源
     * @return
     */
    @GetMapping("/testDataSet")
    public BaseResultEntity testDataSet(String id){
        return testService.testDataSet(id);
    }

    /**
     * 取消运行任务
     * @param taskId
     * @return
     */
    @GetMapping("/killTask")
    public BaseResultEntity killTask(String taskId){
        if (StringUtils.isBlank(taskId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return testService.killTask(taskId);
    }






}
