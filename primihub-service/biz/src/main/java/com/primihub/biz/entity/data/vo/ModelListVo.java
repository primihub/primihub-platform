package com.primihub.biz.entity.data.vo;

import lombok.Data;

@Data
public class ModelListVo {
    // 模型id
    private Long modelId;
    // 模型名称
    private String modelName;
    private String projectName;
    private Integer resourceNum;
    //最近一次任务状态
    private Integer latestTaskStatus;
    private Integer totalTime;
}
