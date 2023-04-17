package com.primihub.biz.util.crypt;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**签名加密*/
public class SignUtil {

    private static Logger log = LoggerFactory.getLogger(SignUtil.class);

    private SignUtil() {
    }

    public static final String DEFAULT_ENCODE="utf-8";

    public enum CaseStyle{
        UPPER,
        LOWER,
        ;
    }

    public enum DigestType {
        SHA1("SHA1"),
        SHA256("SHA-256"),
        SHA512("SHA-512"),
        MD5("MD5"),
        ;

        private String shaInstance;

        DigestType(String shaInstance) {
            this.shaInstance = shaInstance;
        }

        public String getShaInstance() {
            return shaInstance;
        }
    }

    /**
     * 获取MD5值 大写 默认utf-8
     * @param str 加密字符串
     * @return 32位16进制MD5值
     */
    public static String getMD5ValueUpperCaseByDefaultEncode(String str){
        return getDigestValue(str, DigestType.MD5,DEFAULT_ENCODE, CaseStyle.UPPER);
    }

    /**
     * 获取MD5值 大写 默认utf-8
     * @param str 加密字符串
     * @return 32位16进制MD5值
     */
    public static String getMD5ValueLowerCaseByDefaultEncode(String str){
        return getDigestValue(str, DigestType.MD5,DEFAULT_ENCODE, CaseStyle.LOWER);
    }

    /**
     * 获取SHA512值 小写 默认utf-8
     * @param str 加密字符串
     * @return 获取128位SHA1值
     */
    public static String getSha512ValueLowerCaseByDefaultEncode(String str){
        return getDigestValue(str, DigestType.SHA512,DEFAULT_ENCODE, CaseStyle.LOWER);
    }

    /**
     * 获取SHA512值 大写 默认utf-8
     * @param str 加密字符串
     * @return 获取128位SHA1值
     */
    public static String getSha512ValueUpperCaseByDefaultEncode(String str){
        return getDigestValue(str, DigestType.SHA512,DEFAULT_ENCODE, CaseStyle.UPPER);
    }

    /**
     * 获取SHA256值 小写 默认utf-8
     * @param str 加密字符串
     * @return 获取64位SHA1值
     */
    public static String getSha256ValueLowerCaseByDefaultEncode(String str){
        return getDigestValue(str, DigestType.SHA256,DEFAULT_ENCODE, CaseStyle.LOWER);
    }

    /**
     * 获取SHA256值 大写 默认utf-8
     * @param str 加密字符串
     * @return 获取64位SHA1值
     */
    public static String getSha256ValueUpperCaseByDefaultEncode(String str){
        return getDigestValue(str, DigestType.SHA256,DEFAULT_ENCODE, CaseStyle.UPPER);
    }

    /**
     * 获取SHA1值 小写 默认utf-8
     * @param str 加密字符串
     * @return 获取40位SHA1值
     */
    public static String getSha1ValueLowerCaseByDefaultEncode(String str){
        return getDigestValue(str, DigestType.SHA1,DEFAULT_ENCODE, CaseStyle.LOWER);
    }

    /**
     * 获取SHA1值 大写 默认utf-8
     * @param str 加密字符串
     * @return 获取40位SHA1值
     */
    public static String getSha1ValueUpperCaseByDefaultEncode(String str){
        return getDigestValue(str, DigestType.SHA1,DEFAULT_ENCODE, CaseStyle.UPPER);
    }

    /**
     * 获取加密值
     * @param str str 加密字符串
     * @param digestType sha实例
     * @param encoding 编码
     * @param caseStyle 大小写
     * @return 返回所选实例得加密值
     */
    public static String getDigestValue(String str, DigestType digestType, String encoding, CaseStyle caseStyle){
        MessageDigest messagedigest;
        try {
            messagedigest = MessageDigest.getInstance(digestType.getShaInstance());
            messagedigest.update(str.getBytes(encoding));
        }catch (Exception e){
            log.error("getDigestValue",e);
            return null;
        }
        byte[] digestBytes = messagedigest.digest();
        String value= Hex.encodeHexString(digestBytes);
        if(CaseStyle.UPPER.equals(caseStyle)) {
            return value.toUpperCase();
        }
        return value;
    }

}
