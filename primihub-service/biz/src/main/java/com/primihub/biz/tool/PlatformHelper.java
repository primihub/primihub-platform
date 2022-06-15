package com.primihub.biz.tool;

import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.biz.util.crypt.SignUtil;
import org.apache.commons.lang.RandomStringUtils;

import java.util.Date;

public class PlatformHelper {
    public static String generateOwnToken(String prefix,Long sequence,Date date){
        String dt= DateUtil.formatDate(date,DateUtil.DateStyle.TIME_FORMAT_SHORT.getFormat());
        String randomStr=RandomStringUtils.randomAlphanumeric(10);
        String sequenceStr= SignUtil.getMD5ValueUpperCaseByDefaultEncode(randomStr+sequence);
        String token=prefix+dt+sequenceStr;
        return token;
    }
}
