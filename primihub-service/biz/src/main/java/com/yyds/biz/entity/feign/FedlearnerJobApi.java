package com.yyds.biz.entity.feign;

import com.yyds.biz.entity.data.req.DataModelAndComponentReq;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FedlearnerJobApi extends DataModelAndComponentReq {
    private List<FedlearnerJobApiResource> resources ;
}
