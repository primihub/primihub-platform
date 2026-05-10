package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysOrgan;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DataModelConvertTest {

    @Test
    void dataModelReqConvertPo_shouldMapFields() {
        DataModelAndComponentReq req = new DataModelAndComponentReq();
        req.setModelId("100");
        req.setModelDesc("test model");
        req.setIsDraft(0);
        req.setTrainType(1);

        DataModel result = DataModelConvert.dataModelReqConvertPo(req, 42L);

        assertThat(result.getModelId()).isEqualTo(100L);
        assertThat(result.getModelDesc()).isEqualTo("test model");
        assertThat(result.getIsDraft()).isZero();
        assertThat(result.getTrainType()).isEqualTo(1);
        assertThat(result.getUserId()).isEqualTo(42L);
        assertThat(result.getResourceNum()).isZero();
    }

    @Test
    void dataModelReqConvertPo_shouldHandleNullModelId() {
        DataModelAndComponentReq req = new DataModelAndComponentReq();

        DataModel result = DataModelConvert.dataModelReqConvertPo(req, 1L);

        assertThat(result.getModelId()).isNull();
    }

    @Test
    void dataModelReqConvertDataComponentPo_shouldMapFields() {
        DataComponentReq req = new DataComponentReq();
        req.setFrontComponentId("fc1");
        req.setComponentCode("code1");
        req.setComponentId("50");
        req.setComponentName("comp1");
        req.setCoordinateX(100);
        req.setCoordinateY(200);
        req.setHeight(300);
        req.setWidth(400);
        req.setShape("rect");

        DataComponent result = DataModelConvert.dataModelReqConvertDataComponentPo(req);

        assertThat(result.getFrontComponentId()).isEqualTo("fc1");
        assertThat(result.getComponentCode()).isEqualTo("code1");
        assertThat(result.getComponentId()).isEqualTo(50L);
        assertThat(result.getComponentName()).isEqualTo("comp1");
        assertThat(result.getCoordinateX()).isEqualTo(100);
        assertThat(result.getCoordinateY()).isEqualTo(200);
        assertThat(result.getHeight()).isEqualTo(300);
        assertThat(result.getWidth()).isEqualTo(400);
        assertThat(result.getShape()).isEqualTo("rect");
        assertThat(result.getIsDel()).isZero();
    }

    @Test
    void dataComponentPoConvertDataComponentVo_shouldMap() {
        DataComponent dc = new DataComponent();
        dc.setComponentId(1L);
        dc.setComponentCode("code");
        dc.setComponentName("name");
        dc.setStartTime(1000L);
        dc.setEndTime(2000L);
        dc.setComponentState(2);

        DataComponentVo result = DataModelConvert.dataComponentPoConvertDataComponentVo(dc);

        assertThat(result.getComponentId()).isEqualTo(1L);
        assertThat(result.getComponentCode()).isEqualTo("code");
        assertThat(result.getComponentName()).isEqualTo("name");
        assertThat(result.getComponentState()).isEqualTo(2);
    }

    @Test
    void componentDraftReqCovertPo_shouldMap() {
        ComponentDraftReq req = new ComponentDraftReq();
        req.setDraftId(1L);
        req.setDraftName("draft1");
        req.setUserId(42L);
        req.setComponentJson("{}");
        req.setComponentImage("img");

        DataComponentDraft result = DataModelConvert.componentDraftReqCovertPo(req);

        assertThat(result.getDraftId()).isEqualTo(1L);
        assertThat(result.getDraftName()).isEqualTo("draft1");
        assertThat(result.getUserId()).isEqualTo(42L);
        assertThat(result.getComponentJson()).isEqualTo("{}");
        assertThat(result.getComponentImage()).isEqualTo("img");
        assertThat(result.getIsDel()).isZero();
    }

    @Test
    void projectOrganPoCovertProjectOrganVo_shouldMap() {
        DataProjectOrgan organ = new DataProjectOrgan();
        organ.setParticipationIdentity(1);
        organ.setOrganId("organ1");
        organ.setAuditStatus(0);

        SysOrgan sysOrgan = new SysOrgan();
        sysOrgan.setOrganName("TestOrgan");

        ModelProjectResourceVo resourceVo = new ModelProjectResourceVo();

        ModelProjectOrganVo result = DataModelConvert.projectOrganPoCovertProjectOrganVo(
                organ, sysOrgan, resourceVo, "organ1");

        assertThat(result.getParticipationIdentity()).isEqualTo(1);
        assertThat(result.getOrganId()).isEqualTo("organ1");
        assertThat(result.getAuditStatus()).isZero();
        assertThat(result.getCreator()).isTrue();
        assertThat(result.getOrganName()).isEqualTo("TestOrgan");
        assertThat(result.getResources()).hasSize(1);
    }

    @Test
    void projectResourcePoCovertModelResourceVo_shouldMap() {
        DataProjectResource resource = new DataProjectResource();
        resource.setOrganId("organ1");
        resource.setResourceId("r1");
        resource.setAuditStatus(1);
        resource.setParticipationIdentity(0);

        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put("resourceName", "testRes");

        ModelProjectResourceVo result = DataModelConvert.projectResourcePoCovertModelResourceVo(resource, resourceMap);

        assertThat(result.getOrganId()).isEqualTo("organ1");
        assertThat(result.getResourceId()).isEqualTo("r1");
        assertThat(result.getResourceName()).isEqualTo("testRes");
        assertThat(result.getAuditStatus()).isEqualTo(1);
    }
}
