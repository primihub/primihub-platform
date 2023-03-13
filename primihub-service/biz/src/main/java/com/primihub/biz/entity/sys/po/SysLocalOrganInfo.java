package com.primihub.biz.entity.sys.po;

import com.primihub.biz.tool.nodedata.AddressInfoEntity;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SysLocalOrganInfo {
    private String organId;
    private String organName;
    private String pinCode;
    private String gatewayAddress;

    private Map<String,SysOrganFusion> fusionMap;
    private List<SysOrganFusion> fusionList;
    private AddressInfoEntity addressInfo;

    private Map<String,Object> homeMap;
}
