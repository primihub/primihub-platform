package com.primihub.biz.util.crypt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignUtilTest {

    private static final String TEST_STRING = "HelloWorld";

    @Test
    void getMD5ValueUpperCaseByDefaultEncode_shouldReturn32Chars() {
        String result = SignUtil.getMD5ValueUpperCaseByDefaultEncode(TEST_STRING);

        assertThat(result).hasSize(32);
        assertThat(result).isUpperCase();
    }

    @Test
    void getMD5ValueLowerCaseByDefaultEncode_shouldReturn32Chars() {
        String result = SignUtil.getMD5ValueLowerCaseByDefaultEncode(TEST_STRING);

        assertThat(result).hasSize(32);
        assertThat(result).isLowerCase();
    }

    @Test
    void md5_shouldBeDeterministic() {
        String result1 = SignUtil.getMD5ValueUpperCaseByDefaultEncode(TEST_STRING);
        String result2 = SignUtil.getMD5ValueUpperCaseByDefaultEncode(TEST_STRING);

        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void md5_shouldDifferForDifferentInputs() {
        String result1 = SignUtil.getMD5ValueUpperCaseByDefaultEncode("abc");
        String result2 = SignUtil.getMD5ValueUpperCaseByDefaultEncode("abcd");

        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void getSha1ValueLowerCaseByDefaultEncode_shouldReturn40Chars() {
        String result = SignUtil.getSha1ValueLowerCaseByDefaultEncode(TEST_STRING);

        assertThat(result).hasSize(40);
        assertThat(result).isLowerCase();
    }

    @Test
    void getSha256ValueUpperCaseByDefaultEncode_shouldReturn64Chars() {
        String result = SignUtil.getSha256ValueUpperCaseByDefaultEncode(TEST_STRING);

        assertThat(result).hasSize(64);
        assertThat(result).isUpperCase();
    }

    @Test
    void getSha512ValueLowerCaseByDefaultEncode_shouldReturn128Chars() {
        String result = SignUtil.getSha512ValueLowerCaseByDefaultEncode(TEST_STRING);

        assertThat(result).hasSize(128);
        assertThat(result).isLowerCase();
    }

    @Test
    void getDigestValue_shouldHandleUpperAndLowerCases() {
        String lower = SignUtil.getDigestValue(TEST_STRING, SignUtil.DigestType.MD5, "utf-8", SignUtil.CaseStyle.LOWER);
        String upper = SignUtil.getDigestValue(TEST_STRING, SignUtil.DigestType.MD5, "utf-8", SignUtil.CaseStyle.UPPER);

        assertThat(lower).isEqualTo(upper.toLowerCase());
    }

    @Test
    void differentDigestTypesShouldProduceDifferentLengths() {
        String md5 = SignUtil.getMD5ValueUpperCaseByDefaultEncode(TEST_STRING);
        String sha1 = SignUtil.getSha1ValueUpperCaseByDefaultEncode(TEST_STRING);
        String sha256 = SignUtil.getSha256ValueUpperCaseByDefaultEncode(TEST_STRING);
        String sha512 = SignUtil.getSha512ValueUpperCaseByDefaultEncode(TEST_STRING);

        assertThat(md5).hasSize(32);
        assertThat(sha1).hasSize(40);
        assertThat(sha256).hasSize(64);
        assertThat(sha512).hasSize(128);
    }

    @Test
    void emptyStringShouldProduceValidHash() {
        String result = SignUtil.getMD5ValueUpperCaseByDefaultEncode("");

        assertThat(result).hasSize(32);
        assertThat(result).isUpperCase();
    }
}
