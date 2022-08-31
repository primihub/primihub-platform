package com.primihub.application.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

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

@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataModelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public  void testGetModelList() throws Exception {
        // 查询项目列表接口
        this.mockMvc.perform(get("/model/getmodellist")
                .param("pageNo","1")
                .param("pageSize","5")
                .param("modelName","")
                .param("projectName","")
                .param("taskStatus","")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("getmodellist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("pageNo").description("第几页"),
                                parameterWithName("pageSize").description("每页条数"),
                                parameterWithName("modelName").description("每页条数"),
                                parameterWithName("projectName").description("每页条数"),
                                parameterWithName("taskStatus").description("运行状态")

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
                                fieldWithPath("result.data[].modelId").description("模型id"),
                                fieldWithPath("result.data[].modelName").description("模型名称"),
                                fieldWithPath("result.data[].projectName").description("项目名称"),
                                fieldWithPath("result.data[].resourceNum").description("资源数"),
                                fieldWithPath("result.data[].latestTaskId").optional().description("最近一次运行任务ID"),
                                fieldWithPath("result.data[].latestTaskStatus").optional().description("最近一次运行状态"),
                                fieldWithPath("result.data[].totalTime").description("最后一次耗时"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public  void testGetDataModel() throws Exception {
        // 查询项目列表接口
        this.mockMvc.perform(get("/model/getdatamodel")
                .param("taskId","19")
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("getdatamodel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("taskId").description("任务ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.model").description("模型信息"),
                                fieldWithPath("result.model.modelId").description("模型id"),
                                fieldWithPath("result.model.modelName").description("模型名称"),
                                fieldWithPath("result.model.modelDesc").description("模型描述"),
                                fieldWithPath("result.model.modelType").description("模型模板 1.联邦学习ID对齐 2.V-XGBoost 3.V-逻辑回归 4.线性回归"),
                                fieldWithPath("result.model.projectId").description("项目id"),
                                fieldWithPath("result.model.resourceNum").description("资源个数"),
                                fieldWithPath("result.model.yValueColumn").description("y值字段"),
                                fieldWithPath("result.model.isDraft").description("是否草稿 0是 1否"),
                                fieldWithPath("result.model.latestTaskId").description("最近运行一次任务id"),
                                fieldWithPath("result.model.latestCostTime").description("最近一次运行时间"),
                                fieldWithPath("result.model.latestTaskStatus").description("最近一次任务状态"),
                                fieldWithPath("result.model.latestAlignmentRatio").description("数据对齐比例"),
                                fieldWithPath("result.model.latestAlignmentCost").description("据对齐耗时"),
                                fieldWithPath("result.model.latestAnalyzeRatio").description("统计分析比例"),
                                fieldWithPath("result.model.latestAnalyzeCost").description("统计分析耗时"),
                                fieldWithPath("result.model.latestFeatureRatio").description("特征筛选比例"),
                                fieldWithPath("result.model.latestFeatureCost").description("特征筛选耗时"),
                                fieldWithPath("result.model.latestSampleRatio").description("样本抽样设计比例"),
                                fieldWithPath("result.model.latestSampleCost").description("样本抽样设计耗时"),
                                fieldWithPath("result.model.latestTrainRatio").description("训练测试设计比例"),
                                fieldWithPath("result.model.latestTrainCost").description("训练测试设计耗时"),
                                fieldWithPath("result.model.latestLackRatio").description("缺失值处理比例"),
                                fieldWithPath("result.model.latestLlackCost").description("缺失值处理耗时"),
                                fieldWithPath("result.model.latestExceptionRatio").description("异常值处理比例"),
                                fieldWithPath("result.model.latestExceptionCost").description("异常值处理耗时"),
                                fieldWithPath("result.model.createDate").description("创建时间"),
                                fieldWithPath("result.model.totalTime").description("最近一次总耗时 单位 s（秒）"),
                                fieldWithPath("result.modelResources").description("模型资源信息"),
                                fieldWithPath("result.modelResources[].resourceId").description("资源id"),
                                fieldWithPath("result.modelResources[].resourceName").description("资源名称"),
                                fieldWithPath("result.modelResources[].organId").description("机构id"),
                                fieldWithPath("result.modelResources[].organName").description("机构名称"),
                                fieldWithPath("result.modelResources[].fileNum").description("原始记录数"),
                                fieldWithPath("result.modelResources[].alignmentNum").description("对齐后记录数量"),
                                fieldWithPath("result.modelResources[].primitiveParamNum").description("原始变量数量"),
                                fieldWithPath("result.modelResources[].modelParamNum").description("入模变量数量"),
                                fieldWithPath("result.anotherQuotas.*").optional().ignored().description("模型指标信息"),
                                fieldWithPath("result.modelComponent").description("组件执行信息"),
                                fieldWithPath("result.modelComponent[].componentId").description("组件id"),
                                fieldWithPath("result.modelComponent[].modelId").description("模型id"),
                                fieldWithPath("result.modelComponent[].componentCode").description("组件code"),
                                fieldWithPath("result.modelComponent[].componentName").description("组件名称"),
                                fieldWithPath("result.modelComponent[].timeConsuming").description("组件耗时 单位秒"),
                                fieldWithPath("result.modelComponent[].timeRatio").description("组件耗时占比"),
                                fieldWithPath("result.modelComponent[].componentState").description("组件执行状态 0初始 1成功 2运行中 3失败"),
                                fieldWithPath("result.taskState").description("任务状态 0未运行 1完成 2运行中 3失败 4取消 默认0 "),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
    //---------------------------------v0.2----------------------------------
    @Test
    public  void testGetModelComponent() throws Exception {
        // 查询模型左侧组件列表接口
        this.mockMvc.perform(get("/model/getModelComponent")
                .param("modelId","1")
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("getModelComponent",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("modelId").description("模型id 非必填")

                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result[].isShow").description("是否展示"),
                                fieldWithPath("result[].isMandatory").description("是否必须的"),
                                fieldWithPath("result[].componentCode").description("组件code"),
                                fieldWithPath("result[].componentName").description("组件名称"),
                                fieldWithPath("result[].componentTypes").description("组件左侧参数列表"),
                                fieldWithPath("result[].componentTypes[].typeCode").description("参数code"),
                                fieldWithPath("result[].componentTypes[].typeName").description("参数名称"),
                                fieldWithPath("result[].componentTypes[].isRequired").description("是否必填"),
                                fieldWithPath("result[].componentTypes[].inputType").description("参数类型"),
                                fieldWithPath("result[].componentTypes[].inputValue").description("参数值"),
                                fieldWithPath("result[].componentTypes[].inputValues").description("参数数组"),
                                fieldWithPath("result[].componentTypes[].inputValues[].key").description("参数数组key"),
                                fieldWithPath("result[].componentTypes[].inputValues[].val").description("参数数组val"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
    @Test
    public void testSaveModelAndComponent() throws Exception {
        String boby_json = "{\"timestamp\":1649922411717,\"nonce\":102,\"token\":\"SU20220413115241B420D205A118104993DB9064FD5E79E1\",\"param\":{\"isDraft\":0,\"modelComponents\":[{\"componentCode\":\"start\",\"componentId\":\"13\",\"componentName\":\"开始\",\"componentValues\":[{\"key\":\"modelName\",\"val\":\"模型回归测试02\"},{\"key\":\"modelDesc\",\"val\":\"\"},{\"key\":\"trainType\",\"val\":\"1\"}],\"coordinateX\":495,\"coordinateY\":150,\"frontComponentId\":\"89322f23-6d8d-4350-b804-c3b3f3c5aae3\",\"height\":40,\"input\":[],\"output\":[{\"componentCode\":\"dataSet\",\"componentId\":\"14\",\"pointJson\":\"\",\"pointType\":\"edge\",\"portId\":\"port1\"}],\"shape\":\"start-node\",\"width\":120},{\"componentCode\":\"dataSet\",\"componentId\":\"14\",\"componentName\":\"选择数据集\",\"componentValues\":[{\"key\":\"selectData\",\"val\":\"[{\\\\\\\"organId\\\\\\\":\\\\\\\"8bf56ee6-b004-4ada-b078-591acb22b324\\\\\\\",\\\\\\\"resourceId\\\\\\\":\\\\\\\"591acb22b324-82586ab3-f94b-4219-a818-657da7b28d1b\\\\\\\",\\\\\\\"resourceName\\\\\\\":\\\\\\\"d-host\\\\\\\",\\\\\\\"resourceRowsCount\\\\\\\":50,\\\\\\\"resourceColumnCount\\\\\\\":8,\\\\\\\"resourceContainsY\\\\\\\":0,\\\\\\\"auditStatus\\\\\\\":1,\\\\\\\"participationIdentity\\\\\\\":1,\\\\\\\"fileHandleField\\\\\\\":[\\\\\\\"Class\\\\\\\",\\\\\\\"x0\\\\\\\",\\\\\\\"x1\\\\\\\",\\\\\\\"x2\\\\\\\",\\\\\\\"x3\\\\\\\",\\\\\\\"x4\\\\\\\",\\\\\\\"x5\\\\\\\",\\\\\\\"x6\\\\\\\"],\\\\\\\"calculationField\\\\\\\":\\\\\\\"Class\\\\\\\",\\\\\\\"organName\\\\\\\":\\\\\\\"test1\\\\\\\"},{\\\\\\\"organId\\\\\\\":\\\\\\\"2cad8338-2e8c-4768-904d-2b598a7e3298\\\\\\\",\\\\\\\"resourceId\\\\\\\":\\\\\\\"2b598a7e3298-cacd9b49-75a5-4f2b-94d9-c6a19c80d1c1\\\\\\\",\\\\\\\"resourceName\\\\\\\":\\\\\\\"d-guest1\\\\\\\",\\\\\\\"resourceRowsCount\\\\\\\":50,\\\\\\\"resourceColumnCount\\\\\\\":8,\\\\\\\"resourceContainsY\\\\\\\":0,\\\\\\\"auditStatus\\\\\\\":1,\\\\\\\"participationIdentity\\\\\\\":2,\\\\\\\"fileHandleField\\\\\\\":[\\\\\\\"x6\\\\\\\",\\\\\\\"x7\\\\\\\",\\\\\\\"x8\\\\\\\",\\\\\\\"x9\\\\\\\",\\\\\\\"x10\\\\\\\",\\\\\\\"x11\\\\\\\",\\\\\\\"x12\\\\\\\",\\\\\\\"x13\\\\\\\"],\\\\\\\"calculationField\\\\\\\":\\\\\\\"x6\\\\\\\",\\\\\\\"organName\\\\\\\":\\\\\\\"test2\\\\\\\"}]\"}],\"coordinateX\":650,\"coordinateY\":300,\"frontComponentId\":\"ef92c2bd-9f0a-46f0-8213-9a9dce839016\",\"height\":50,\"input\":[{\"componentCode\":\"start\",\"componentId\":\"13\",\"pointJson\":\"\",\"pointType\":\"edge\",\"portId\":\"port2\"}],\"output\":[{\"componentCode\":\"model\",\"componentId\":\"15\",\"pointJson\":\"\",\"pointType\":\"edge\",\"portId\":\"port1\"}],\"shape\":\"dag-node\",\"width\":180},{\"componentCode\":\"model\",\"componentId\":\"15\",\"componentName\":\"模型选择\",\"componentValues\":[{\"key\":\"modelType\",\"val\":\"2\"}],\"coordinateX\":450,\"coordinateY\":430,\"frontComponentId\":\"03e301d0-41e3-434c-815c-96f12e9a8384\",\"height\":50,\"input\":[{\"componentCode\":\"dataSet\",\"componentId\":\"14\",\"pointJson\":\"\",\"pointType\":\"edge\",\"portId\":\"port2\"}],\"output\":[],\"shape\":\"dag-node\",\"width\":180}],\"modelId\":\"\",\"modelPointComponents\":[{\"frontComponentId\":\"ceb17e16-ed6c-4026-9df8-0c936b9e75b0\",\"input\":{\"port\":\"port2\",\"cell\":\"89322f23-6d8d-4350-b804-c3b3f3c5aae3\"},\"output\":{\"port\":\"port1\",\"cell\":\"ef92c2bd-9f0a-46f0-8213-9a9dce839016\"},\"shape\":\"edge\"},{\"frontComponentId\":\"c90441f5-987f-4de9-a988-6c82cb716112\",\"input\":{\"port\":\"port2\",\"cell\":\"ef92c2bd-9f0a-46f0-8213-9a9dce839016\"},\"output\":{\"port\":\"port1\",\"cell\":\"03e301d0-41e3-434c-815c-96f12e9a8384\"},\"shape\":\"edge\"}],\"projectId\":20,\"trainType\":0}}";
        // 项目添加
        this.mockMvc.perform(post("/model/saveModelAndComponent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(boby_json)
                .header("userId","1")
                .header("organId","1"))
                .andExpect(status().isOk())
                .andDo(document("saveModelAndComponent",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)"),
                                headerWithName("organId").description("机构id (前端不用传参)")
                        ),
                        requestFields(
                                fieldWithPath("timestamp").description("时间戳"),
                                fieldWithPath("nonce").description("随机数"),
                                fieldWithPath("token").description("token"),
                                fieldWithPath("param.modelId").description("模型id isDraft=0不用传。保存必传"),
                                fieldWithPath("param.projectId").description("项目ID"),
                                fieldWithPath("param.trainType").description("训练类型 训练类型 0纵向 1横向 默认纵向"),
                                fieldWithPath("param.isDraft").description("保存类型 0草稿 1保存"),
                                fieldWithPath("param.modelComponents[].frontComponentId").description("前端组件id"),
                                fieldWithPath("param.modelComponents[].componentId").description("组件id"),
                                fieldWithPath("param.modelComponents[].componentCode").description("组件code"),
                                fieldWithPath("param.modelComponents[].componentName").description("组件名称"),
                                fieldWithPath("param.modelComponents[].coordinateY").description("坐标y"),
                                fieldWithPath("param.modelComponents[].coordinateX").description("坐标x"),
                                fieldWithPath("param.modelComponents[].width").description("宽度"),
                                fieldWithPath("param.modelComponents[].height").description("高度"),
                                fieldWithPath("param.modelComponents[].shape").description("形状"),
                                fieldWithPath("param.modelComponents[].componentValues[0].key").description("组件入参key"),
                                fieldWithPath("param.modelComponents[].componentValues[0].val").description("组件入参val"),
                                fieldWithPath("param.modelComponents[].input[]").description("输入组件"),
                                fieldWithPath("param.modelComponents[].input[].componentId").description("输入组件id"),
                                fieldWithPath("param.modelComponents[].input[].componentCode").description("输入组件code"),
                                fieldWithPath("param.modelComponents[].input[].pointType").description("指向类型"),
                                fieldWithPath("param.modelComponents[].input[].pointJson").description("指向json"),
                                fieldWithPath("param.modelComponents[].input[].port").optional().type(String.class).description("前端信息"),
                                fieldWithPath("param.modelComponents[].input[].cell").optional().type(String.class).description("前端信息"),
                                fieldWithPath("param.modelComponents[].input[].portId").optional().type(String.class).description("前端信息"),
                                fieldWithPath("param.modelComponents[].output[]").description("输出组件"),
                                fieldWithPath("param.modelComponents[].output[].componentId").description("输入组件id"),
                                fieldWithPath("param.modelComponents[].output[].componentCode").description("输入组件code"),
                                fieldWithPath("param.modelComponents[].output[].pointType").description("指向类型"),
                                fieldWithPath("param.modelComponents[].output[].pointJson").type(String.class).description("指向json"),
                                fieldWithPath("param.modelComponents[].output[].port").optional().type(String.class).description("前端信息"),
                                fieldWithPath("param.modelComponents[].output[].cell").optional().type(String.class).description("前端信息"),
                                fieldWithPath("param.modelComponents[].output[].portId").optional().type(String.class).description("前端信息"),
                                fieldWithPath("param.modelPointComponents[].*").description("前端信息"),
                                fieldWithPath("param.modelPointComponents[].*.*").description("前端信息")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.modelId").description("模型id"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }




    @Test
    public void testDeleteModel() throws Exception{
        // 删除资源接口
        this.mockMvc.perform(get("/model/deleteModel")
                .param("modelId","3")
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("deleteModel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("modelId").description("模型id")
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
    public  void testgetModelComponentDetail() throws Exception {
        // 查询模型左侧组件列表接口
        this.mockMvc.perform(get("/model/getModelComponentDetail")
                .param("modelId","3")
                .param("projectId","")
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("getModelComponentDetail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("modelId").description("模型id 非必填没有则根据用户id查询"),
                                parameterWithName("projectId").description("项目id 非必填没有择根据modelId或userId")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.modelId").description("模型id"),
                                fieldWithPath("result.modelDesc").description("模型描述"),
                                fieldWithPath("result.modelName").description("模型名称"),
                                fieldWithPath("result.trainType").description("训练类型 0纵向 1横向"),
                                fieldWithPath("result.isDraft").description("是否草稿 0是 1不是"),
                                fieldWithPath("result.projectId").description("项目ID"),
                                fieldWithPath("result.modelComponents[].componentId").description("组件id"),
                                fieldWithPath("result.modelComponents[].frontComponentId").description("前端组件id"),
                                fieldWithPath("result.modelComponents[].componentCode").description("组件code"),
                                fieldWithPath("result.modelComponents[].componentName").description("组件名称"),
                                fieldWithPath("result.modelComponents[].coordinateY").description("坐标y"),
                                fieldWithPath("result.modelComponents[].coordinateX").description("坐标x"),
                                fieldWithPath("result.modelComponents[].width").description("宽度"),
                                fieldWithPath("result.modelComponents[].height").description("高度"),
                                fieldWithPath("result.modelComponents[].shape").description("形状"),
                                fieldWithPath("result.modelComponents[].componentValues").description("组件值list"),
                                fieldWithPath("result.modelComponents[].componentValues[].key").description("参数key"),
                                fieldWithPath("result.modelComponents[].componentValues[].val").description("参数val"),
                                fieldWithPath("result.modelComponents[].input").description("输入"),
                                fieldWithPath("result.modelComponents[].input[].componentId").description("输入组件id"),
                                fieldWithPath("result.modelComponents[].input[].componentCode").description("输入组件code"),
                                fieldWithPath("result.modelComponents[].input[].pointType").description("指向"),
                                fieldWithPath("result.modelComponents[].input[].portId").description("前端组件信息"),
                                fieldWithPath("result.modelComponents[].input[].pointJson").description("指向json"),
                                fieldWithPath("result.modelComponents[].output").description("输出"),
                                fieldWithPath("result.modelComponents[].output[].componentId").description("输出组件id"),
                                fieldWithPath("result.modelComponents[].output[].componentCode").description("输出组件code"),
                                fieldWithPath("result.modelComponents[].output[].pointType").description("指向"),
                                fieldWithPath("result.modelComponents[].output[].portId").description("前端组件信息"),
                                fieldWithPath("result.modelComponents[].output[].pointJson").description("指向json"),
                                fieldWithPath("result.modelPointComponents[].frontComponentId").description("前端组件id"),
                                fieldWithPath("result.modelPointComponents[].shape").description("前端信息"),
                                fieldWithPath("result.modelPointComponents[].input").description("前端信息"),
                                fieldWithPath("result.modelPointComponents[].input.port").description("前端信息"),
                                fieldWithPath("result.modelPointComponents[].input.cell").description("前端信息"),
                                fieldWithPath("result.modelPointComponents[].output").description("前端信息"),
                                fieldWithPath("result.modelPointComponents[].output.port").description("前端信息"),
                                fieldWithPath("result.modelPointComponents[].output.cell").description("前端信息"),
                                fieldWithPath("result.modelPointComponents[].").description(""),
                                fieldWithPath("result.modelPointComponents[].").description(""),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testRunTaskModel() throws Exception{
        // 删除资源接口
        this.mockMvc.perform(get("/model/runTaskModel")
                .param("modelId","3")
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("runTaskModel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("modelId").description("模型id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.modelId").description("模型ID"),
                                fieldWithPath("result.taskId").description("任务ID"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testRestartTaskModel() throws Exception{
        // 删除资源接口
        this.mockMvc.perform(get("/model/restartTaskModel")
                .param("taskId","3"))
                .andExpect(status().isOk())
                .andDo(document("restartTaskModel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
//                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("taskId").description("任务id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.modelId").description("模型ID"),
                                fieldWithPath("result.taskId").description("任务ID"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testGetTaskModelComponent() throws Exception{
        // 删除资源接口
        this.mockMvc.perform(get("/model/getTaskModelComponent")
                .param("taskId","3")
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("getTaskModelComponent",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("taskId").description("任务id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.taskState").description("任务状态"),
                                fieldWithPath("result.components[].componentId").description("组件id"),
                                fieldWithPath("result.components[].componentCode").description("组件编码"),
                                fieldWithPath("result.components[].componentName").description("组件名称"),
                                fieldWithPath("result.components[].componentState").description("0初始 1成功 2运行中 3失败"),
                                fieldWithPath("result.components[].complete").description("是否执行完毕"),
                                fieldWithPath("result.components[].timeConsuming").description("耗时"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }
}
