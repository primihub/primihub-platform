package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataReasoning;
import com.primihub.biz.entity.data.po.DataReasoningResource;
import com.primihub.biz.entity.data.req.DataReasoningReq;
import com.primihub.biz.entity.data.req.DataReasoningResourceReq;
import com.primihub.biz.entity.data.vo.DataReasoningVo;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DataReasoningConvertTest {

    @Test
    void dataReasoningReqConvertPo_shouldMapFields() {
        DataReasoningResourceReq resReq = new DataReasoningResourceReq();
        DataReasoningReq req = new DataReasoningReq();
        req.setReasoningName("test reasoning");
        req.setReasoningDesc("desc");
        req.setResourceList(Collections.singletonList(resReq));
        req.setTaskId(1L);
        req.setUserId(42L);

        DataReasoning result = DataReasoningConvert.dataReasoningReqConvertPo(req);

        assertThat(result.getReasoningId()).isNotNull();
        assertThat(result.getReasoningName()).isEqualTo("test reasoning");
        assertThat(result.getReasoningDesc()).isEqualTo("desc");
        assertThat(result.getReasoningType()).isEqualTo(1);
        assertThat(result.getReasoningState()).isZero();
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(42L);
    }

    @Test
    void dataReasoningResourceReqConvertPo_shouldMap() {
        DataReasoningResourceReq req = new DataReasoningResourceReq();
        req.setResourceId("r1");
        req.setParticipationIdentity(0);

        DataReasoningResource result = DataReasoningConvert.dataReasoningResourceReqConvertPo(req, 100L);

        assertThat(result.getReasoningId()).isEqualTo(100L);
        assertThat(result.getResourceId()).isEqualTo("r1");
        assertThat(result.getParticipationIdentity()).isZero();
    }

    @Test
    void dataReasoningConvertVo_shouldMap() {
        DataReasoning dr = new DataReasoning();
        dr.setId(1L);
        dr.setReasoningId("rid1");
        dr.setReasoningName("name");
        dr.setReasoningDesc("desc");
        dr.setReasoningType(2);
        dr.setReasoningState(1);
        dr.setTaskId(1L);
        dr.setRunTaskId(2L);
        dr.setReleaseDate(new Date());

        DataReasoningVo result = DataReasoningConvert.dataReasoningConvertVo(dr);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReasoningId()).isEqualTo("rid1");
        assertThat(result.getReasoningName()).isEqualTo("name");
        assertThat(result.getReasoningState()).isEqualTo(1);
        assertThat(result.getRunTaskId()).isEqualTo(2L);
    }
}
