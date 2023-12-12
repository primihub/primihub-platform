package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.CreateFileParam;
import com.primihub.biz.service.sys.SysFileService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 */
@Api(value = "文件接口",tags = "文件接口")
@RequestMapping("file")
@RestController
public class FileController {

    @Autowired
    private SysFileService sysFileService;

    /**
     * 小文件上传
     * @param file
     * @param fileSource
     * @return
     */
    @RequestMapping("upload")
    public BaseResultEntity upload(@RequestParam(value = "file") MultipartFile file,Integer fileSource){
        if(fileSource==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileSource");
        }
        if(file.getSize()==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"file大小为0");
        }
        return sysFileService.upload(file,fileSource);
    }

    @RequestMapping("createFile")
    public BaseResultEntity createFile(CreateFileParam createFileParam){
        if(createFileParam.getFileSource()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileSource");
        }
        if(createFileParam.getFileSize()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileSize");
        }
        if(createFileParam.getFileSize()<=0) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"fileSize");
        }
        if(createFileParam.getFileSuffix()==null|| "".equals(createFileParam.getFileSuffix())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileSuffix");
        }
        return sysFileService.createFile(createFileParam);
    }

    @GetMapping("getFileById")
    public BaseResultEntity getFileById(Long fileId){
        if(fileId==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileId");
        }
        return sysFileService.getFileById(fileId);
    }

    @RequestMapping("uploadPart")
    public BaseResultEntity uploadPart(@RequestParam(value = "file") MultipartFile file,Long fileId){
        if(fileId==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileId");
        }
        if(file.getSize()==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"file大小为0");
        }
        return sysFileService.uploadPart(file,fileId);
    }

}
