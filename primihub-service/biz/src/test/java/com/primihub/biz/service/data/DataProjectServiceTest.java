package com.primihub.biz.service.data;

import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.req.DataProjectQueryReq;
import com.primihub.biz.entity.data.req.DataProjectReq;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.repository.primarydb.data.DataProjectPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysUserSecondarydbRepository;
import com.primihub.biz.service.sys.SysUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.SubscribableChannel;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataProjectServiceTest {

    @Mock private DataProjectRepository dataProjectRepository;
    @Mock private DataProjectPrRepository dataProjectPrRepository;
    @Mock private SysUserSecondarydbRepository sysUserSecondarydbRepository;
    @Mock private OrganConfiguration organConfiguration;
    @Mock private DataModelRepository dataModelRepository;
    @Mock private DataModelService dataModelService;
    @Mock private OtherBusinessesService otherBusinessesService;
    @Mock private DataResourceRepository dataResourceRepository;
    @Mock private SysUserService sysUserService;
    @Mock private com.primihub.biz.config.mq.SingleTaskChannel singleTaskChannel;
    @Mock private SubscribableChannel mockChannel;
    @InjectMocks private DataProjectService dataProjectService;

    @Test
    void saveOrUpdateProject_shouldCreateNewProject() {
        SysLocalOrganInfo organInfo = new SysLocalOrganInfo();
        organInfo.setOrganId("myOrgan");
        organInfo.setOrganName("MyOrgan");
        given(organConfiguration.getSysLocalOrganInfo()).willReturn(organInfo);
        given(organConfiguration.getSysLocalOrganId()).willReturn("myOrgan");

        SysUser user = new SysUser();
        user.setUserName("admin");
        given(sysUserSecondarydbRepository.selectSysUserByUserId(42L)).willReturn(user);
        given(organConfiguration.generateUniqueCode()).willReturn("PROJ-001");

        doAnswer(i -> {
            DataProject dp = i.getArgument(0);
            dp.setId(1L);
            dp.setProjectId("p1");
            return null;
        }).when(dataProjectPrRepository).saveDataProject(any());

        given(singleTaskChannel.input()).willReturn(mockChannel);
        given(dataProjectRepository.selectDataProjcetOrganByProjectId(anyString())).willReturn(Collections.emptyList());
        given(dataProjectRepository.selectProjectResourceByProjectId(anyString())).willReturn(Collections.emptyList());

        DataProjectReq req = new DataProjectReq();
        req.setProjectName("Test Project");
        req.setProjectDesc("A test project");

        BaseResultEntity result = dataProjectService.saveOrUpdateProject(req, 42L);

        assertThat(result.getCode()).isZero();
        verify(dataProjectPrRepository).saveDataProject(any());
    }

    @Test
    void saveOrUpdateProject_shouldFail_whenNoOrganInfo() {
        given(organConfiguration.getSysLocalOrganInfo()).willReturn(null);

        DataProjectReq req = new DataProjectReq();
        BaseResultEntity result = dataProjectService.saveOrUpdateProject(req, 42L);

        assertThat(result.getCode()).isEqualTo(1001);
    }

    @Test
    void saveOrUpdateProject_shouldUpdateExisting() {
        SysLocalOrganInfo organInfo = new SysLocalOrganInfo();
        organInfo.setOrganId("myOrgan");
        organInfo.setOrganName("MyOrgan");
        given(organConfiguration.getSysLocalOrganInfo()).willReturn(organInfo);
        given(organConfiguration.getSysLocalOrganId()).willReturn("myOrgan");

        DataProject existing = new DataProject();
        existing.setId(1L);
        existing.setProjectId("p1");
        existing.setProjectName("Old Name");
        existing.setProjectDesc("Old Desc");
        existing.setStatus(1);
        given(singleTaskChannel.input()).willReturn(mockChannel);
        given(dataProjectRepository.selectDataProjectByProjectId(1L, null)).willReturn(existing);
        given(dataProjectRepository.selectDataProjcetOrganByProjectId("p1")).willReturn(Collections.emptyList());
        given(dataProjectRepository.selectProjectResourceByProjectId("p1")).willReturn(Collections.emptyList());

        DataProjectReq req = new DataProjectReq();
        req.setId(1L);
        req.setProjectName("New Name");
        req.setProjectDesc("New Desc");

        BaseResultEntity result = dataProjectService.saveOrUpdateProject(req, 42L);

        assertThat(result.getCode()).isZero();
        assertThat(existing.getProjectName()).isEqualTo("New Name");
        assertThat(existing.getProjectDesc()).isEqualTo("New Desc");
    }

    @Test
    void getProjectList_shouldReturnPaginatedProjects() {
        given(organConfiguration.getSysLocalOrganId()).willReturn("myOrgan");

        DataProject project = new DataProject();
        project.setId(1L);
        project.setProjectId("p1");
        project.setProjectName("Test");
        project.setCreatedOrganId("o1");
        project.setCreatedOrganName("Organ");
        project.setCreatedUsername("admin");
        project.setCreateDate(new Date());
        project.setUpdateDate(new Date());
        project.setStatus(1);

        given(dataProjectRepository.selectDataProjectPage(any())).willReturn(Collections.singletonList(project));
        given(dataProjectRepository.selectDataProjectCount(any())).willReturn(1);
        given(dataModelRepository.queryModelNumByProjectIds(anySet())).willReturn(Collections.emptyList());

        DataProjectQueryReq req = new DataProjectQueryReq();
        req.setPageNo(1);
        req.setPageSize(10);

        BaseResultEntity result = dataProjectService.getProjectList(req);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getProjectDetails_shouldReturnDetails() {
        DataProject project = new DataProject();
        project.setId(1L);
        project.setProjectId("p1");
        project.setProjectName("Test");
        project.setCreatedOrganId("myOrgan");
        project.setCreatedOrganName("Organ");
        project.setCreatedUsername("admin");
        project.setStatus(1);

        given(dataProjectRepository.selectDataProjectByProjectId(1L, null)).willReturn(project);

        SysLocalOrganInfo organInfo = new SysLocalOrganInfo();
        organInfo.setOrganId("myOrgan");
        given(organConfiguration.getSysLocalOrganInfo()).willReturn(organInfo);

        given(dataProjectRepository.selectDataProjcetOrganByProjectId("p1")).willReturn(Collections.emptyList());
        given(dataProjectRepository.selectProjectResourceByProjectId("p1")).willReturn(Collections.emptyList());
        given(otherBusinessesService.getOrganListMap(anyList())).willReturn(Collections.emptyMap());
        given(otherBusinessesService.getResourceListMap(anyList())).willReturn(Collections.emptyMap());

        BaseResultEntity result = dataProjectService.getProjectDetails(1L);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getProjectDetails_shouldFail_whenNotFound() {
        given(dataProjectRepository.selectDataProjectByProjectId(99L, null)).willReturn(null);

        BaseResultEntity result = dataProjectService.getProjectDetails(99L);

        assertThat(result.getCode()).isEqualTo(1003);
    }

    @Test
    void closeProject_shouldCloseSuccessfully() {
        SysLocalOrganInfo organInfo = new SysLocalOrganInfo();
        organInfo.setOrganId("myOrgan");
        given(organConfiguration.getSysLocalOrganInfo()).willReturn(organInfo);

        given(singleTaskChannel.input()).willReturn(mockChannel);
        DataProject project = new DataProject();
        project.setId(1L);
        project.setProjectId("p1");
        project.setStatus(1);
        given(dataProjectRepository.selectDataProjectByProjectId(1L, null)).willReturn(project);

        BaseResultEntity result = dataProjectService.closeProject(1L);

        assertThat(result.getCode()).isZero();
    }
}
