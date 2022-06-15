package com.primihub.biz.entity.feign;

import com.primihub.biz.entity.data.req.DataModelAndComponentReq;
import lombok.Data;

import java.util.List;

@Data
public class FedlearnerJobApi extends DataModelAndComponentReq {
    private List<FedlearnerJobApiResource> resources ;
}
