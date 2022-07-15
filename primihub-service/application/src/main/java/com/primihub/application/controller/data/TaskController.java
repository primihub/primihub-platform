package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataPsi;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.service.data.DataTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.UUID;

@RequestMapping("task")
@RestController
@Slf4j
public class TaskController {

    @Autowired
    private DataTaskService dataTaskService;

    @RequestMapping("getModelTaskList")
    public BaseResultEntity getModelTaskList(Long modelId, PageReq req){
        if (modelId==null||modelId==0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        return dataTaskService.getModelTaskList(modelId,req);
    }

    @GetMapping("downloadTaskFile")
    public void downloadPirTask(HttpServletResponse response, Long taskId) {
        if (taskId==null||taskId==0L)
            return;
        DataTask dataTask = dataTaskService.getDataTaskById(taskId);
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
            log.info("downloadPsiTask -- fileName:{} -- fileContent -- e:{}",fileName,content,e.getMessage());
        }
    }
}
