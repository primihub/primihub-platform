package com.primihub.biz.util.crypt;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Base64;

public class SM3Util {

    /**
     * SM3参数加密方法
     * @param message 要加密的信息
     * @param secret  秘钥
     * @return
     */
    public static String encrypt(String message, String secret) {
        String signature = null;
        KeyParameter keyParameter;
        try {
            keyParameter = new KeyParameter(secret.getBytes("UTF-8"));
            SM3Digest digest = new SM3Digest();
            HMac mac = new HMac(digest);
            mac.init(keyParameter);
            mac.update(message.getBytes("UTF-8"), 0, message.length());
            byte[] byteSM3 = new byte[mac.getMacSize()];
            mac.doFinal(byteSM3, 0);
            Base64.Encoder encoder = Base64.getEncoder();
            signature = encoder.encodeToString(byteSM3);
        } catch (UnsupportedEncodingException e) {
            System.out.println("getSignatureBySM3 error :");
            e.printStackTrace();
        }
        return signature;
    }

    private static final String ENCODING = "UTF-8";
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     *
     * sm3算法加密
     * @explain
     * @param paramStr 待加密字符串
     * @return 返回加密后结果
     */
    public static String encrypt(String paramStr) {

        // 将返回的hash值转换成16进制字符串
        String resultHexString = "";
        try {
            // 将字符串转换成byte数组
            byte[] srcData = paramStr.getBytes(ENCODING);
            // 调用hash()
            byte[] resultHash = hash(srcData);
            // 将返回的hash值转换成16进制字符串
//			resultHexString = ByteUtils.toHexString(resultHash);
            Base64.Encoder encoder = Base64.getEncoder();
            resultHexString = encoder.encodeToString(resultHash);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return resultHexString;

    }

    /**
     * 返回长度=32的byte数组
     * @explain 生成对应的hash值
     * @param srcData
     * @return byte[]
     *
     */
    public static byte[] hash(byte[] srcData) {

        SM3Digest digest = new SM3Digest();
        digest.update(srcData, 0, srcData.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }
}
