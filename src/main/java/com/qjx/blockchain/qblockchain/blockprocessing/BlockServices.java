package com.qjx.blockchain.qblockchain.blockprocessing;

import com.alibaba.fastjson.JSON;
import com.qjx.blockchain.qblockchain.basemodel.*;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.WalletUtils;
import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.List;


public class BlockServices {
    public void setBlockChain(List<Block> blockChain) {
        this.blockChain = blockChain;
    }
    public boolean isAddblockChainEvent() {
        return addblockChainEvent;
    }

    public void setAddblockChainEvent(boolean addblockChainEvent) {
        this.addblockChainEvent = addblockChainEvent;
    }

    private volatile boolean addblockChainEvent = false;

    public final static Logger logger = LoggerFactory.getLogger(BlockServices.class);

    private List<Block> blockChain;

    public BlockServices() {
        super();
    }

    /**
     * 初始化blockchain
     *
     * @param blocks   将已有区块链传入
     *
     */
    public BlockServices(List<Block> blocks) {
        this.blockChain = blocks;
    }


    /**
     * @param recipient 挖矿程序 传入 接受奖励的矿工
     * @return 返回一个打包的block
     */
    public Block Mineral(byte[] recipient,String memo,Block newblock) throws Exception {
        if(null == recipient)
            throw  new IllegalArgumentException("Recipient Cannot be null!");
        if (!ObjectUtils.notEmpty(this.blockChain) || newblock==null)
            throw new IllegalArgumentException("Blockchain correctness verification failed!");
        //设置交易的第一笔交易为奖励交易
        newblock.getData().set(0,Transaction.createCoinBase(recipient,memo));
        //随机数
        Long nonce = new Long(1);
        // 通过 将 前一个区块链 和 当前区块的json格式化字符串 和 一个随机数(工作证明) 拼接起来计算hash
        String hash = null;
        while (true) {
            //设置工作证明
            newblock.setNonce(nonce);
            //记录下挖矿时间
            newblock.setTimestamp(System.currentTimeMillis());
            //得到这个区块的hash
            hash = newblock.gethash();
            logger.info("第 " + nonce +"次挖矿" +hash);
            //以 2个0开头的hash 算出来正确的话 那恭喜你可能挖到矿了
            //00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048  这是第一块区块链的hash值
            // 大概有8个0 但现在要挖矿的话就可能达到20个0了 随着0的数量越来越多 每次瞎猜你能挖到的概率也是越来越低
            if (ProofOfWork.isHashValid(hash,newblock.getnBits())) {
                //挖矿成功记录下时间
                logger.info("=======计算结果正确,挖了" + nonce + "次 计算出hash值为:" + hash);
                break;
            }
            nonce++;
        }
        return newblock;
    }
    public Block PackageBlock(List<Transaction> transpackage) {
        Transaction.SUBSIDY = ProofOfWork.getAward(this.blockChain.size()-1);
        if (!ObjectUtils.notEmpty(transpackage)) {
            logger.info("Only reward blocks will be generated when Zero transactions are received!");
            transpackage = new ArrayList<Transaction>();
            transpackage.add(null);
        }
        //获取最后一笔交易
        if(blockChain.size() ==0)
            inintFirstblock();
        Block lastBlock = blockChain.get(blockChain.size() - 1);
        //根据上一个区块 来生成一个这个区块 用来挖矿
        Block newblock = new Block(lastBlock.getIndex().add(new BigInteger("1")), lastBlock.gethash(), transpackage, new Long(1) ,System.currentTimeMillis());
        newblock.setData(transpackage);
        newblock.setnBits(ProofOfWork.GetNextWorkRequired(blockChain));
        return newblock;
    }
    public List<Block> getBlockChain() {
        return blockChain;
    }
    public String getBlockChainjson() {
        return JSON.toJSONString(blockChain);
    }
    /**
     * 初始化创世区块
     */
    public void inintFirstblock() {
        if (StringUtils.isEmpty(blockChain) || blockChain.size() == 0){
            blockChain = new ArrayList<>();
            blockChain.add(Block.newGenesisBlock());
        }
        else{
            blockChain.set(0,Block.newGenesisBlock());
        }
        logger.info("Genesis  block is ready");
    }

    /**
     * 从交易输入中查询区块链中所有自己的已被花费了的交易输出
     *
     * @return
     * @throws Exception
     */
    public Map<String, Integer[]> getSpentUTXOs(byte[] address) throws Exception {
        if (!WalletStruts.isValid(address))
            throw new IllegalArgumentException("address is  invalid");
        Map<String, Integer[]> allintx = new HashMap<String, Integer[]>();
        for (int c = 0; c < blockChain.size(); c++) {
            Block block = blockChain.get(c);
            if (block.isgenesisBlock())
                continue;
            for (int i = 0; i < block.getData().size(); i++) {
                Transaction next = block.getData().get(i);
                for (int n = 0; n < next.getVin().size(); n++) {
                    //遍历所有节点头
                    TXInput vinnext = next.getVin().get(n);
                    //判断 是否是系统奖励
                    if (vinnext.isSysReward())
                        continue;
                    if (vinnext.canUnlockInputWith(address)) {
                        if (!allintx.containsKey(vinnext.getTxId()))
                            allintx.put(new String(vinnext.getTxId()), new Integer[]{vinnext.getVoi()});
                        else {
                            Integer[] origindata = allintx.get(vinnext.getTxId());
                            Integer[] arr = new Integer[origindata.length + 1];
                            for (int m = 0; m < origindata.length; m++) {
                                arr[m] = origindata[m];
                            }
                            arr[arr.length - 1] = vinnext.getVoi();
                            allintx.put(new String(vinnext.getTxId()), arr);
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        return allintx;
    }

    /**
     * 从交易 输出中的到所有未被使用的交易 = 所有自己的输出交易 - 所有自己的输入交易
     *
     * @param address 钱包地址
     * @return
     */
    public UtxoInfo getUnSpendUtxos(byte[] address) throws Exception {
        if (!WalletStruts.isValid(address))
            throw new IllegalArgumentException("Address is  invalid");
        //List<Transaction> outTrans= new ArrayList<Transaction>();
        UtxoInfo utf = new UtxoInfo();
        UtfoStruts ufs = null;
        //将区块List转成迭代器
        Iterator<Block> blockIter = blockChain.listIterator();
        Map<String, Integer[]> spendUtxos = getSpentUTXOs(address);
        //循环迭代 遍历所有区块
        while (blockIter.hasNext()) {
            //获取当前区块对象
            Block nextBlock = blockIter.next();
            //将当前区块对象的和交易 data 转换成迭代器
            Iterator<Transaction> TransIter = nextBlock.getData().listIterator();
            //判断是迭代器是否为空
            while (TransIter.hasNext()) {
                //取出一笔 交易记录
                Transaction nextTrans = TransIter.next();
                //将交易输出 list转化成迭代器
                Iterator<TXOutput> VoutIter = nextTrans.getVout().listIterator();
                int i = 0;
                //判断是否为空
                while (VoutIter.hasNext()) {
                    //取出交易处处
                    TXOutput txout = VoutIter.next();
                    if (spendUtxos.containsKey(new String(nextTrans.getTxId()))) {
                        for (Integer valuex : spendUtxos.get(new String(nextTrans.getTxId()))) {
                            if (null != valuex && !(valuex.equals(i))) {
                                if (txout.canUnlockOutputWith(address)) {
                                    ufs = new UtfoStruts(new String(nextTrans.getTxId()));
                                    ufs.add(i, txout);
                                    utf.add(ufs);
                                    logger.info("找到了自己可用的value:" + txout.getValue());
                                }
                            } else {
                                logger.info("当前UTXO 已经被使用:" + txout.getValue() + " 解锁UTXO的id:" + new String(nextTrans.getTxId()) + " 输入脚本索引" + "[" + i + "]");
                            }
                        }
                    } else {
                        if (txout.canUnlockOutputWith(address)) {
                            ufs = new UtfoStruts(new String(nextTrans.getTxId()));
                            ufs.add(i, txout);
                            utf.add(ufs);
                            logger.info("找到了可用value:" + txout.getValue() + "  锁定脚本:" + new String(txout.getHashPublicKey()));
                        }
                    }


                    i++;
                }

            }

        }

        logger.info("=======================================================");
        return utf;
    }

    /**
     * 将区块加到 区块链之前先得验证 区块是否有效
     * @param block
     * @throws Exception
     */
    public void addBlock(Block block) throws Exception {
        if(null == block)
            throw new IllegalArgumentException("传入的区块链不能为空");
        if(block.getPreviousHash().equals(blockChain.get(blockChain.size()-1).gethash())){
            //循环遍历去验证每笔交易
            for(int i =0;i<block.getData().size();i++){
                Transaction trans = block.getData().get(i);
                if(!Transaction.VerifyTransaction(trans,blockChain)){
                    throw new IllegalArgumentException("交易签名校验失败,无法被加入区块链~");
                }
            }
            logger.info("区块链中交易签名验证成功~");
            //Block newblock = new Block(lastBlock.getIndex().add(new BigInteger("1")), lastBlock.gethash(), trans,diffculty, 1);
            blockChain.add(block);
            //添加成功后序列化保存
            BlockChain.serializeBlockChain(blockChain);
        }

    }

    /**
     * @param address 钱包地址
     * @return 返回账户的余额
     * @throws Exception
     */
    public BigDecimal getBalcnce(byte[] address) throws Exception {
        if (!WalletStruts.isValid(address))
            throw new IllegalArgumentException("wallet address is  invalid");
        //获取账户余额
        UtxoInfo balance = getUnSpendUtxos(address);
        return balance.getBalance();
    }


    /**
     * @param to     收款人
     * @param amount 金额
     */
    public Transaction newTransaction(byte[] publicKey, byte[] to, BigDecimal amount) throws Exception {
        if (!WalletStruts.isValid(to))
            throw new IllegalArgumentException("Address is  invalid");
        if(!ObjectUtils.notEmpty(to) || !ObjectUtils.notEmpty(amount) || !ObjectUtils.notEmpty(publicKey))
            throw  new  IllegalArgumentException(" newTransaction args  null error!");
        //遍历账本,找到属于付款人适合的金额,找到这些outputs
        UtxoInfo ufo = getUnSpendUtxos(WalletUtils.generateAddressWithBase58Check(publicKey).getBytes());
        //获得钱包里最小的一些零碎金额 //todo后期可以按照算法更改 怎么筛选金额 比如动态规划 贪心算法
        Map<String, BigDecimal> minExchange = ufo.getMinExchange(amount);
        //将排好序 并且算好金额的Map 再次转换成UtxoInfo
        UtxoInfo uto = UtxoInfo.formatMap(minExchange);
        //获取所有钱包里的零钱
        BigDecimal exchange = uto.getTotalAmount();
        //转换成 交易输入
        List<TXInput> txIn = UtxoInfo.tansToTxInPut(publicKey, uto);
        //初始化 交易输出
        TXOutput newtxout = TXOutput.newTxOutPut(amount, to);
        //初始化交易输出数组
        List<TXOutput> txouts = new ArrayList<TXOutput>();
        //添加一笔 交易输出
        txouts.add(newtxout);
        //计算零钱 如果有剩余 就返回
        BigDecimal surPlus = exchange.subtract(amount);
        //有剩余就生成 一笔返回给自己的UTXO
        if (surPlus.compareTo(new BigDecimal("0")) == 1) {
            //比零大就返回一笔 用自己的公钥生成钱包地址
            TXOutput changeTxOut = TXOutput.newTxOutPut(surPlus, WalletUtils.generateAddressWithBase58Check(publicKey).getBytes());
            txouts.add(changeTxOut);
        } else if (surPlus.compareTo(new BigDecimal("0")) == -1)
            //钱如果为 负就认为没钱可以花了
            throw new IllegalArgumentException("你的钱包被掏空了!");
        //创建这笔交易
        Transaction trans = new Transaction(null, txIn, txouts,System.currentTimeMillis());
        //对交易进行sha256
        trans.setTxId();
        //对交易进行签名
        logger.info("钱包:" + WalletUtils.generateAddressWithBase58Check(publicKey) + "  向钱包:" + new String(to) + " →→转账$" + amount.floatValue() + " 付款:$" + exchange + " 找零:$" + surPlus);
        logger.info("生成交易ID:" + new String(trans.getTxId()));
        logger.info("====================================================================================");
        //设置交易id
        //返回交易结构
        return trans;

    }
}
