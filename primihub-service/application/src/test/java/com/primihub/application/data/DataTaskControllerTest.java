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
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
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
public class DataTaskControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Test
    public void testGetModelTaskList() throws Exception{
        // 查询资源列表接口
        this.mockMvc.perform(get("/task/getModelTaskList")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("modelId","265"))
                .andExpect(status().isOk())
                .andDo(document("getModelTaskList",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("modelId").description("模型id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.total").description("总共的数据量"),
                                fieldWithPath("result.pageSize").description("每页显示多少条"),
                                fieldWithPath("result.totalPage").description("共有多少页"),
                                fieldWithPath("result.index").description("当前是第几页"),
                                fieldWithPath("result.data[]").optional().description("数据"),
                                fieldWithPath("result.data[].taskId").optional().description("任务ID"),
                                fieldWithPath("result.data[].taskIdName").optional().description("任务名称"),
                                fieldWithPath("result.data[].taskState").optional().description("任务状态(0未开始 1成功 2运行中 3失败 4取消)"),
                                fieldWithPath("result.data[].taskType").optional().description("任务类型 1、模型 2、PSI 3、PIR"),
                                fieldWithPath("result.data[].taskStartDate").optional().description("开始运行时间"),
                                fieldWithPath("result.data[].taskEndDate").optional().description("结束运行时间"),
                                fieldWithPath("result.data[].taskErrorMsg").optional().description("状态失败 错误信息"),
                                fieldWithPath("result.data[].timeConsuming").optional().description("耗时 单位秒"),
                                fieldWithPath("result.data[].taskName").optional().description("任务名称 模型目前无"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
}
