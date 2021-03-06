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
                                parameterWithName("pOrganId").description("?????????id ????????? ?????????0"),
                                parameterWithName("organName").description("???????????? ?????????"),
                                parameterWithName("pageNum").description("?????? ????????? ?????????1"),
                                parameterWithName("pageSize").description("???????????? ????????? ?????????10")
                        ),
                        responseFields(
                                fieldWithPath("code").description("?????????"),
                                fieldWithPath("msg").description("???????????????"),
                                fieldWithPath("result").description("???????????????"),
                                fieldWithPath("result.sysOrganList").description("????????????"),
                                fieldWithPath("result.sysOrganList[].organId").description("??????id"),
                                fieldWithPath("result.sysOrganList[].organName").description("????????????"),
                                fieldWithPath("result.sysOrganList[].porganId").description("?????????id"),
                                fieldWithPath("result.sysOrganList[].rorganId").description("?????????id"),
                                fieldWithPath("result.sysOrganList[].fullPath").description("????????????"),
                                fieldWithPath("result.sysOrganList[].organIndex").description("??????"),
                                fieldWithPath("result.sysOrganList[].organDepth").description("??????"),
                                fieldWithPath("result.sysOrganList[].isDel").description("????????????"),
                                fieldWithPath("result.sysOrganList[].ctime").description("????????????"),
                                fieldWithPath("result.sysOrganList[].cTime").description("????????????"),
                                fieldWithPath("result.sysOrganList[].utime").description("????????????"),
                                fieldWithPath("result.pageParam").description("????????????"),
                                fieldWithPath("result.pageParam.pageNum").description("??????"),
                                fieldWithPath("result.pageParam.pageSize").description("????????????"),
                                fieldWithPath("result.pageParam.pageIndex").description("????????????"),
                                fieldWithPath("result.pageParam.itemTotalCount").description("??????"),
                                fieldWithPath("result.pageParam.pageCount").description("?????????"),
                                fieldWithPath("result.pageParam.isLoadMore").description("?????????????????????"),
                                fieldWithPath("extra").description("????????????")
                        )
                ));
    }

    @Test
    public void testCreateOrganNode() throws Exception {
        this.mockMvc.perform(post("/organ/createOrganNode")
                .param("organName","????????????")
                .param("pOrganId","")
                .param("organIndex","1"))
                .andExpect(status().isOk())
                .andDo(document("createOrganNode",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(

                        ),
                        requestParameters(
                                parameterWithName("organName").description("????????????"),
                                parameterWithName("pOrganId").description("?????????id"),
                                parameterWithName("organIndex").description("??????")
                        ),
                        responseFields(
                                fieldWithPath("code").description("?????????"),
                                fieldWithPath("msg").description("???????????????"),
                                fieldWithPath("result").description("???????????????"),
                                fieldWithPath("result.sysOrgan").description("????????????"),
                                fieldWithPath("result.sysOrgan.organId").description("??????id"),
                                fieldWithPath("result.sysOrgan.organName").description("????????????"),
                                fieldWithPath("result.sysOrgan.porganId").description("?????????id"),
                                fieldWithPath("result.sysOrgan.rorganId").description("?????????id"),
                                fieldWithPath("result.sysOrgan.fullPath").description("????????????"),
                                fieldWithPath("result.sysOrgan.organIndex").description("??????"),
                                fieldWithPath("result.sysOrgan.organDepth").description("??????"),
                                fieldWithPath("result.sysOrgan.isDel").description("????????????"),
                                fieldWithPath("result.sysOrgan.ctime").description("????????????"),
                                fieldWithPath("result.sysOrgan.utime").description("????????????"),
                                fieldWithPath("extra").description("????????????")
                        )
                ));
    }

    @Test
    public void testAlterOrganNodeStatus() throws Exception {
        this.mockMvc.perform(post("/organ/alterOrganNodeStatus")
                .param("organId","1")
                .param("organName","????????????"))
                .andExpect(status().isOk())
                .andDo(document("alterOrganNodeStatus",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(

                        ),
                        requestParameters(
                                parameterWithName("organId").description("??????id"),
                                parameterWithName("organName").description("????????????")
                        ),
                        responseFields(
                                fieldWithPath("code").description("?????????"),
                                fieldWithPath("msg").description("???????????????"),
                                fieldWithPath("result").description("???????????????"),
                                fieldWithPath("extra").description("????????????")
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
                                parameterWithName("organId").description("??????id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("?????????"),
                                fieldWithPath("msg").description("???????????????"),
                                fieldWithPath("result").description("???????????????"),
                                fieldWithPath("extra").description("????????????")
                        )
                ));
    }

    @Test
    public void testChangeLocalOrganInfo() throws Exception {
        this.mockMvc.perform(get("/organ/changeLocalOrganInfo")
                .param("organName","????????????"))
                .andExpect(status().isOk())
                .andDo(document("changeLocalOrganInfo",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("organName").description("????????????")
                        ),
                        responseFields(
                                fieldWithPath("code").description("?????????"),
                                fieldWithPath("msg").description("???????????????"),
                                fieldWithPath("result").description("???????????????"),
                                fieldWithPath("result.sysLocalOrganInfo").description("????????????"),
                                fieldWithPath("result.sysLocalOrganInfo.organId").description("??????ID"),
                                fieldWithPath("result.sysLocalOrganInfo.organName").description("????????????"),
                                fieldWithPath("result.sysLocalOrganInfo.pinCode").description("16???code"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap").optional().description("????????????map"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList").optional().description("????????????list"),
                                fieldWithPath("extra").description("????????????")
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
                                fieldWithPath("code").description("?????????"),
                                fieldWithPath("msg").description("???????????????"),
                                fieldWithPath("result").description("???????????????"),
                                fieldWithPath("result.sysLocalOrganInfo").description("????????????"),
                                fieldWithPath("result.sysLocalOrganInfo.organId").description("??????ID"),
                                fieldWithPath("result.sysLocalOrganInfo.organName").description("????????????"),
                                fieldWithPath("result.sysLocalOrganInfo.pinCode").description("16???code"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap").optional().description("????????????map"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.[]").optional().description("????????????map"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.[].serverAddress").optional().description("??????????????????"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.[].registered").optional().description("??????????????????"),
//                                fieldWithPath("result.sysLocalOrganInfo.fusionMap.[].show").optional().description("????????????????????????"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[]").optional().description("????????????list"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[].serverAddress").optional().description("??????????????????"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[].registered").optional().description("??????????????????"),
                                fieldWithPath("result.sysLocalOrganInfo.fusionList[].show").optional().description("????????????????????????"),
                                fieldWithPath("extra").description("????????????")
                        )
                ));
    }
}
