package com.primihub.biz.util.crypt;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilTest {

    @Test
    void formatDate_shouldFormatCorrectly() {
        Date date = new Date(0); // 1970-01-01 00:00:00 UTC

        String result = DateUtil.formatDate(date, "yyyy-MM-dd");

        assertThat(result).isEqualTo("1970-01-01");
    }

    @Test
    void parseDate_withFormat_shouldParseCorrectly() {
        Date result = DateUtil.parseDate("2024-01-15", "yyyy-MM-dd");

        assertThat(result).isNotNull();
        String formatted = DateUtil.formatDate(result, "yyyy-MM-dd");
        assertThat(formatted).isEqualTo("2024-01-15");
    }

    @Test
    void parseDate_withoutFormat_shouldAutoDetectTimeNormal() {
        Date result = DateUtil.parseDate("2024-01-15 10:30:00");

        assertThat(result).isNotNull();
    }

    @Test
    void parseDate_withoutFormat_shouldAutoDetectDateNormal() {
        Date result = DateUtil.parseDate("2024-01-15");

        assertThat(result).isNotNull();
    }

    @Test
    void parseDate_withoutFormat_shouldAutoDetectDateShort() {
        Date result = DateUtil.parseDate("20240115");

        assertThat(result).isNotNull();
    }

    @Test
    void parseDate_withoutFormat_shouldAutoDetectTimeShort() {
        Date result = DateUtil.parseDate("20240115103000");

        assertThat(result).isNotNull();
    }

    @Test
    void parseDate_withInvalidFormat_shouldReturnNull() {
        Date result = DateUtil.parseDate("not-a-date", "yyyyMMdd");

        assertThat(result).isNull();
    }

    @Test
    void changeDate_shouldAddDays() {
        Date original = DateUtil.parseDate("2024-01-15", "yyyy-MM-dd");
        assertThat(original).isNotNull();

        Date result = DateUtil.changeDate(original, Calendar.DATE, 5);

        String formatted = DateUtil.formatDate(result, "yyyy-MM-dd");
        assertThat(formatted).isEqualTo("2024-01-20");
    }

    @Test
    void changeDate_shouldSubtractDays() {
        Date original = DateUtil.parseDate("2024-01-15", "yyyy-MM-dd");
        assertThat(original).isNotNull();

        Date result = DateUtil.changeDate(original, Calendar.DATE, -3);

        String formatted = DateUtil.formatDate(result, "yyyy-MM-dd");
        assertThat(formatted).isEqualTo("2024-01-12");
    }

    @Test
    void changeDate_shouldAddMonths() {
        Date original = DateUtil.parseDate("2024-01-31", "yyyy-MM-dd");
        assertThat(original).isNotNull();

        Date result = DateUtil.changeDate(original, Calendar.MONTH, 1);

        String formatted = DateUtil.formatDate(result, "yyyy-MM");
        assertThat(formatted).isEqualTo("2024-02");
    }

    @Test
    void utcToString_shouldConvertCorrectly() throws ParseException {
        String result = DateUtil.UTCToString("2024-01-15T10:30:00", "yyyy-MM-dd");

        assertThat(result).isEqualTo("2024-01-15");
    }

    @Test
    void getNextMonthStr_shouldWork() throws ParseException {
        String result = DateUtil.getNextMonthStr("2024-01", "yyyy-MM");

        assertThat(result).isEqualTo("2024-02");
    }

    @Test
    void getLastMonthStr_shouldWork() throws ParseException {
        String result = DateUtil.getLastMonthStr("2024-03", "yyyy-MM");

        assertThat(result).isEqualTo("2024-02");
    }

    @Test
    void getResultOFCompareTime_shouldReturnTrueWhenFirstIsEarlier() throws ParseException {
        boolean result = DateUtil.getResultOFCompareTime(
                "2024-01-01", "yyyy-MM-dd",
                "2024-01-15", "yyyy-MM-dd"
        );

        assertThat(result).isTrue();
    }

    @Test
    void getResultOFCompareTime_shouldReturnFalseWhenFirstIsLater() throws ParseException {
        boolean result = DateUtil.getResultOFCompareTime(
                "2024-01-15", "yyyy-MM-dd",
                "2024-01-01", "yyyy-MM-dd"
        );

        assertThat(result).isFalse();
    }

    @Test
    void getDayOfMonth_shouldReturnCorrectDay() {
        Date date = DateUtil.parseDate("2024-03-15", "yyyy-MM-dd");
        assertThat(date).isNotNull();

        int result = DateUtil.getDayOfMonth(date);

        assertThat(result).isEqualTo(15);
    }

    @Test
    void getMinute_shouldConvertSeconds() {
        assertThat(DateUtil.getMinute(0)).isEqualTo("0:0");
        assertThat(DateUtil.getMinute(30)).isEqualTo("0:30");
        assertThat(DateUtil.getMinute(60)).isEqualTo("1:0");
        assertThat(DateUtil.getMinute(61)).isEqualTo("1:1");
        assertThat(DateUtil.getMinute(150)).isEqualTo("2:30");
    }
}
