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
public class SysAuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAuthTree() throws Exception{
        this.mockMvc.perform(get("/auth/getAuthTree"))
                .andExpect(status().isOk())
                .andDo(document("getAuthTree",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysAuthRootList").description("根节点集合"),
                                fieldWithPath("result.sysAuthRootList[].authId").description("权限id"),
                                fieldWithPath("result.sysAuthRootList[].authName").description("权限名称"),
                                fieldWithPath("result.sysAuthRootList[].authType").description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
                                fieldWithPath("result.sysAuthRootList[].authCode").description("权限代码"),
                                fieldWithPath("result.sysAuthRootList[].dataAuthCode").description("数据权限代码"),
                                fieldWithPath("result.sysAuthRootList[].authIndex").description("顺序"),
                                fieldWithPath("result.sysAuthRootList[].authDepth").description("深度"),
                                fieldWithPath("result.sysAuthRootList[].isShow").description("是否展示"),
                                fieldWithPath("result.sysAuthRootList[].isEditable").description("是否可编辑"),
                                fieldWithPath("result.sysAuthRootList[].isDel").description("是否删除"),
                                fieldWithPath("result.sysAuthRootList[].pauthId").description("父id"),
                                fieldWithPath("result.sysAuthRootList[].rauthId").description("根id"),
                                fieldWithPath("result.sysAuthRootList[].fullPath").description("全路径"),
                                fieldWithPath("result.sysAuthRootList[].authUrl").description("过滤url"),
                                fieldWithPath("result.sysAuthRootList[].ctime").description("创建时间"),
                                fieldWithPath("result.sysAuthRootList[].utime").description("更新时间"),
                                fieldWithPath("result.sysAuthRootList[].children").optional().description("子节点"),
                                fieldWithPath("result.sysAuthRootList[].isGrant").description("是否授权"),

                                fieldWithPath("result.sysAuthRootList[].children[].authId").optional().description("权限id"),
                                fieldWithPath("result.sysAuthRootList[].children[].authName").optional().description("权限名称"),
                                fieldWithPath("result.sysAuthRootList[].children[].authType").optional().description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
                                fieldWithPath("result.sysAuthRootList[].children[].authCode").optional().description("权限代码"),
                                fieldWithPath("result.sysAuthRootList[].children[].dataAuthCode").optional().description("数据权限代码"),
                                fieldWithPath("result.sysAuthRootList[].children[].authIndex").optional().description("顺序"),
                                fieldWithPath("result.sysAuthRootList[].children[].authDepth").optional().description("深度"),
                                fieldWithPath("result.sysAuthRootList[].children[].isShow").optional().description("是否展示"),
                                fieldWithPath("result.sysAuthRootList[].children[].isEditable").optional().description("是否可编辑"),
                                fieldWithPath("result.sysAuthRootList[].children[].isDel").optional().description("是否删除"),
                                fieldWithPath("result.sysAuthRootList[].children[].pauthId").optional().description("父id"),
                                fieldWithPath("result.sysAuthRootList[].children[].rauthId").optional().description("根id"),
                                fieldWithPath("result.sysAuthRootList[].children[].fullPath").description("全路径"),
                                fieldWithPath("result.sysAuthRootList[].children[].authUrl").description("过滤url"),
                                fieldWithPath("result.sysAuthRootList[].children[].ctime").optional().description("创建时间"),
                                fieldWithPath("result.sysAuthRootList[].children[].utime").optional().description("更新时间"),
                                fieldWithPath("result.sysAuthRootList[].children[].children").optional().description("子节点"),
                                fieldWithPath("result.sysAuthRootList[].children[].isGrant").optional().description("是否授权"),

                                fieldWithPath("result.sysAuthRootList[].children[].children[].authId").optional().description("权限id"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].authName").optional().description("权限名称"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].authType").optional().description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].authCode").optional().description("权限代码"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].dataAuthCode").optional().description("数据权限代码"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].authIndex").optional().description("顺序"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].authDepth").optional().description("深度"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].isShow").optional().description("是否展示"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].isEditable").optional().description("是否可编辑"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].isDel").optional().description("是否删除"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].pauthId").optional().description("父id"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].rauthId").optional().description("根id"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].fullPath").description("全路径"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].authUrl").description("过滤url"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].ctime").optional().description("创建时间"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].utime").optional().description("更新时间"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].children").optional().description("子节点"),
                                fieldWithPath("result.sysAuthRootList[].children[].children[].isGrant").optional().description("是否授权"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testAuthCreateAuthNode() throws Exception{
        this.mockMvc.perform(post("/auth/createAuthNode")
                .param("authName","项目管理1")
                .param("authType","1")
                .param("authCode","project_code")
                .param("pAuthId","0")
                .param("dataAuthCode","own")
                .param("authIndex","1")
                .param("isShow","1")
                .param("authUrl",""))
                .andExpect(status().isOk())
                .andDo(document("createAuthNode",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(

                        ),
                        requestParameters(
                                parameterWithName("authName").description("资源名称"),
                                parameterWithName("authType").description("权限类型（1.菜单 2.列表 3.按钮）"),
                                parameterWithName("authCode").description("权限代码(前端识别具体权限)"),
                                parameterWithName("pAuthId").description("上级父节点id 如果为根则传0"),
                                parameterWithName("authIndex").description("顺序"),
                                parameterWithName("dataAuthCode").description("数据权限代码 非必填 默认为个人权限"),
                                parameterWithName("isShow").description("是否展示 非必填 默认为1 展示"),
                                parameterWithName("authUrl").description("过滤url 非必填 默认空字符串")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysAuth.authId").description("权限id"),
                                fieldWithPath("result.sysAuth.authName").description("权限名称"),
                                fieldWithPath("result.sysAuth.authType").description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
                                fieldWithPath("result.sysAuth.authCode").description("权限代码"),
                                fieldWithPath("result.sysAuth.dataAuthCode").description("数据权限代码"),
                                fieldWithPath("result.sysAuth.authIndex").description("顺序"),
                                fieldWithPath("result.sysAuth.authDepth").description("深度"),
                                fieldWithPath("result.sysAuth.isShow").description("是否展示"),
                                fieldWithPath("result.sysAuth.isEditable").description("是否可编辑"),
                                fieldWithPath("result.sysAuth.isDel").description("是否删除"),
                                fieldWithPath("result.sysAuth.pauthId").description("父id"),
                                fieldWithPath("result.sysAuth.rauthId").description("根id"),
                                fieldWithPath("result.sysAuth.fullPath").description("全路径"),
                                fieldWithPath("result.sysAuth.authUrl").description("过滤url"),
                                fieldWithPath("result.sysAuth.ctime").description("创建时间"),
                                fieldWithPath("result.sysAuth.utime").description("更新时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }


    @Test
    public void testAlterAuthNodeStatus() throws Exception{
        this.mockMvc.perform(get("/auth/alterAuthNodeStatus")
                .param("authId","1")
                .param("authName","项目管理")
                .param("authType","1")
                .param("authCode","Project")
                .param("dataAuthCode","own")
                .param("isShow","1"))
                .andExpect(status().isOk())
                .andDo(document("alterAuthNodeStatus",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(

                        ),
                        requestParameters(
                                parameterWithName("authId").description("权限id"),
                                parameterWithName("authName").description("资源名称"),
                                parameterWithName("authType").description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
                                parameterWithName("authCode").description("权限代码(前端识别具体权限)"),
                                parameterWithName("dataAuthCode").description("数据权限代码 非必填 默认为个人权限"),
                                parameterWithName("isShow").description("是否展示 非必填 默认为1 展示")
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
    public void testDeleteAuthNode() throws Exception {
        this.mockMvc.perform(get("/auth/deleteAuthNode")
                .param("authId","999"))
                .andExpect(status().isOk())
                .andDo(document("deleteAuthNode",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(

                        ),
                        requestParameters(
                                parameterWithName("authId").description("权限id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
}
