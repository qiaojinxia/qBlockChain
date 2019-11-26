package com.qjx.blockchain.qblockchain.p2pnetwork;

import com.qjx.blockchain.qblockchain.basemodel.*;
import com.qjx.blockchain.qblockchain.blockprocessing.BlockServices;
import com.qjx.blockchain.qblockchain.blockprocessing.TransactionsPool;
import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caomaoboy 2019-11-05
 **/
public class P2PController {
    //钱包服务
    private Wallet wallet;
    //用于存放其他人的钱包
    private Map<String,byte[]> othersWallets;

    /**
     *
     * @return 返回账户名 和钱包地址
     */
    public Map<String,byte[]> getWallet() {
        Map<String,byte[]> walletx = new HashMap<String, byte[]>();
        wallet.getWallet().forEach((address,name)->{
                    try {
                        walletx.put(address,name.getAddress());
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
        );
        return walletx;
    }
    public void setWallet(Map<String,byte[]> wallet) {
        if(!ObjectUtils.notEmpty(wallet))
            throw  new IllegalArgumentException("Get wallets is empty!");
        if(!ObjectUtils.notEmpty(othersWallets))
            this.othersWallets = new HashMap<String,byte[]>();
        wallet.forEach((name,address)-> {
            this.othersWallets.put(name, address);
        });
    }

    //区块链核心服务
    private BlockServices BlockServices;
    //交易池服务
    private TransactionsPool transactionPool;
    //打包数量
    private static final Integer PACKAGE_NUM = 100;
    public final static Logger logger = LoggerFactory.getLogger(P2PController.class);
    public P2PController(BlockServices BlockServices, Wallet wallet, TransactionsPool transactionPool){
        this.wallet = wallet;
        this.BlockServices = BlockServices;
        this.transactionPool =transactionPool;
    }
    /**
     *
     * @return 获取区块链最后一个block
     */
    public Block getLastestBlock() {
        return this.BlockServices.getBlockChain().get(this.BlockServices.getBlockChain().size()-1);
    }

    /**
     * 获取一个交易打包的list
     * @return
     */
    public List<Transaction> getPackagedTransaction() {
        return transactionPool.getPackageTrans();
    }

    /**
     * 获取所有交易集合
     * @return
     */
    public List<Transaction> getAllTransation() {
        List<Transaction> trans = transactionPool.getallTrans();
        if(!ObjectUtils.notEmpty(trans))
            return null;
        return trans;
    }


    /**
     * 设置交易
     * @return
     */
    public void setAllTransation(List<Transaction> txs) {
        transactionPool.setTranSactionPool(txs);
    }
    /**
     *
     * @param trans 添加交易
     */
    public void addTransaction(List<Transaction> trans){
        try{
            transactionPool.addTransPlool(trans);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }
    /**
     *
     * @param lastestBlockReceived 将获取到的上一块区块链 添加到本地区块链最后
     * @return
     */
    public boolean addBlock(Block lastestBlockReceived) {
        try{
            this.BlockServices.addBlock(lastestBlockReceived);
        }catch (Exception e){
            logger.error("添加区块链失败!");
            return false;
        }
        return true;
    }

    /**
     *
     * @return 获取区块链
     */
    public List<Block> getBlockChain(){
        return this.BlockServices.getBlockChain();
    }

    /**
     *
     * @param blockchain 替换区块链
     * @return
     */
    public List<Block> replaceChain(List<Block> blockchain,String file) {
        //区块链长度小于 2 就是只有创世区块时 不执行替换
        if(blockchain.size() <2)
            return null;
        //将地一块区块设置成创世区块 然后再去检查后面区块
        blockchain.set( 0, Block.newGenesisBlock());
        //通过blockchain构造器验证 后的blockChain 来判断是否大于当前节点
        BlockChain _valid = new BlockChain(blockchain);
        //如果验证过的区块链大于当前的就替换
        if (_valid.getBlockchain().size() > this.BlockServices.getBlockChain().size()) {
            this.BlockServices.setBlockChain(blockchain);
            if(null == file){
                BlockChain.serializeBlockChain(blockchain);
            }else{
                BlockChain.serializeBlockChain(blockchain,file);
            }
            return this.BlockServices.getBlockChain();
        } else {
            logger.info("验证后区块链长度不足无法替换!");
        }
        return null;
    }

    public List<Block> replaceChain(List<Block> blockchain) {
        return replaceChain(blockchain,null);
    }
}