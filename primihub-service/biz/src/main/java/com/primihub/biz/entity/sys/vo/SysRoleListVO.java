package com.primihub.biz.entity.sys.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.primihub.biz.entity.sys.po.SysRole;
import lombok.Data;

import java.util.Date;

@Data
public class SysRoleListVO extends SysRole {
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cTime;
}
