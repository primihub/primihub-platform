package com.primihub.application.controller.data;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.dto.ModelOutputPathDto;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.service.data.DataTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RequestMapping("task")
@RestController
@Slf4j
public class TaskController {

    @Autowired
    private DataTaskService dataTaskService;

    @RequestMapping("getTaskData")
    public BaseResultEntity getTaskData(Long taskId){
        if (taskId==null||taskId==0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        return dataTaskService.getTaskData(taskId);
    }

    @RequestMapping("getModelTaskList")
    public BaseResultEntity getModelTaskList(Long modelId, PageReq req){
        if (modelId==null||modelId==0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        return dataTaskService.getModelTaskList(modelId,req);
    }

    @GetMapping("downloadTaskFile")
    public void downloadTaskFile(HttpServletResponse response, Long taskId,Long modelId) throws Exception {
        DataTask dataTask = dataTaskService.getDataTaskById(taskId,modelId);
        if (dataTask!=null&&dataTask.getTaskType().equals(TaskTypeEnum.MODEL.getTaskType())){
            downloadModelTask(response,dataTask);
        }else {
            downloadDefaultTask(response,dataTask);
        }
    }
    public void downloadModelTask(HttpServletResponse response,DataTask dataTask) throws Exception {
        String taskResultContent = dataTask.getTaskResultContent();
        if (StringUtils.isNotBlank(taskResultContent)){
            ModelOutputPathDto modelOutputPathDto = JSONObject.parseObject(taskResultContent, ModelOutputPathDto.class);
            File file = new File(modelOutputPathDto.getModelRunZipFilePath());
            if (file.exists()){
                // ?????????????????????
                FileInputStream inputStream = new FileInputStream(file);
                // ?????????????????????????????????????????????
                response.setContentType("application/zip");
                response.setHeader("content-disposition", "attachment; fileName=" + new String(file.getName().getBytes("UTF-8"),"iso-8859-1"));
                ServletOutputStream outputStream = response.getOutputStream();
                int len = 0;
                byte[] data = new byte[1024];
                while ((len = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, len);
                }
                outputStream.close();
                inputStream.close();
            }else {
                downloadTaskError(response,"?????????");
            }
        }else {
            downloadTaskError(response,"?????????");
        }

    }

    public void downloadDefaultTask(HttpServletResponse response,DataTask dataTask) throws Exception{
        String content = "no data";
        String fileName = null;
        if (dataTask!=null){
            if (dataTask.getTaskResultContent()!=null){
                content = dataTask.getTaskResultContent();
            }
            fileName = dataTask.getTaskIdName()+".csv";
        }else {
            fileName = UUID.randomUUID().toString()+".csv";
        }
        OutputStream outputStream = null;
        //???????????????????????????
        byte[] currentLogByte = content.getBytes();
        try {
            // ???????????????????????????????????????????????????
            response.setHeader("content-Type","application/vnd.ms-excel");
            // ???????????????????????????
            response.setHeader("Content-disposition","attachment;filename="+ new String(fileName.getBytes("UTF-8"),"iso-8859-1"));
            response.setCharacterEncoding("UTF-8");
            outputStream = response.getOutputStream();
            outputStream.write(currentLogByte);
            outputStream.close();
            outputStream.flush();
        }catch (Exception e) {
            log.info("downloadPsiTask -- fileName:{} -- fileContent -- e:{}",fileName,content,e.getMessage());
            downloadTaskError(response,"??????????????????");
        }
    }


    public void downloadTaskError(HttpServletResponse response,String message) throws IOException {
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().println(BaseResultEntity.failure(BaseResultEnum.DATA_DOWNLOAD_TASK_ERROR_FAIL,message));
    }
}
