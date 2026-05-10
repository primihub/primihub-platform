package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.po.DataProjectResource;
import com.primihub.biz.entity.data.req.DataProjectReq;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DataProjectConvertTest {

    @Test
    void dataProjectReqConvertPo_shouldMapFields() {
        DataProjectReq req = new DataProjectReq();
        req.setId(1L);
        req.setProjectId("p1");
        req.setProjectName("test project");
        req.setProjectDesc("desc");

        SysLocalOrganInfo organInfo = new SysLocalOrganInfo();
        organInfo.setOrganId("organ1");
        organInfo.setOrganName("MyOrgan");

        DataProject result = DataProjectConvert.dataProjectReqConvertPo(req, organInfo, "admin");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProjectId()).isEqualTo("p1");
        assertThat(result.getProjectName()).isEqualTo("test project");
        assertThat(result.getProjectDesc()).isEqualTo("desc");
        assertThat(result.getCreatedOrganId()).isEqualTo("organ1");
        assertThat(result.getCreatedOrganName()).isEqualTo("MyOrgan");
        assertThat(result.getCreatedUsername()).isEqualTo("admin");
        assertThat(result.getResourceNum()).isZero();
        assertThat(result.getStatus()).isZero();
        assertThat(result.getProviderOrganNames()).isEmpty();
    }

    @Test
    void dataProjectConvertListVo_shouldMapBasicFields() {
        DataProject dataProject = new DataProject();
        dataProject.setId(1L);
        dataProject.setProjectId("p1");
        dataProject.setProjectName("proj");
        dataProject.setProjectDesc("desc");
        dataProject.setCreatedOrganId("organ1");
        dataProject.setCreatedOrganName("MyOrgan");
        dataProject.setCreatedUsername("admin");
        dataProject.setResourceNum(3);
        dataProject.setProviderOrganNames("org1,org2");
        dataProject.setStatus(0);
        dataProject.setCreateDate(new Date());
        dataProject.setUpdateDate(new Date());

        DataProjectListVo result = DataProjectConvert.dataProjectConvertListVo(dataProject, null, null);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProjectId()).isEqualTo("p1");
        assertThat(result.getProjectName()).isEqualTo("proj");
        assertThat(result.getOrganId()).isEqualTo("organ1");
        assertThat(result.getResourceNum()).isEqualTo(3);
        assertThat(result.getProviderOrganNames()).isEqualTo("org1,org2");
        assertThat(result.getStatus()).isZero();
        assertThat(result.getCreateDate()).isNotNull();
        assertThat(result.getUpdateDate()).isNotNull();
    }

    @Test
    void dataProjectConvertListVo_shouldOverrideStatus_whenProvided() {
        DataProject dataProject = new DataProject();
        dataProject.setStatus(0);

        DataProjectListVo result = DataProjectConvert.dataProjectConvertListVo(dataProject, null, 2);

        assertThat(result.getStatus()).isEqualTo(2);
    }

    @Test
    void dataProjectConvertListVo_shouldParseTaskStatus() {
        DataProject dataProject = new DataProject();
        dataProject.setStatus(0);

        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("latestTaskStatus", "1");
        statusMap.put("statusCount", "5");
        List<Map<String, Object>> list = Collections.singletonList(statusMap);

        DataProjectListVo result = DataProjectConvert.dataProjectConvertListVo(dataProject, list, null);

        assertThat(result.getTaskSuccessNum()).isEqualTo(5);
        assertThat(result.getTaskRunNum()).isZero();
        assertThat(result.getTaskFailNum()).isZero();
    }

    @Test
    void dataProjectConvertDetailsVo_shouldMapCorrectly() {
        DataProject dp = new DataProject();
        dp.setId(1L);
        dp.setProjectId("p1");
        dp.setProjectName("proj");
        dp.setProjectDesc("desc");
        dp.setCreatedUsername("admin");
        dp.setStatus(0);
        dp.setCreateDate(new Date());

        DataProjectDetailsVo result = DataProjectConvert.dataProjectConvertDetailsVo(dp);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProjectName()).isEqualTo("proj");
        assertThat(result.getUserName()).isEqualTo("admin");
        assertThat(result.getStatus()).isZero();
    }

    @Test
    void dataProjectOrganConvertVo_shouldMarkCreatorAndThisInstitution() {
        DataProjectOrgan organ = new DataProjectOrgan();
        organ.setId(1L);
        organ.setProjectId("p1");
        organ.setOrganId("myOrgan");
        organ.setParticipationIdentity(0);
        organ.setAuditStatus(1);
        organ.setAuditOpinion("ok");

        SysLocalOrganInfo organInfo = new SysLocalOrganInfo();
        organInfo.setOrganId("myOrgan");

        SysOrgan sysOrgan = new SysOrgan();
        sysOrgan.setOrganName("MyOrgan");

        DataProjectOrganVo result = DataProjectConvert.DataProjectOrganConvertVo(organ, true, organInfo, sysOrgan);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreator()).isTrue();
        assertThat(result.getThisInstitution()).isTrue();
        assertThat(result.getOrganName()).isEqualTo("MyOrgan");
        assertThat(result.getAuditStatus()).isEqualTo(1);
        assertThat(result.getResources()).isEmpty();
    }

    @Test
    void dataProjectResourceConvertVo_shouldHandleNullResourceMap() {
        DataProjectResource resource = new DataProjectResource();
        resource.setId(1L);
        resource.setProjectId("p1");
        resource.setResourceId("r1");

        DataProjectResourceVo result = DataProjectConvert.DataProjectResourceConvertVo(resource, null);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getResourceName()).isEmpty();
        assertThat(result.getResourceTag()).isEmpty();
    }

    @Test
    void dataProjectResourceConvertVo_shouldMapResourceMap() {
        DataProjectResource resource = new DataProjectResource();
        resource.setId(1L);
        resource.setProjectId("p1");
        resource.setResourceId("r1");

        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put("resourceName", "testResource");
        resourceMap.put("resourceRowsCount", "1000");
        resourceMap.put("resourceColumnCount", "10");
        resourceMap.put("resourceContainsY", "1");
        resourceMap.put("resourceYRowsCount", "500");
        resourceMap.put("resourceYRatio", "0.5");

        DataProjectResourceVo result = DataProjectConvert.DataProjectResourceConvertVo(resource, resourceMap);

        assertThat(result.getResourceName()).isEqualTo("testResource");
        assertThat(result.getResourceRowsCount()).isEqualTo(1000);
        assertThat(result.getResourceColumnCount()).isEqualTo(10);
        assertThat(result.getResourceContainsY()).isEqualTo(1);
        assertThat(result.getResourceYRowsCount()).isEqualTo(500);
        assertThat(result.getResourceYRatio()).isEqualTo(new BigDecimal("0.5"));
    }
}
