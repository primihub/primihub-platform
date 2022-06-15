package com.primihub.biz.entity.sys.po;

import lombok.Data;

import java.util.Date;

/**
 * 日志表
 */
@Data
public class SysLog {
    /**
     * 操作日志id
     */
    private Long id;
    /**
     * 日志类型 1.登录 2.新增 3.修改 4.删除
     */
    private Integer logType;
    /**
     * 请求参数
     */
    private String logParam;
    /**
     * 返回结构
     */
    private String logResult;
    /**
     * 结果码
     */
    private Integer logCode;
    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    private Date cTime;
    /**
     * 修改时间
     */
    private Date uTime;

}
