package com.primihub.biz.entity.data.req;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class DataPirCopyReq {
    /** 营销分模型 */
    private String scoreModelType;
    private String taskName;
    private String psiRecordId;

    /** idNum */
    private Set<String> targetValueSet;


    // return
    private String targetResourceId;
    private String originOrganId;
}
