package com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSACoder {

    /**
     * 缺省的2048位密钥对,可处理245个字节(81个汉字)的数据
     */
    public static String PRIVATE_KEY = "MIIXSDEv***XFu4=";

    public static String PUBLIC_KEY = "MINXBIB***DAQAB";

    /**
     * 字符集
     */
    public static String CHARSET = "utf-8";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_INSTANCE = "SHA1WithRSA";

    /**
     * 生成密钥对
     * @param keyLength
     * @return
     * @throws Exception
     */
    public static KeyPair getKeyPair(int keyLength) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keyLength);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 公钥字符串转PublicKey实例
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKey(String publicKey) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 私钥字符串转PrivateKey实例
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String privateKey) throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKey.getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }


    /**
     * 公钥加密
     * @param content
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] content, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    public static byte[] encryptByPublicKey(byte[] content) throws Exception {
        return encryptByPublicKey(content, getPublicKey(PUBLIC_KEY));
    }

    public static String encryptByPublicKey(String content, String publicKey) throws Exception {
        return new String(Base64.getEncoder().encode(encryptByPublicKey(content.getBytes(CHARSET), getPublicKey(publicKey))));

    }

    public static String encryptByPublicKey(String content) throws Exception {
        return new String(Base64.getEncoder().encode(encryptByPublicKey(content.getBytes(CHARSET))));
    }

    /**
     * 私钥解密
     * @param content
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] content, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

    public static byte[] decryptByPrivateKey(byte[] content) throws Exception {
        return decryptByPrivateKey(content, getPrivateKey(PRIVATE_KEY));
    }

    public static String decryptByPrivateKey(String content, String privateKey) throws Exception {
        return new String(decryptByPrivateKey(Base64.getDecoder().decode(content), getPrivateKey(privateKey)), CHARSET);

    }

    public static String decryptByPrivateKey(String content) throws Exception {
        return new String(decryptByPrivateKey(Base64.getDecoder().decode(content)), CHARSET);
    }

    /**
     * 私钥加密
     * @param content
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] content, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

    public static byte[] encryptByPrivateKey(byte[] content) throws Exception {
        return encryptByPrivateKey(content, getPrivateKey(PRIVATE_KEY));
    }

    public static String encryptByPrivateKey(String content, String privateKey) throws Exception {
        return new String(encryptByPrivateKey(content.getBytes(CHARSET), getPrivateKey(privateKey)), CHARSET);
    }

    public static String encryptByPrivateKey(String content) throws Exception {
        return new String(encryptByPrivateKey(content.getBytes(CHARSET)), CHARSET);
    }

    /**
     * 公钥解密
     * @param content
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static byte[] decrypByPublicKey(byte[] content, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    public static byte[] decrypByPublicKey(byte[] content) throws Exception {
        return decrypByPublicKey(content, getPublicKey(PUBLIC_KEY));
    }

    public static String decrypByPublicKey(String content, String publicKey) throws Exception {
        return new String(decrypByPublicKey(Base64.getDecoder().decode(content), getPublicKey(publicKey)), CHARSET);

    }

    public static String decrypByPublicKey(String content) throws Exception {
        return new String(decrypByPublicKey(Base64.getDecoder().decode(content)), CHARSET);
    }

    /**
     * 签名
     * @param content
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static byte[] sign(byte[] content, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_INSTANCE);
        signature.initSign(privateKey);
        signature.update(content);
        return signature.sign();
    }

    public static byte[] sign(byte[] content) throws Exception {
        return sign(content, getPrivateKey(PRIVATE_KEY));
    }

    public static String sign(String content, String privateKey) throws Exception {
        return new String(Base64.getEncoder().encode(sign(content.getBytes(CHARSET), getPrivateKey(privateKey))), CHARSET);
    }

    public static String sign(String content) throws Exception {
        return new String(Base64.getEncoder().encode(sign(content.getBytes(CHARSET))), CHARSET);
    }

    /**
     * 验签
     * @param content
     * @param sign
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] content, byte[] sign,  PublicKey publicKey) throws Exception {

        Signature signature = Signature.getInstance(SIGNATURE_INSTANCE);
        signature.initVerify(publicKey);
        signature.update(content);
        return signature.verify(sign);
    }

    public static boolean verify(byte[] content, byte[] sign) throws Exception {
        return verify(content, sign, getPublicKey(PUBLIC_KEY));
    }

    public static boolean verify(String content, String sign, String publicKey) throws Exception {
        return verify(content.getBytes(CHARSET), Base64.getDecoder().decode(sign), getPublicKey(publicKey));
    }

    public static boolean verify(String content, String sign) throws Exception {
        return verify(content.getBytes(CHARSET), Base64.getDecoder().decode(sign), getPublicKey(PUBLIC_KEY));
    }


}

