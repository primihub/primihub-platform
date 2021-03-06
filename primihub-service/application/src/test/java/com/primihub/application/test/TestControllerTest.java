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
                                parameterWithName("testId").description("??????id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("?????????"),
                                fieldWithPath("msg").description("???????????????"),
                                fieldWithPath("result").description("???????????????"),
                                fieldWithPath("result.port").description("??????"),
                                fieldWithPath("result.name").description("????????????"),
                                fieldWithPath("result.first").description("nacos???????????????"),
                                fieldWithPath("result.second").description("nacos???????????????"),
                                fieldWithPath("result.third").description("nacos??????????????????"),
                                fieldWithPath("result.id").description("nacos??????id"),
                                fieldWithPath("result.sql").description("sql????????????"),
                                fieldWithPath("result.sql.id").description("sql????????????id"),
                                fieldWithPath("result.testId").description("??????"),
                                fieldWithPath("extra").description("????????????")
                        )
                ));
    }




}
