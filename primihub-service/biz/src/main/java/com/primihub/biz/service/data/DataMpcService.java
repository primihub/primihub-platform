package com.primihub.biz.service.data;

import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataScriptReq;
import com.primihub.biz.entity.data.vo.DataRunScript;
import com.primihub.biz.repository.primarydb.data.DataMpcPrRepository;
import com.primihub.biz.repository.resourcesecondarydb.data.DataResourceSecondaryRepository;
import com.primihub.biz.repository.secondarydb.data.DataMpcRepository;
import com.primihub.biz.convert.DataMpcConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.vo.DataScriptVo;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.primihub.biz.util.sql.mysql.AstMySqlStatementFactory;
import com.primihub.biz.util.sql.mysql.AstMysqlSelectProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataMpcService {

    @Autowired
    private DataMpcPrRepository dataMpcPrRepository;

    @Autowired
    private DataMpcRepository dataMpcRepository;

    @Autowired
    private DataProjectRepository dataProjectRepository;

    @Autowired
    private DataResourceRepository dataResourceRepository;

    @Autowired
    private SysFileSecondarydbRepository sysFileSecondarydbRepository;

    @Autowired
    private DataResourceSecondaryRepository dataResourceSecondaryRepository;


    public BaseResultEntity saveOrUpdateScript(Long userId, Long organId, DataScriptReq req) {
        DataScript dataScript = DataMpcConvert.dataScriptReqConvertPo(req, userId, organId);
        if (dataScript.getScriptId()!=null&&dataScript.getScriptId()!=0L){
            dataMpcPrRepository.updateDataScript(dataScript);
        }else {
            dataMpcPrRepository.saveDataScript(dataScript);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("scriptId",dataScript.getScriptId());
        map.put("name",dataScript.getName());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getDataScriptList(Long userId, Long organId, String scriptName) {
        List<DataScript> dataScripts = dataMpcRepository.selectDataScriptByUser(userId, scriptName);
        if (dataScripts.size()==0)
            return BaseResultEntity.success(new ArrayList<>());
        List<DataScriptVo> scriptVoList = dataScripts.stream().map(DataMpcConvert::dataScriptPoConvertVo).collect(Collectors.toList());
        if (StringUtils.isBlank(scriptName)){
            List<DataScriptVo> tops = scriptVoList.stream().filter(vo -> vo.getPScriptId() == null).collect(Collectors.toList());
            if (tops.size() == 0){
                return BaseResultEntity.success(scriptVoList);
            }else {
                for (DataScriptVo top : tops) {
                    combinedFileTree(top,scriptVoList);
                }
            }
            return BaseResultEntity.success(tops);
        }
        return BaseResultEntity.success(scriptVoList);
    }

    private void combinedFileTree(DataScriptVo top, List<DataScriptVo> scriptVoList){
        if (top.getCatalogue()==1){
            List<DataScriptVo> pList = scriptVoList.stream().filter(vo -> top.getScriptId().equals(vo.getPScriptId())).collect(Collectors.toList());
            top.setChildren(pList);
            for (DataScriptVo vo : pList) {
                combinedFileTree(vo,scriptVoList);
            }
        }
    }

    public BaseResultEntity delDataScript(Long scriptId) {
        DataScript dataScript = dataMpcRepository.selectDataScriptById(scriptId);
        if (dataScript==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"未查询到信息");
        if (dataScript.getCatalogue()==1){
            if(dataMpcRepository.selectDataScriptByPId(scriptId).size()>0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"有下级目录或文件");
        }
        dataMpcPrRepository.deleteDataScript(scriptId);
        return BaseResultEntity.success();
    }

    public BaseResultEntity runDataScript(Long scriptId,Long projectId,Long userId,String scriptContent) {
        AstMysqlSelectProperty astMysqlSelectProperty = null;
        try {
            astMysqlSelectProperty = AstMySqlStatementFactory.generateAstMysqlSelectProperty(scriptContent);
        } catch (Exception e) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_SQL_CHECK_FAIL,"sql 校验失败",e.getMessage());
        }
        if (astMysqlSelectProperty == null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_SQL_CHECK_FAIL,"sql 校验失败");
        List<DataProjectResource> dataProjectResources = dataProjectRepository.queryProjectResourceByProjectId(projectId);
        if (dataProjectResources.size()==0)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_SQL_CHECK_FAIL,"未找到资源数据");
        Map<String, DataRunScript> dataRunScriptMap = getDataRunScript(dataProjectResources);
        BaseResultEntity baseResultEntity = checkSqlResources(astMysqlSelectProperty, dataRunScriptMap, scriptContent);
        log.info(baseResultEntity.getMsg());
        if (baseResultEntity.getCode()!=0)
            return baseResultEntity;
        log.info(baseResultEntity.getResult().toString());
        DataMpcTask mpcTask = new DataMpcTask(UUID.randomUUID().toString(),scriptId,projectId,userId);
        dataMpcPrRepository.saveDataMpcTask(mpcTask);
        Map<String,Object> map = new HashMap<>();
        map.put("taskId",mpcTask.getTaskId());
        map.put("taskIdName",mpcTask.getTaskIdName());
        map.put("scriptContent",baseResultEntity.getResult().toString());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity checkSqlResources(AstMysqlSelectProperty astMysqlSelectProperty,Map<String, DataRunScript> dataRunScriptMap,String scriptContent){
        // 校验表信息
        Iterator<Map.Entry<String, String>> tableIt = astMysqlSelectProperty.getTableNameMap().entrySet().iterator();
        DataRunScript dataRunScript = null;
        Set<String> columnSet = new HashSet<>();
        Set<String> columnAsSet = new HashSet<>();
//        columnSet.add("*");
        while (tableIt.hasNext()){
            Map.Entry<String, String> next = tableIt.next();
            String tableUnicode = cnToUnicode(next.getKey());
            dataRunScript = dataRunScriptMap.get(tableUnicode);
            if (dataRunScript==null)
                dataRunScript = dataRunScriptMap.get("60"+tableUnicode+"60");
            if (dataRunScript==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_SQL_CHECK_FAIL,"资源:"+next.getKey() + "不存在");
            scriptContent = scriptContent.replaceAll(next.getKey(),"`"+dataRunScript.getFileName()+"`");
            String asName = next.getValue();
            for (DataFileField dataFileField : dataRunScript.getDataFileFields()) {
                String fieldName = StringUtils.isNotBlank(dataFileField.getFieldAs()) ? dataFileField.getFieldAs() : dataFileField.getFieldName();
                columnSet.add(fieldName);
                if (StringUtils.isNotBlank(asName)){
                    columnAsSet.add(asName+"."+fieldName);
                }else {
                    columnAsSet.add(dataRunScript.getResourceName()+"."+fieldName);
                }
            }
        }
        columnAsSet.addAll(columnSet);
        // 校验查询列信息
        Iterator<Map.Entry<String, String>> columnIt = astMysqlSelectProperty.getColumnMap().entrySet().iterator();
        while (columnIt.hasNext()){
            Map.Entry<String, String> next = columnIt.next();
            if (next.getKey().indexOf("*")!=-1){
                String[] split = next.getKey().split("\\.");
                if (split.length == 2){
                    String as = split[0];
                    String asColumn = StringUtils.join(columnAsSet.stream().filter(a->a.indexOf(as)!=-1).collect(Collectors.toSet()).toArray(), ",");
                    scriptContent = scriptContent.replace(next.getKey(),asColumn);
                }else {
                    String allColumn = StringUtils.join(columnSet.toArray(), ",");
                    scriptContent = scriptContent.replace("*",allColumn);
                }
            }else {
                if (!columnSet.contains(next.getKey()))
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_SQL_CHECK_FAIL,"字段:"+next.getKey() + "不存在");
            }
        }
        return BaseResultEntity.success(scriptContent);
    }




    public Map<String, DataRunScript> getDataRunScript(List<DataProjectResource> dataProjectResources){
        Map<String, DataRunScript> map = new HashMap<>();
        Set<Long> resourceIds = dataProjectResources.stream().map(DataProjectResource::getResourceId).collect(Collectors.toSet());
        List<DataFileField> dataFileField = dataResourceRepository.queryDataFileField(new HashMap() {{
            put("resourceIds", resourceIds);
            put("protectionStatus", 1);
        }});
        Map<Long, List<DataFileField>> dataFileFieldMap = dataFileField.stream().collect(Collectors.groupingBy(DataFileField::getResourceId));
        List<DataResource> dataResources = dataResourceRepository.queryDataResourceByResourceIds(resourceIds);
        Set<Long> fileIds = dataResources.stream().map(DataResource::getFileId).collect(Collectors.toSet());
        Map<Long, String> fileNameMap = sysFileSecondarydbRepository.selectSysFileByFileIds(fileIds).stream().collect(Collectors.toMap(SysFile::getFileId, SysFile::getFileName));
        DataRunScript dataRunScript = null;
        for (DataResource dataResource : dataResources) {
            dataRunScript = new DataRunScript(dataResource.getResourceId(), dataResource.getResourceName(), dataResource.getFileId(), fileNameMap.get(dataResource.getFileId()), dataFileFieldMap.get(dataResource.getResourceId()));
            map.put(cnToUnicode(dataResource.getResourceName()),dataRunScript);
            map.put("60"+cnToUnicode(dataResource.getResourceName())+"60",dataRunScript);
        }
        return map;
    }

    /**
     * 中文转Unicode
     * 其他英文字母或特殊符号也可进行Unicode编码
     * @param cn
     * @return
     */
    public static String cnToUnicode(String cn) {
        char[] chars = cn.toCharArray();
        StringBuilder returnStr = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
//            returnStr.append("\\u").append(Integer.toString(chars[i], 16));
            returnStr.append(Integer.toString(chars[i], 16));
        }
        return returnStr.toString();
    }


}
