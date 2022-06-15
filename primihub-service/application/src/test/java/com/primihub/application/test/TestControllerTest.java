package com.primihub.application.test;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@AutoConfigureMockMvc
//@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class TestControllerTest {

//    @Autowired
    private MockMvc mockMvc;

//    @Test
    public void test() throws Exception{
        this.mockMvc.perform(get("/testInfo")
                .param("testId","123456"))
                .andExpect(status().isOk())
                .andDo(document("test",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(

                        ),
                        requestParameters(
                                parameterWithName("testId").description("测试id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.port").description("端口"),
                                fieldWithPath("result.name").description("平台名称"),
                                fieldWithPath("result.first").description("nacos第一个参数"),
                                fieldWithPath("result.second").description("nacos第二个参数"),
                                fieldWithPath("result.third").description("nacos第三个个参数"),
                                fieldWithPath("result.id").description("nacos保存id"),
                                fieldWithPath("result.sql").description("sql运行结果"),
                                fieldWithPath("result.sql.id").description("sql运行结果id"),
                                fieldWithPath("result.testId").description("入参"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }




}
