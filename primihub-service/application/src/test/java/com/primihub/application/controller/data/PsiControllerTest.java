package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.req.DataPsiReq;
import com.primihub.biz.service.data.DataPsiService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PsiController.class)
class PsiControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private DataPsiService psiService;

    @Test
    void saveDataPsi_shouldFail_whenMissingOwnOrganId() throws Exception {
        mockMvc.perform(post("/psi/saveDataPsi")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("ownResourceId", "r1")
                .param("ownKeyword", "k1")
                .param("otherOrganId", "o2")
                .param("otherResourceId", "r2")
                .param("otherKeyword", "k2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void saveDataPsi_shouldFail_whenMissingUserId() throws Exception {
        mockMvc.perform(post("/psi/saveDataPsi"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void saveDataPsi_shouldSucceed() throws Exception {
        given(psiService.saveDataPsi(any(DataPsiReq.class), anyLong()))
            .willReturn(BaseResultEntity.success());

        mockMvc.perform(post("/psi/saveDataPsi")
                .header("userId", "1")
                .queryParam("ownOrganId", "o1")
                .queryParam("ownResourceId", "r1")
                .queryParam("ownKeyword", "k1")
                .queryParam("otherOrganId", "o2")
                .queryParam("otherResourceId", "r2")
                .queryParam("otherKeyword", "k2")
                .queryParam("resultName", "test result")
                .queryParam("resultOrganIds", "o1,o2")
                .queryParam("psiTag", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getPsiTaskList_shouldReturnList() throws Exception {
        given(psiService.getPsiTaskList(any()))
            .willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/psi/getPsiTaskList")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk());
    }

    @Test
    void getPsiTaskDetails_shouldReturnDetails() throws Exception {
        given(psiService.getPsiTaskDetails(1L))
            .willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/psi/getPsiTaskDetails").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void delPsiTask_shouldSucceed() throws Exception {
        given(psiService.delPsiTask(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/psi/delPsiTask").param("taskId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
