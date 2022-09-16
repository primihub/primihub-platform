package com.primihub.application.data;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
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
public class DataReasoningControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public  void testSaveReasoning() throws Exception {
        // 项目添加
        this.mockMvc.perform(post("/reasoning/saveReasoning")
                .param("reasoningName","推理")
                .param("reasoningDesc","推理描述")
                .param("taskId","1")
                .param("resourceList[0].resourceId","2b598a7e3298-98af0345-1d93-4472-88ab-f72c79dbc4c8")
                .param("resourceList[0].participationIdentity","1")
                .param("resourceList[1].resourceId","2b598a7e3298-f9fd4938-5fe3-4b67-a551-3aab52215153")
                .param("resourceList[1].participationIdentity","2")
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("saveReasoning",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id 前端不用传")
                        ),
                        requestParameters(
                                parameterWithName("reasoningName").description("推理名称"),
                                parameterWithName("reasoningDesc").description("推理描述"),
                                parameterWithName("taskId").description("任务ID"),
                                parameterWithName("resourceList[0].resourceId").optional().description("资源ID"),
                                parameterWithName("resourceList[0].participationIdentity").optional().description("参与身份 1发起者 2协作者"),
                                parameterWithName("resourceList[1].resourceId").optional().description("资源ID"),
                                parameterWithName("resourceList[1].participationIdentity").optional().description("参与身份 1发起者 2协作者")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.id").description("id"),
                                fieldWithPath("result.reasoningId").description("推理uuid"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public  void testGetReasoningList() throws Exception {
        // 项目添加
        this.mockMvc.perform(get("/reasoning/getReasoningList")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("id","")
                .param("reasoningName","")
                .param("reasoningState","")
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("getReasoningList",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("id").description("推理id"),
                                parameterWithName("reasoningName").description("推理名称"),
                                parameterWithName("reasoningState").description("推理状态")
                        ),
                        requestHeaders(
                                headerWithName("userId").description("用户id 前端不用传")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.total").description("总共的数据量"),
                                fieldWithPath("result.pageSize").description("每页显示多少条"),
                                fieldWithPath("result.totalPage").description("共有多少页"),
                                fieldWithPath("result.index").description("当前是第几页"),
                                fieldWithPath("result.data").description("数据"),
                                fieldWithPath("result.data[].id").description("推理id"),
                                fieldWithPath("result.data[].reasoningId").description("推理uuid"),
                                fieldWithPath("result.data[].reasoningName").description("推理名称"),
                                fieldWithPath("result.data[].reasoningDesc").description("推理描述"),
                                fieldWithPath("result.data[].reasoningType").description("返回几 就是几方"),
                                fieldWithPath("result.data[].reasoningState").description("状态"),
                                fieldWithPath("result.data[].taskId").description("任务id"),
                                fieldWithPath("result.data[].releaseDate").optional().description("上线时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public  void testGetReasoning() throws Exception {
        // 项目添加
        this.mockMvc.perform(get("/reasoning/getReasoning")
                .param("id","1"))
                .andExpect(status().isOk())
                .andDo(document("getReasoning",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("id").description("推理id")
                        ),
                        requestHeaders(
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.id").description("推理id"),
                                fieldWithPath("result.reasoningId").description("推理uuid"),
                                fieldWithPath("result.reasoningName").description("推理名称"),
                                fieldWithPath("result.reasoningDesc").description("推理描述"),
                                fieldWithPath("result.reasoningType").description("返回几 就是几方"),
                                fieldWithPath("result.reasoningState").description("状态"),
                                fieldWithPath("result.taskId").description("任务id"),
                                fieldWithPath("result.releaseDate").optional().description("上线时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

}
