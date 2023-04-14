package com.primihub.biz.service.sys;

import com.primihub.biz.repository.primarydb.sys.SysFilePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.CreateFileParam;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.util.crypt.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class SysFileService {

    @Autowired
    private SysFilePrimarydbRepository sysFilePrimarydbRepository;
    @Autowired
    private SysFileSecondarydbRepository sysFileSecondarydbRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;

    public BaseResultEntity upload(MultipartFile file, Integer fileSource){
        SysFile sysFile=new SysFile();
        sysFile.setFileSource(fileSource);
        String[] strArray=file.getOriginalFilename().split("\\.");
        sysFile.setFileSuffix(strArray[strArray.length-1]);
        sysFile.setFileName(UUID.randomUUID().toString());
        Date date=new Date();
        StringBuilder sb=new StringBuilder().append(baseConfiguration.getUploadUrlDirPrefix()).append(fileSource)
                .append("/").append(DateUtil.formatDate(date,DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/");
        sysFile.setFileArea("local");
        sysFile.setFileSize(file.getSize());
        sysFile.setFileCurrentSize(file.getSize());
        sysFile.setIsDel(0);

        try {
            File tempFile=new File(sb.toString());
            if(!tempFile.exists()) {
                tempFile.mkdirs();
            }
            sysFile.setFileUrl(sb.append(sysFile.getFileName()).append(".").append(sysFile.getFileSuffix()).toString());
            file.transferTo(new File(sysFile.getFileUrl()));
        } catch (IOException e) {
            log.error("upload",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"写硬盘失败");
        }

        sysFilePrimarydbRepository.insertSysFile(sysFile);
        Map map=new HashMap<>();
        map.put("sysFile",sysFile);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity createFile(CreateFileParam createFileParam){
        SysFile sysFile=new SysFile();
        BeanUtils.copyProperties(createFileParam,sysFile);
        sysFile.setFileName(UUID.randomUUID().toString());
        Date date=new Date();
        StringBuilder sb=new StringBuilder().append(baseConfiguration.getUploadUrlDirPrefix()).append(createFileParam.getFileSource())
                .append("/").append(DateUtil.formatDate(date,DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/");
        sysFile.setFileUrl(sb.append(sysFile.getFileName()).append(".").append(sysFile.getFileSuffix()).toString());
        sysFile.setFileArea("local");
        sysFile.setFileCurrentSize(0L);
        sysFile.setIsDel(0);
        sysFilePrimarydbRepository.insertSysFile(sysFile);
        Map map=new HashMap<>();
        map.put("sysFile",sysFile);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getFileById(Long fileId){
        SysFile sysFile=sysFileSecondarydbRepository.selectSysFileByFileId(fileId);
        Map map=new HashMap<>();
        map.put("sysFile",sysFile);
        BigDecimal statusPercent=new BigDecimal(sysFile.getFileCurrentSize()).divide(new BigDecimal(sysFile.getFileSize()),4,BigDecimal.ROUND_HALF_EVEN);
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(2);

        if(sysFile.getFileSize().equals(sysFile.getFileCurrentSize())) {
            map.put("status", 1);
            map.put("statusDesc", "已完成");
        }else{
            map.put("status",0);
            map.put("statusDesc","未完成");
        }
        map.put("statusPercent",percent.format(statusPercent.doubleValue()));
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity uploadPart(MultipartFile file,Long fileId){
        SysFile sysFile=sysFileSecondarydbRepository.selectSysFileByFileId(fileId);
        if(sysFile.getFileSize().equals(sysFile.getFileCurrentSize())){
            Map map=new HashMap<>();
            map.put("sysFile",sysFile);
            map.put("status",1);
            map.put("statusDesc","已完成");
            map.put("statusPercent","100%");
            return BaseResultEntity.success(map);
        }

        try {
            File tempFile=new File(sysFile.getFileUrl());
            if(!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }
            RandomAccessFile raf = new RandomAccessFile(sysFile.getFileUrl(), "rw");
            long fileLength = raf.length();
            raf.seek(fileLength);
            raf.write(file.getBytes());
            sysFile.setFileCurrentSize(sysFile.getFileCurrentSize()+file.getSize());
        } catch (IOException e) {
            log.error("upload",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"写硬盘失败");
        }

        sysFilePrimarydbRepository.updateSysFileCurrentSize(sysFile.getFileId(),sysFile.getFileCurrentSize());

        Map map=new HashMap<>();
        map.put("sysFile",sysFile);
        BigDecimal statusPercent=new BigDecimal(sysFile.getFileCurrentSize()).divide(new BigDecimal(sysFile.getFileSize()),4,BigDecimal.ROUND_HALF_EVEN);
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(2);

        if(sysFile.getFileSize().equals(sysFile.getFileCurrentSize())) {
            map.put("status", 1);
            map.put("statusDesc", "已完成");
        }else{
            map.put("status",0);
            map.put("statusDesc","未完成");
        }
        map.put("statusPercent",percent.format(statusPercent.doubleValue()));
        return BaseResultEntity.success(map);
    }

}
