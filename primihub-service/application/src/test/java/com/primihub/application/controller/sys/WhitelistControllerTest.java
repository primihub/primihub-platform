package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.sys.WhitelistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WhitelistController.class)
class WhitelistControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private WhitelistService whitelistService;

    @Test
    void findWhitelistPage_shouldReturnList() throws Exception {
        given(whitelistService.findWhitelistPage(any(), any(), any(), anyInt(), anyInt())).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/whitelist/findWhitelistPage"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getWhitelistConfigList_shouldReturnConfigs() throws Exception {
        given(whitelistService.findWhitelistConfigList()).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/whitelist/findWhitelistConfigList"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
