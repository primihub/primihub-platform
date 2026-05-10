package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.sys.SysOrganService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganController.class)
class OrganControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SysOrganService organService;

    @Test
    void getLocalOrganInfo_shouldReturnInfo() throws Exception {
        given(organService.getLocalOrganInfo()).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/organ/getLocalOrganInfo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getOrganList_shouldReturnList() throws Exception {
        given(organService.getOrganList(any())).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/organ/getOrganList"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getHomepage_shouldReturnHome() throws Exception {
        given(organService.getHomepage()).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/organ/getHomepage"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getAvailableOrganList_shouldReturnList() throws Exception {
        given(organService.getAvailableOrganList()).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/organ/getAvailableOrganList"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
