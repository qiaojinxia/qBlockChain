package com.qjx.blockchain.qblockchain.basemodel;


import com.alibaba.fastjson.JSON;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.RSACoder;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.Sha256;
import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 用来存储交易记录
 */
public class Transaction {

    public  static Float SUBSIDY = null;
    /**
     * 交易的Hash
     */
    private byte[] txId;

    private long lock_time;
    public byte[] getTxId() {
        return this.txId;
    }

    public void setTxId(byte[] txId) {
        this.txId = txId;
    }
    public Transaction(){}

    public long getLock_time() {
        return lock_time;
    }

    public void setLock_time(long lock_time) {
        this.lock_time = lock_time;
    }

    public Transaction(byte[] txId, List<TXInput> txInputs, List<TXOutput> txOutputs, long lock_time) {
        this.txId =txId;
        this.vin = txInputs;
        this.vout =txOutputs;
        this.lock_time =lock_time;
    }

    public void setTxId() {
        this.txId =hash().getBytes();
    }

    public String hash(){
        StringBuilder buff = new StringBuilder();
        buff.append(JSON.toJSONString(vin) + JSON.toJSONString(vout) + this.lock_time);
        return Sha256.getSHA256(buff.toString());
    }

    public List<TXInput> getVin() {
        return vin;
    }

    public void setVin(List<TXInput> vin) {
        this.vin = vin;
    }

    public List<TXOutput> getVout() {
        return vout;
    }

    public void setVout(List<TXOutput> vout) {
        this.vout = vout;
    }

    /**
     * 交易输入
     */
    private List<TXInput> vin;

    /**
     * 交易输出
     */
    private List<TXOutput> vout;

    /**
     * 系统奖励挖矿交易
     * @param to 挖矿奖励人
     * @param memo 可以写成 挖矿人的姓名信息
     * @return
     */
    public static Transaction createCoinBase(byte[] to,String memo){
        if(StringUtils.isEmpty(memo))
            memo = String.format("Reward to '%s'", to.toString());
        // 创建交易输入 由于没有矿工 挖矿交易 没有交易输入 所以可以写成 自己的信息
        TXInput txInput = new TXInput(new byte[]{}, -7, memo.getBytes());
        // 创建交易输出
        List txInputs = new ArrayList<TXInput>();
        //添加 交易输入
        txInputs.add(txInput);
        TXOutput txOutput =null;
        if(Arrays.equals(to,"yaoyao".getBytes()))
             txOutput = TXOutput.newTxOutPut(new BigDecimal(SUBSIDY));
        else
             txOutput = TXOutput.newTxOutPut(new BigDecimal(SUBSIDY), to);
        //添加 交易输出
        List txOutputs = new ArrayList<TXOutput>();
        txOutputs.add(txOutput);
        // 创建交易
        Transaction tx = null;
        if(Arrays.equals(to,"yaoyao".getBytes())){
            tx = new Transaction(null, txInputs, txOutputs,1572929869);
        }else{
            tx = new Transaction(null, txInputs, txOutputs,System.currentTimeMillis());
        }

        // 设置交易ID
        tx.setTxId();
        //返回交易
        return tx;
    }

    //判断是否是创世区块
    public boolean isBaseTrans(){
        if(this.getVin().get(0).getVoi().equals(-7)){
            return true;
        }
        return false;
    }


    /**
     *
     * @param txid 交易的id
     * @param blockChain 区块链
     * @return 根据区块链的id 来寻找区块链交易
     */
    private static Transaction getTransByTxId(byte[] txid,List<Block> blockChain){
        ListIterator<Block> blockChainIter = blockChain.listIterator();
        //遍历所有区块链
        while(blockChainIter.hasNext()){
            Block blockChainIterNext = blockChainIter.next();
            //遍历所有交易
            ListIterator<Transaction> transIter = blockChainIterNext.getData().listIterator();
            while(transIter.hasNext()){
                Transaction transNext = transIter.next();
                if(Arrays.equals(transNext.getTxId(),txid))
                    return transNext;
            }
        }
        return null;
    }

    /**
     * 旷工 或者 签名 需要获取到 一笔交易所有引用到的数据
     * @param tr 获得签名 或验证交易的Transaction
     *
     */
    private static Map<String,Transaction>  getSVTransaction(Transaction tr,List<Block> blockChain){
        if(tr.isBaseTrans()){return null;}//如果是创世区块就返回
        ListIterator<TXInput> tranIter = tr.getVin().listIterator();
        //存储遍历出来的Transaction
        Map<String,Transaction> tansmap =new HashMap<String,Transaction>();
        while(tranIter.hasNext()){
            //遍历这笔交易的
            TXInput txIter = tranIter.next();
            //系统奖励 没有交易输入 不需要获取
            if(txIter.isSysReward())
                continue;
            //根据id 获取对应的Transation
            Transaction _trans = Transaction.getTransByTxId(txIter.getTxId(),blockChain);
            //将遍历到的transaction 存储起来
            tansmap.put(new String(_trans.getTxId()),_trans);
        }
        return tansmap;
    }

    /**
     * 对数据进行签名
     * @param tr
     * @param privateKey 进行签名的私钥
     * @param blockChain 传入区块链
     * @throws Exception
     */
    public static void SignTransaction(Transaction tr,String privateKey,List<Block> blockChain) throws Exception {
        //拷贝一份交易
        if(StringUtils.isEmpty(tr))
            throw new IllegalArgumentException("Trans null error!");
        //如果是创始区块就不要签名了
        if(tr.isBaseTrans())
            return;
        //交易输入的 签名 和 publickey 设置为 null
        Transaction copyTrans= transCopy(tr);
        //找到所有的需要签名的这笔交易的头引用的所有vout
        Map<String,Transaction> allTransRefByVin = getSVTransaction(tr,blockChain);
        //如果没有找到 要么是创世区块 要么只有一笔挖矿交易
        if(allTransRefByVin.size() == 0 )
            return;
        //遍历输入交易
        Iterator<TXInput> txiIter =copyTrans.getVin().iterator();
        int i =0;
        while(txiIter.hasNext()){
            TXInput txiNext = txiIter.next();
            //找到引用的交易
            byte[] hashPublicKey = allTransRefByVin.get(new String(txiNext.getTxId())).getVout().get(txiNext.getVoi()).getHashPublicKey();
            //添加交易的publickey
            copyTrans.getVin().get(i).setPublicKey(hashPublicKey);
            //设置交易id
            copyTrans.setTxId();
            System.out.println("将要对 data:" + new String(copyTrans.getTxId()) +" 进行签名!");
            //对数据进行签名
            String sign = RSACoder.sign(new String(copyTrans.getTxId()),privateKey);
            System.out.println("已完成 签名:" + sign);
            //将交易数据进行还原 以便不影响下一次签名
            copyTrans.getVin().get(i).setPublicKey(null);
            //对原始数据进行签名
            tr.getVin().get(i).setSignature(sign.getBytes());
            //索引加1
            i++;

        }
    }

    /**
     * 矿工对签名进行验证 保证交易没有被篡改 发送人和签名人是对应的
     * @param tr 要校验的交易
     * @param blockChain 传入区块链
     * @return 返回校验结果
     * @throws Exception
     */

    public static boolean VerifyTransaction(Transaction tr,List<Block> blockChain) throws Exception {
        if(!tr.isTransValid())
            throw new IllegalArgumentException("Not a valid transaction!");
        boolean flag =true;
        if (StringUtils.isEmpty(tr))
            throw new IllegalArgumentException("Trans null error!");
        //如果是创始区块就不要签名了
        if (tr.isBaseTrans())
            return true;
        try {
            //复制一份交易
            Transaction copyTrans= transCopy(tr);
            ListIterator<TXInput> transIter = tr.getVin().listIterator();
            //找到所有的需要签名的这笔交易的头引用的所有vout
            Map<String,Transaction> allTransRefByVin = getSVTransaction(tr,blockChain);
            int i = 0;
            while(transIter.hasNext()){
                TXInput txiNext = transIter.next();
                //系统奖励不需要验证
                if(txiNext.getVoi().equals(-1))
                    continue;
                //签名为空
                if(StringUtils.isEmpty(txiNext.getSignature()))
                    return false;
                //找到引用的交易
                byte[] hashPublicKey = allTransRefByVin.get(new String(txiNext.getTxId())).getVout().get(txiNext.getVoi()).getHashPublicKey();
                //副本设置id
                copyTrans.getVin().get(i).setPublicKey(hashPublicKey);
                //对交易景象hash
                copyTrans.setTxId();
                //获取要签名的数据
                String transDta = new String(copyTrans.getTxId());
                flag = RSACoder.verify(transDta,new String(txiNext.getSignature()),new String(txiNext.getPublicKey()));
                System.out.println("验证签名结果:"+flag);
                //重置数据
                copyTrans.getVin().get(i).setPublicKey(null);
                if(flag){
                    return flag;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("验证签名失败");
        }finally {
            flag =false;
        }
        return flag;
    }


    /**
     * 创建一笔交易的副本用来 签名 校验
     * @param tx 待校验的交易 TODO如果以后要扩展字段就要完善验证
     * @return
     */
    private static Transaction transCopy(Transaction tx){
        List<TXInput> txinput = new ArrayList<TXInput>();;
        //需要把 tansa的vin 和 vout 遍历拿出来 复制给副本trans
        ListIterator<TXInput> txInIter = tx.getVin().listIterator();
        //对 交易输入 进行复制 publickey 填充为null
        while(txInIter.hasNext()){
            TXInput txiNext =  txInIter.next();
            //创建input的副本
            TXInput _buffInput = new TXInput(txiNext.getTxId(),txiNext.getVoi(),null);
            //添加进数组
            txinput.add(_buffInput);
        }
        //新建一笔交易副本
        Transaction newTrans =new Transaction(null,txinput,tx.vout,tx.lock_time);
        //返回交易副本
        return newTrans;
    }

    public boolean isTransValid(){
        //遍历vin 和 vout 验证参数是否正确
        ListIterator<TXInput> vinIter = this.getVin().listIterator();
        ListIterator<TXOutput> voutIter = this.getVout().listIterator();
        while(vinIter.hasNext()){
            TXInput vinNext = vinIter.next();
            //如果是系统奖励就跳过
            if(vinNext.isSysReward())
                continue;
            //交易输入的字段都不能为空
            if(!ObjectUtils.notEmpty(vinNext.getVoi()) || !ObjectUtils.notEmpty(vinNext.getTxId()) || !ObjectUtils.notEmpty(vinNext.getSignature())
            || !ObjectUtils.notEmpty(vinNext.getPublicKey())){
                return false;
            }

        }
        while(voutIter.hasNext()) {
            TXOutput voutNext = voutIter.next();
            //验证输出交易 的信息 还有输出交易的金额
            if(!ObjectUtils.notEmpty(voutNext.getValue()) && !ObjectUtils.notEmpty(voutNext.getHashPublicKey())){
                return false;
            }
        }
        return true;
    }

}
