package com.primihub.biz.util.crypt;

import com.google.common.io.BaseEncoding;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

@Slf4j
public class CryptUtil {
    private CryptUtil(){

    }

    public static String[] genRsaKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(1024,new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
        String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
        return new String[]{publicKeyString,privateKeyString};
    }

    public static String encryptRsaWithPublicKey(String str, String publicKey) throws Exception{
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }

    /**
     * 分段RSA公钥加密
     */
    public static String multipartEncrypt( String str, String publicKey ) throws Exception{
//        byte[] decoded = Base64.decodeBase64(publicKey);
//        long start = System.currentTimeMillis();
        byte[] decoded = BaseEncoding.base64().decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] bytes = str.getBytes();
        int inputLen = bytes.length;
        int offLen = 0;//偏移量
        int i = 0;
        ByteArrayOutputStream bops = new ByteArrayOutputStream();
        while(inputLen - offLen > 0){
            byte [] cache;
            if(inputLen - offLen > 117){
                cache = cipher.doFinal(bytes, offLen,117);
            }else{
                cache = cipher.doFinal(bytes, offLen,inputLen - offLen);
            }
            bops.write(cache);
            i++;
            offLen = 117 * i;
        }
        bops.close();
        byte[] encryptedData = bops.toByteArray();
        String encodeToString = Base64.encodeBase64String(encryptedData);
//        long end = System.currentTimeMillis();
//        log.info("加密耗时:{}",end-start);
        return encodeToString;

    }

    public static String decryptRsaWithPrivateKey(String str, String privateKey) throws Exception{
        byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;
    }

    /**
     * 分段RSA私钥解密
     */
    public static String multipartDecrypt(String str, String privateKey) throws Exception{
//        byte[] decoded = Base64.decodeBase64(privateKey);
//        long start = System.currentTimeMillis();
        byte[] decoded = BaseEncoding.base64().decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        byte[] bytes = Base64.decodeBase64(str);
        int inputLen = bytes.length;
        int offLen = 0;
        int i = 0;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while(inputLen - offLen > 0){
            byte[] cache;
            if(inputLen - offLen > 128){
                cache = cipher.doFinal(bytes,offLen,128);
            }else{
                cache = cipher.doFinal(bytes,offLen,inputLen - offLen);
            }
            byteArrayOutputStream.write(cache);
            i++;
            offLen = 128 * i;

        }
        byteArrayOutputStream.close();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
//        long end = System.currentTimeMillis();
//        log.info("解密耗时:{}",end-start);
        return new String(byteArray);
    }
}
