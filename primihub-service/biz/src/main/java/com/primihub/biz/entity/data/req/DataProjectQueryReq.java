package com.primihub.biz.entity.data.req;

import com.primihub.biz.util.crypt.DateUtil;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DataProjectQueryReq extends PageReq {
    /**
     * 查询类型
     * 0 全部
     * 1 我发起的
     * 2 我协作的
     */
    private Integer queryType = 0;
    /**
     * 本机构ID
     */
    private String ownOrganId;
    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 机构ID
     */
    private String organId;

    /**
     * 机构项目中参与身份 1发起者 2协作者
     */
    private Integer participationIdentity;

    /**
     * 项目状态
     * 0审核中
     * 1可用
     *  可用扩展1、  11 全部可用
     *  可用扩展2、  12 部分可用
     * 2关闭
     */
    private Integer status;
    /**
     * 开始时间
     */
    private String startDate;
    /**
     * 结束时间
     */
    private String endDate;
}
