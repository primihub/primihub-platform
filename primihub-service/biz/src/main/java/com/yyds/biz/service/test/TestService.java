package com.yyds.biz.service.test;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.data.vo.ModelComponentJson;
import com.yyds.biz.entity.feign.FedlearnerJobApi;
import com.yyds.biz.entity.feign.FedlearnerJobApiResource;
import com.yyds.biz.grpc.client.WorkGrpcClient;
import com.yyds.biz.repository.primarydb.test.TestPrimaryRepository;
import com.yyds.biz.repository.primaryredis.test.TestRedisRepository;
import com.yyds.biz.repository.resourceprimarydb.test.TestResourcePrimaryRepository;
import com.yyds.biz.repository.resourcesecondarydb.test.TestResourceSecondaryRepository;
import com.yyds.biz.repository.secondarydb.data.DataModelRepository;
import com.yyds.biz.repository.secondarydb.test.TestSecondaryRepository;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private DataModelRepository dataModelRepository;

    @Autowired
    private WorkGrpcClient workGrpcClient;

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @Autowired
    private TestResourcePrimaryRepository testResourcePrimaryRepository;
    @Autowired
    private TestResourceSecondaryRepository testResourceSecondaryRepository;

    public List<Map> testTable(){
        testResourcePrimaryRepository.dropTestTable();
        testResourcePrimaryRepository.createTestTable();
        for(int i=0;i<10;i++){
            testResourcePrimaryRepository.insertTestTable();
        }
        return testResourceSecondaryRepository.findAll();
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

        ModelComponentJson modelComponent = dataModelRepository.queryModelComponenJsonByUserId(null,0,1L);
        FedlearnerJobApi fedlearnerJobApi = null;
        if (modelComponent!=null) {
            if (StringUtils.isNotBlank(modelComponent.getComponentJson())) {
                fedlearnerJobApi  = JSONObject.parseObject(modelComponent.getComponentJson(), FedlearnerJobApi.class);
            }
        }
        if (fedlearnerJobApi!=null){
            List<FedlearnerJobApiResource> resources = new ArrayList();
            resources.add(new FedlearnerJobApiResource(1L,"/data/upload/1/2022042910/a72bffc9-d36e-461f-8612-e2d54e69c96a.csv"));
            resources.add(new FedlearnerJobApiResource(2L,"/data/upload/1/2022042910/d4e792cc-46d1-4167-8e60-bb7450dbab53.csv"));
            fedlearnerJobApi.setResources(resources);
            HttpHeaders headers = new HttpHeaders();
            // 以表单的方式提交
            headers.setContentType(MediaType.APPLICATION_JSON);
            //将请求头部和参数合成一个请求
            String apiJson = JSONObject.toJSONString(fedlearnerJobApi);
            log.info(apiJson);
            HttpEntity<String> requestEntity = new HttpEntity<>(apiJson, headers);
//            try{
//                BaseResultEntity result=restTemplate.getForObject("http://fedlearner/job_api/run",BaseResultEntity.class);
//                log.info(result.getMsg());
//                log.info(result.getCode().toString());
//            }catch (Exception e){
//                log.info(e.getMessage());
//            }
            try{
                ResponseEntity<String> a = restTemplate.postForEntity("http://fedlearner/job_api/run", requestEntity, String.class);
                log.info(a.getBody());
            }catch (Exception e){
                log.info(e.getMessage());
            }
            return new HashMap(){{put("feignResult", "");}};
        }
        return new HashMap();
    }

    public void runGrpc(){
        log.info("请求开始");
        Common.ParamValue revealIntersectionParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
        Common.ParamValue clientDataDirParamValue=Common.ParamValue.newBuilder().setValueString("/usr/local/src/data").build();
        Common.ParamValue clientFileNameParamValue=Common.ParamValue.newBuilder().setValueString("client.csv").build();
        Common.ParamValue serverDataDirParamValue=Common.ParamValue.newBuilder().setValueString("/usr/local/src/data").build();
        Common.ParamValue serverFileNameParamValue=Common.ParamValue.newBuilder().setValueString("server.csv").build();
        Common.ParamValue colParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
        Common.ParamValue clientStartRowParamValue=Common.ParamValue.newBuilder().setValueInt32(2).build();
        Common.ParamValue serverColParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
        Common.ParamValue serverStartRowParamValue=Common.ParamValue.newBuilder().setValueInt32(2).build();
        // out
        Common.ParamValue outputDataDirParamValue=Common.ParamValue.newBuilder().setValueString("/usr/local/src/data").build();
        Common.ParamValue outputFileNameParamValue=Common.ParamValue.newBuilder().setValueString("output.csv").build();
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
        primihub.rpc.Common.Task task= Common.Task.newBuilder()
                .setType(Common.TaskType.PSI_TASK)
                .setParams(params)
                .setName("testTask")
                .setLanguage(Common.Language.JAVA)
                .setCode("import sys;")
                .setJobId(ByteString.copyFrom("2".getBytes(StandardCharsets.UTF_8)))
                .setTaskId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                .build();
        PushTaskRequest request=PushTaskRequest.newBuilder()
                .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                .setTask(task)
                .setSequenceNumber(11)
                .setClientProcessedUpTo(22)
                .build();
        PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
        log.info("grpc结果:"+reply);
    }
}
