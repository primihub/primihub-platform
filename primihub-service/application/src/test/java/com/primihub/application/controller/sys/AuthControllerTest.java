package com.primihub.application.controller.sys;

import com.primihub.biz.service.sys.SysAuthService;
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

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SysAuthService authService;

    @Test
    void getAuthTree_shouldReturnTree() throws Exception {
        given(authService.getAuthTree()).willReturn(
            com.primihub.biz.entity.base.BaseResultEntity.success()
        );

        mockMvc.perform(get("/auth/getAuthTree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void createAuthNode_shouldFail_whenMissingName() throws Exception {
        mockMvc.perform(get("/auth/createAuthNode")
                .param("authCode", "TEST")
                .param("pAuthId", "0")
                .param("authIndex", "1")
                .param("authType", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void createAuthNode_shouldFail_whenMissingCode() throws Exception {
        mockMvc.perform(get("/auth/createAuthNode")
                .param("authName", "Test")
                .param("pAuthId", "0")
                .param("authIndex", "1")
                .param("authType", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void createAuthNode_shouldFail_whenMissingPAuthId() throws Exception {
        mockMvc.perform(get("/auth/createAuthNode")
                .param("authName", "Test")
                .param("authCode", "TEST")
                .param("authIndex", "1")
                .param("authType", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void deleteAuthNode_shouldCallService() throws Exception {
        given(authService.deleteAuthNode(1L)).willReturn(
            com.primihub.biz.entity.base.BaseResultEntity.success()
        );

        mockMvc.perform(get("/auth/deleteAuthNode").param("authId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void generateAllAuth_shouldSucceed() throws Exception {
        given(authService.generateAllAuth()).willReturn(
            com.primihub.biz.entity.base.BaseResultEntity.success()
        );

        mockMvc.perform(get("/auth/generateAllAuth"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
