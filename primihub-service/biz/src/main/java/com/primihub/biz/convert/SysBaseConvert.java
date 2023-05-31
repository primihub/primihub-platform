package com.primihub.biz.convert;

import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.entity.sys.vo.SysOrganVO;

public class SysBaseConvert {



    public static SysOrganVO SysOrganConvertVo(SysOrgan sysOrgan){
        SysOrganVO sysOrganVO = new SysOrganVO();
        sysOrganVO.setGlobalId(sysOrgan.getOrganId());
        sysOrganVO.setGlobalName(sysOrgan.getOrganName());
        return sysOrganVO;

    }
}
