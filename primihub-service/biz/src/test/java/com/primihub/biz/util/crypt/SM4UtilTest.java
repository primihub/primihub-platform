package com.primihub.biz.util.crypt;


import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public class SM4UtilTest {

    public static final String secretId = "58644534";
    public static final String secretKey = "cDVMxEGRC9sMPZP1xtKnGWShi2YqwWW9yoEBoqo5PS0=";
    public static final String signTem = "requestRefId=<id>&secretId=<secretId>";

    // requestRefId=SJSREQ_201601010809108632A&secretId=KFZQpn74WFkmLPx3gnP

    public static void main(String[] args) {
        /*String s = test1("SJSREQ_201601010809108632A", secretId);
        System.out.println("sign 加密后: " + s);

        Map<String, Object> map = new HashMap<String, Object>();

        String sm3PhoneNum = SM3Util.encrypt("15871801048");
        map.put("param", new HashMap<String, String>() {{
            put("mobile", sm3PhoneNum);
            put("empowerNo", String.valueOf(System.currentTimeMillis()));
        }});
        String s1 = test2(map);
        System.out.println("参数加密后: " + s1);*/
        try {
            test3("a684c92f509bafed25b28785b61793b35dcc0e25f567d3d58b1420619cb6427d");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String test1(String requestRefId, String secretId) {
        String replace = signTem.replace("<id>", requestRefId);
        String replace1 = replace.replace("<secretId>", secretId);
        System.out.println("sign: " + replace1);
        String encrypt = SM3Util.encrypt(replace1, secretKey);
        return encrypt;
    }

    // 参数 SM4 加密
    public static String test2(Map<String, Object> map) {
        String mapJson = JSONObject.toJSONString(map);
        System.out.println("mapJson: " + mapJson);
        return SM4Util.encrypt(mapJson, secretKey);
    }

    public static void test3(String cipherStr) throws Exception {
        String decrypt = SM4Util.decrypt(cipherStr, secretKey);
        System.out.println(decrypt);
    }


}