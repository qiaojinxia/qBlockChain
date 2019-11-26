package com.qjx.blockchain.qblockchain.basemodel;

import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.Base58;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.HexUtil;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.WalletUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by caomaoboy 2019-10-30
 **/
public class TXInput {


    public byte[] getTxId() {
        return txId;
    }

    public void setTxId(byte[] txId) {
        this.txId = txId;
    }



    /**
     * 交易Id的hash值
     */
    private byte[] txId;

    public Integer getVoi() {
        return voi;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setVoi(Integer voi) {
        this.voi = voi;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getSignature() {
        return signature;
    }

    /**
     * 签名
     */
    private byte[] signature;
    private Integer voi;
    private Integer sequence;

    private byte[] publicKey;

    /**
     * 交易输出索引
     */

    public TXInput(){
        super();
    }
    public TXInput(byte[] txId, Integer voi, byte[] publicKey){
        this.txId = txId;
        this.voi = voi;
        this.publicKey =publicKey;
    }


    /**
     * 判断解锁数据是否能够解锁交易输出
     *
     * @param address 传入钱包地址
     * @return
     */
    public boolean canUnlockInputWith(byte[] address) throws NoSuchAlgorithmException {
        if(StringUtils.isEmpty(address))
            return false;
        //System.out.println(bk.getIndex() +"解锁：传入address:"+unlockingData +"解锁脚本:"+ scriptSig+ " " + "加密后"+WalletUtils.generateAddressWithBase58Check(scriptSig.getBytes()));
        byte[] vaild = WalletUtils.generateAddress(publicKey).getBytes();
        byte[] hashKey = decodeAddress(address);
        return Arrays.equals(vaild,hashKey);
    }
    /**
     * 将交易地址转换成publickeyhash 用于锁定地址
     * @param address
     * @return
     */
    public byte[] decodeAddress(byte[] address){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] hex58 = Base58.decode(new String(address));
        //对地址base58解码 去除第一位 和 最后4位 但会的就是publichashkey
        baos.write(hex58,1,hex58.length-5);
        //字节流转成十六进制表示
        String hexEncode = HexUtil.encode(baos.toByteArray());
        return hexEncode.getBytes();
    }
    /**
     * 判断当前交易是否 系统奖励
     * @return
     */
    public boolean isSysReward(){
        if(StringUtils.isEmpty(new String(txId)) || voi.equals(-1))
            return true;
        return false;
    }


    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
}
