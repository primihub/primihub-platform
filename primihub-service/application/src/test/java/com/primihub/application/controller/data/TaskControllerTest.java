package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.data.DataTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private DataTaskService taskService;
    @MockBean private com.primihub.biz.repository.secondarydb.data.DataPsiRepository dataPsiRepository;

    @Test
    void updateTaskDesc_shouldSucceed() throws Exception {
        given(taskService.updateTaskDesc(1L, "new desc")).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/task/updateTaskDesc")
                .param("taskId", "1")
                .param("taskDesc", "new desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateTaskDesc_shouldFail_whenMissingTaskId() throws Exception {
        mockMvc.perform(get("/task/updateTaskDesc").param("taskDesc", "desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void updateTaskDesc_shouldFail_whenMissingDesc() throws Exception {
        mockMvc.perform(get("/task/updateTaskDesc").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void getTaskData_shouldReturnTask() throws Exception {
        given(taskService.getTaskData(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/task/getTaskData").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getTaskList_shouldReturnList() throws Exception {
        given(taskService.getTaskList(any())).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/task/getTaskList")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteTask_shouldSucceed() throws Exception {
        given(taskService.deleteTaskData(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(post("/task/deleteTask").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void cancelTask_shouldSucceed() throws Exception {
        given(taskService.cancelTask("task1")).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/task/cancelTask").param("taskId", "task1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getTaskLogInfo_shouldSucceed() throws Exception {
        given(taskService.getTaskLogInfo(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/task/getTaskLogInfo").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
