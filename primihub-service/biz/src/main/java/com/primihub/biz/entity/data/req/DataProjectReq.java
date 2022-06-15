package com.primihub.biz.entity.data.req;

import lombok.Data;
import java.util.List;

@Data
public class DataProjectReq extends PageReq {
    private String projectName;
    private String projectDesc;
    private List<Long> resources;
}
