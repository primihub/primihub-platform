package com.primihub.biz.entity.sys.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class SysUserLoginVO {
    private Long userId;
    private String userAccount;
    private String userName;
    private boolean forbid;
}
