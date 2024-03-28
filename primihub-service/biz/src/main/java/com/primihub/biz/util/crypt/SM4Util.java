package com.primihub.biz.util.crypt;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Base64;

public class SM4Util {
    public static final String ALGORITHM_NAME = "SM4";
    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 加密方法，SM4/ECB/PKCS5Padding 填充方式，没有iv
     */
    public static String encrypt(String data, String key) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] keyBytes = decoder.decode(key);
            SMS4KeySpec sms4KeySpec = new SMS4KeySpec(keyBytes);

            Cipher cipher = generateECBCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, sms4KeySpec.getKey());

            String encodeSource = Hex.encodeHexString(cipher.doFinal(dataBytes));

            return encodeSource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 解密方法，SM4/ECB/PKCS5Padding 填充方式，没有iv
     *
     * @throws Exception
     */
    public static String decrypt(String encrypted, String key) throws Exception {
        byte[] cipherText = Hex.decodeHex(encrypted.toCharArray());
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] keyBytes = decoder.decode(key);
        SMS4KeySpec sms4KeySpec = new SMS4KeySpec(keyBytes);

        Cipher cipher = generateECBCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, sms4KeySpec.getKey());

        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }

    private static Cipher generateECBCipher(String algorithmName, int mode, byte[] key)
            throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key);

        return cipher;
    }

    static class SMS4KeySpec implements KeySpec {
        private byte[] key;

        public SMS4KeySpec(byte[] key) throws InvalidKeyException {
            this(key, 0);
        }

        public SMS4KeySpec(byte[] key, int offset) throws InvalidKeyException {
            if (key.length - offset < 16) {
                throw new InvalidKeyException("Wrong key size");
            } else {
                this.key = new byte[16];
                System.arraycopy(key, offset, this.key, 0, 16);
            }
        }

        public byte[] getKey() {
            return (byte[]) ((byte[]) this.key.clone());
        }
    }
}
