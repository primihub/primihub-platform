package com.yyds.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.yyds.biz.config.base.BaseConfiguration;
import com.yyds.biz.constant.DataConstant;
import com.yyds.biz.entity.data.po.DataFileField;
import com.yyds.biz.entity.data.po.DataMpcTask;
import com.yyds.biz.entity.data.po.DataResource;
import com.yyds.biz.entity.sys.po.SysFile;
import com.yyds.biz.repository.primarydb.data.DataMpcPrRepository;
import com.yyds.biz.repository.primarydb.data.DataResourcePrRepository;
import com.yyds.biz.repository.resourceprimarydb.data.DataResourcePrimaryRepository;
import com.yyds.biz.repository.resourcesecondarydb.data.DataResourceSecondaryRepository;
import com.yyds.biz.repository.secondarydb.data.DataMpcRepository;
import com.yyds.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.yyds.biz.util.FileUtil;
import com.yyds.biz.util.crypt.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataTaskService {
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private SysFileSecondarydbRepository sysFileSecondarydbRepository;
    @Autowired
    private DataResourcePrimaryRepository dataResourcePrimaryRepository;
    @Autowired
    private DataMpcRepository dataMpcRepository;
    @Autowired
    private DataMpcPrRepository dataMpcPrRepository;
    @Autowired
    private DataResourceSecondaryRepository dataResourceSecondaryRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;

    public void handleDataResourceFile(String paramStr) {
//        log.info("消息队列:{}",paramStr);
        DataResource dataResource = JSONObject.parseObject(paramStr, DataResource.class);
        if (dataResource.getFileId() == null || dataResource.getFileId() == 0L)
            return;
        SysFile sysFile = sysFileSecondarydbRepository.selectSysFileByFileId(dataResource.getFileId());
        if (sysFile.getFileSuffix().indexOf("csv") != -1) {
            File file = new File(sysFile.getFileUrl());
            if (file.exists()) {
                dataResource.setFileRows(FileUtil.getFileLineNumber(sysFile.getFileUrl()));
                List<String> fileContent = FileUtil.getFileContent(sysFile.getFileUrl(), null);
                if (fileContent.size() > 0) {
                    dataResource.setFileHandleField(fileContent.get(0));
                    dataResource.setFileColumns(dataResource.getFileHandleField().split(",").length);
                } else {
                    dataResource.setFileHandleField("");
                    dataResource.setFileColumns(0);
                }
                try {
                    // 处理字段信息
                    List<DataFileField> dataFileFieldList = batchInsertDataFileField(dataResource);
                    if (dataFileFieldList.size() == 0) {
                        dataResource.setFileHandleStatus(3);
                    } else {
                        Map<String, Object> tableStructureMap = initializeTableStructure(sysFile, dataFileFieldList);
                        initializeTableData(tableStructureMap, fileContent);
                        dataResource.setFileHandleStatus(2);
                    }
                } catch (Exception e) {
                    dataResource.setFileHandleStatus(3);
                    e.printStackTrace();
                }
                dataResourcePrRepository.editResource(dataResource);
            }
        }
    }


    public List<DataFileField> batchInsertDataFileField(DataResource dataResource) {
        List<DataFileField> fileFieldList = new ArrayList<>();
        String fileHandleField = dataResource.getFileHandleField();
        int i = 1;
        if (StringUtils.isNotBlank(fileHandleField)) {
            String[] fieldSplit = fileHandleField.split(",");
            for (String fieldName : fieldSplit) {
                if (StringUtils.isNotBlank(fieldName)) {
                    if (fieldName.substring(0, 1).matches(DataConstant.MATCHES)) {
                        fileFieldList.add(new DataFileField(dataResource.getFileId(), dataResource.getResourceId(), fieldName, null));
                    } else {
                        fileFieldList.add(new DataFileField(dataResource.getFileId(), dataResource.getResourceId(), fieldName, DataConstant.FIELD_NAME_AS + i));
                        i++;
                    }
                } else {
                    fileFieldList.add(new DataFileField(dataResource.getFileId(), dataResource.getResourceId(), fieldName, DataConstant.FIELD_NAME_AS + i));
                    i++;
                }
            }
            dataResourcePrRepository.saveResourceFileFieldBatch(fileFieldList);
        }
        return fileFieldList;
    }

    public Map<String, Object> initializeTableStructure(SysFile sysFile, List<DataFileField> dataFileFieldList) {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", sysFile.getFileName());
        List<String> columnList = new ArrayList<>();
        for (DataFileField dataFileField : dataFileFieldList) {
            if (StringUtils.isNotBlank(dataFileField.getFieldAs())) {
                columnList.add(dataFileField.getFieldAs());
            } else {
                columnList.add(dataFileField.getFieldName());
            }
        }
        map.put("columnList", columnList);
        dataResourcePrimaryRepository.dropDataTable(sysFile.getFileName());
        dataResourcePrimaryRepository.createDataTable(map);
        return map;
    }

    public void initializeTableData(Map<String, Object> map, List<String> fileContent) {
        List<String> columnList = (List<String>) map.get("columnList");
        List<List<Object>> dataList = new ArrayList();
        List<Object> lineContenList = null;
        fileContent.remove(0);
        for (String lineContent : fileContent) {
            String[] lineContentSplit = lineContent.split(",");
            if (lineContentSplit.length > 0) {
                lineContenList = new ArrayList<>();
                for (int i = 0; i < columnList.size(); i++) {
                    String val = "";
                    try {
                        val = lineContentSplit[i];
                    } catch (Exception e) {
                        log.info("解析:{} - index:{} - lineContent:{} - 异常：{}", map.get("tableName"), i, lineContent, e.getMessage());
                    }
                    lineContenList.add(val);
                }
                dataList.add(lineContenList);
            }
        }
        int totalPage = dataList.size() % DataConstant.INSERT_DATA_TABLE_PAGESIZE == 0 ? dataList.size() / DataConstant.INSERT_DATA_TABLE_PAGESIZE : dataList.size() / DataConstant.INSERT_DATA_TABLE_PAGESIZE + 1;
        for (int i = 0; i < totalPage; i++) {
//            log.info("第{}页",i);
            map.put("dataList", dataList.stream().skip(i * DataConstant.INSERT_DATA_TABLE_PAGESIZE).limit(DataConstant.INSERT_DATA_TABLE_PAGESIZE).collect(Collectors.toList()));
            dataResourcePrimaryRepository.insertDataTable(map);
        }
    }

    public void handleRunSql(String paramStr) {
        JSONObject jsonObject = JSONObject.parseObject(paramStr);
        Long taskId = jsonObject.getLong("taskId");
        String scriptSqlContent = jsonObject.getString("scriptSqlContent");
        DataMpcTask dataMpcTask = dataMpcRepository.selectDataMpcTaskById(taskId);
        dataMpcTask.setTaskStatus(2);
        this.dataMpcPrRepository.updateDataMpcTask(dataMpcTask);
        try {
            List<Map> all = dataResourceSecondaryRepository.findAll(scriptSqlContent);
            if (all != null && all.size() > 0) {
                Set columnSet = new TreeSet(all.get(0).keySet());
                if (columnSet.size()>0){
                    List<String> dataList = new ArrayList<>();
                    dataList.add(StringUtils.join(columnSet.toArray(),","));
                    StringBuilder sb=new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append("mpc").append("/").append(DateUtil.formatDate(new Date(),DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat()));
                    String filePath = sb.toString();
                    List<String> dataValList = null;
                    for (Map map : all) {
                        dataValList = new ArrayList<>();
                        for (Object key : columnSet) {
                            dataValList.add(map.get(key)==null?"":map.get(key).toString());
                        }
                        dataList.add(StringUtils.join(dataValList.toArray(),","));
                    }
                    sb.append("/").append(dataMpcTask.getTaskIdName()).append(".csv");
                    dataMpcTask.setResultFilePath(sb.toString());
                    FileUtil.writeFile(filePath,dataMpcTask.getResultFilePath(),dataList);
                }
            }
            dataMpcTask.setTaskStatus(1);
        } catch (Exception e) {
            dataMpcTask.setResultFilePath(null);
            dataMpcTask.setLogData(e.getMessage());
            dataMpcTask.setTaskStatus(3);
        }
        this.dataMpcPrRepository.updateDataMpcTask(dataMpcTask);
    }

//    public static void main(String[] args) {
//        int total = 35;
//        List<Integer> dataList = new ArrayList<>();
//        for (int i = 1; i <= total; i++) {
//            dataList.add(i);
//        }
//        int totalPage = dataList.size() % DataConstant.INSERT_DATA_TABLE_PAGESIZE == 0 ? dataList.size() / DataConstant.INSERT_DATA_TABLE_PAGESIZE : dataList.size() / DataConstant.INSERT_DATA_TABLE_PAGESIZE + 1;
//        for (int i = 0; i < totalPage; i++) {
//            System.out.println(dataList.stream().skip(i*DataConstant.INSERT_DATA_TABLE_PAGESIZE).limit(DataConstant.INSERT_DATA_TABLE_PAGESIZE).collect(Collectors.toList()));
//        }
//    }
}
