package com.yyds.application.controller.sys;

import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.sys.param.CreateFileParam;
import com.yyds.biz.entity.sys.po.SysFile;
import com.yyds.biz.service.sys.SysFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequestMapping("file")
@RestController
public class FileController {

    @Autowired
    private SysFileService sysFileService;

    @RequestMapping("upload")
    public BaseResultEntity upload(@RequestParam(value = "file") MultipartFile file,Integer fileSource){
        if(fileSource==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileSource");
        if(file.getSize()==0)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"file大小为0");
        return sysFileService.upload(file,fileSource);
    }

    @RequestMapping("createFile")
    public BaseResultEntity createFile(CreateFileParam createFileParam){
        if(createFileParam.getFileSource()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileSource");
        if(createFileParam.getFileSize()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileSize");
        if(createFileParam.getFileSize()<=0)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"fileSize");
        if(createFileParam.getFileSuffix()==null||createFileParam.getFileSuffix().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileSuffix");
        return sysFileService.createFile(createFileParam);
    }

    @RequestMapping("getFileById")
    public BaseResultEntity getFileById(Long fileId){
        if(fileId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileId");
        return sysFileService.getFileById(fileId);
    }

    @RequestMapping("uploadPart")
    public BaseResultEntity uploadPart(@RequestParam(value = "file") MultipartFile file,Long fileId){
        if(fileId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileId");
        if(file.getSize()==0)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"file大小为0");
        return sysFileService.uploadPart(file,fileId);
    }

}
