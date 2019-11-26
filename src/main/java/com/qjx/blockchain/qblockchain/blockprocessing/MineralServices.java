//package com.qjx.blockchain.qblockchain.blockprocessing;
//
//import com.qjx.blockchain.qblockchain.basemodel.Block;
//import com.qjx.blockchain.qblockchain.basemodel.Transaction;
//import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by caomaoboy 2019-11-23
// **/
//public class MineralServices implements Runnable {
//    //挖矿线程数大小
//    private int num;
//    private static List<Thread> mineralThread;
//    public final static Logger logger = LoggerFactory.getLogger(MineralServices.class);
//    private MineralServices(){
//        //如果为空创建线程池
//        if(!ObjectUtils.notEmpty(num))
//            mineralThread =new ArrayList<Thread>();
//    }
//    @Override
//    public void run() {
//
//    }
//
//    /**
//     * 获取线程实例
//     */
//    public List<Thread> getInstance(int num){
//        for(int i=0;i<num;i++){
//            mineralThread.add(new Thread(new MineralServices()));
//        }
//        return mineralThread;
//    }
//
//    /**
//     * @param recipient 挖矿程序 传入 接受奖励的矿工
//     * @return 返回一个打包的block
//     */
//    private void Mineral(byte[] recipient, String memo, Block newblock) throws Exception {
//        if(null == recipient)
//            throw  new IllegalArgumentException("Recipient Cannot be null!");
//        if (!ObjectUtils.notEmpty(this.blockChain) || newblock==null)
//            throw new IllegalArgumentException("Blockchain correctness verification failed!");
//        //设置交易的第一笔交易为奖励交易
//        newblock.getData().set(0, Transaction.createCoinBase(recipient,memo));
//        //随机数
//        Long nonce = new Long(1);
//        // 通过 将 前一个区块链 和 当前区块的json格式化字符串 和 一个随机数(工作证明) 拼接起来计算hash
//        String hash = null;
//        while (true) {
//            //设置工作证明
//            newblock.setNonce(nonce);
//            //记录下挖矿时间
//            newblock.setTimestamp(System.currentTimeMillis());
//            //得到这个区块的hash
//            hash = newblock.gethash();
//            logger.info("第 " + nonce +"次挖矿" +hash);
//            //以 2个0开头的hash 算出来正确的话 那恭喜你可能挖到矿了
//            //00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048  这是第一块区块链的hash值
//            // 大概有8个0 但现在要挖矿的话就可能达到20个0了 随着0的数量越来越多 每次瞎猜你能挖到的概率也是越来越低
//            if (ProofOfWork.isHashValid(hash,newblock.getnBits())) {
//                //挖矿成功记录下时间
//                logger.info("=======计算结果正确,挖了" + nonce + "次 计算出hash值为:" + hash);
//                break;
//            }
//            nonce++;
//        }
//        return newblock;
//    }
//}
