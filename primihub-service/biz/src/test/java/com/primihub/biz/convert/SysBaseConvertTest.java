package com.primihub.biz.convert;

import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.entity.sys.vo.SysOrganVO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SysBaseConvertTest {

    @Test
    void sysOrganConvertVo_shouldMapFields() {
        SysOrgan organ = new SysOrgan();
        organ.setOrganId("organ1");
        organ.setOrganName("TestOrgan");

        SysOrganVO result = SysBaseConvert.SysOrganConvertVo(organ);

        assertThat(result.getGlobalId()).isEqualTo("organ1");
        assertThat(result.getGlobalName()).isEqualTo("TestOrgan");
    }
}
