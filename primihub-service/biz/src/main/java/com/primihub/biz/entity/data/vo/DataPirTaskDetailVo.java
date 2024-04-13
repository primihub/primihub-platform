package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 匿踪查询任务详情VO
 */
@Data
public class DataPirTaskDetailVo {
    /** 任务名称 */
    private String taskName;
    /** 任务全局唯一ID */
    private String taskIdName;
    /** 任务状态:
     * INIT(0,"初始未开始"),
     * SUCCESS(1,"成功"),
     * IN_OPERATION(2,"运行中"),
     * FAIL(3,"失败"),
     * CANCEL(4,"取消"),
     * DELETE(5,"删除") 
     */
    private Integer taskState;
    /** 机构名称 */
    private String organName;
    /** 资源名称 */
    private String resourceName;
    /** 资源ID */
    private String resourceId;
    /** 查询内容 */
    private String retrievalId;
    /** 任务错误信息 */
    private String taskError;
    /** 任务创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /** 创建时间 */
    private Date createDate;
    /**
     * 任务开始时间
     */
    private Long taskStartTime;
    /**
     * 任务结束时间
     */
    private Long taskEndTime;
    /** 结果文件示例数据(最多50条) */
    private List<LinkedHashMap<String, Object>> list;

    /** 总耗时(秒) */
    public Long getConsuming() {
        if (taskStartTime==null) {
            return 0L;
        }
        if (taskEndTime==null||taskEndTime==0) {
            return (System.currentTimeMillis() - taskStartTime)/1000;
        }
        return (taskEndTime - taskStartTime)/1000;
    }
}
