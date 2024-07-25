package com.primihub.biz.entity.data.req;

import com.primihub.biz.entity.data.base.DataPirKeyQuery;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class DataPirCopyReq implements Serializable {
    /** 营销分模型 */
    private String scoreModelType;
    private String taskName;
    private String psiRecordId;
    private String pirRecordId;

    /** idNum */
    private Set<String> targetValueSet;

    /** field needed by async */
    private Long dataTaskId;
    private Long dataPirTaskId;
    private String resourceColumnNames;
    private List<DataPirKeyQuery> dataPirKeyQueries;

    // return
    private String targetResourceId;
    private String targetOrganId;
    private String originOrganId;
    private String originResourceId;

    /** 任务状态 */
    private Integer taskState;

    private String targetField;
}
