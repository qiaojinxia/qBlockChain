package com.qjx.blockchain.qblockchain.basemodel.bak;

import java.math.BigDecimal;

/**
 * Created by caomaoboy 2019-10-29
 **/
public class TransactionInputbak {
    /**
     * 以前的交易id
     */
    private String txId;

    /**
     * 转账人 金额
     */
    private BigDecimal amount;

    /**
     * 交易签名
     */
    private String signature;

    /**
     * 交易发送方的钱包地址
     */
    private String sendAddress;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSendAddress() {
        return sendAddress;
    }

    public void setSendAddress(String sendAddress) {
        this.sendAddress = sendAddress;
    }

    public String getSendPublicKey() {
        return sendPublicKey;
    }

    public void setSendPublicKey(String sendPublicKey) {
        this.sendPublicKey = sendPublicKey;
    }

    /**
     * 交易发送方的钱包公钥
     */
    private String sendPublicKey;

    public TransactionInputbak() {
    }

    public TransactionInputbak(String txId, BigDecimal amount, String signature, String sendPublicKey) {
        this.txId = txId;
        this.amount = amount;
        this.signature = signature;
        this.sendPublicKey = sendPublicKey;

    }

    /**
     * 用来获取系统生成的 交易输入
     *
     * @return
     */
    public static TransactionInputbak getSystemTransactionInuput() {
        TransactionInputbak transactionInputbak = new TransactionInputbak();
        transactionInputbak.setTxId("0");
        transactionInputbak.setAmount(new BigDecimal(-1));
        return transactionInputbak;

    }


}