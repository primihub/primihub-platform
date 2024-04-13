package com.primihub.biz.entity.data.req;

import lombok.Data;

/**
 * Fusion数据集列表请求参数
 */
@Data
public class DataFResourceReq extends PageReq {
    /** 数据集唯一ID */
    private String resourceId;
    /** 数据集名称 模糊搜索 */
    private String resourceName;
    /** 数据集来源 */
    private Integer resourceSource;
    /** 机构ID */
    private String organId;
    /** 数据集标签 */
    private String tagName;
    /** 数据集属性是否包含Y列 */
    private Integer fileContainsY;
}
