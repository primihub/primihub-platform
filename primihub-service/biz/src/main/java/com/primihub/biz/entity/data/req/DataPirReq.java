package com.primihub.biz.entity.data.req;

import com.primihub.biz.entity.data.base.DataPirKeyQuery;
import lombok.Data;

import java.util.List;

/**
 * 提交匿踪查询任务请求参数
 */
@Data
public class DataPirReq{
    /** 字符串格式资源ID */
    private String resourceId;
    /** 任务名称 */
    private String taskName;
    /** 查询条件 */
    private List<DataPirKeyQuery> keyQuerys;

}

