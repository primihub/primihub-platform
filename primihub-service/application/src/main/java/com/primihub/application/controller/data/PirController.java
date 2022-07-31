package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.service.data.PirService;
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

@RequestMapping("pir")
@RestController
@Slf4j
public class PirController {

    @Autowired
    private PirService pirService;

    @RequestMapping("pirSubmitTask")
    public BaseResultEntity pirSubmitTask(String serverAddress,String resourceId,String pirParam){
        if (StringUtils.isBlank(resourceId)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        if (StringUtils.isBlank(pirParam)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"pirParam");
        }
        if (StringUtils.isBlank(serverAddress)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        }
        return pirService.pirSubmitTask(serverAddress,resourceId,pirParam);
    }

    @GetMapping("downloadPirTask")
    public void downloadPirTask(HttpServletResponse response, String taskId,String taskDate) {
        if (StringUtils.isBlank(taskId)||StringUtils.isBlank(taskDate))
            return;
        String resultFilePath = pirService.getResultFilePath(taskId,taskDate);
        String fileName = taskId+".csv";
        OutputStream outputStream = null;
        try {
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type","application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-disposition","attachment;filename="+ URLEncoder.encode(new String(fileName.getBytes("UTF-8"),"iso-8859-1"),"utf-8"));
            outputStream = response.getOutputStream();
            File file = new File(resultFilePath);
            if (!file.exists()){
                log.info("{}-不存在",resultFilePath);
                //将字符串转化为文件
                byte[] currentLogByte = "no data".getBytes();
                outputStream.write(currentLogByte);
            }else {
                InputStream stream = new FileInputStream(file);
                byte buff[] = new byte[1024];
                int length = 0;
                while ((length = stream.read(buff)) > 0) {
                    outputStream.write(buff,0,length);
                }
                stream.close();
            }
            outputStream.close();
            outputStream.flush();
        }catch (Exception e) {
            log.info("downloadPirTask -- fileName:{} -- resultFilePath:{} -- e:{}",fileName,resultFilePath,e.getMessage());
        }
    }
}
