package com.yyds.biz.entity.data.req;

import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class DataProjectReq extends PageReq {
    private String projectName;
    private String projectDesc;
    private List<Long> resources;
}
