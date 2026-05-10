package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.data.DataResourceService;
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

@WebMvcTest(ResourceController.class)
class ResourceControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private DataResourceService resourceService;

    @Test
    void getResourceTags_shouldReturnTags() throws Exception {
        given(resourceService.getResourceTags()).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/resource/getResourceTags"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getDataResourceList_shouldReturnList() throws Exception {
        given(resourceService.getDataResourceList(any(), anyLong())).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/resource/getdataresourcelist").header("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
