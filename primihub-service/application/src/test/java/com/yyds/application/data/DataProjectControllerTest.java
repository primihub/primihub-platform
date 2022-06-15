package com.yyds.application.data;


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
public class DataProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public  void testSaveProject() throws Exception {
        // 项目添加
        this.mockMvc.perform(post("/project/saveproject")
                .param("projectName","项目名称")
                .param("projectDesc","项目描述")
                .param("resources","1","2","3")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("saveproject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("projectName").description("项目名称"),
                                parameterWithName("projectDesc").description("项目描述"),
                                parameterWithName("resources").description("资源列表 id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.projectId").description("项目id"),
                                fieldWithPath("result.projectName").description("项目名称"),
                                fieldWithPath("result.projectDesc").description("项目描述"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
    @Test
    public  void testGetProjectList() throws Exception {
        // 查询项目列表接口
        this.mockMvc.perform(get("/project/getprojectlist")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("projectName","")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("getprojectlist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("projectName").description("项目名称")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.total").description("总共的数据量"),
                                fieldWithPath("result.pageSize").description("每页显示多少条"),
                                fieldWithPath("result.totalPage").description("共有多少页"),
                                fieldWithPath("result.index").description("当前是第几页"),
                                fieldWithPath("result.data[]").description("数据"),
                                fieldWithPath("result.data[].projectId").description("项目id"),
                                fieldWithPath("result.data[].projectName").description("项目名称"),
                                fieldWithPath("result.data[].projectDesc").description("项目描述"),
                                fieldWithPath("result.data[].organId").description("机构id"),
                                fieldWithPath("result.data[].organNum").description("机构数"),
                                fieldWithPath("result.data[].modelNum").description("模型数"),
                                fieldWithPath("result.data[].resourceNum").description("资源数"),
                                fieldWithPath("result.data[].resourceOrganNum").description("机构数"),
                                fieldWithPath("result.data[].authResourceNum").description("已授权资源数"),
                                fieldWithPath("result.data[].userId").description("用户id"),
                                fieldWithPath("result.data[].userName").description("用户名称"),
                                fieldWithPath("result.data[].createDate").description("创建时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public  void testGetDataProject() throws Exception {
        // 查询项目详情接口
        this.mockMvc.perform(get("/project/getdataproject")
                .param("projectId","1")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("getdataproject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("projectId").description("项目id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.projectId").description("项目id"),
                                fieldWithPath("result.projectName").description("项目名称"),
                                fieldWithPath("result.projectDesc").description("项目描述"),
                                fieldWithPath("result.organId").description("创建机构id"),
                                fieldWithPath("result.organName").description("创建机构名称"),
                                fieldWithPath("result.organNames").description("机构名称数组"),
                                fieldWithPath("result.resources[].resourceId").description("资源id"),
                                fieldWithPath("result.resources[].resourceName").description("资源名称"),
                                fieldWithPath("result.resources[].fileSize").description("文件大小"),
                                fieldWithPath("result.resources[].userId").description("用户id"),
                                fieldWithPath("result.resources[].userName").description("用户名称"),
                                fieldWithPath("result.resources[].organId").description("机构id"),
                                fieldWithPath("result.resources[].organName").description("机构名称"),
                                fieldWithPath("result.resources[].isAuthed").description("是否授权 0.未授权 1.已授权"),
                                fieldWithPath("result.resources[].createDate").description("资源创建时间"),
                                fieldWithPath("result.models").description("模型list"),
                                fieldWithPath("result.models[].modelId").description("模型id"),
                                fieldWithPath("result.models[].modelName").description("模型名称"),
                                fieldWithPath("result.models[].modelDesc").description("模型描述"),
                                fieldWithPath("result.models[].modelType").description("模型类型 1.联邦学习ID对齐 2.V-XGBoost 3.V-逻辑回归 4.线性回归"),
                                fieldWithPath("result.createDate").description("创建时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
    @Test
    public void testDelDataProject() throws Exception{
        // 删除资源接口
        this.mockMvc.perform(get("/project/deldataproject")
                .param("projectId","3")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("deldataproject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("projectId").description("项目id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
    //---------------------------------v0.2----------------------------------
    @Test
    public void testGetProjectAuthedeList() throws Exception{
        // 查询项目中资源都审核通过的项目
        this.mockMvc.perform(get("/project/getProjectAuthedeList")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("projectName","")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("getProjectAuthedeList",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("projectName").description("项目名称")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.total").description("总共的数据量"),
                                fieldWithPath("result.pageSize").description("每页显示多少条"),
                                fieldWithPath("result.totalPage").description("共有多少页"),
                                fieldWithPath("result.index").description("当前是第几页"),
                                fieldWithPath("result.data[]").description("数据"),
                                fieldWithPath("result.data[].projectId").description("项目id"),
                                fieldWithPath("result.data[].projectName").description("项目名称"),
                                fieldWithPath("result.data[].projectDesc").description("项目描述"),
                                fieldWithPath("result.data[].organId").description("机构id"),
                                fieldWithPath("result.data[].organNum").description("机构数"),
                                fieldWithPath("result.data[].modelNum").description("模型数"),
                                fieldWithPath("result.data[].resourceNum").description("资源数"),
                                fieldWithPath("result.data[].resourceOrganNum").description("机构数"),
                                fieldWithPath("result.data[].authResourceNum").description("已授权资源数"),
                                fieldWithPath("result.data[].userId").description("用户id"),
                                fieldWithPath("result.data[].userName").description("用户名称"),
                                fieldWithPath("result.data[].createDate").description("创建时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testGetProjectResourceData() throws Exception{
        // 删除资源接口
        this.mockMvc.perform(get("/project/getProjectResourceData")
                .param("projectId","1")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("getProjectResourceData",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("projectId").description("项目id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.projectId").description("项目id"),
                                fieldWithPath("result.projectName").description("项目名称"),
                                fieldWithPath("result.resource[].resourceId").description("资源id"),
                                fieldWithPath("result.resource[].resourceName").description("资源名称"),
                                fieldWithPath("result.resource[].yfile").description("y字段"),
                                fieldWithPath("result.resource[].yfile[].yid").description("y字段id"),
                                fieldWithPath("result.resource[].yfile[].yname").description("y字段name"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
}
