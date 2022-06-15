//package com.yyds.application.data;
//
//import com.alibaba.fastjson.JSONObject;
//import com.yyds.biz.entity.base.BaseResultEntity;
//import com.yyds.biz.entity.data.dataenum.FieldTypeEnum;
//import com.yyds.biz.entity.data.po.DataFileField;
//import com.yyds.biz.entity.data.po.DataResource;
//import com.yyds.biz.entity.data.req.DataResourceFieldReq;
//import com.yyds.biz.entity.sys.po.SysFile;
//import com.yyds.biz.repository.secondarydb.data.DataResourceRepository;
//import com.yyds.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
//import com.yyds.biz.service.data.DataMpcService;
//import com.yyds.biz.service.data.DataResourceService;
//import com.yyds.biz.service.data.DataTaskService;
//import com.yyds.biz.service.data.ModelInitService;
//import com.yyds.biz.util.FileUtil;
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
//    public void handleRunSql(){
//        Map<String, String> map = new HashMap(){{
//            put("taskId",2);
//            put("scriptSqlContent","select x19,x18 from `28f92cce-9a6e-488f-9c7a-7f8d8ee92f9d`");
//        }};
//        dataTaskService.handleRunSql(JSONObject.toJSONString(map));
//    }
//
//}
