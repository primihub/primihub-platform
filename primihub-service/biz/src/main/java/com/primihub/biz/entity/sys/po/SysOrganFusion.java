package com.primihub.biz.entity.sys.po;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SysOrganFusion {
    private String serverAddress;
    private boolean registered;
    private boolean show;
}
