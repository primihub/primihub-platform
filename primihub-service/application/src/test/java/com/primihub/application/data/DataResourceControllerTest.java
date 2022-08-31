package com.primihub.application.data;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
        dataResourceReq.setPageNo(null);
        dataResourceReq.setPageSize(null);
        dataResourceReq.setSelectTag(null);
        String tojson = JSONObject.toJSONString(dataResourceReq,true);
        System.out.println(tojson);
        this.mockMvc.perform(post("/resource/saveorupdateresource")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tojson)
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("dataResource",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                        ),
                        requestFields(
                                fieldWithPath("resourceId").optional().type(Long.class).description("资源id 编辑入参"),
                                fieldWithPath("resourceName").optional().description("资源名称"),
                                fieldWithPath("resourceDesc").optional().description("资源描述"),
                                fieldWithPath("resourceAuthType").optional().description("授权类型 1.公开 2.私有 3.授权"),
                                fieldWithPath("resourceSource").optional().description("资源来源 1.文件上传 2.数据库链接"),
                                fieldWithPath("fileId").optional().description("文件id"),
                                fieldWithPath("tags").optional().description("标签name数组"),
                                fieldWithPath("fieldList[].fieldName").optional().description("数组字段名称"),
                                fieldWithPath("fieldList[].fieldType").optional().description("数组字段类型"),
                                fieldWithPath("fieldList[].fieldDesc").optional().type(String.class).description("数组字段描述"),
                                fieldWithPath("fieldList[].grouping").optional().description("关键字 0否 1是"),
                                fieldWithPath("fieldList[].protectionStatus").optional().description("分组 0否 1是"),
                                fieldWithPath("fieldList[].relevance").optional().description("保护开关 0关闭 1开启"),
                                fieldWithPath("fusionOrganList[].organGlobalId").optional().description("机构id"),
                                fieldWithPath("fusionOrganList[].organName").optional().description("机构名称"),
                                fieldWithPath("fusionOrganList[].organServerAddress").optional().description("机构中心节点")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.resourceId").description("资源id"),
                                fieldWithPath("result.resourceName").description("资源名称"),
                                fieldWithPath("result.resourceDesc").description("资源描述"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }



    @Test
    public void testGetdataResourceList() throws Exception{
        // 查询资源列表接口
        this.mockMvc.perform(get("/resource/getdataresourcelist")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("resourceName","")
                .param("resourceAuthType","")
                .param("tag","")
                .param("selectTag","0")
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("getdataresourcelist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("resourceName").description("资源名称"),
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
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("getdataresource",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
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
                                fieldWithPath("result.dataList[].*").ignored().optional().description("展示文件数据"),
                                fieldWithPath("result.fieldList[].*").ignored().optional().description("文件字段数据"),
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
                .param("resourceId","1")
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
