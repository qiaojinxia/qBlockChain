package com.qjx.blockchain.qblockchain.blockprocessing;

import com.qjx.blockchain.qblockchain.basemodel.Block;
import com.qjx.blockchain.qblockchain.basemodel.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;


/**
 * Created by caomaoboy 2019-11-04
 **/
public class TransactionsPool {

    public boolean isAddEvent() {
        return addEvent;
    }
    public void setAddEvent(boolean addEvent) {
        this.addEvent = addEvent;
    }

    private volatile boolean addEvent = false;
    private Integer size;
    public void setTranSactionPool(List<Transaction> tranSactionPool) {
        TranSactionPool = tranSactionPool;
    }
    private static final Integer PACKAGE_NUM = 70;
    /**存放暂时不能通过验证的交易信息*/
    private  List<Transaction> failedChecked ;
    /**
     * 交易池 用来存放矿工所接收到的交易记录
     */
    private List<Transaction> TranSactionPool;
    private List<Block> blocChain;

    public final static Logger logger = LoggerFactory.getLogger(TransactionsPool.class);
    public List<Transaction> getTransactions(Integer size){

        if(StringUtils.isEmpty(TranSactionPool)
        || TranSactionPool.size() == 0){
            logger.info("Has no trans to get!");
            return null;
        }
        List<Transaction> _transBuff =new ArrayList<Transaction>();
        //迪比翼留给矿工挖矿时添加
        if(TranSactionPool.get(0) != null)
            _transBuff.add(null);
        for(int i = 0;i<TranSactionPool.size() ;i++){
            //已加入当前区块的不返回
            if(null != TranSactionPool.get(i) && isContains(TranSactionPool.get(i).getTxId()))
                continue;
            _transBuff.add(TranSactionPool.get(i));
        }
        return _transBuff;
    }


    /**
     * 判断交易是否存在 在区块链中
     * @return
     */
    public boolean isContains(byte[] txid){
        ListIterator<Block> blockIter = blocChain.listIterator();
        while(blockIter.hasNext()){
            Block blockchainNext = blockIter.next();
            ListIterator<Transaction> transIter= blockchainNext.getData().listIterator();
            while(transIter.hasNext()){
                Transaction transNext = transIter.next();
                if(Arrays.equals(transNext.getTxId(),txid)){
                    return true;
                }

            }
        }
        return false;
    }
    /**
     * 返回所有交易集合
     * @return
     */
    public List<Transaction> getallTrans(){
        return getTransactions(TranSactionPool.size());
    }

    public List<Transaction> getPackageTrans(){
        //规定70比交易为一个包
        return getTransactions(PACKAGE_NUM);
    }

    /**
     * 更新交易池里的数据
     */
    public void updataTransPool(){

    }
    public boolean validTransactions(){
        return false;
    }

    /**
     * 将交易添加到交易池
     */
    public void addTransPlool(List<Transaction> trans) throws Exception {
        //添加进交易池之前进行验证
        for(int i = 0;i<trans.size();i++){
            if(Transaction.VerifyTransaction(trans.get(i), blocChain)){
                this.TranSactionPool.add(trans.get(i));
                size ++;
            }
        }
    }
    public boolean transIsExist(byte[] txid){
        for(int i=0;i<TranSactionPool.size();i++){
            if(Arrays.equals(txid,TranSactionPool.get(i).getTxId()))
                return true;
        }
        return false;
    }

    /**
     * 将交易添加到交易池
     */
    public void addTransPlool(Transaction trans) throws Exception {
        //如果这笔交易存在了就不要添加进来了
        if(transIsExist(trans.getTxId()))
            return;
        //添加进交易池之前进行验证
            if(Transaction.VerifyTransaction(trans, blocChain)){
                this.TranSactionPool.add(trans);
                size ++;
        }else{
                logger.error("An invalid transaction was received , Will be abandoned!");
                logger.error("the transaction ID is  "+new String(trans.getTxId()));

            }
    }
    public TransactionsPool(List<Block> blockChain){
        this.blocChain =blockChain;
        this.TranSactionPool =new ArrayList<Transaction>();
        this.size = 0 ;
    }

}
