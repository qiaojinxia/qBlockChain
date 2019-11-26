package com.qjx.blockchain.qblockchain.basemodel;

import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.Base58;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.HexUtil;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by caomaoboy 2019-10-30
 **/
public class TXOutput {

    public TXOutput(){
        super();
    }
    public Integer getN() {
        return n;
    }

    /**
     * 金额
     */
    private BigDecimal value;

    public void setHashPublicKey(byte[] hashPublicKey) {
        this.hashPublicKey = hashPublicKey;
    }

    public byte[] getHashPublicKey() {
        return hashPublicKey;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * 公钥哈希
     */
    private byte[] hashPublicKey;

    public void setN(Integer n) {
        this.n = n;
    }
    /**
     * 输出编号
     */
    private Integer n;

//    public String getScriptPubKey() {
//        return scriptPubKey;
//    }

    public BigDecimal getValue() {
        return value;
    }

//    /**
//     * 锁定脚本 钱包地址
//     */
//    private String scriptPubKey;

    /**
     * 初始化 TxOutPut 构造器
     * @param value 金钱数额度
     * @param n
     */
    public TXOutput(BigDecimal value,Integer n) {
        this.n = n;
        this.value = value;
    }
    /**
     * 锁定交易输出
     * @param address
     */
    private void Lock(byte[] address){
        //对地址进行base58 反向解锁加密
        byte[] lockAddress = this.decodeAddress(address);
        this.hashPublicKey = lockAddress;
    }

    /**
     * 将交易地址转换成publickeyhash 用于锁定地址
     * @param address
     * @return
     */
    public byte[] decodeAddress(byte[] address){
        if(StringUtils.isEmpty(address))
            throw  new IllegalArgumentException("address null error;");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] hex58 = Base58.decode(new String(address));
        //对地址base58解码 去除第一位 和 最后4位 但会的就是publichashkey
        baos.write(hex58,1,hex58.length-5);
        //字节流转成十六进制表示
        String hexEncode =HexUtil.encode(baos.toByteArray());
        return hexEncode.getBytes();
    }
    /**
     * 判断解锁数据是否能够解锁交易输出
     *
     * @param address
     * @return
     */
    public boolean canUnlockOutputWith(byte[] address) throws IOException {
        return Arrays.equals(this.hashPublicKey,decodeAddress(address));
    }
    /**
     * 创建一笔新的转账金额
     * @param address 要转账的地址
     */
    public static TXOutput newTxOutPut(BigDecimal value,byte[] address){
        //创建一笔新的交易输出
        TXOutput newTransfer = new TXOutput(value,0);
        //对交易输出进行地址锁定
        newTransfer.Lock(address);
        return newTransfer;
    }

    /**
     * 特殊情况 不需要锁定 比如创世区块
     * @param value
     * @return
     */
    public static TXOutput newTxOutPut(BigDecimal value){
        //创建一笔新的交易输出
        TXOutput newTransfer = new TXOutput(value,-1);
        //创世区块连
        newTransfer.hashPublicKey = null;
        return newTransfer;
    }

}