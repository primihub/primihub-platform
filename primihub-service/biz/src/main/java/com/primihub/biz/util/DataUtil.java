package com.primihub.biz.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtil {

    private static final String MYSQL_PATTERN="jdbc:(?<type>[a-z]+)://(?<host>[a-zA-Z0-9-//.]+):(?<port>[0-9]+)/(?<database>[a-zA-Z0-9_//-]+)?";

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+){0,4}@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+){0,4}$";

    public static Map<String,String> getJDBCData(String url){
        Map<String,String> map = new HashMap<>();
        Pattern namePattern = Pattern.compile(MYSQL_PATTERN);
        Matcher dateMatcher = namePattern.matcher(url);
        while (dateMatcher.find()) {
            map.put("type",dateMatcher.group("type"));
            map.put("host",dateMatcher.group("host"));
            map.put("port",dateMatcher.group("port"));
            map.put("database",dateMatcher.group("database"));
        }
        return map;
    }

    public static boolean isEmail(String email){
        return Pattern.compile(EMAIL_PATTERN).matcher(email).find();
    }
}
