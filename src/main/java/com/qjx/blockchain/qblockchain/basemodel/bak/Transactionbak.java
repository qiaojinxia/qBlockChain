package com.qjx.blockchain.qblockchain.basemodel.bak;

import com.alibaba.fastjson.JSON;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.Sha256;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.SignUtils;
import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;

import java.math.BigDecimal;


/**
 * 用来存储交易记录
 */
public class Transactionbak {
    public String getId() {
        return id;
    }

    public TransactionInputbak getTxIn() {
        return txIn;
    }

    public TransactionOutputbak getTxOut() {
        return txOut;
    }

    /**
     * 交易的编号
     */
   private String id;
    /**
     * 交易的输入
     */
    private TransactionInputbak txIn;

    /**
     * 交易的输出
     */
    private TransactionOutputbak txOut;


    public Transactionbak(){
        super();
    }
    public Transactionbak(String id, TransactionInputbak txIn, TransactionOutputbak txOut){
        this();
        if(!ObjectUtils.notEmpty(id) || !ObjectUtils.notEmpty(txIn) || !ObjectUtils.notEmpty(txOut))
            throw  new IllegalArgumentException("Args empty failed!");
        this.id = id;
        this.txIn =txIn;
        this.txOut = txOut;


    }
    /**
     * 是否系统生成区块链奖励交易
     * @return
     */
    public boolean isSysTx() {
        return txIn.getTxId().equals("0") && txIn.getAmount().equals(new BigDecimal(-1));
    }

    /**
     * 生成用于交易签名的交易记录副本
     */
    public Transactionbak cloneTx(){
        TransactionInputbak transactionInputbak = new TransactionInputbak(txIn.getTxId(),txIn.getAmount(),null,null);
        TransactionOutputbak transactionOutputbak = new TransactionOutputbak(txOut.getAmount(),txOut.getReceiverPublicKey());
        return new Transactionbak(id, transactionInputbak, transactionOutputbak);
    }
    /**
     * 生成交易hash
     */
    public String hash(){
        return Sha256.getSHA256(JSON.toJSONString(this));
    }

    /**
     * 对当前交易 用私钥生成交易签名
     * @param privateKey
     * @param prevTx
     */
    public void sign(String privateKey, Transactionbak prevTx){
        if(isSysTx()){
            return;
        }
        /**
         * 就相当于把上一笔交易和这一笔交易通过id连接在一起
         */
        if(!prevTx.getId().equals(txIn.getTxId())){
            System.err.println("签名失败:当前交易交易头与前一笔交易尾不匹配");
            return;
        }
        Transactionbak txClone = cloneTx();
        //获取上一个区块的 公钥 设置到  输入交易中
        txClone.getTxIn().setSendPublicKey(prevTx.getTxOut().getReceiverPublicKey());
        String sign = "";
        try{
            //打包 签名 保证信息 是我认可的 用我自己私钥签名
            sign = SignUtils.sign(txClone.hash().getBytes(),privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        txIn.setSignature(sign);


    }
    public boolean verify(Transactionbak prevTx){
        if(isSysTx()){
            return true;
        }
        if(!prevTx.getId().equals(getTxIn().getTxId())){
            System.err.println("签名失败:当前交易交易头与前一笔交易尾不匹配");
        }
        Transactionbak txClone =cloneTx();
        txClone.getTxIn().setSendPublicKey(prevTx.getTxOut().getReceiverPublicKey());
        boolean  result =false;
        try{
            result = SignUtils.verify(txClone.hash().getBytes(),txIn.getSendPublicKey(),txIn.getSignature());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

}
