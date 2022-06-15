package com.yyds.biz.entity.sys.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yyds.biz.entity.sys.po.SysOrgan;
import lombok.Data;

import java.util.Date;

/**
 * 机构表
 */
@Data
public class SysOrganListVO extends SysOrgan {

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date cTime;
}
