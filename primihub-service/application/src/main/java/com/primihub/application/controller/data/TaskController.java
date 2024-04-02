package com.primihub.application.controller.data;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.dto.ModelOutputPathDto;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.DataTaskReq;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.repository.secondarydb.data.DataPsiRepository;
import com.primihub.biz.service.data.DataTaskService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 任务接口
 */
@Api(value = "任务接口",tags = "任务接口")
@RequestMapping("task")
@RestController
@Slf4j
public class TaskController {

    @Autowired
    private DataTaskService dataTaskService;
    @Autowired
    private DataPsiRepository dataPsiRepository;


    /**
     * 修改任务描述
     * @param taskId    任务ID-数字的
     * @param taskDesc  任务描述信息
     * @return
     */
    @GetMapping("updateTaskDesc")
    public BaseResultEntity updateTaskDesc(Long taskId,String taskDesc){
        if (taskId==null||taskId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        if (StringUtils.isBlank(taskDesc)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskDesc");
        }
        return dataTaskService.updateTaskDesc(taskId,taskDesc);
    }

    @PostMapping("deleteTask")
    public BaseResultEntity deleteTask(Long taskId){
        if (taskId==null||taskId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return dataTaskService.deleteTaskData(taskId);
    }

    @GetMapping("getTaskData")
    public BaseResultEntity getTaskData(Long taskId){
        if (taskId==null||taskId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return dataTaskService.getTaskData(taskId);
    }

    @GetMapping("getModelTaskList")
    public BaseResultEntity getModelTaskList(Long modelId, PageReq req){
        if (modelId==null||modelId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        return dataTaskService.getModelTaskList(modelId,req);
    }

    @GetMapping("getTaskList")
    public BaseResultEntity getTaskList(DataTaskReq req){
        return dataTaskService.getTaskList(req);
    }


    @GetMapping("cancelTask")
    public BaseResultEntity cancelPsiTask(String taskId){
        if (StringUtils.isBlank(taskId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return  dataTaskService.cancelTask(taskId);
    }

    /**
     * 用于创建连接
     */
    @GetMapping(value = "sseConnect/{taskId}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@PathVariable String taskId,Integer all) {
        if (all==null) {
            all = 0;
        }
        return dataTaskService.connectSseTask(taskId,all);
    }

    /**
     * 关闭连接
     */
    @GetMapping("sseClose/{taskId}")
    public BaseResultEntity close(@PathVariable("taskId") String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        dataTaskService.removeSseTask(taskId);
        return BaseResultEntity.success();
    }

    @GetMapping("getTaskLogInfo")
    public BaseResultEntity getTaskLogInfo(Long taskId){
        if (taskId==null||taskId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return  dataTaskService.getTaskLogInfo(taskId);
    }

    @GetMapping("downloadTaskLog")
    public void downloadTaskLog(HttpServletResponse response, Long taskId) throws Exception {
        DataTask dataTask = dataTaskService.getDataTaskById(taskId,null);
        if (dataTask==null){
            downloadTaskError(response,"无任务信息");
        }else {
            if (dataTask.getTaskState().equals(TaskStateEnum.IN_OPERATION.getStateType())){
                downloadTaskError(response,"任务运行中不可下载");
                return;
            }
            File logFile = dataTaskService.getLogFile(dataTask);
            if (logFile.exists()){
                // 获得文件输入流
                FileInputStream inputStream = new FileInputStream(logFile);
                // 设置响应头、以附件形式打开文件
                response.setContentType("text/x-log");
                response.setHeader("content-disposition", "attachment; fileName=" + new String((dataTask.getTaskIdName()+".log").getBytes("UTF-8"),"iso-8859-1"));
                ServletOutputStream outputStream = response.getOutputStream();
                int len = 0;
                byte[] data = new byte[1024];
                while ((len = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, len);
                }
                outputStream.close();
                inputStream.close();
            }else {
                downloadTaskError(response,"无日志文件");
            }
        }
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
            boolean isCooperation = dataTask.getIsCooperation() == 1;
            File file = new File(isCooperation?modelOutputPathDto.getGuestModelFileName():modelOutputPathDto.getHostModelFileName());
            if (file.exists()){
                FileInputStream inputStream = new FileInputStream(file);
                response.setHeader("content-Type","application/vnd.ms-excel");
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
                downloadTaskError(response,"无文件");
            }
        }else {
            downloadTaskError(response,"无文件");
        }

    }

    public void downloadDefaultTask(HttpServletResponse response,DataTask dataTask) throws Exception{
        File file = null;
        if (dataTask.getTaskType().equals(TaskTypeEnum.PSI.getTaskType())){
            DataPsiTask dataPsiTask = dataPsiRepository.selectPsiTaskById(dataTask.getTaskId());
            if (dataPsiTask!=null) {
                file = new File(dataPsiTask.getFilePath());
            }
        }else{
            file = new File(dataTask.getTaskResultPath());
        }
        if (file!=null&&file.exists()){
            FileInputStream inputStream = new FileInputStream(file);
            response.setHeader("content-Type","application/vnd.ms-excel");
            if (dataTask.getTaskType().equals(TaskTypeEnum.PIR.getTaskType())){
                response.setHeader("content-disposition", "attachment; fileName=" + new String((dataTask.getTaskName()+"任务结果.csv").getBytes("UTF-8"),"iso-8859-1"));
            }else {
                response.setHeader("content-disposition", "attachment; fileName=" + new String(file.getName().getBytes("UTF-8"),"iso-8859-1"));
            }
            ServletOutputStream outputStream = response.getOutputStream();
            int len = 0;
            byte[] data = new byte[1024];
            while ((len = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, len);
            }
            outputStream.close();
            inputStream.close();
        }else {
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
            //将字符串转化为文件
            byte[] currentLogByte = content.getBytes();
            try {
                // 告诉浏览器用什么软件可以打开此文件
                response.setHeader("content-Type","application/vnd.ms-excel");
                // 下载文件的默认名称
                response.setHeader("Content-disposition","attachment;filename="+ new String(fileName.getBytes("UTF-8"),"iso-8859-1"));
                response.setCharacterEncoding("UTF-8");
                outputStream = response.getOutputStream();
                outputStream.write(currentLogByte);
                outputStream.close();
                outputStream.flush();
            }catch (Exception e) {
                log.info("downloadPsiTask -- fileName:{} -- fileContent:{} -- e:{}",fileName,content,e.getMessage());
                downloadTaskError(response,"文件读取失败");
            }
        }

    }


    public void downloadTaskError(HttpServletResponse response,String message) throws IOException {
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().println(JSONObject.toJSONString(BaseResultEntity.failure(BaseResultEnum.DATA_DOWNLOAD_TASK_ERROR_FAIL,message)));
    }
}
