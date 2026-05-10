package com.primihub.biz.entity.base;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseResultEntityTest {

    @Test
    void success_shouldCreateWithCodeZero() {
        BaseResultEntity result = BaseResultEntity.success();

        assertThat(result.getCode()).isZero();
        assertThat(result.getMsg()).isEqualTo("请求成功");
    }

    @Test
    void success_shouldAcceptResult() {
        BaseResultEntity<String> result = BaseResultEntity.success("hello");

        assertThat(result.getCode()).isZero();
        assertThat(result.getResult()).isEqualTo("hello");
    }

    @Test
    void success_shouldAcceptResultAndExtra() {
        BaseResultEntity<String> result = BaseResultEntity.success("data", "extraInfo");

        assertThat(result.getCode()).isZero();
        assertThat(result.getResult()).isEqualTo("data");
        assertThat(result.getExtra()).isEqualTo("extraInfo");
    }

    @Test
    void failure_shouldCreateWithErrorCode() {
        BaseResultEntity result = BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM);

        assertThat(result.getCode()).isEqualTo(100);
        assertThat(result.getMsg()).contains("缺少参数");
    }

    @Test
    void failure_shouldAppendExtraInfo() {
        BaseResultEntity result = BaseResultEntity.failure(BaseResultEnum.CAN_NOT_ALTER, "该记录不存在");

        assertThat(result.getCode()).isEqualTo(104);
        assertThat(result.getMsg()).contains("无法修改");
        assertThat(result.getMsg()).contains("该记录不存在");
    }

    @Test
    void constructor_shouldAcceptResultEnum() {
        BaseResultEntity result = new BaseResultEntity(BaseResultEnum.TOKEN_INVALIDATION);

        assertThat(result.getCode()).isEqualTo(102);
    }

    @Test
    void constructor_shouldAcceptResult() {
        BaseResultEntity<String> result = new BaseResultEntity<>("test");

        assertThat(result.getCode()).isZero();
        assertThat(result.getResult()).isEqualTo("test");
    }
}
