package com.primihub.biz.util.crypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**时间相关操作*/
public class DateUtil {

    private static Logger log = LoggerFactory.getLogger(DateUtil.class);

    public enum DateStyle{
        TIME_FORMAT_SHORT("yyyyMMddHHmmss","\\d{14}"),
        TIME_FORMAT_NORMAL("yyyy-MM-dd HH:mm:ss","\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}"),
        TIME_FORMAT_ENGLISH("MM/dd/yyyy HH:mm:ss","\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}"),

        DATE_FORMAT_MAST_SHORT("yyyy-MM","\\d{4}-\\d{1,2}"),
        DATE_FORMAT_SHORT("yyyyMMdd","\\d{8}"),
        DATE_FORMAT_NORMAL("yyyy-MM-dd","\\d{4}-\\d{1,2}-\\d{1,2}"),
        DATE_FORMAT_ENGLISH("MM/dd/yyyy","\\d{1,2}/\\d{1,2}/\\d{4}"),

        MINUTE_FORMAT_SHORT("yyyyMMddHHmm","\\d{12}"),
        MINUTE_FORMAT_NORMAL("yyyy-MM-dd HH:mm","\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}"),
        MINUTE_FORMAT_ENGLISH("MM/dd/yyyy HH:mm","\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}"),

        HOUR_FORMAT_SHORT("yyyyMMddHH","\\d{10}"),
        HOUR_FORMAT_NORMAL("yyyy-MM-dd HH","\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}"),
        HOUR_FORMAT_ENGLISH("MM/dd/yyyy HH","\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}"),


        TIME_JAVA_DEFAULT("EEE MMM dd HH:mm:ss Z yyyy","0"),
        UTC_DEFAULT("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'","0"),
        ;

        private String format;
        private String regex;

        DateStyle(String format, String regex) {
            this.format = format;
            this.regex = regex;
        }

        public String getFormat() {
            return format;
        }

        public String getRegex() {
            return regex;
        }
    }


    private DateUtil() {
    }

    /**
     * 格式化一个日期数据.
     *
     * @param date 需要格式的时间类型
     * @param formatStr 自定义格式
     *
     * @return string
     */
    public static String formatDate(Date date, String formatStr) {
        return new SimpleDateFormat(formatStr).format(date);
    }

    /**
     * 按自定义解析字符串为日期.
     *
     * @param dateStr 日期字符串
     * @param formatStr 自定义格式
     *
     * @return date
     */
    public static Date parseDate(String dateStr,String formatStr) {
        try {
            return new SimpleDateFormat(formatStr).parse(dateStr);
        } catch (ParseException e) {
            log.info("parseDate",e);
        }
        return null;
    }

    /**
     * 按自定义解析字符串为日期.
     *
     * @param dateStr 日期字符串
     *
     * @return date
     */
    public static Date parseDate(String dateStr) {
        DateFormat fmt;
        if (dateStr.matches(DateStyle.TIME_FORMAT_SHORT.getRegex())) {
            fmt = new SimpleDateFormat(DateStyle.TIME_FORMAT_SHORT.getFormat());
        } else if (dateStr.matches(DateStyle.TIME_FORMAT_NORMAL.getRegex())) {
            fmt = new SimpleDateFormat(DateStyle.TIME_FORMAT_NORMAL.getFormat());
        } else if (dateStr.matches(DateStyle.TIME_FORMAT_ENGLISH.getRegex())) {
            fmt = new SimpleDateFormat(DateStyle.TIME_FORMAT_ENGLISH.getFormat());
        } else if (dateStr.matches(DateStyle.DATE_FORMAT_SHORT.getRegex())) {
            fmt = new SimpleDateFormat(DateStyle.DATE_FORMAT_SHORT.getFormat());
        } else if (dateStr.matches(DateStyle.DATE_FORMAT_NORMAL.getRegex())) {
            fmt = new SimpleDateFormat(DateStyle.DATE_FORMAT_NORMAL.getFormat());
        } else if (dateStr.matches(DateStyle.DATE_FORMAT_ENGLISH.getRegex())) {
            fmt = new SimpleDateFormat(DateStyle.DATE_FORMAT_ENGLISH.getFormat());
        } else if (dateStr.matches(DateStyle.DATE_FORMAT_MAST_SHORT.getRegex())){
            fmt = new SimpleDateFormat(DateStyle.DATE_FORMAT_MAST_SHORT.getFormat());
        }
        else{
            fmt = new SimpleDateFormat(DateStyle.TIME_JAVA_DEFAULT.getFormat(),Locale.UK);
        }
        try {
            return fmt.parse(dateStr);
        } catch (ParseException e) {
            log.info("parseDate",e);
        }
        return null;
    }

    /**
     * 改变时间 譬如： changeDate(new Date(),Calendar.DATE, 2) 就是把当前时间加两天
     *
     * @param originDate 原始时间
     * @param calendarField 改变类型(取值为Calendar的取值)
     * @param distance 长度
     *
     * @return 改变后的时间
     */
    public static Date changeDate(Date originDate, int calendarField, int distance) {
        Calendar c = Calendar.getInstance(Locale.CHINA);
        c.setTime(originDate);
        c.add(calendarField, distance);
        return c.getTime();
    }

    /**
     * @title 把UTC格式的字符串转换成其他格式的字符串
     * @author leevy
     * @param dateStr
     * @param format
     * @return
     * @throws ParseException
     */
    public static String UTCToString(String dateStr,String format) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = formatter.parse(dateStr);
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        String sDate=sdf.format(date);
        return sDate;

    }
    /**
     * @title 获取下个月字符串
     * @author leevy
     * @param dateStr
     * @param format
     * @return
     * @throws ParseException
     */
    public static String getNextMonthStr(String dateStr,String format) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        Date date = formatter.parse(dateStr);
        date = changeDate(date,2,1);
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        String sDate=sdf.format(date);
        return sDate;

    }
    /**
     * @title 获取上个月字符串
     * @author leevy
     * @param dateStr
     * @param format
     * @return
     * @throws ParseException
     */
    public static String getLastMonthStr(String dateStr,String format) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        Date date = formatter.parse(dateStr);
        date = changeDate(date,2,-1);
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        String sDate=sdf.format(date);
        return sDate;

    }

    /**
     * 比较两个时间格式的字符串(第二个时间是否比一个时间大)
     * @param dateStr1  字符串1
     * @param format1   时间格式1
     * @param dateStr2  字符串2
     * @param format2   时间格式2
     * @return 默认值false
     * @throws ParseException
     */
    public static Boolean getResultOFCompareTime(String dateStr1,String format1,String dateStr2,String format2) throws ParseException {
        Boolean result = false;
        SimpleDateFormat formatter1 = new SimpleDateFormat(format1);
        Date date1= formatter1.parse(dateStr1);
        SimpleDateFormat formatter2 = new SimpleDateFormat(format2);
        Date date2= formatter2.parse(dateStr2);
        if(date1.getTime() < date2.getTime()){
            result = true;
        }
        return result;
    }
    public static String getMinute(int second){
        String time = "";
        int minute = 0;
        if (second>60){
            minute = second/60;
            second = second%60;
        }
        time = ""+minute+":"+second;
        return time;
    }
//    获取当前日期的在当前月的是第几天
    public static int getDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance(Locale.CHINA);
        c.setTime(date);
        return c.DAY_OF_MONTH;
    }
}
