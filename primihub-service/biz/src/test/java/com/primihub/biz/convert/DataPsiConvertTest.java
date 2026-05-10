package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataPsi;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.DataPsiReq;
import com.primihub.biz.entity.data.vo.DataPsiVo;
import com.primihub.biz.entity.data.vo.PsiTaskVo;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DataPsiConvertTest {

    @Test
    void dataPsiReqConvertPo_shouldMapAllFields() {
        DataPsiReq req = new DataPsiReq();
        req.setOwnOrganId("organ1");
        req.setOwnResourceId("res1");
        req.setOwnKeyword("keyword1");
        req.setOtherOrganId("organ2");
        req.setOtherResourceId("res2");
        req.setOtherKeyword("keyword2");
        req.setOutputFilePathType(1);
        req.setOutputNoRepeat(1);
        req.setPsiTag(1);
        req.setResultName("result1");
        req.setOutputContent(1);
        req.setOutputFormat("csv");
        req.setResultOrganIds("organ1,organ2");
        req.setRemarks("remark1");
        req.setTeeOrganId("tee1");

        DataPsi result = DataPsiConvert.DataPsiReqConvertPo(req);

        assertThat(result.getOwnOrganId()).isEqualTo("organ1");
        assertThat(result.getOwnResourceId()).isEqualTo("res1");
        assertThat(result.getOwnKeyword()).isEqualTo("keyword1");
        assertThat(result.getOtherOrganId()).isEqualTo("organ2");
        assertThat(result.getOtherResourceId()).isEqualTo("res2");
        assertThat(result.getOtherKeyword()).isEqualTo("keyword2");
        assertThat(result.getOutputFilePathType()).isEqualTo(1);
        assertThat(result.getOutputNoRepeat()).isEqualTo(1);
        assertThat(result.getTag()).isEqualTo(1);
        assertThat(result.getResultName()).isEqualTo("result1");
        assertThat(result.getOutputContent()).isEqualTo(1);
        assertThat(result.getOutputFormat()).isEqualTo("csv");
        assertThat(result.getResultOrganIds()).isEqualTo("organ1,organ2");
        assertThat(result.getRemarks()).isEqualTo("remark1");
        assertThat(result.getTeeOrganId()).isEqualTo("tee1");
    }

    @Test
    void dataPsiReqConvertPo_shouldApplyDefaults_whenNull() {
        DataPsiReq req = new DataPsiReq();

        DataPsi result = DataPsiConvert.DataPsiReqConvertPo(req);

        assertThat(result.getOutputFilePathType()).isZero();
        assertThat(result.getOutputNoRepeat()).isZero();
        assertThat(result.getOutputContent()).isZero();
        assertThat(result.getOutputFormat()).isEqualTo("csv");
    }

    @Test
    void dataPsiTaskConvertVo_shouldMapFields() {
        DataPsiTask task = new DataPsiTask();
        task.setId(100L);
        task.setPsiId(200L);
        task.setTaskId("t1");
        task.setTaskState(1);
        task.setAscription("organ1");
        task.setAscriptionType(0);
        task.setCreateDate(new Date());

        PsiTaskVo result = DataPsiConvert.DataPsiTaskConvertVo(task);

        assertThat(result.getTaskId()).isEqualTo(100L);
        assertThat(result.getPsiId()).isEqualTo(200L);
        assertThat(result.getTaskIdName()).isEqualTo("t1");
        assertThat(result.getTaskState()).isEqualTo(1);
        assertThat(result.getAscription()).isEqualTo("organ1");
        assertThat(result.getAscriptionType()).isEqualTo(0);
        assertThat(result.getCreateDate()).isNotNull();
    }

    @Test
    void dataPsiConvertVo_shouldBuildComplexVo() {
        DataPsiTask task = new DataPsiTask();
        task.setId(1L);
        task.setTaskId("t1");
        task.setTaskState(2);

        DataPsi dataPsi = new DataPsi();
        dataPsi.setOwnOrganId("o1");
        dataPsi.setOwnResourceId("r1");
        dataPsi.setOwnKeyword("k1");
        dataPsi.setOtherOrganId("o2");
        dataPsi.setOtherResourceId("r2");
        dataPsi.setOtherKeyword("k2");
        dataPsi.setOutputFilePathType(1);
        dataPsi.setOutputNoRepeat(0);
        dataPsi.setTag(0);
        dataPsi.setResultName("result");
        dataPsi.setOutputFormat("csv");
        dataPsi.setOutputContent(1);
        dataPsi.setResultOrganIds("o1,o2");
        dataPsi.setRemarks("remark");
        dataPsi.setTeeOrganId("tee");

        DataResource dataResource = new DataResource();
        dataResource.setResourceName("ownResource");

        Map<String, Object> otherDataResource = new HashMap<>();
        otherDataResource.put("organName", "OtherOrgan");
        otherDataResource.put("resourceName", "OtherResource");

        SysLocalOrganInfo organInfo = new SysLocalOrganInfo();
        organInfo.setOrganName("MyOrgan");

        DataTask dataTask = new DataTask();
        dataTask.setTaskStartTime(1000L);
        dataTask.setTaskEndTime(2000L);
        dataTask.setTaskErrorMsg("error msg");
        dataTask.setTaskName("taskName");

        DataPsiVo result = DataPsiConvert.DataPsiConvertVo(
                task, dataPsi, dataResource, otherDataResource,
                organInfo, dataTask, null, "TeeOrgan"
        );

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOwnOrganName()).isEqualTo("MyOrgan");
        assertThat(result.getOwnResourceName()).isEqualTo("ownResource");
        assertThat(result.getOtherOrganName()).isEqualTo("OtherOrgan");
        assertThat(result.getOtherResourceName()).isEqualTo("OtherResource");
        assertThat(result.getResultOrganName()).isEqualTo("MyOrgan,OtherOrgan");
        assertThat(result.getTaskName()).isEqualTo("taskName");
        assertThat(result.getTaskError()).isEqualTo("error msg");
        assertThat(result.getTeeOrganName()).isEqualTo("TeeOrgan");
    }

    @Test
    void dataPsiConvertVo_shouldHandleNullOtherResource() {
        DataPsiTask task = new DataPsiTask();
        task.setId(1L);
        DataPsi dataPsi = new DataPsi();
        dataPsi.setOwnOrganId("o1");
        dataPsi.setOwnResourceId("r1");
        dataPsi.setOtherOrganId("o2");
        dataPsi.setOtherResourceId("r2");
        dataPsi.setResultOrganIds("o1");
        DataResource dataResource = new DataResource();
        dataResource.setResourceName("r");
        SysLocalOrganInfo organInfo = new SysLocalOrganInfo();
        organInfo.setOrganName("MyOrgan");
        DataTask dataTask = new DataTask();

        DataPsiVo result = DataPsiConvert.DataPsiConvertVo(
                task, dataPsi, dataResource, null,
                organInfo, dataTask, null, null
        );

        assertThat(result.getOtherOrganName()).isEmpty();
        assertThat(result.getOtherResourceName()).isEmpty();
        assertThat(result.getResultOrganName()).isEqualTo("MyOrgan");
    }
}
