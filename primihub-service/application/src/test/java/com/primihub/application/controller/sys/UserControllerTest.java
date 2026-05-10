package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.sys.SysUserService;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SysUserService userService;

    @Test
    void login_shouldSucceed() throws Exception {
        given(userService.login(any(), anyString())).willReturn(BaseResultEntity.success());

        mockMvc.perform(post("/user/login")
                .param("userAccount", "admin")
                .param("userPassword", "123456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void login_shouldFail_whenMissingAccount() throws Exception {
        mockMvc.perform(post("/user/login").param("userPassword", "123456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void login_shouldFail_whenMissingPassword() throws Exception {
        mockMvc.perform(post("/user/login").param("userAccount", "admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void logout_shouldSucceed() throws Exception {
        given(userService.logout(anyString(), anyLong())).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/user/logout")
                .header("token", "testToken")
                .header("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void findUserPage_shouldReturnList() throws Exception {
        given(userService.findUserPage(any(), anyInt(), anyInt())).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/user/findUserPage")
                .param("pageNum", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteSysUser_shouldSucceed() throws Exception {
        given(userService.deleteSysUser(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(post("/user/deleteSysUser").param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void initPassword_shouldSucceed() throws Exception {
        given(userService.initPassword(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(post("/user/initPassword").param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void freezeUser_shouldSucceed() throws Exception {
        given(userService.freezeUser(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(post("/user/freezeUser").param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void unfreezeUser_shouldSucceed() throws Exception {
        given(userService.unfreezeUser(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(post("/user/unfreezeUser").param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
