package com.primihub.application.data;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.primihub.biz.entity.data.req.DataProjectOrganReq;
import com.primihub.biz.entity.data.req.DataProjectReq;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;

    /**
     * 初次保存
     * @throws Exception
     */
    @Test
    public void testSaveProject() throws Exception {
        DataProjectReq req = new DataProjectReq();
        req.setServerAddress("http://localhost:8099");
        req.setProjectName("创建项目2");
        req.setProjectDesc("创建项目描述2");
        List<DataProjectOrganReq> organReqList = new ArrayList<>();
        DataProjectOrganReq organReq = new DataProjectOrganReq();
        // 发起者
        organReq.setOrganId("64d32f5d-190e-4a7e-bb38-9da4b67d70bc");
        organReq.setParticipationIdentity(1);
        organReq.setResourceIds(new ArrayList<String>(){{add("9da4b67d70bc-d1ece07b-67a5-4bd6-b504-15072ce2451f");}});
        organReqList.add(organReq);
        organReq = new DataProjectOrganReq();
        // 协作者
        organReq.setOrganId("3cf42c25-4436-4b7a-9bbf-3dc7fa9bff7d");
        organReq.setParticipationIdentity(2);
        organReqList.add(organReq);
        req.setProjectOrgans(organReqList);
        String jsonStr = JSONObject.toJSONString(req);
        log.info(jsonStr);
        // 项目添加
        this.mockMvc.perform(post("/project/saveOrUpdateProject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("saveproject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                        ),
                        requestFields(
                                fieldWithPath("projectName").description("项目名称"),
                                fieldWithPath("projectDesc").description("项目描述"),
                                fieldWithPath("serverAddress").description("中心地址"),
                                fieldWithPath("projectOrgans").optional().description("项目机构列表"),
                                fieldWithPath("projectOrgans[].organId").description("机构ID"),
                                fieldWithPath("projectOrgans[].participationIdentity").description("机构项目中参与身份 1发起者 2协作者"),
                                fieldWithPath("projectOrgans[].resourceIds").optional().description("参与项目的资源ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.id").description("id"),
                                fieldWithPath("result.projectId").description("项目id"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testUpdateProject() throws Exception {
        DataProjectReq req = new DataProjectReq();
        req.setId(1L);
        req.setProjectName("创建项目编辑");
        req.setProjectDesc("创建项目描述编辑");
        String jsonStr = JSONObject.toJSONString(req);
        log.info(jsonStr);
        // 项目添加
        this.mockMvc.perform(post("/project/saveOrUpdateProject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("updateProject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                        ),
                        requestFields(
                                fieldWithPath("id").optional().description("本地项目ID"),
                                fieldWithPath("projectName").optional().description("项目名称"),
                                fieldWithPath("projectDesc").optional().description("项目描述")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.id").description("id"),
                                fieldWithPath("result.projectId").description("项目id"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testAddProjectOrgan() throws Exception {
        DataProjectReq req = new DataProjectReq();
        req.setId(1L);
        List<DataProjectOrganReq> organReqList = new ArrayList<>();
        DataProjectOrganReq organReq = new DataProjectOrganReq();
        // 协作者
        organReq.setOrganId("391f4895-51e3-4929-8e02-88a587998939");
        organReq.setParticipationIdentity(2);
        organReqList.add(organReq);
        req.setProjectOrgans(organReqList);
        String jsonStr = JSONObject.toJSONString(req);
        log.info(jsonStr);
        // 项目添加
        this.mockMvc.perform(post("/project/saveOrUpdateProject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("addProjectOrgan",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                        ),
                        requestFields(
                                fieldWithPath("id").optional().description("本地项目ID"),
                                fieldWithPath("projectOrgans").optional().description("项目机构列表"),
                                fieldWithPath("projectOrgans[].organId").description("机构ID"),
                                fieldWithPath("projectOrgans[].participationIdentity").description("机构项目中参与身份 1发起者 2协作者")
//                                fieldWithPath("projectOrgans[].resourceIds").optional().description("参与项目的资源ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.id").description("id"),
                                fieldWithPath("result.projectId").description("项目id"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testAddProjectOrganResource() throws Exception {
        DataProjectReq req = new DataProjectReq();
        req.setId(1L);
        List<DataProjectOrganReq> organReqList = new ArrayList<>();
        DataProjectOrganReq organReq = new DataProjectOrganReq();
        // 协作者
        organReq.setOrganId("391f4895-51e3-4929-8e02-88a587998939");
        organReq.setParticipationIdentity(2);
        organReq.setResourceIds(new ArrayList<String>(){{add("88a587998939-39e65985-340d-4c2e-aee4-caace95043a9");add("88a587998939-a03f4041-e929-403d-b442-274b93690b7b");}});
        organReqList.add(organReq);
        req.setProjectOrgans(organReqList);
        String jsonStr = JSONObject.toJSONString(req);
        log.info(jsonStr);
        // 项目添加
        this.mockMvc.perform(post("/project/saveOrUpdateProject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("addProjectOrganResource",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                        ),
                        requestFields(
                                fieldWithPath("id").optional().description("本地项目ID"),
                                fieldWithPath("projectOrgans").optional().description("项目机构列表"),
                                fieldWithPath("projectOrgans[].organId").description("机构ID"),
                                fieldWithPath("projectOrgans[].participationIdentity").description("机构项目中参与身份 1发起者 2协作者"),
                                fieldWithPath("projectOrgans[].resourceIds").optional().description("参与项目的资源ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.id").description("id"),
                                fieldWithPath("result.projectId").description("项目id"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }


    @Test
    public  void testGetProjectList() throws Exception {
        // 查询项目列表接口
        this.mockMvc.perform(get("/project/getProjectList")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("projectName","")
                .param("organId","")
                .param("participationIdentity","")
                .param("queryType","0")
                .param("startDate","2022-06-23 00:00:00")
                .param("endDate","")
                .param("queryType","0")
                .param("status",""))
                .andExpect(status().isOk())
                .andDo(document("getprojectlist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("projectName").description("项目名称 模糊查询"),
                                parameterWithName("queryType").description("查询类型 0全部 1我发起的 2我协作的"),
                                parameterWithName("organId").description("机构ID"),
                                parameterWithName("participationIdentity").description("机构项目中参与身份 1发起者 2协作者"),
                                parameterWithName("startDate").description("开始日期"),
                                parameterWithName("endDate").description("结束日期"),
                                parameterWithName("status").description("项目状态 0审核中 1可用 2关闭")
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
                                fieldWithPath("result.data[].id").description("本地项目ID"),
                                fieldWithPath("result.data[].projectId").description("项目id"),
                                fieldWithPath("result.data[].projectName").description("项目名称"),
                                fieldWithPath("result.data[].projectDesc").description("项目描述"),
                                fieldWithPath("result.data[].userName").description("用户名称"),
                                fieldWithPath("result.data[].createdOrganName").description("创建项目机构名称"),
                                fieldWithPath("result.data[].resourceNum").description("资源数量"),
                                fieldWithPath("result.data[].modelNum").description("模型数量"),
                                fieldWithPath("result.data[].modelAssembleNum").description("模型配置中数量"),
                                fieldWithPath("result.data[].modelRunNum").description("模型运行数量"),
                                fieldWithPath("result.data[].modelSuccessNum").description("模型运行成功数量"),
                                fieldWithPath("result.data[].providerOrganNames").description("协作方机构名称"),
                                fieldWithPath("result.data[].status").description("项目状态 0审核中 1可用 2关闭"),
                                fieldWithPath("result.data[].createDate").description("创建时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public  void testGetDataProject() throws Exception {
        // 查询项目详情接口
        this.mockMvc.perform(get("/project/getProjectDetails")
                .param("id","1"))
                .andExpect(status().isOk())
                .andDo(document("getProjectDetails",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("id").description("项目本地id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.id").description("项目本地id"),
                                fieldWithPath("result.projectId").description("项目id"),
                                fieldWithPath("result.projectName").description("项目名称"),
                                fieldWithPath("result.projectDesc").description("项目描述"),
                                fieldWithPath("result.creator").description("是否创建者"),
                                fieldWithPath("result.userName").description("用户名称"),
                                fieldWithPath("result.status").description("项目状态 0审核中 1可用 2关闭"),
                                fieldWithPath("result.serverAddress").optional().description("中心节点地址"),
                                fieldWithPath("result.organs[]").optional().description("项目机构列表"),
                                fieldWithPath("result.organs[].id").optional().description("项目机构真实ID"),
                                fieldWithPath("result.organs[].projectId").description("项目ID"),
                                fieldWithPath("result.organs[].creator").description("是否创建者"),
                                fieldWithPath("result.organs[].thisInstitution").description("是否本机构"),
                                fieldWithPath("result.organs[].participationIdentity").description("机构项目中参与身份 1发起者 2协作者"),
                                fieldWithPath("result.organs[].organId").optional().description("机构id"),
                                fieldWithPath("result.organs[].organName").optional().description("机构名称"),
                                fieldWithPath("result.organs[].auditStatus").optional().description("审核状态 0审核中 1同意 2拒绝"),
                                fieldWithPath("result.organs[].auditOpinion").optional().description("审核意见"),
                                fieldWithPath("result.organs[].resources[]").optional().description("机构资源列表"),
                                fieldWithPath("result.organs[].resources[].id").optional().description("项目资源真实ID"),
                                fieldWithPath("result.organs[].resources[].projectId").optional().description("项目ID"),
                                fieldWithPath("result.organs[].resources[].participationIdentity").optional().description("机构项目中参与身份 1发起者 2协作者"),
                                fieldWithPath("result.organs[].resources[].resourceId").optional().description("资源ID"),
                                fieldWithPath("result.organs[].resources[].resourceName").optional().description("资源名称"),
                                fieldWithPath("result.organs[].resources[].resourceTag").optional().description("资源标签"),
                                fieldWithPath("result.organs[].resources[].resourceRowsCount").optional().description("资源行数"),
                                fieldWithPath("result.organs[].resources[].resourceColumnCount").optional().description("资源列数"),
                                fieldWithPath("result.organs[].resources[].resourceContainsY").optional().description("资源字段中是否包含y字段 0否 1是"),
                                fieldWithPath("result.organs[].resources[].resourceYRowsCount").optional().description("文件字段y值内容不为空和0的行数"),
                                fieldWithPath("result.organs[].resources[].resourceYRatio").optional().description("文件字段y值内容不为空的行数在总行的占比"),
                                fieldWithPath("result.organs[].resources[].auditStatus").optional().description("审核状态 0审核中 1同意 2拒绝"),
                                fieldWithPath("result.organs[].resources[].auditOpinion").optional().description("审核意见"),
                                fieldWithPath("result.createDate").description("创建时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testProjectApproval() throws Exception{
        this.mockMvc.perform(post("/project/approval")
                .param("type","1")
                .param("id","37")
                .param("auditStatus","1")
                .param("auditOpinion","测试通过"))
                .andExpect(status().isOk())
                .andDo(document("projectApproval",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("type").description("审批类型 1、项目机构审批 2、项目资源审批"),
                                parameterWithName("id").description("审批ID 1、项目机构审批ID 2、项目资源审批ID"),
                                parameterWithName("auditStatus").description("审核状态 1同意 2拒绝"),
                                parameterWithName("auditOpinion").description("审核意见")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述 0成功 1005审核授权失败"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testGetListStatistics() throws Exception{
        this.mockMvc.perform(post("/project/getListStatistics"))
                .andExpect(status().isOk())
                .andDo(document("getListStatistics",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述 0成功 1005审核授权失败"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.total").description("总数量"),
                                fieldWithPath("result.own").description("我发起的数量"),
                                fieldWithPath("result.other").description("我协作的数量"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testCloseProject() throws Exception{
        this.mockMvc.perform(post("/project/closeProject")
                .param("id","6"))
                .andExpect(status().isOk())
                .andDo(document("closeProject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("id").description("本地项目ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述 0成功"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testRemoveResource() throws Exception{
        this.mockMvc.perform(post("/project/removeResource")
                .param("id","1"))
                .andExpect(status().isOk())
                .andDo(document("removeResource",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("id").description("本地项目资源ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述 0成功"),
                                fieldWithPath("result").description("返回码结果"),
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

    @Test
    public void testGetProjectResourceOrgan() throws Exception{
        // 模型数据集选择机构列表
        this.mockMvc.perform(get("/project/getProjectResourceOrgan")
                .param("projectId","15")
                .param("modelId","259"))
                .andExpect(status().isOk())
                .andDo(document("getProjectResourceOrgan",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("projectId").description("项目id"),
                                parameterWithName("modelId").description("模型id 非必填")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result[]").optional().description("返回码结果"),
                                fieldWithPath("result[].creator").optional().description("是否创建者"),
                                fieldWithPath("result[].participationIdentity").optional().description("机构项目中参与身份 1发起者 2协作者"),
                                fieldWithPath("result[].organId").optional().description("机构ID"),
                                fieldWithPath("result[].organName").optional().description("机构名称"),
                                fieldWithPath("result[].auditStatus").optional().description("审核状态 0审核中 1同意 2拒绝"),
                                fieldWithPath("result[].resources[]").optional().description("机构资源列表"),
                                fieldWithPath("result[].resources[].organId").optional().description("资源id"),
                                fieldWithPath("result[].resources[].resourceId").optional().description("资源id"),
                                fieldWithPath("result[].resources[].resourceName").optional().description("资源名称"),
                                fieldWithPath("result[].resources[].resourceRowsCount").optional().description("资源行数"),
                                fieldWithPath("result[].resources[].resourceColumnCount").optional().description("资源列数"),
                                fieldWithPath("result[].resources[].resourceContainsY").optional().description("资源字段中是否包含y字段 0否 1是"),
                                fieldWithPath("result[].resources[].auditStatus").optional().description("审核状态 0审核中 1同意 2拒绝"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }


    @Test
    public void testGetProjectResourceData() throws Exception{
        // 模型选择数据源机构下资源列表
        this.mockMvc.perform(get("/project/getProjectResourceData")
                .param("projectId","15")
                .param("organId","945908dc-bef5-4e39-be1a-40bdea66c03a"))
                .andExpect(status().isOk())
                .andDo(document("getProjectResourceData",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("projectId").description("项目id"),
                                parameterWithName("organId").description("机构id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result[]").optional().description("返回码结果"),
                                fieldWithPath("result[].organId").description("机构id"),
                                fieldWithPath("result[].resourceId").description("资源id"),
                                fieldWithPath("result[].resourceName").description("资源名称"),
                                fieldWithPath("result[].resourceRowsCount").description("资源行数"),
                                fieldWithPath("result[].resourceColumnCount").description("资源列数"),
                                fieldWithPath("result[].resourceContainsY").description("资源字段中是否包含y字段 0否 1是"),
                                fieldWithPath("result[].auditStatus").description("审核状态 0审核中 1同意 2拒绝"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
}
