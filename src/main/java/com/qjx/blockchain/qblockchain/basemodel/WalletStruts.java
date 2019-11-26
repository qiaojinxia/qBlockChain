package com.qjx.blockchain.qblockchain.basemodel;

import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.Base58;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.HexUtil;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.RSACoder;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.WalletUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

/**
 * 钱包
 * @author :caomaoboy
 */
public class WalletStruts {

    /**
     * @return 获取钱包地址
     */
    public byte[] getAddress() throws NoSuchAlgorithmException {

        return WalletUtils.generateAddressWithBase58Check(publicKey).getBytes();
    }

    //公钥 相当于账号
    private byte[] publicKey;

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public byte[] getPublicKeyHash() {
        return publicKeyHash;
    }

    public void setPublicKeyHash(byte[] publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

    //私钥 相当于密码
    private byte[] privateKey;

    //对钥匙进行hash 更安全
    private byte[] publicKeyHash;

    public WalletStruts(byte[] publicKey, byte[] privateKey) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.publicKeyHash = WalletUtils.generateAddress(publicKey).getBytes();
    }

    /**
     * 生成钱包
     * @return
     * @throws Exception
     */
    public static WalletStruts generateWallet() throws Exception {
        //随机生成公钥 和私钥
        KeyPair keyPair = RSACoder.getKeyPair(1024);
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] publickey = new String(Base64.getEncoder().encode(publicKey.getEncoded())).getBytes();
        byte[] privatekey = new String(Base64.getEncoder().encode(privateKey.getEncoded())).getBytes();
        return new WalletStruts(publickey, privatekey);
    }
    /**
     * 将交易地址转换成publickeyhash 用于锁定地址
     * @param address
     * @return
     */
    public byte[] decodeAddress(byte[] address){
        String hexEncode = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] hex58 = Base58.decode(new String(address));
            //对地址base58解码 去除第一位 和 最后4位 但会的就是publichashkey
            baos.write(hex58, 1, hex58.length - 5);
            //字节流转成十六进制表示
            hexEncode = HexUtil.encode(baos.toByteArray());
        }catch (Exception e){
            throw new IllegalArgumentException("交易地址输入有误!");
        }

        return hexEncode.getBytes();
    }

    /**
     * 验证交易地址是否有效
     * @param address
     * @return
     */
    public static  boolean isValid(byte[] address) throws NoSuchAlgorithmException {
        if(StringUtils.isEmpty(address))
            throw  new IllegalArgumentException("valid address error,address must not be null!");
        //
//        if(address.length != 34)
//            throw  new IllegalArgumentException("valid address error,address length " + address.length+ " is incorrect;!");
        //将输入地址解码
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream _checkSum2 = new ByteArrayOutputStream();
            byte[] hex58 = Base58.decode(new String(address));
            //取出前21个字符
            baos.write(hex58, 0, hex58.length - 5);
            //字节流转成十六进制表示
            String _buff = HexUtil.encode(baos.toByteArray());
            //取出地址的后4位
            _checkSum2.write(hex58, hex58.length - 5, 4);
            //字节流转成十六进制表示
            String _buff2 = HexUtil.encode(_checkSum2.toByteArray());
            //进行2次sha加密取出后4位
            byte[] _checkSum1 = WalletUtils.generateBase58CheckSum(_buff.getBytes());
            //如果 2次hash 的后 4位 和 address 的后四位相同 就说明地址通过验证 就相当你的地址符合我的加密规则
            if (Arrays.equals(_checkSum1, _checkSum1)) {
                return true;
            }
            return false;
        }catch (Exception e){
            throw new IllegalArgumentException("Validatoion fail! invalid address:" + new String(address));
        }
        //取出前21个自己

    }

}