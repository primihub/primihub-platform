//
//package com.primihub.application.data;
//
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.primihub.biz.entity.base.BaseResultEntity;
//import com.primihub.biz.entity.data.dataenum.FieldTypeEnum;
//import com.primihub.biz.entity.data.po.DataFileField;
//import com.primihub.biz.entity.data.po.DataPsi;
//import com.primihub.biz.entity.data.po.DataPsiTask;
//import com.primihub.biz.entity.data.po.DataResource;
//import com.primihub.biz.entity.data.req.DataResourceFieldReq;
//import com.primihub.biz.entity.sys.po.SysFile;
//import com.primihub.biz.repository.secondarydb.data.DataPsiRepository;
//import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
//import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
//import com.primihub.biz.service.data.*;
//import com.primihub.biz.util.FileUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class DataResourceTest {
//
//    @Autowired
//    private ModelInitService modelInitService;
//    @Autowired
//    private DataResourceRepository dataResourceRepository;
//    @Autowired
//    private SysFileSecondarydbRepository sysFileSecondarydbRepository;
//    @Autowired
//    private DataResourceService dataResourceService;
//    @Autowired
//    private DataTaskService dataTaskService;
//    @Autowired
//    private DataMpcService dataMpcService;
//    @Autowired
//    private PsiAsyncService psiAsyncService;
//    @Autowired
//    private PirService pirService;
//
//    @Autowired
//    private DataPsiRepository dataPsiRepository;
//
//    @Test
//    public void batchInsertDataFileField(){
//        DataResource dataResource = dataResourceRepository.queryDataResourceById(37L);
//        SysFile sysFile = sysFileSecondarydbRepository.selectSysFileByFileId(dataResource.getFileId());
//        List<String> fileContent = FileUtil.getFileContent(sysFile.getFileUrl(), null);
//        List<DataFileField> dataFileFieldList = dataResourceRepository.queryDataFileField(new HashMap() {{
//            put("resourceId", dataResource.getResourceId());
//        }});
//        Map<String, Object> map = dataTaskService.initializeTableStructure(sysFile, dataFileFieldList);
//        dataTaskService.initializeTableData(map,fileContent);
////        dataResourceTaskService.handleDataResourceFile(JSONObject.toJSONString(dataResource));
////        modelInitService.batchInsertDataFileField(dataResource);
//    }
//
//    @Test
//    public void updateDataResourceField(){
//        DataResourceFieldReq req = new DataResourceFieldReq();
//        req.setFieldId(13L);
//        req.setFieldAs("liweihua");
//        req.setFieldType("string");
//        req.setFieldDesc("测试时");
//        req.setRelevance(1);
//        req.setGrouping(0);
//        req.setProtectionStatus(1);
//        FieldTypeEnum fieldTypeEnum = FieldTypeEnum.STRING;
//        dataResourceService.updateDataResourceField(req,fieldTypeEnum);
//    }
//    @Test
//    public void runDataScript(){
//        String scriptContent = "select * from 测试脚本数据3";
//        BaseResultEntity baseResultEntity = dataMpcService.runDataScript(12L, 29L, 1000L, scriptContent);
//        System.out.println(JSONObject.toJSONString(baseResultEntity.getResult()));
//    }
//
//    @Test
//    public void handleDataResourceFile(){
//        DataResource dataResource = dataResourceRepository.queryDataResourceById(70L);
////        dataTaskService.handleDataResourceFile(JSONObject.toJSONString(dataResource));
//    }
//
//    @Test
//    public void handleRunSql(){
//        Map<String, String> map = new HashMap(){{
//            put("taskId",2);
//            put("scriptSqlContent","select x19,x18 from `28f92cce-9a6e-488f-9c7a-7f8d8ee92f9d`");
//        }};
//        dataTaskService.handleRunSql(JSONObject.toJSONString(map));
//    }
//
//    @Test
//    public void psiGrpcRun(){
//        DataPsi dataPsi = dataPsiRepository.selectPsiById(276L);
//        DataPsiTask task = dataPsiRepository.selectPsiTaskById(276L);
//        psiAsyncService.psiGrpcRun(task,dataPsi);
//    }
//
//    @Test
//    public void pirGrpcRun(){
//        pirService.pirSubmitTask(60L,"1,3,5");
//    }
//
//    @Test
//    public void resourceFilePreview(){
////        SysFile sysFile = sysFileSecondarydbRepository.selectSysFileByFileId(1063L);
//        BaseResultEntity baseResultEntity = dataResourceService.resourceFilePreview(1063L);
//        log.info(JSONObject.toJSONString(baseResultEntity, SerializerFeature.WriteMapNullValue));
//    }
//
//    @Test
//    public void testBatchDataFusionResource(){
//        dataTaskService.batchDataFusionResource("{\"serverAddress\":\"http://localhost:8099\"}");
//    }
//
//}
//
