package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

@Data
public class DataComponent {
    /**
     * 组件id
     */
    private Long componentId;
    /**
     * 前端组件id
     */
    private String frontComponentId;
    /**
     * 模型id
     */
    private Long modelId;
    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 组件code
     */
    private String componentCode;
    /**
     * 组件名称
     */
    private String componentName;
    /**
     * 形状
     */
    private String shape;
    /**
     * 宽度
     */
    private Integer width;
    /**
     * 高度
     */
    private Integer height;
    /**
     * 坐标y
     */
    private Integer coordinateY;
    /**
     * 坐标x
     */
    private Integer coordinateX;
    /**
     * 组件参数json
     */
    private String dataJson;
    /**
     * 开始时间
     */
    private Long startTime;
    /**
     * 结束时间
     */
    private Long endTime;
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
    /**
     * 0初始 1成功 2运行中 3失败
     */
    private Integer componentState;

    public Long getTimeConsuming(){
        if ((startTime!=null&&startTime!=0L)&&(endTime!=null&&endTime!=0L)){
            return (endTime-startTime)/1000;
        }else if (startTime!=null&&startTime!=0L){
            return (System.currentTimeMillis()-startTime)/1000;
        }
        return 0L;
    }

}
