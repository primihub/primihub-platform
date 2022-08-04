package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

@Data
public class DataModelComponent {
    public DataModelComponent() {
    }

    public DataModelComponent(String inputComponentCode, String outputComponentCode, String pointType, String pointJson) {
        this.inputComponentCode = inputComponentCode;
        this.outputComponentCode = outputComponentCode;
        this.pointType = pointType;
        this.pointJson = pointJson;
    }

    public DataModelComponent(Long inputComponentId, String inputComponentCode, Long outputComponentId, String outputComponentCode, String pointType, String pointJson) {
        this.inputComponentId = inputComponentId;
        this.inputComponentCode = inputComponentCode;
        this.outputComponentId = outputComponentId;
        this.outputComponentCode = outputComponentCode;
        this.pointType = pointType;
        this.pointJson = pointJson;
    }

    /**
     * 关系id
     */
    private Long mcId;
    /**
     * 模型id
     */
    private Long modelId;
    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 输入组件id
     */
    private Long inputComponentId;
    /**
     * 输入组件code
     */
    private String inputComponentCode;
    /**
     * 输出组件id
     */
    private Long outputComponentId;
    /**
     * 输出组件code
     */
    private String outputComponentCode;
    /**
     * 指向类型  直线、曲线等
     */
    private String pointType;
    /**
     * 指向json
     */
    private String pointJson;
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
