package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.service.data.FederatedLearningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FederatedLearningController.class)
class FederatedLearningControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private FederatedLearningService flService;

    @Test
    void getTaskList_shouldReturnList() throws Exception {
        given(flService.getTaskList(any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/federatedLearning/getTaskList"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getTaskDetails_shouldReturnDetails() throws Exception {
        given(flService.getTaskDetails("1")).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/federatedLearning/getTaskDetails").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getTaskDetails_shouldFail_whenMissingId() throws Exception {
        mockMvc.perform(get("/federatedLearning/getTaskDetails"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteTask_shouldSucceed() throws Exception {
        given(flService.deleteTask("1")).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/federatedLearning/deleteTask").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void cancelTask_shouldSucceed() throws Exception {
        given(flService.cancelTask("1")).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/federatedLearning/cancelTask").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getTrainingProgress_shouldReturnProgress() throws Exception {
        given(flService.getTrainingProgress("1")).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/federatedLearning/getTrainingProgress").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void createTask_shouldFail_whenMissingUserId() throws Exception {
        String reqJson = "{}";
        mockMvc.perform(post("/federatedLearning/createTask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void createTask_shouldFail_whenMissingTaskType() throws Exception {
        String reqJson = "{}";
        mockMvc.perform(post("/federatedLearning/createTask")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }
}
