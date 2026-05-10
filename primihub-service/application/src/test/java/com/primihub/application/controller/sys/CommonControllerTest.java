package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.sys.SysCommonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommonController.class)
class CommonControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SysCommonService commonService;

    @Test
    void getValidatePublicKey_shouldReturnKey() throws Exception {
        given(commonService.getValidatePublicKey()).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/common/getValidatePublicKey"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getTrackingID_shouldReturnId() throws Exception {
        mockMvc.perform(get("/common/getTrackingID"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
