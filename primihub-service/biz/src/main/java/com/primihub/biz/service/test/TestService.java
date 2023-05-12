package com.primihub.biz.service.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.sys.po.DataSet;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import com.primihub.biz.repository.primarydb.test.TestPrimaryRepository;
import com.primihub.biz.repository.primaryredis.test.TestRedisRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.repository.secondarydb.test.TestSecondaryRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TestService {
    @Autowired
    private TestPrimaryRepository testPrimaryRepository;
    @Autowired
    private TestSecondaryRepository testSecondaryRepository;
    @Autowired
    private TestRedisRepository testRedisRepository;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;
    @Autowired
    private OtherBusinessesService otherBusinessesService;

    @Resource
    private Environment environment;

    public void testPublish(){
        String serverAddr=environment.getProperty("nacos.config.server-addr");
        String group=environment.getProperty("nacos.config.group");
        try {
            ConfigService configService=NacosFactory.createConfigService(serverAddr);
            configService.publishConfig("xyz",group,"{\"groupId\":\"123456\"}", ConfigType.JSON.getType());
            System.out.println(configService.getConfig("xyz",group,3000));
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    public void testTask(String param){
        log.info("-----test task and param is "+param+" -----");
    }

    public Map test(){
        testPrimaryRepository.insertTest();
        testRedisRepository.testIncr();
        return testSecondaryRepository.testFindOneData();
    }

    public Map runFeign(){
        return new HashMap();
    }

    public void runGrpc(){
        log.info("请求开始");
        Common.ParamValue revealIntersectionParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
        Common.ParamValue clientDataDirParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom("/usr/local/src/data".getBytes(StandardCharsets.UTF_8))).build();
        Common.ParamValue clientFileNameParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom("client.csv".getBytes(StandardCharsets.UTF_8))).build();
        Common.ParamValue serverDataDirParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom("/usr/local/src/data".getBytes(StandardCharsets.UTF_8))).build();
        Common.ParamValue serverFileNameParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom("server.csv".getBytes(StandardCharsets.UTF_8))).build();
        Common.ParamValue colParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
        Common.ParamValue clientStartRowParamValue=Common.ParamValue.newBuilder().setValueInt32(2).build();
        Common.ParamValue serverColParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
        Common.ParamValue serverStartRowParamValue=Common.ParamValue.newBuilder().setValueInt32(2).build();
        // out
        Common.ParamValue outputDataDirParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom("/usr/local/src/data".getBytes(StandardCharsets.UTF_8))).build();
        Common.ParamValue outputFileNameParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom("output.csv".getBytes(StandardCharsets.UTF_8))).build();
        Common.Params params=Common.Params.newBuilder()
                .putParamMap("reveal_intersection",revealIntersectionParamValue)
                .putParamMap("client_data_dir",clientDataDirParamValue)
                .putParamMap("client_filename",clientFileNameParamValue)
                .putParamMap("client_col",colParamValue)
                .putParamMap("client_start_row",clientStartRowParamValue)
                .putParamMap("server_data_dir",serverDataDirParamValue)
                .putParamMap("server_filename",serverFileNameParamValue)
                .putParamMap("server_col",serverColParamValue)
                .putParamMap("server_start_row",serverStartRowParamValue)
                .putParamMap("output_data_dir",outputDataDirParamValue)
                .putParamMap("output_filename",outputFileNameParamValue)
                .build();
        Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId("1").setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId("1").build();
        primihub.rpc.Common.Task task= Common.Task.newBuilder()
                .setType(Common.TaskType.PSI_TASK)
                .setParams(params)
                .setName("testTask")
                .setLanguage(Common.Language.PROTO)
                .setTaskInfo(taskBuild)
//                .setCode(ByteString.copyFrom("import sys;".getBytes(StandardCharsets.UTF_8)))
                .build();
        PushTaskRequest request=PushTaskRequest.newBuilder()
                .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                .setTask(task)
                .setSequenceNumber(11)
                .setClientProcessedUpTo(22)
//                .setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                .build();
        PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
        log.info("grpc结果:"+reply);
    }

    public void formatResources(String tag) {
        tag = StringUtils.isBlank(tag)?"grpc":tag;
        List<DataResource> copyResourceList = dataResourceRepository.findCopyResourceList(0L, 5000L);
        for (DataResource dataResource : copyResourceList) {
            if (tag.contains("grpc")){
                dataResourceService.resourceSynGRPCDataSet(dataResource.getFileSuffix(),dataResource.getResourceFusionId(), dataResource.getUrl(),dataResourceRepository.queryDataFileFieldByFileId(dataResource.getResourceId()));
            }else if (tag.contains("copy")){
                if (StringUtils.isBlank(dataResource.getResourceHashCode())){
                    try {
                        File file = new File(dataResource.getUrl());
                        if (file.exists()){
                            dataResource.setResourceHashCode(FileUtil.md5HashCode(file));
                            dataResourcePrRepository.editResource(dataResource);
                        }
                    }catch (Exception e){
                        log.info("id:{}  ====  url:{}   错误：",dataResource.getResourceId(),dataResource.getUrl());
                        e.printStackTrace();
                    }
                }
            }
        }
        if (tag.contains("copy")){
            List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectSysOrganByExamine();
            for (SysOrgan sysOrgan : sysOrgans) {
                singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.BATCH_DATA_FUSION_RESOURCE_TASK.getHandleType(),sysOrgan.getOrganGateway()))).build());
            }
        }
    }

    public BaseResultEntity testDataSet(String id) {
        BaseResultEntity testDataSet = fusionResourceService.getTestDataSet(id);
        log.info(JSONObject.toJSONString(testDataSet));
        if (testDataSet.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            Map<String,Object> map = new HashMap<>();
            map.put("dataSets",JSONObject.toJSONString(testDataSet.getResult()));
            List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectSysOrganByExamine();
            for (SysOrgan sysOrgan : sysOrgans) {
                otherBusinessesService.syncGatewayApiData(map,sysOrgan.getOrganGateway()+"/share/shareData/batchSaveTestDataSet",sysOrgan.getPublicKey());
            }
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity batchSaveTestDataSet(List<DataSet> dataSets) {
        log.info(JSONObject.toJSONString(dataSets));
        return fusionResourceService.batchSaveTestDataSet(dataSets);
    }

}
