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
public class SysOrganControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testChangeLocalOrganInfo() throws Exception {
        this.mockMvc.perform(get("/organ/changeLocalOrganInfo")
                .param("organName","测试机构")
                .param("gatewayAddress","1"))
                .andExpect(status().isOk())
                .andDo(document("changeLocalOrganInfo",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("organName").description("机构名称"),
                                parameterWithName("gatewayAddress").description("网关地址")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysLocalOrganInfo").description("机构信息"),
                                fieldWithPath("result.sysLocalOrganInfo.organId").description("机构ID"),
                                fieldWithPath("result.sysLocalOrganInfo.organName").description("机构名称"),
                                fieldWithPath("result.sysLocalOrganInfo.pinCode").description("16位code"),
                                fieldWithPath("result.sysLocalOrganInfo.gatewayAddress").description("网关地址"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.*").optional().description("中心节点map"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.*.*").optional().description("中心节点map"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList").optional().description("中心节点list"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testGetLocalOrganInfo() throws Exception {
        this.mockMvc.perform(get("/organ/getLocalOrganInfo"))
                .andExpect(status().isOk())
                .andDo(document("getLocalOrganInfo",
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
                                fieldWithPath("result.sysLocalOrganInfo").description("机构信息"),
                                fieldWithPath("result.sysLocalOrganInfo.organId").description("机构ID"),
                                fieldWithPath("result.sysLocalOrganInfo.organName").description("机构名称"),
                                fieldWithPath("result.sysLocalOrganInfo.pinCode").description("16位code"),
                                fieldWithPath("result.sysLocalOrganInfo.gatewayAddress").description("网关地址"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionMap").optional().description("中心节点map"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.*").optional().description("中心节点地址"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.*.serverAddress").optional().description("中心节点地址"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.*.registered").optional().description("中心节点状态"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.*.show").optional().description("中心节点是否展示"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[]").optional().description("中心节点list"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[].serverAddress").optional().description("中心节点地址"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[].registered").optional().description("中心节点状态"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[].show").optional().description("中心节点是否展示"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
}
