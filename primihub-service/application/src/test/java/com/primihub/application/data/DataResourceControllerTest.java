package com.primihub.application.data;


import com.alibaba.fastjson.JSON;
import com.primihub.biz.entity.data.req.DataResourceFieldReq;
import com.primihub.biz.entity.data.req.DataResourceReq;
import com.primihub.biz.entity.data.req.DataSourceOrganReq;
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
public class DataResourceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSaveResource() throws Exception{
        DataResourceReq dataResourceReq=new DataResourceReq();
//        dataResourceReq.setResourceId(87L);
        dataResourceReq.setResourceName("借贷信息");
        dataResourceReq.setResourceDesc("关于银行贷款用户的数据清单，对借还贷业务提供辅助，借贷额度、分期数，用于针对逾期提醒等");
        dataResourceReq.setResourceAuthType(3);
        List<DataSourceOrganReq> organList=new ArrayList(){{
            add(new DataSourceOrganReq("A111111","机构A","http://localhost:8099"));
            add(new DataSourceOrganReq("B222222","机构B","http://localhost:8099"));
            add(new DataSourceOrganReq("C333333","机构C","http://localhost:8099"));;}};
        dataResourceReq.setFusionOrganList(organList);
        dataResourceReq.setResourceSource(1);
        dataResourceReq.setFileId(1000L);
        dataResourceReq.setTags(new ArrayList(){{add("额度");add("分期数");add("开卡支行");}});
        DataResourceFieldReq dataResourceFieldReq=new DataResourceFieldReq();
        dataResourceFieldReq.setFieldName("id");
        dataResourceFieldReq.setFieldType("String");
        dataResourceReq.setFieldList(new ArrayList(){{add(dataResourceFieldReq);}});

        this.mockMvc.perform(post("/resource/saveorupdateresource")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(dataResourceReq))
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk());
//                .andDo(document("dataResource",
//                        preprocessRequest(prettyPrint()),
//                        preprocessResponse(prettyPrint()),
//                        requestHeaders(
//                                headerWithName("userId").description("用户id (前端不用传参)"),
//                                headerWithName("organId").description("机构id (前端不用传参)")
//                        ),
//                        requestFields(
//                                fieldWithPath("resourceId").description("资源id 编辑入参"),
//                                fieldWithPath("resourceName").description("资源名称"),
//                                fieldWithPath("resourceDesc").description("资源描述"),
//                                fieldWithPath("resourceSortType").description("资源分类 1.银行 2.电商 3.媒体 4.运营商 5.保险"),
//                                fieldWithPath("resourceAuthType").description("授权类型 1.公开 2.私有 3.授权"),
//                                fieldWithPath("resourceSource").description("资源来源 1.文件上传 2.数据库链接"),
//                                fieldWithPath("fileId").description("文件id"),
//                                fieldWithPath("tags").description("标签name数组"),
//                                fieldWithPath("fieldList[0].fieldName").description("数组字段名称"),
//                                fieldWithPath("fieldList[0].fieldType").description("数组字段类型"),
//                                fieldWithPath("fieldList[0].fieldDesc").description("数组字段描述")
//                        ),
//                        responseFields(
//                                fieldWithPath("code").description("返回码"),
//                                fieldWithPath("msg").description("返回码描述"),
//                                fieldWithPath("result").description("返回码结果"),
//                                fieldWithPath("result.resourceId").description("资源id"),
//                                fieldWithPath("result.resourceName").description("资源名称"),
//                                fieldWithPath("result.resourceDesc").description("资源描述"),
//                                fieldWithPath("extra").description("额外信息")
//                        )
//                ));
    }



    @Test
    public void testGetdataResourceList() throws Exception{
        // 查询资源列表接口
        this.mockMvc.perform(get("/resource/getdataresourcelist")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("resourceName","")
                .param("resourceAuthType","")
                .param("tag","卡")
                .param("selectTag","1")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("getdataresourcelist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("resourceName").description("资源名称"),
                                parameterWithName("resourceSortType").description("资源分类 1.银行 2.电商 3.媒体 4.运营商 5.保险"),
                                parameterWithName("resourceAuthType").description("授权类型 1.公开 2.私有"),
                                parameterWithName("tag").description("标签名称"),
                                parameterWithName("selectTag").description("标签条件状态  0:选择 1:输入 默认0选择")
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
                                fieldWithPath("result.data[].resourceSortType").description("源分类 1.银行 2.电商 3.媒体 4.运营商 5.保险"),
                                fieldWithPath("result.data[].resourceAuthType").description("授权类型 1.公开 2.私有 2.授权"),
                                fieldWithPath("result.data[].resourceSource").description("资源来源 文件上传 数据库链接"),
                                fieldWithPath("result.data[].resourceNum").description("资源数"),
                                fieldWithPath("result.data[].fileId").description("文件id"),
                                fieldWithPath("result.data[].fileSize").description("文件大小"),
                                fieldWithPath("result.data[].fileSuffix").description("文件后缀"),
                                fieldWithPath("result.data[].fileRows").description("文件行数"),
                                fieldWithPath("result.data[].fileColumns").description("文件列数"),
                                fieldWithPath("result.data[].fileHandleField").description("文件字段信息"),
                                fieldWithPath("result.data[].fileHandleStatus").description("文件处理状态 0 未处理 1处理中 2处理完成"),
                                fieldWithPath("result.data[].fileContainsY").optional().description("文件字段中是否包含y字段 0否 1是"),
                                fieldWithPath("result.data[].fileYRows").optional().description("文件y字段有效行数"),
                                fieldWithPath("result.data[].fileYRatio").optional().description("文件y字段有效行数占总行数的比例"),
                                fieldWithPath("result.data[].dbId").description("数据库id"),
//                                fieldWithPath("result.data[].url").description("文件url"),
                                fieldWithPath("result.data[].userId").description("用户id"),
                                fieldWithPath("result.data[].userName").description("用户名称"),
                                fieldWithPath("result.data[].organId").description("机构id"),
                                fieldWithPath("result.data[].organName").description("机构名称"),
                                fieldWithPath("result.data[].createDate").description("创建时间"),
                                fieldWithPath("result.data[].tags[]").description("标签数据"),
                                fieldWithPath("result.data[].tags[].tagId").description("标签id"),
                                fieldWithPath("result.data[].tags[].tagName").description("标签名称"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testGetDataResource() throws Exception{
        // 查询资源详情接口
        this.mockMvc.perform(get("/resource/getdataresource")
                .param("resourceId","1")
                .header("userId","1")
                .header("organId","1")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("getdataresource",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("resourceId").description("资源id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.resource").description("资源信息"),
                                fieldWithPath("result.resource.resourceId").description("资源id"),
                                fieldWithPath("result.resource.resourceName").description("资源名称"),
                                fieldWithPath("result.resource.resourceDesc").description("资源描述"),
                                fieldWithPath("result.resource.resourceSortType").description("源分类 1.银行 2.电商 3.媒体 4.运营商 5.保险"),
                                fieldWithPath("result.resource.resourceAuthType").description("授权类型 1.公开 2.私有 3.授权"),
                                fieldWithPath("result.resource.resourceSource").description("资源来源 文件上传 数据库链接"),
                                fieldWithPath("result.resource.resourceNum").description("资源数"),
                                fieldWithPath("result.resource.fileId").description("文件id"),
                                fieldWithPath("result.resource.fileSize").description("文件大小"),
                                fieldWithPath("result.resource.fileSuffix").description("文件后缀"),
                                fieldWithPath("result.resource.fileRows").description("文件行数"),
                                fieldWithPath("result.resource.fileColumns").description("文件列数"),
                                fieldWithPath("result.resource.fileHandleField").description("文件字段信息"),
                                fieldWithPath("result.resource.fileHandleStatus").description("文件处理状态 0 未处理 1处理中 2处理完成"),
                                fieldWithPath("result.resource.fileContainsY").optional().description("文件字段中是否包含y字段 0否 1是"),
                                fieldWithPath("result.resource.fileYRows").optional().description("文件y字段有效行数"),
                                fieldWithPath("result.resource.fileYRatio").optional().description("文件y字段有效行数占总行数的比例"),
                                fieldWithPath("result.resource.userId").description("用户id"),
                                fieldWithPath("result.resource.userName").description("用户名称"),
                                fieldWithPath("result.resource.dbId").description("数据库id"),
//                                fieldWithPath("result.resource.url").description("文件url"),
                                fieldWithPath("result.resource.organId").description("机构id"),
                                fieldWithPath("result.resource.organName").description("机构名称"),
                                fieldWithPath("result.resource.createDate").description("创建时间"),
                                fieldWithPath("result.resource.tags").description("资源标签信息"),
                                fieldWithPath("result.resource.tags[].tagId").description("标签id"),
                                fieldWithPath("result.resource.tags[].tagName").description("标签名称"),
//                                fieldWithPath("result.file").description("文件信息"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testGetAuthorizationList() throws Exception{
        // 查询资源列表接口
        this.mockMvc.perform(get("/resource/getauthorizationlist")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("status","1")
                .header("userId","1000")
                .header("organId","1000"))
                .andExpect(status().isOk())
                .andDo(document("getauthorizationlist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("status").description("审批状态")
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
                                fieldWithPath("result.data[].recordId").description("审批id"),
                                fieldWithPath("result.data[].recordStatus").description("审批状态 0未授权 1已授权"),
                                fieldWithPath("result.data[].userId").description("审批人id"),
                                fieldWithPath("result.data[].userName").description("审批人名称"),
                                fieldWithPath("result.data[].projectId").description("项目id"),
                                fieldWithPath("result.data[].projectName").description("项目名称"),
                                fieldWithPath("result.data[].resourceId").description("资源id"),
                                fieldWithPath("result.data[].resourceName").description("资源名称"),
                                fieldWithPath("result.data[].organId").description("机构id"),
                                fieldWithPath("result.data[].organName").description("机构名称"),
                                fieldWithPath("result.data[].createDate").description("创建时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testApprovalAuthorization() throws Exception{
        this.mockMvc.perform(post("/resource/approval")
                .param("recordId","16")
                .param("status","1")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("approval",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("recordId").description("审批id"),
                                parameterWithName("status").description("1通过 2拒绝")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.recordId").description("审批id"),
                                fieldWithPath("result.status").description("审批状态 0 未授权 1已授权 2拒绝"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testDelDataResource() throws Exception{
        // 删除资源接口
        this.mockMvc.perform(get("/resource/deldataresource")
                .param("resourceId","1")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("deldataresource",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("resourceId").description("资源id")
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
    public void testGetDataResourceFieldPage() throws Exception{
        this.mockMvc.perform(get("/resource/getDataResourceFieldPage")
                .param("resourceId","58")
                .param("pageSize","5")
                .param("pageNo","1")
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("getDataResourceFieldPage",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("resourceId").description("资源id"),
                                parameterWithName("pageSize").description("每页展示数"),
                                parameterWithName("pageNo").description("页码")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.isUpdate").description("是否可编辑"),
                                fieldWithPath("result.pageData.total").description("总共的数据量"),
                                fieldWithPath("result.pageData.pageSize").description("每页显示多少条"),
                                fieldWithPath("result.pageData.totalPage").description("共有多少页"),
                                fieldWithPath("result.pageData.index").description("当前是第几页"),
                                fieldWithPath("result.pageData.data").description("数据"),
                                fieldWithPath("result.pageData.data[].fieldId").description("字段id"),
                                fieldWithPath("result.pageData.data[].fieldName").description("字段名称"),
                                fieldWithPath("result.pageData.data[].fieldAs").description("字段别名"),
                                fieldWithPath("result.pageData.data[].fieldType").description("字段类型"),
                                fieldWithPath("result.pageData.data[].fieldDesc").description("字段描述"),
                                fieldWithPath("result.pageData.data[].relevance").description("关键字 0否 1是"),
                                fieldWithPath("result.pageData.data[].grouping").description("分组 0否 1是"),
                                fieldWithPath("result.pageData.data[].protectionStatus").description("保护开关 0关闭 1开启"),
                                fieldWithPath("result.pageData.data[].createDate").description("创建时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testUpdateDataResourceField() throws Exception{
        this.mockMvc.perform(get("/resource/updateDataResourceField")
                .param("fieldId","659")
                .param("fieldAs","liweihua")
                .param("fieldType","string")
                .param("fieldDesc","测试实施")
                .param("relevance","1")
                .param("grouping","0")
                .param("protectionStatus","1"))
                .andExpect(status().isOk())
                .andDo(document("updateDataResourceField",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("fieldId").description("字段id"),
                                parameterWithName("fieldAs").description("字段别名"),
                                parameterWithName("fieldType").description("字段类型"),
                                parameterWithName("fieldDesc").description("字段描述"),
                                parameterWithName("relevance").description("关键字 0否 1是"),
                                parameterWithName("grouping").description("分组 0否 1是"),
                                parameterWithName("protectionStatus").description("保护开关 0关闭 1开启")
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
    public void testGetResourceTags() throws Exception{
        this.mockMvc.perform(get("/resource/getResourceTags"))
                .andExpect(status().isOk())
                .andDo(document("getResourceTags",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
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
