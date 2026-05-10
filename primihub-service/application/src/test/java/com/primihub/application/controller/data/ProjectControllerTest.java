package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.data.DataProjectService;
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

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private DataProjectService projectService;

    @Test
    void getProjectList_shouldReturnList() throws Exception {
        given(projectService.getProjectList(any())).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/project/getProjectList"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getProjectDetails_shouldReturnDetails() throws Exception {
        given(projectService.getProjectDetails(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/project/getProjectDetails").param("id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void closeProject_shouldSucceed() throws Exception {
        given(projectService.closeProject(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(post("/project/closeProject").param("id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getListStatistics_shouldReturnStats() throws Exception {
        given(projectService.getListStatistics()).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/project/getListStatistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void saveOrUpdateProject_shouldFail_whenMissingName() throws Exception {
        mockMvc.perform(post("/project/saveOrUpdateProject")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }
}
