package com.qjx.blockchain.qblockchain.basemodel.bak;

import java.math.BigDecimal;

/**
 * Created by caomaoboy 2019-10-29
 **/
public class TransactionOutputbak {

    /**
     * 交易的结算金额
     */
    private BigDecimal amount;

    /**
     * 交易 接收方 钱包地址
     */
    private String receiverAddress;

    public TransactionOutputbak(BigDecimal amount, String receiverPublicKey) {
        this.amount = amount;
        this.receiverPublicKey =receiverPublicKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public void setReceiverPublicKey(String receiverPublicKey) {
        this.receiverPublicKey = receiverPublicKey;
    }

    /**
     * 接收方的公钥
     */
    private  String receiverPublicKey;


}
