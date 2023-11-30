package com.primihub.application.controller.test;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.base.ResourceFileData;
import com.primihub.biz.repository.primaryredis.sys.SysAuthPrimaryRedisRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.test.TestService;
import com.primihub.biz.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Map;

/**
 * 测试接口
 */
@Api(value = "测试接口",tags = "测试接口")
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

    @ApiOperation(value = "检测通信",httpMethod = "GET")
    @GetMapping("/healthConnection")
    public BaseResultEntity healthConnection(){
        return BaseResultEntity.success(System.currentTimeMillis());
    }

    @ApiOperation(value = "stream mq send test",httpMethod = "GET")
    @GetMapping("/testStream")
    public BaseResultEntity testStream(){
        BaseFunctionHandleEntity entity=new BaseFunctionHandleEntity();
        entity.setHandleType(BaseFunctionHandleEnum.Test.getHandleType());
        entity.setParamStr("{\"a\":1,\"b\":2}");
        singleTaskChannel.output().send(MessageBuilder.withPayload(JSON.toJSONString(entity)).build());
        return BaseResultEntity.success();
    }

    @ApiOperation(value = "删除菜单权限Redis缓存",httpMethod = "GET")
    @GetMapping("/testDelete")
    public BaseResultEntity testDelete(){
        sysAuthPrimaryRedisRepository.deleteSysAuthForBfs();
        return BaseResultEntity.success();
    }

    @ApiOperation(value = "重新加载资源集",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name = "tag", value = "触发类型(grpc,copy) 默认grpc", dataType = "String", paramType = "query")
    @GetMapping("/testFormatResources")
    public BaseResultEntity formatResources(String tag){
        testService.formatResources(tag);
        return BaseResultEntity.success();
    }

    @ApiOperation(value = "加载文件信息",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name = "filePath", value = "文件路径", dataType = "String", paramType = "query")
    @GetMapping("/testFile")
    public BaseResultEntity testFile(String filePath) throws Exception {
        if (StringUtils.isBlank(filePath)) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL);
        }
        ResourceFileData resourceFileData = dataResourceService.getResourceFileData(filePath);
        return BaseResultEntity.success(resourceFileData);
    }

    @ApiOperation(value = "加载文件MD5",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name = "filePath", value = "文件路径", dataType = "String", paramType = "query")
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
    @ApiOperation(value = "注册后端node上的测试资源",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name = "id", value = "资源ID", dataType = "String", paramType = "query")
    @GetMapping("/testDataSet")
    public BaseResultEntity testDataSet(String id){
        return testService.testDataSet(id);
    }

    /**
     * 取消运行任务
     * @param taskId
     * @return
     */
    @ApiOperation(value = "取消运行任务",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name = "taskId", value = "任务ID", dataType = "String", paramType = "query")
    @GetMapping("/killTask")
    public BaseResultEntity killTask(String taskId){
        if (StringUtils.isBlank(taskId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return testService.killTask(taskId);
    }






}
