package cloud.apposs.util;

import org.junit.Assert;
import org.junit.Test;

public class TestAESUtil {
    @Test
    public void testAESEncryptGeneric() throws Exception {
        String data = "Hello Tunnel Data";
        String key = "235325fdgerteGHdsfsdewred4345341";
        String iv = "4387438hfdhfdjhg";
        String encrypt = AESUtil.encrypt(data, key, iv);
        System.out.println(encrypt);
        String decrypt = AESUtil.decrypt(encrypt, key, iv);
        System.out.println(decrypt);
        Assert.assertEquals(data, decrypt);
    }

    @Test
    public void testAESEncryptWithRandomIv() throws Exception {
        String data = "Hello AES Data";
        String key = "key.applss.cloud";
        String encrypt = AESUtil.encrypt(data, key);
        System.out.println(encrypt);
        String decrypt = AESUtil.decrypt(encrypt, key);
        System.out.println(decrypt);
        Assert.assertEquals(data, decrypt);
    }

    @Test
    public void testAESEncryptWithRandomKeyIv() throws Exception {
        String data = "Hello AES Data";
        String encrypt = AESUtil.encrypt(data);
        System.out.println(encrypt);
        String decrypt = AESUtil.decrypt(encrypt);
        System.out.println(decrypt);
        Assert.assertEquals(data, decrypt);
    }
}
