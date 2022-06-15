package com.primihub.biz.entity.sys.po;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SysLocalOrganInfo {
    private String organId;
    private String organName;
    private String pinCode;

    private Map<String,SysOrganFusion> fusionMap;
    private List<SysOrganFusion> fusionList;
}
