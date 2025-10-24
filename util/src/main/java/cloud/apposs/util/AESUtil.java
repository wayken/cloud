package cloud.apposs.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;

/**
 * AES加密解密算法类，主要应用于前后端数据安全加密传输
 */
public final class AESUtil {
    // 算法名
    public static final String KEY_ALGORITHM = "AES";

    // 加解密算法/模式/填充方式
    // ECB模式只用密钥即可对数据进行加密解密，CBC模式需要添加一个参数iv
    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    // 字符串编码/解码
    public static final String CHARSET_UTF8 = "utf-8";

    /**
     * AES CBC 加密并对加密数据进行BASE64编码，Key和IV均为随机生成并附加到加密数据中
     *
     * @param  data 原文数据
     * @return 加密结果
     */
    public static String encrypt(String data) throws Exception {
        String iv = doGetRandomChars(16);
		String key = doGetRandomChars(16);
        return StrUtil.reverse(iv) + encrypt(data, key, iv) + key;
    }

    /**
     * AES CBC 加密并对加密数据进行BASE64编码，同时添加随机IV盐值
     *
     * @param  data 原文数据
     * @param  key 秘钥
     * @return 加密结果
     */
    public static String encrypt(String data, String key) throws Exception {
        String iv = doGetRandomChars(16);
        return iv + encrypt(data, key, iv);
    }

    /**
     * AES CBC 加密并对加密数据进行BASE64编码
     *
     * @param  data 原文数据
     * @param  key 秘钥
     * @param  iv 加密向量字节数组（盐值）
     * @return 加密结果
     */
    public static String encrypt(String data, String key, String iv) throws Exception {
        if (StrUtil.isEmpty(data) || StrUtil.isEmpty(key) || StrUtil.isEmpty(iv)) {
            return null;
        }
        byte[] encryptedData = encrypt(data.getBytes(CHARSET_UTF8), key.getBytes(CHARSET_UTF8), iv.getBytes(CHARSET_UTF8));
        return Base64.encodeBytes(encryptedData);
    }

    /**
     * AES CBC 加密
     *
     * @param  data 加密内容字节数组
     * @param  keyBytes 密钥
     * @param  iv 加密向量字节数组（盐值）
     * @return 加密后字节内容
     */
    public static byte[] encrypt(byte[] data, byte[] keyBytes, byte[] iv) throws Exception {
        SecretKey key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        AlgorithmParameters params = AlgorithmParameters.getInstance(KEY_ALGORITHM);
        params.init(new IvParameterSpec(iv));
        // 设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, key, params);
        return cipher.doFinal(data);
    }

    /**
     * AES CBC BASE64解码解密
     *
     * @param  data 原文数据
     * @return 解密数据
     */
    public static String decrypt(String data) throws Exception {
        if (data == null || data.length() < 32) {
            return null;
        }
        String iv = StrUtil.reverse(data.substring(0, 16));
        String key = data.substring(data.length() - 16, data.length());
        String encryptData = data.substring(16, data.length() - 16);
        return decrypt(encryptData, key, iv);
    }

    /**
     * AES CBC BASE64解码解密
     *
     * @param  data 原文数据
     * @param  key 秘钥
     * @return 解密数据
     */
    public static String decrypt(String data, String key) throws Exception {
        if (data == null || data.length() < 16) {
            return null;
        }
        String iv = data.substring(0, 16);
        String encryptData = data.substring(16);
        return decrypt(encryptData, key, iv);
    }

    /**
     * AES CBC BASE64解码解密
     *
     * @param  data 原文数据
     * @param  key 秘钥
     * @param  iv 加密向量字节数组（盐值）
     * @return 解密数据
     */
    public static String decrypt(String data, String key, String iv) throws Exception {
        if (StrUtil.isEmpty(data) || StrUtil.isEmpty(key) || StrUtil.isEmpty(iv)) {
            return null;
        }
        byte[] decryptData = decrypt(Base64.decode(data.getBytes(CHARSET_UTF8)), key.getBytes(CHARSET_UTF8), iv.getBytes(CHARSET_UTF8));
        return new String(decryptData, CHARSET_UTF8);
    }

    /**
     * AES CBC 解密
     *
     * @param data 原始数据
     * @param keyBytes 密钥
     * @param iv 加密向量字节数组（盐值）
     * @return 解密后的内容
     */
    public static byte[] decrypt(byte[] data, byte[] keyBytes, byte[] iv) throws Exception {
        SecretKey key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        AlgorithmParameters params = AlgorithmParameters.getInstance(KEY_ALGORITHM);
        params.init(new IvParameterSpec(iv));
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        // 设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        return cipher.doFinal(data);
    }

    /**
     * 生成随机字符串
     *
     * @param  length 随机字符串的长度
     * @return 随机字符串
     */
    private static String doGetRandomChars(int length) {
        String randomChars = "";
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            // 字母和数字中随机
            if (random.nextInt(2) % 2 == 0) {
                // 输出是大写字母还是小写字母
                int letterIndex = random.nextInt(2) % 2 == 0 ? 65 : 97;
                randomChars += (char) (random.nextInt(26) + letterIndex);
            } else {
                randomChars += String.valueOf(random.nextInt(10));
            }
        }
        return randomChars;
    }
}
