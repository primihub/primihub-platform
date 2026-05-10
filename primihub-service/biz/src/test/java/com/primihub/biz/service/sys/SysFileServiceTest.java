package com.primihub.biz.service.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.sys.param.CreateFileParam;
import com.primihub.biz.repository.primarydb.sys.SysFilePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.primihub.biz.entity.sys.param.CreateFileParam;

@ExtendWith(MockitoExtension.class)
class SysFileServiceTest {

    @Mock private SysFilePrimarydbRepository primaryRepo;
    @Mock private SysFileSecondarydbRepository secondaryRepo;
    @Mock private com.primihub.biz.config.base.BaseConfiguration baseConfiguration;
    @InjectMocks private SysFileService fileService;

    @Test
    void getFileById_shouldReturnFile() {
        com.primihub.biz.entity.sys.po.SysFile file = new com.primihub.biz.entity.sys.po.SysFile();
        file.setFileId(1L);
        file.setFileName("test.csv");
        file.setFileSize(1024L);
        file.setFileCurrentSize(512L);
        given(secondaryRepo.selectSysFileByFileId(1L)).willReturn(file);

        BaseResultEntity result = fileService.getFileById(1L);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getFileById_shouldFail_whenNotFound() {
        given(secondaryRepo.selectSysFileByFileId(99L)).willReturn(null);

        BaseResultEntity result = fileService.getFileById(99L);

        assertThat(result.getCode()).isEqualTo(1003);
    }

    @Test
    void createFile_shouldSucceed() {
        given(baseConfiguration.getUploadUrlDirPrefix()).willReturn("/data/upload/");

        CreateFileParam param = new CreateFileParam();
        param.setFileSource(1);
        param.setFileSuffix("csv");

        BaseResultEntity result = fileService.createFile(param);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).insertSysFile(any());
    }
}
