package com.primihub.application.sys;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@RunWith(SpringRunner.class)
@SpringBootTest
public class SysFusionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHealthConnection() throws Exception {
        this.mockMvc.perform(get("/fusion/healthConnection")
                .param("serverAddress","http://localhost:8099"))
                .andExpect(status().isOk())
                .andDo(document("healthConnection",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址")
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
    public void testRegisterConnection() throws Exception {
        this.mockMvc.perform(get("/fusion/registerConnection")
                .param("serverAddress","http://localhost:8099"))
                .andExpect(status().isOk())
                .andDo(document("registerConnection",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.isRegistered").description("是否注册"),
                                fieldWithPath("result.fusionMsg").description("返回消息"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testDeleteConnection() throws Exception {
        this.mockMvc.perform(get("/fusion/deleteConnection")
                .param("serverAddress","http://localhost:8096"))
                .andExpect(status().isOk())
                .andDo(document("deleteConnection",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址")
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
    public void testCreateGroup() throws Exception {
        this.mockMvc.perform(get("/fusion/createGroup")
                .param("serverAddress","http://localhost:8099")
                .param("groupName","测试的群组"))
                .andExpect(status().isOk())
                .andDo(document("createGroup",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址"),
                                parameterWithName("groupName").description("群组名称")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.fusionMsg").description("返回消息"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
    @Test
    public void testFindAllGroup() throws Exception {
        this.mockMvc.perform(get("/fusion/findAllGroup")
                .param("serverAddress","http://localhost:8099"))
                .andExpect(status().isOk())
                .andDo(document("findAllGroup",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.fusionMsg").description("返回信息"),
                                fieldWithPath("result.organList.groupList[]").description("群组列表"),
                                fieldWithPath("result.organList.groupList[].id").description("群组ID"),
                                fieldWithPath("result.organList.groupList[].groupName").description("群组名称"),
                                fieldWithPath("result.organList.groupList[].groupOrganId").description("机构id"),
                                fieldWithPath("result.organList.groupList[].isDel").description("是否删除"),
                                fieldWithPath("result.organList.groupList[].in").description("是否在机构里"),
                                fieldWithPath("result.organList.groupList[].utime").description("修改时间"),
                                fieldWithPath("result.organList.groupList[].ctime").description("创建时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testJoinGroup() throws Exception {
        this.mockMvc.perform(get("/fusion/joinGroup")
                .param("serverAddress","http%3A%2F%2Flocalhost%3A8099")
                .param("groupId","1"))
                .andExpect(status().isOk())
                .andDo(document("joinGroup",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址"),
                                parameterWithName("groupId").description("群组ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.fusionMsg").description("群组加入消息"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testExitGroup() throws Exception {
        this.mockMvc.perform(get("/fusion/exitGroup")
                .param("serverAddress","http://localhost:8099")
                .param("groupId","1"))
                .andExpect(status().isOk())
                .andDo(document("exitGroup",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址"),
                                parameterWithName("groupId").description("群组ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.fusionMsg").description("群组退出消息"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
    @Test
    public void testFindOrganInGroup() throws Exception {
        this.mockMvc.perform(get("/fusion/findOrganInGroup")
                .param("serverAddress","http://localhost:8099")
                .param("groupId","1"))
                .andExpect(status().isOk())
                .andDo(document("findOrganInGroup",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址"),
                                parameterWithName("groupId").description("群组ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.dataList.organList[]").description("群组下的机构列表"),
                                fieldWithPath("result.dataList.organList[].id").optional().description("id"),
                                fieldWithPath("result.dataList.organList[].globalId").optional().description("机构id"),
                                fieldWithPath("result.dataList.organList[].globalName").optional().description("机构名称"),
                                fieldWithPath("result.dataList.organList[].pinCodeMd").optional().description("code"),
                                fieldWithPath("result.dataList.organList[].registerTime").optional().description("注册时间"),
                                fieldWithPath("result.dataList.organList[].isDel").optional().description("是否删除"),
                                fieldWithPath("result.dataList.organList[].ctime").optional().description("创建时间"),
                                fieldWithPath("result.dataList.organList[].utime").optional().description("修改时间"),
                                fieldWithPath("result.fusionMsg").description("群组退出消息"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }


}
