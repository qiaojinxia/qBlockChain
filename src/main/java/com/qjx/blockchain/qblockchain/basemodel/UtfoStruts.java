package com.qjx.blockchain.qblockchain.basemodel;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Created by caomaoboy 2019-11-02
 **/
public class UtfoStruts {
        private Integer size;
        private byte[] txid; //交易id
        private Integer[] voi; //交易输出 索引
        private TXOutput[] vout; //TXOutput 对象

    public Integer getSize() {
        return size;
    }

    public byte[] getTxid() {
        return txid;
    }

    public Integer[] getVoi() {
        return voi;
    }

    public TXOutput[] getVout() {
        return vout;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    private BigDecimal totalAmount;//总的金钱数量
        public UtfoStruts(String id) {
            this.txid = id.getBytes();
            this.voi = new Integer[1];
            this.vout = new TXOutput[1];
            this.totalAmount = new BigDecimal("0");
            this.size = 0;
        }
        public UtfoStruts(){
            super();
        }
        public Integer getLength(){
            return this.size;
        }
        public void add(Integer voi,TXOutput vout){
            if(StringUtils.isEmpty(voi) || StringUtils.isEmpty(vout))
                throw new IllegalArgumentException("null error!");
            //如果数组内容为 空 则执行出事化操作然后将值存入
            if(StringUtils.isEmpty(voi) || StringUtils.isEmpty(vout)){
                this.voi = new Integer[]{voi};
                this.vout = new TXOutput[]{vout};
                //这里如果先前没有交易 则为第一笔 的value
                this.totalAmount = this.vout[0].getValue();

            }else{

                autoExpandSpace();
                this.voi[size] = voi;
                TXOutput _buff = new TXOutput(vout.getValue(),0);
                _buff.setHashPublicKey(vout.getHashPublicKey());
                this.vout[size] = _buff;


                this.totalAmount = this.totalAmount.add(vout.getValue());


            }
            this.size ++;
        }
        //对数组进行扩容
        private void autoExpandSpace(){
            //如果超过空间的2/3 就执行扩容
            if(this.voi.length * 2/3 >= this.size){
                Integer[] buffVoi = new Integer[this.voi.length * 2];
                TXOutput[] buffVout = new TXOutput[this.vout.length * 2];
                for(int i=0;i<this.voi.length;i++){
                    buffVoi[i] = this.voi[i];
                    buffVout[i] = this.vout[i];
                }
                this.vout =buffVout;
                this.voi = buffVoi;
            }
        }
        //找到数额最少的那笔交易
        public TXOutput findMaxValue(){
            if(size == 0)
                throw  new IllegalArgumentException("empty data error");
            TXOutput recoder =null;
            BigDecimal Select = new BigDecimal("0");
            for (int i = 0;i<this.size;i++){
                if(this.vout[i].getValue().compareTo(Select) == 1){
                    Select =  new BigDecimal(this.vout[i].getValue().toString());
                    recoder = this.vout[i];

                }
            }
            return recoder;
        }

    //找到数额最小的那笔交易
    public TXOutput findMixValue(){
        if(size == 0)
            throw  new IllegalArgumentException("empty data error");
        TXOutput recoder =null;
        //默认第一笔交易 为最小值
        BigDecimal Select = new BigDecimal(this.vout[0].getValue().toString());
        for (int i = 0;i<this.size;i++){
            if(this.vout[i].getValue().compareTo(Select) == -1){
                Select =  new BigDecimal(this.vout[i].getValue().toString());
                recoder = this.vout[i];

            }
        }
        return recoder;
    }



}
