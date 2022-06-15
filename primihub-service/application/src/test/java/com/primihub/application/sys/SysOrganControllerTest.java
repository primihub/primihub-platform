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
public class SysOrganControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testFindOrganPage() throws Exception {
        this.mockMvc.perform(get("/organ/findOrganPage")
                .param("pOrganId","")
                .param("organName","")
                .param("pageNum","1")
                .param("pageSize","10"))
                .andExpect(status().isOk())
                .andDo(document("findOrganPage",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("pOrganId").description("父节点id 非必填 默认为0"),
                                parameterWithName("organName").description("机构名称 非必填"),
                                parameterWithName("pageNum").description("页数 非必填 默认为1"),
                                parameterWithName("pageSize").description("每页条数 非必填 默认为10")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysOrganList").description("机构列表"),
                                fieldWithPath("result.sysOrganList[].organId").description("机构id"),
                                fieldWithPath("result.sysOrganList[].organName").description("机构名称"),
                                fieldWithPath("result.sysOrganList[].porganId").description("父节点id"),
                                fieldWithPath("result.sysOrganList[].rorganId").description("根节点id"),
                                fieldWithPath("result.sysOrganList[].fullPath").description("完整路径"),
                                fieldWithPath("result.sysOrganList[].organIndex").description("顺序"),
                                fieldWithPath("result.sysOrganList[].organDepth").description("深度"),
                                fieldWithPath("result.sysOrganList[].isDel").description("是否删除"),
                                fieldWithPath("result.sysOrganList[].ctime").description("创建时间"),
                                fieldWithPath("result.sysOrganList[].cTime").description("创建时间"),
                                fieldWithPath("result.sysOrganList[].utime").description("更新时间"),
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

    @Test
    public void testCreateOrganNode() throws Exception {
        this.mockMvc.perform(post("/organ/createOrganNode")
                .param("organName","测试机构")
                .param("pOrganId","")
                .param("organIndex","1"))
                .andExpect(status().isOk())
                .andDo(document("createOrganNode",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(

                        ),
                        requestParameters(
                                parameterWithName("organName").description("机构名称"),
                                parameterWithName("pOrganId").description("父节点id"),
                                parameterWithName("organIndex").description("顺序")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysOrgan").description("机构信息"),
                                fieldWithPath("result.sysOrgan.organId").description("机构id"),
                                fieldWithPath("result.sysOrgan.organName").description("机构名称"),
                                fieldWithPath("result.sysOrgan.porganId").description("父节点id"),
                                fieldWithPath("result.sysOrgan.rorganId").description("根节点id"),
                                fieldWithPath("result.sysOrgan.fullPath").description("完整路径"),
                                fieldWithPath("result.sysOrgan.organIndex").description("顺序"),
                                fieldWithPath("result.sysOrgan.organDepth").description("深度"),
                                fieldWithPath("result.sysOrgan.isDel").description("是否删除"),
                                fieldWithPath("result.sysOrgan.ctime").description("创建时间"),
                                fieldWithPath("result.sysOrgan.utime").description("更新时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testAlterOrganNodeStatus() throws Exception {
        this.mockMvc.perform(post("/organ/alterOrganNodeStatus")
                .param("organId","1")
                .param("organName","测试机构"))
                .andExpect(status().isOk())
                .andDo(document("alterOrganNodeStatus",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(

                        ),
                        requestParameters(
                                parameterWithName("organId").description("机构id"),
                                parameterWithName("organName").description("机构名称")
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
    public void testDeleteOrganNode() throws Exception {
        this.mockMvc.perform(get("/organ/deleteOrganNode")
                .param("organId","999"))
                .andExpect(status().isOk())
                .andDo(document("deleteOrganNode",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("organId").description("节点id")
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
    public void testChangeLocalOrganInfo() throws Exception {
        this.mockMvc.perform(get("/organ/changeLocalOrganInfo")
                .param("organName","测试机构"))
                .andExpect(status().isOk())
                .andDo(document("changeLocalOrganInfo",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("organName").description("机构名称")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysLocalOrganInfo").description("机构信息"),
                                fieldWithPath("result.sysLocalOrganInfo.organId").description("机构ID"),
                                fieldWithPath("result.sysLocalOrganInfo.organName").description("机构名称"),
                                fieldWithPath("result.sysLocalOrganInfo.pinCode").description("16位code"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap").optional().description("中心节点map"),
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
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap").optional().description("中心节点map"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.[]").optional().description("中心节点map"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.[].serverAddress").optional().description("中心节点地址"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.[].registered").optional().description("中心节点状态"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.[].show").optional().description("中心节点是否展示"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[]").optional().description("中心节点list"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[].serverAddress").optional().description("中心节点地址"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[].registered").optional().description("中心节点状态"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[].show").optional().description("中心节点是否展示"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
}
