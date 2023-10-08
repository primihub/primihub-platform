package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataPsiQueryReq extends PageReq{
    private String resultName;
    private String organId;
    private String taskName;
    private Integer taskState;
    private String startDate;
    private String endDate;
}
