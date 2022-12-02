package com.primihub.biz.entity.data.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class DataComponentVo {
    private Long componentId;
    private String componentCode;
    private String componentName;
    /**
     * 开始时间
     */
    @JsonIgnore
    private Long startTime;
    /**
     * 结束时间
     */
    @JsonIgnore
    private Long endTime;

    /**
     * 0初始 1成功 2运行中 3失败
     */
    private Integer componentState;

    public boolean isComplete(){
        return (startTime!=null&&startTime!=0L)&&(endTime!=null&&endTime!=0L);
    }

    public Long getTimeConsuming(){
        if ((startTime!=null&&startTime!=0L)&&(endTime!=null&&endTime!=0L)){
            return endTime-startTime;// j815
        }else if (startTime!=null&&startTime!=0L){
            return System.currentTimeMillis()-startTime;
        }
        return 0L;
    }
}
