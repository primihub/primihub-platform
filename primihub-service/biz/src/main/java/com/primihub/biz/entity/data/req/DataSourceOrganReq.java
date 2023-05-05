package com.primihub.biz.entity.data.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceOrganReq {
    private String organGlobalId;
    private String organName;

}
