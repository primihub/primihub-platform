package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DataModelTask {

    /**
     * 自增id
     */
    @JsonIgnore
    private Long id;
    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 模型id
     */
    private Long modelId;
    /**
     * 预测文件路径
     */
//    @JsonIgnore
    private String predictFile;
    /**
     * 预测文件内容
     */
    private String predictContent;
    /**
     * 模型运行组件列表json
     */
    private String componentJson;
    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 修改时间
     */
    private Date updateDate;
}
