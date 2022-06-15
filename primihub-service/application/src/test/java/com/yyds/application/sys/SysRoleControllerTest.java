package com.yyds.application.sys;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@RunWith(SpringRunner.class)
@SpringBootTest
public class SysRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSaveOrUpdateRole() throws Exception {
        this.mockMvc.perform(post("/role/saveOrUpdateRole")
                .param("roleId","")
                .param("roleName","测试角色")
                .param("grantAuthArray","")
                .param("cancelAuthArray",""))
                .andExpect(status().isOk())
                .andDo(document("saveOrUpdateRole",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("roleId").description("角色id(为空时新增，不为空修改)"),
                                parameterWithName("roleName").description("角色名称"),
                                parameterWithName("grantAuthArray").description("需要授权的权限id) 非必填"),
                                parameterWithName("cancelAuthArray").description("取消授权的id 非必填")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysRole").description("角色信息"),
                                fieldWithPath("result.sysRole.roleId").description("角色id"),
                                fieldWithPath("result.sysRole.roleName").description("角色名称"),
                                fieldWithPath("result.sysRole.isEditable").description("是否可编辑"),
                                fieldWithPath("result.sysRole.isDel").description("是否删除"),
                                fieldWithPath("result.sysRole.ctime").description("创建时间"),
                                fieldWithPath("result.sysRole.utime").description("更新时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testDeleteRole() throws Exception {
        this.mockMvc.perform(get("/role/deleteSysRole")
                .param("roleId","999"))
                .andExpect(status().isOk())
                .andDo(document("deleteSysRole",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("roleId").description("角色id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testGetRoleAuthTree() throws Exception {
        this.mockMvc.perform(get("/role/getRoleAuthTree")
                .param("roleId","1"))
                .andExpect(status().isOk())
                .andDo(document("getRoleAuthTree",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("roleId").description("角色id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysRole").description("角色信息"),
                                fieldWithPath("result.sysRole.roleId").description("角色id"),
                                fieldWithPath("result.sysRole.roleName").description("角色名称"),
                                fieldWithPath("result.sysRole.isEditable").description("是否可编辑"),
                                fieldWithPath("result.sysRole.isDel").description("是否删除"),
                                fieldWithPath("result.sysRole.ctime").description("创建时间"),
                                fieldWithPath("result.sysRole.utime").description("更新时间"),

                                fieldWithPath("result.roleAuthRootList").description("根节点集合"),
                                fieldWithPath("result.roleAuthRootList[].authId").description("权限id"),
                                fieldWithPath("result.roleAuthRootList[].authName").description("权限名称"),
                                fieldWithPath("result.roleAuthRootList[].authType").description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
                                fieldWithPath("result.roleAuthRootList[].authCode").description("权限代码"),
                                fieldWithPath("result.roleAuthRootList[].dataAuthCode").description("数据权限代码"),
                                fieldWithPath("result.roleAuthRootList[].authIndex").description("顺序"),
                                fieldWithPath("result.roleAuthRootList[].authDepth").description("深度"),
                                fieldWithPath("result.roleAuthRootList[].isShow").description("是否展示"),
                                fieldWithPath("result.roleAuthRootList[].isEditable").description("是否可编辑"),
                                fieldWithPath("result.roleAuthRootList[].isDel").description("是否删除"),
                                fieldWithPath("result.roleAuthRootList[].pauthId").description("父id"),
                                fieldWithPath("result.roleAuthRootList[].rauthId").description("根id"),
                                fieldWithPath("result.roleAuthRootList[].fullPath").description("完整路径"),
                                fieldWithPath("result.roleAuthRootList[].authUrl").description("过滤路径"),
                                fieldWithPath("result.roleAuthRootList[].ctime").description("创建时间"),
                                fieldWithPath("result.roleAuthRootList[].utime").description("更新时间"),
                                fieldWithPath("result.roleAuthRootList[].children").optional().description("子节点"),
                                fieldWithPath("result.roleAuthRootList[].isGrant").description("是否已授权"),

                                fieldWithPath("result.roleAuthRootList[].children[].authId").optional().description("权限id"),
                                fieldWithPath("result.roleAuthRootList[].children[].authName").optional().description("权限名称"),
                                fieldWithPath("result.roleAuthRootList[].children[].authType").optional().description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
                                fieldWithPath("result.roleAuthRootList[].children[].authCode").optional().description("权限代码"),
                                fieldWithPath("result.roleAuthRootList[].children[].dataAuthCode").optional().description("数据权限代码"),
                                fieldWithPath("result.roleAuthRootList[].children[].authIndex").optional().description("顺序"),
                                fieldWithPath("result.roleAuthRootList[].children[].authDepth").optional().description("深度"),
                                fieldWithPath("result.roleAuthRootList[].children[].isShow").optional().description("是否展示"),
                                fieldWithPath("result.roleAuthRootList[].children[].isEditable").optional().description("是否可编辑"),
                                fieldWithPath("result.roleAuthRootList[].children[].isDel").optional().description("是否删除"),
                                fieldWithPath("result.roleAuthRootList[].children[].pauthId").optional().description("父id"),
                                fieldWithPath("result.roleAuthRootList[].children[].rauthId").optional().description("根id"),
                                fieldWithPath("result.roleAuthRootList[].children[].fullPath").description("完整路径"),
                                fieldWithPath("result.roleAuthRootList[].children[].authUrl").description("过滤路径"),
                                fieldWithPath("result.roleAuthRootList[].children[].ctime").optional().description("创建时间"),
                                fieldWithPath("result.roleAuthRootList[].children[].utime").optional().description("更新时间"),
                                fieldWithPath("result.roleAuthRootList[].children[].children").optional().description("子节点"),
                                fieldWithPath("result.roleAuthRootList[].children[].isGrant").optional().description("是否已授权"),

                                fieldWithPath("result.roleAuthRootList[].children[].children[].authId").optional().description("权限id"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].authName").optional().description("权限名称"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].authType").optional().description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].authCode").optional().description("权限代码"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].dataAuthCode").optional().description("数据权限代码"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].authIndex").optional().description("顺序"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].authDepth").optional().description("深度"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].isShow").optional().description("是否展示"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].isEditable").optional().description("是否可编辑"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].isDel").optional().description("是否删除"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].pauthId").optional().description("父id"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].rauthId").optional().description("根id"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].fullPath").description("完整路径"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].authUrl").description("过滤路径"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].ctime").optional().description("创建时间"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].utime").optional().description("更新时间"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].children").optional().description("子节点"),
                                fieldWithPath("result.roleAuthRootList[].children[].children[].isGrant").optional().description("是否授权"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testFindRolePage() throws Exception {
        this.mockMvc.perform(get("/role/findRolePage")
                .param("roleName","")
                .param("pageNum","1")
                .param("pageSize","10"))
                .andExpect(status().isOk())
                .andDo(document("findRolePage",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("roleName").description("角色名称 非必填"),
                                parameterWithName("pageNum").description("页数 非必填 默认为1"),
                                parameterWithName("pageSize").description("每页条数 非必填 默认为10")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysRoleList").description("角色列表"),
                                fieldWithPath("result.sysRoleList[].roleId").description("角色id"),
                                fieldWithPath("result.sysRoleList[].roleName").description("角色名称"),
                                fieldWithPath("result.sysRoleList[].isEditable").description("是否可编辑"),
                                fieldWithPath("result.sysRoleList[].isDel").description("是否删除"),
                                fieldWithPath("result.sysRoleList[].ctime").description("创建时间"),
                                fieldWithPath("result.sysRoleList[].cTime").description("创建时间"),
                                fieldWithPath("result.sysRoleList[].utime").description("更新时间"),
                                fieldWithPath("result.pageParam").description("分页参数"),
                                fieldWithPath("result.pageParam.pageNum").description("页数"),
                                fieldWithPath("result.pageParam.pageSize").description("每页条数"),
                                fieldWithPath("result.pageParam.pageIndex").description("分页索引"),
                                fieldWithPath("result.pageParam.itemTotalCount").description("总数"),
                                fieldWithPath("result.pageParam.pageCount").description("页总数"),
                                fieldWithPath("result.pageParam.isLoadMore").description("是否加载下一页"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }


}
