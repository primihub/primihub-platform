package com.primihub.biz.entity.data.req;

import lombok.Data;

/**
 * 成功后的模型列表参数
 */
@Data
public class ModelTaskSuccessReq extends PageReq {
    /** 模型ID */
    private Long modelId;
    /** 模型名称 */
    private String modelName;
    /** 模型类型 */
    private Integer modelType;
    /** 开始时间 */
    private String startDate;
    /** 结束时间 */
    private String endDate;
    private Long startTime;
    private Long endTime;
    private Long userId;
    private Integer isAdmin = 0;
}
