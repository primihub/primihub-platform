package com.primihub.biz.util.crypt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptUtilTest {

    @Test
    void genRsaKeyPair_shouldGenerateValidKeys() throws Exception {
        String[] keys = CryptUtil.genRsaKeyPair();

        assertThat(keys).hasSize(2);
        assertThat(keys[0]).isNotNull().isNotEmpty(); // public key
        assertThat(keys[1]).isNotNull().isNotEmpty(); // private key
    }

    @Test
    void encryptAndDecrypt_shouldRoundtrip() throws Exception {
        String[] keys = CryptUtil.genRsaKeyPair();
        String original = "Hello, PrimiHub!";

        String encrypted = CryptUtil.encryptRsaWithPublicKey(original, keys[0]);
        String decrypted = CryptUtil.decryptRsaWithPrivateKey(encrypted, keys[1]);

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void multipartEncryptAndDecrypt_shouldRoundtripLongText() throws Exception {
        String[] keys = CryptUtil.genRsaKeyPair();
        String original = "A".repeat(500);

        String encrypted = CryptUtil.multipartEncrypt(original, keys[0]);
        String decrypted = CryptUtil.multipartDecrypt(encrypted, keys[1]);

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void multipartEncryptAndDecrypt_shouldHandleShortText() throws Exception {
        String[] keys = CryptUtil.genRsaKeyPair();
        String original = "short";

        String encrypted = CryptUtil.multipartEncrypt(original, keys[0]);
        String decrypted = CryptUtil.multipartDecrypt(encrypted, keys[1]);

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void encryptRsaWithPublicKey_shouldProduceDifferentOutputEachTime() throws Exception {
        String[] keys = CryptUtil.genRsaKeyPair();
        String original = "test";

        String encrypted1 = CryptUtil.encryptRsaWithPublicKey(original, keys[0]);
        String encrypted2 = CryptUtil.encryptRsaWithPublicKey(original, keys[0]);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void keyPairGeneration_shouldBeUnique() throws Exception {
        String[] keys1 = CryptUtil.genRsaKeyPair();
        String[] keys2 = CryptUtil.genRsaKeyPair();

        assertThat(keys1[0]).isNotEqualTo(keys2[0]);
        assertThat(keys1[1]).isNotEqualTo(keys2[1]);
    }
}
