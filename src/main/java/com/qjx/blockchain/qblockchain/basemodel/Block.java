package com.qjx.blockchain.qblockchain.basemodel;

import com.qjx.blockchain.qblockchain.blockprocessing.BlockAlgorithm;
import com.qjx.blockchain.qblockchain.blockprocessing.BlockServices;
import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.Sha256;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;


public class Block implements Serializable {

    public static String getVersion() {
        return version;
    }


    public void setMerKleRoot(String merKleRoot) {
        hashTransactions();
    }

    //区块链版本号
    private final static String version ="V1.0";

    //当前区块索引

    public Integer getNonce() {
        return this.nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public BigInteger getIndex() {
        return index;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }



    public void setIndex(BigInteger index) {
        this.index = index;
    }
    //区块链索引
    private BigInteger index;
    //记录上一个区块的hash值
    private String previousHash;
    //时间戳
    //区块链 存放的数据信息
    private List<Transaction> data;
    //区块链生成的时间
    private long timestamp;

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    private Integer difficulty;
    //挖矿的工作量证明 计算Hash的次数
    private Integer nonce;

    //默克尔树
    private MerkleTree MerKleRoot;

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
    /**
     * 获取交易数据
     * @return
     */
    public List<Transaction> getData() {
        return data;
    }

    /**
     * 设置交易数据
     * @param data
     */
    public void setData(List<Transaction> data) {
        this.data = data;
        //每一次 更新 交易数据后都要重新算一下 默克尔树的hash值
        hashTransactions();
    }


    /**
     *  由矿工得到后去计算 这个区块的 hash
     * @return
     */
    public String gethash() {
        if(!ObjectUtils.notEmpty(this.index) && !ObjectUtils.notEmpty(this.previousHash) && !ObjectUtils.notEmpty(this.data)
                 &&!ObjectUtils.notEmpty(this.nonce) &&!ObjectUtils.notEmpty(this.difficulty))
            throw  new IllegalArgumentException("Cannot get hash every args must be init ");
        if(!ObjectUtils.notEmpty(MerKleRoot)){
            hashTransactions();
        }
        //System.out.println("计算hash" +"index:"+index +"version:"  +version +"prehash:" +previousHash +"timestap:" + timestamp +"梅克尔:" + MerKleRoot.getRoot().getHash() +"NOCE:" + nonce + "难度:"+difficulty);
        return calculateHash(this.index,version,this.previousHash,this.timestamp,MerKleRoot.getRoot().getHash(),this.nonce,this.difficulty);
    }

    /**
     * 计算hash服务
     * @param index 索引
     * @param previousHash 前一个区块的hash值
     * @param timestamp 时间戳
     * @param data 数据
     * @return 当前hash
     */
    private String calculateHash(BigInteger index,String version, String previousHash, long timestamp, String data,long nonce,Integer difficulty) {
        StringBuilder builder = new StringBuilder(index.toString());
        builder.append(previousHash).append(data).append(nonce).append(version).append(difficulty).append(timestamp);
        System.out.println(Sha256.getSHA256(builder.toString()));
        return Sha256.getSHA256(builder.toString());
    }

    /**
     * 空参构造 调试用 后期删除
     */
    public Block(){
        super();
        this.data = new ArrayList<Transaction>();
    }


    /**
     * 获取 默克尔树hash
     * @return
     */
    public String getMerKleRoot() {
        if( !ObjectUtils.notEmpty(MerKleRoot))
            return "";
        return MerKleRoot.getRoot().getHash();
    }

    /**
     * 对每一笔 交易进行 id 累加 hash
     */
    public void hashTransactions(){
        List<String> buff = new ArrayList<String>();
        //初始化block 时 trans为空 此时不要计算
        if(data.get(0) ==null ){
            return;
        }
        for(int i =0;i<data.size();i++){
            //将 所有交易数据的id(hash256) 进行拼接
            buff.add(new String(data.get(i).getTxId()));
        }
        //拼接后做一次hash256
        MerKleRoot = new MerkleTree(buff);
    }

    /**
     * 获取时间戳
     * @return
     */

    public long getTimestamp() {
        return timestamp;
    }
    /**
     *
     * @param index block的区块号
     * @param previousHash 上一个区块的hash
     * @param data 存储交易信息
     * @param nonce 工作证明
     */
    public Block(BigInteger index , String previousHash, List<Transaction> data,Integer difficulty, Integer nonce,long timestamp){
        this.index = index;
        this.previousHash = previousHash;
        this.difficulty = difficulty;
        this.data = data;
        this.nonce =nonce;
        this.timestamp = timestamp;
    }

    /**
     *
     * @return 判断是否创世区块
     */
    public boolean isGenesisBlock(){
        //如果是创世区块 那么 只有一笔输入交易 和输出交易  并且输入交易的前一笔交易为null
        return this.previousHash.startsWith("767be8afab82c0");
    }

    /**
     *  创造一块创世区块
     * @return
     */
    public static Block newGenesisBlock(){
        // to 锁定脚本 memo 解锁脚本
        Transaction myfirst = Transaction.createCoinBase("yaoyao".getBytes(),"To my love yao yao");
        //将第一笔交易装入 list
        List<Transaction> firsttrans = new ArrayList<Transaction>();
        firsttrans.add(myfirst);
        //传入block
        Block firstBlock = new Block(new BigInteger("1") ,"767be8afab82c0c91c1941effc71f809de1acfded90d7219f7f762909fbe40c9",firsttrans,7,0,1572914406);
        firstBlock.hashTransactions();
        return firstBlock;
    }


    /**
     *
     * @return
     */
    public boolean isBlockValid(List<Block> block) throws Exception {
        if(this.isGenesisBlock())
            return true;
        //验证一些列参数是否为空
        if(!ObjectUtils.notEmpty(this.previousHash) || !ObjectUtils.notEmpty(this.MerKleRoot) || !ObjectUtils.notEmpty(this.difficulty )
               || !ObjectUtils.notEmpty(this.data)  || !ObjectUtils.notEmpty(this.index) || !ObjectUtils.notEmpty(this.nonce)
        || !ObjectUtils.notEmpty(this.timestamp))
            return false;
        for(int i = 0;i< data.size();i++){
            //验证交易失败则认为这个block是无效的
            if(!this.getData().get(i).isTransValid())
                return false;
            //验证交易签名是否正确
            if(!Transaction.VerifyTransaction(this.data.get(i),block))
                return false;
        }
        return true;
    }


    public static boolean isProofValid(Block block){
        if(block.isGenesisBlock())
            return true;
        return BlockAlgorithm.isHashValid(block.gethash(), BlockServices.diffculty);
    }

}