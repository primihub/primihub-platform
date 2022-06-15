package com.primihub.biz.entity.sys.vo;

import com.primihub.biz.entity.sys.po.SysAuth;
import lombok.Data;

import java.util.List;

@Data
public class SysAuthNodeVO extends SysAuth {
    /**
     * 子节点
     */
    private List<SysAuthNodeVO> children;
    /**
     * 是否授权
     */
    private int isGrant;
}
