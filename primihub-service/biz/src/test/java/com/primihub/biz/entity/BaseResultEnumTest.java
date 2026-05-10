package com.primihub.biz.entity;

import com.primihub.biz.entity.base.BaseResultEnum;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BaseResultEnumTest {

    @Test
    void success_shouldHaveCodeZero() {
        assertThat(BaseResultEnum.SUCCESS.getReturnCode()).isZero();
        assertThat(BaseResultEnum.SUCCESS.getMessage()).isEqualTo("请求成功");
    }

    @Test
    void failure_shouldHaveCodeMinusOne() {
        assertThat(BaseResultEnum.FAILURE.getReturnCode()).isEqualTo(-1);
    }

    @Test
    void codes_shouldBeUnique() {
        Set<Integer> codes = new HashSet<>();
        for (BaseResultEnum e : BaseResultEnum.values()) {
            assertThat(codes.add(e.getReturnCode()))
                    .as("Duplicate code %d in %s", e.getReturnCode(), e.name())
                    .isTrue();
        }
    }
}
