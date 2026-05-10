package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.sys.SysRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SysRoleService roleService;

    @Test
    void findRolePage_shouldReturnList() throws Exception {
        given(roleService.findRolePage(any(), anyInt(), anyInt())).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/role/findRolePage"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getRoleAuthTree_shouldReturnTree() throws Exception {
        given(roleService.getRoleAuthTree(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(get("/role/getRoleAuthTree").param("roleId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getRoleAuthTree_shouldFail_whenMissingId() throws Exception {
        mockMvc.perform(get("/role/getRoleAuthTree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void deleteSysRole_shouldSucceed() throws Exception {
        given(roleService.deleteSysRole(1L)).willReturn(BaseResultEntity.success());

        mockMvc.perform(post("/role/deleteSysRole").param("roleId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
