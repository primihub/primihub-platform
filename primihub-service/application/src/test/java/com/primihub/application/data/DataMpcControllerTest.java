//package com.primihub.application.data;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
//import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
//import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
//import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
//import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
//import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
//import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@AutoConfigureMockMvc
//@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class DataMpcControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    public  void testSaveOrUpdateScript() throws Exception {
//        // 项目添加
//        this.mockMvc.perform(get("/mpc/saveOrUpdateScript")
//                .param("scriptId","")
//                .param("name","文件名称")
//                .param("catalogue","0")
//                .param("pScriptId","")
//                .param("scriptType","0")
//                .param("scriptStatus","0")
//                .param("scriptContent","select * from data_a")
//                .header("organId","1000")
//                .header("userId","1000"))
//                .andExpect(status().isOk())
//                .andDo(document("saveOrUpdateScript",
//                        preprocessRequest(prettyPrint()),
//                        preprocessResponse(prettyPrint()),
//                        requestHeaders(
//                                headerWithName("userId").description("用户id 前端不用传"),
//                                headerWithName("organId").description("机构id 前端不用传")
//                        ),
//                        requestParameters(
//                                parameterWithName("scriptId").description("脚本id"),
//                                parameterWithName("name").description("文件名称或文件夹名称"),
//                                parameterWithName("catalogue").description("是否目录 0否 1是"),
//                                parameterWithName("pScriptId").description("上级id"),
//                                parameterWithName("scriptType").description("脚本类型 0sql 1python"),
//                                parameterWithName("scriptStatus").description("脚本状态 0打开 1关闭 默认打开"),
//                                parameterWithName("scriptContent").description(" 脚本内容")
//                        ),
//                        responseFields(
//                                fieldWithPath("code").description("返回码"),
//                                fieldWithPath("msg").description("返回码描述"),
//                                fieldWithPath("result").description("返回码结果"),
//                                fieldWithPath("result.scriptId").description("脚本id"),
//                                fieldWithPath("result.name").description("脚本名称"),
//                                fieldWithPath("extra").description("额外信息")
//                        )
//                ));
//    }
//
//    @Test
//    public  void testGetDataScriptList() throws Exception {
//        // 项目添加
//        this.mockMvc.perform(get("/mpc/getDataScriptList")
//                .param("scriptName","")
//                .header("organId","1000")
//                .header("userId","1000"))
//                .andExpect(status().isOk())
//                .andDo(document("getDataScriptList",
//                        preprocessRequest(prettyPrint()),
//                        preprocessResponse(prettyPrint()),
//                        requestHeaders(
//                                headerWithName("userId").description("用户id 前端不用传"),
//                                headerWithName("organId").description("机构id 前端不用传")
//                        ),
//                        requestParameters(
//                                parameterWithName("scriptName").description("脚本名称")
//                        ),
//                        responseFields(
//                                fieldWithPath("code").description("返回码"),
//                                fieldWithPath("msg").description("返回码描述"),
//                                fieldWithPath("result").description("返回码结果"),
//                                fieldWithPath("result[].scriptId").description("脚本id"),
//                                fieldWithPath("result[].name").description("文件名称或文件夹名称"),
//                                fieldWithPath("result[].catalogue").description("是否目录 0否 1是"),
//                                fieldWithPath("result[].scriptType").optional().description("脚本类型 0sql 1python"),
//                                fieldWithPath("result[].scriptStatus").description("脚本状态 0打开 1关闭 默认打开"),
//                                fieldWithPath("result[].scriptContent").description("脚本内容"),
//                                fieldWithPath("result[].children[]").description("下级列表"),
//                                fieldWithPath("result[].pscriptId").description("上级id"),
//                                fieldWithPath("extra").description("额外信息")
//                        )
//                ));
//    }
//
//    @Test
//    public  void testDelDataScript() throws Exception {
//        // 项目添加
//        this.mockMvc.perform(get("/mpc/delDataScript")
//                .param("scriptId","11"))
//                .andExpect(status().isOk())
//                .andDo(document("delDataScript",
//                        preprocessRequest(prettyPrint()),
//                        preprocessResponse(prettyPrint()),
//                        requestParameters(
//                                parameterWithName("scriptId").description("脚本id")
//                        ),
//                        responseFields(
//                                fieldWithPath("code").description("返回码"),
//                                fieldWithPath("msg").description("返回码描述"),
//                                fieldWithPath("result").description("返回码结果"),
//                                fieldWithPath("extra").description("额外信息")
//                        )
//                ));
//    }
//
//    @Test
//    public  void testGetMpcProjectResourceData() throws Exception {
//        // 项目添加
//        this.mockMvc.perform(get("/project/getMpcProjectResourceData")
//                .param("projectId","1"))
//                .andExpect(status().isOk())
//                .andDo(document("getMpcProjectResourceData",
//                        preprocessRequest(prettyPrint()),
//                        preprocessResponse(prettyPrint()),
//                        requestParameters(
//                                parameterWithName("projectId").description("项目id")
//                        ),
//                        responseFields(
//                                fieldWithPath("code").description("返回码"),
//                                fieldWithPath("msg").description("返回码描述"),
//                                fieldWithPath("result").description("返回码结果"),
//                                fieldWithPath("result[].id").description("机构id"),
//                                fieldWithPath("result[].label").description("机构名称"),
//                                fieldWithPath("result[].children[].id").optional().description("资源id"),
//                                fieldWithPath("result[].children[].label").optional().description("资源名称"),
//                                fieldWithPath("result[].children[].children[].id").optional().description("资源字段id"),
//                                fieldWithPath("result[].children[].children[].label").optional().description("资源字段名称"),
//                                fieldWithPath("result[].children[].children[].children[]").optional().description("下级"),
//                                fieldWithPath("extra").description("额外信息")
//                        )
//                ));
//    }
//
//}
