package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.data.MarketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketController.class)
class MarketControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private MarketService marketService;

    @Test
    void getMarketInfo_shouldReturnInfo() throws Exception {
        given(marketService.getMarketInfo()).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/market/marketInfo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getVisitingUsers_shouldReturnList() throws Exception {
        given(marketService.getVisitingUsers()).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/market/getvisitingusers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
