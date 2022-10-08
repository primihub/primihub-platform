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
        this.mockMvc.perform(get("/task/getModelTaskList")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("modelId","3"))
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


    @Test
    public void testGetTaskData() throws Exception{
        this.mockMvc.perform(get("/task/getTaskData")
                .param("taskId","1"))
                .andExpect(status().isOk())
                .andDo(document("getTaskData",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("taskId").description("任务ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.taskId").optional().description("任务ID"),
                                fieldWithPath("result.taskIdName").optional().description("任务名称"),
                                fieldWithPath("result.taskState").optional().description("任务状态(0未开始 1成功 2运行中 3失败 4取消)"),
                                fieldWithPath("result.taskType").optional().description("任务类型 1、模型 2、PSI 3、PIR"),
                                fieldWithPath("result.taskStartDate").optional().description("开始运行时间"),
                                fieldWithPath("result.taskEndDate").optional().description("结束运行时间"),
                                fieldWithPath("result.taskErrorMsg").optional().description("状态失败 错误信息"),
                                fieldWithPath("result.timeConsuming").optional().description("耗时 单位秒"),
                                fieldWithPath("result.taskName").optional().description("任务名称 模型目前无"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testDeleteTask() throws Exception{
        this.mockMvc.perform(get("/task/deleteTask")
                .param("taskId","1"))
                .andExpect(status().isOk())
                .andDo(document("deleteTask",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("taskId").description("任务ID")
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
    public void testGetPirTaskList() throws Exception{
        this.mockMvc.perform(get("/pir/getPirTaskList")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("organName","")
                .param("resourceName","")
                .param("retrievalId","")
                .param("taskState","")
                .param("serverAddress",""))
                .andExpect(status().isOk())
                .andDo(document("getPirTaskList",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("organName").description("机构名称"),
                                parameterWithName("resourceName").description("资源名称"),
                                parameterWithName("retrievalId").description("检索ID"),
                                parameterWithName("taskState").description("任务状态(0未开始 1成功 2查询中 3失败)"),
                                parameterWithName("serverAddress").description("中心节点")
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
                                fieldWithPath("result.data[].taskState").optional().description("任务状态(0未开始 1成功 2运行中 3失败 4取消)"),
                                fieldWithPath("result.data[].serverAddress").optional().description("中心节点"),
                                fieldWithPath("result.data[].organId").optional().description("机构ID"),
                                fieldWithPath("result.data[].organName").optional().description("机构名称"),
                                fieldWithPath("result.data[].resourceId").optional().description("资源ID"),
                                fieldWithPath("result.data[].resourceName").optional().description("资源名称"),
                                fieldWithPath("result.data[].resourceRowsCount").description("资源行数"),
                                fieldWithPath("result.data[].resourceColumnCount").description("资源列数"),
                                fieldWithPath("result.data[].resourceContainsY").optional().description("资源字段中是否包含y字段 0否 1是"),
                                fieldWithPath("result.data[].resourceYRowsCount").optional().description("资源y字段有效行数"),
                                fieldWithPath("result.data[].resourceYRatio").optional().description("资源y字段有效行数占总行数的比例"),
                                fieldWithPath("result.data[].retrievalId").optional().description("检索ID"),
                                fieldWithPath("result.data[].createDate").optional().description("查询日期"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
}
