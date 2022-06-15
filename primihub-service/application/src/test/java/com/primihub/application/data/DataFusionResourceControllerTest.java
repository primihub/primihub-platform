package com.primihub.application.data;


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
public class DataFusionResourceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetDataFusionResourceList() throws Exception{
        // 查询资源列表接口
        this.mockMvc.perform(get("/fusionResource/getResourceList")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("resourceId","")
                .param("resourceName","")
                .param("resourceAuthType","")
                .param("organId","")
                .param("serverAddress","http://localhost:8099")
                .param("tagName","额度"))
                .andExpect(status().isOk())
                .andDo(document("getDataFusionResourceList",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址"),
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("resourceId").description("资源ID"),
                                parameterWithName("resourceName").description("资源名称"),
                                parameterWithName("resourceAuthType").description("授权类型 1.公开 2.私有 3.指定机构可见"),
                                parameterWithName("tagName").description("标签名称"),
                                parameterWithName("organId").description("机构ID")
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
                                fieldWithPath("result.data[].resourceId").description("资源id"),
                                fieldWithPath("result.data[].resourceName").description("资源名称"),
                                fieldWithPath("result.data[].resourceDesc").description("资源描述"),
                                fieldWithPath("result.data[].resourceAuthType").description("授权类型 1.公开 2.私有 2.授权"),
                                fieldWithPath("result.data[].resourceType").description("资源来源 文件上传 数据库链接"),
                                fieldWithPath("result.data[].resourceRowsCount").description("资源行数"),
                                fieldWithPath("result.data[].resourceColumnCount").description("资源列数"),
                                fieldWithPath("result.data[].resourceColumnNameList").optional().description("资源字段信息"),
                                fieldWithPath("result.data[].resourceContainsY").optional().description("资源字段中是否包含y字段 0否 1是"),
                                fieldWithPath("result.data[].resourceYRowsCount").optional().description("资源y字段有效行数"),
                                fieldWithPath("result.data[].resourceYRatio").optional().description("资源y字段有效行数占总行数的比例"),
                                fieldWithPath("result.data[].organId").description("机构id"),
                                fieldWithPath("result.data[].organName").description("机构名称"),
                                fieldWithPath("result.data[].createDate").description("上传时间"),
                                fieldWithPath("result.data[].resourceTag[]").description("标签数据"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testGetDataFusionResourceData() throws Exception{
        // 查询资源列表接口
        this.mockMvc.perform(get("/fusionResource/getDataResource")
                .param("resourceId","e3d24cf47b71-faeb62b6-abc9-455b-a29a-e08dc89ac059")
                .param("serverAddress","http://localhost:8099"))
                .andExpect(status().isOk())
                .andDo(document("getDataFusionResourceData",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("serverAddress").description("中心节点地址"),
                                parameterWithName("resourceId").description("资源ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.resourceId").description("资源id"),
                                fieldWithPath("result.resourceName").description("资源名称"),
                                fieldWithPath("result.resourceDesc").description("资源描述"),
                                fieldWithPath("result.resourceAuthType").description("授权类型 1.公开 2.私有 2.授权"),
                                fieldWithPath("result.resourceType").description("资源来源 文件上传 数据库链接"),
                                fieldWithPath("result.resourceRowsCount").description("资源行数"),
                                fieldWithPath("result.resourceColumnCount").description("资源列数"),
                                fieldWithPath("result.resourceColumnNameList").optional().description("资源字段信息"),
                                fieldWithPath("result.resourceContainsY").optional().description("资源字段中是否包含y字段 0否 1是"),
                                fieldWithPath("result.resourceYRowsCount").optional().description("资源y字段有效行数"),
                                fieldWithPath("result.resourceYRatio").optional().description("资源y字段有效行数占总行数的比例"),
                                fieldWithPath("result.organId").description("机构id"),
                                fieldWithPath("result.organName").description("机构名称"),
                                fieldWithPath("result.createDate").description("上传时间"),
                                fieldWithPath("result.resourceTag[]").description("标签数据"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testGetFusionResourceTags() throws Exception{
        this.mockMvc.perform(get("/fusionResource/getResourceTagList")
                .param("serverAddress","http://localhost:8099"))
                .andExpect(status().isOk())
                .andDo(document("getFusionResourceTags",
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
                                fieldWithPath("result[]").description("标签列表"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
}
