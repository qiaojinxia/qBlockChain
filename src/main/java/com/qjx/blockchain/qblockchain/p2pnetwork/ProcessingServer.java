package com.qjx.blockchain.qblockchain.p2pnetwork;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qjx.blockchain.qblockchain.basemodel.*;
import com.qjx.blockchain.qblockchain.cli.Main;
import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by caomaoboy 2019-11-05
 **/
public class ProcessingServer {
    class listenEvents implements Runnable{
        @Override
        public void run() {
            logger.info("开始监听全网广播事件!");
                while(true){
                    if(Main.tp.isAddEvent()){
                        broatcast(responseTransaction());
                        logger.info("监听到交易池添加时间准备全网广播!");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Main.tp.setAddEvent(false);
                    }
                    if(Main.blockServices.isAddblockChainEvent()){
                        logger.info("监听到区块更新添加时间准备全网广播!");
                        broatcast(responseBlockChainMsg());
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Main.blockServices.setAddblockChainEvent(false);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

        }
    }//p2pserver -listenport 52112 -seedport ws://localhost:52111

    private List<WebSocket> socket;
    private P2PController blockController;
    //查询最新的区块
    public final static int QUERY_LATEST_BLOCK = 0;
    //查询整个区块链
    public final static int QUERY_BLOCKCHAIN = 1;
    //查询交易集合
    public final static int QUERY_TRANSACTION = 2;
    //查询已打包交易集合
    public final static int QUERY_PACKED_TRANSACTION = 3;
    //查询钱包集合
    public final static int QUERY_WALLET = 4;
    //返回区块集合
    public final static int RESPONSE_BLOCKCHAIN = 5;
    //返回交易集合
    public final static int RESPONSE_TRANSACTION = 6;
    //返回已打包交易集合
    public final static int RESPONSE_PACKED_TRANSACTION = 7;
    //返回钱包集合
    public final static int RESPONSE_WALLET = 8;
    //处理最后一个区块
    public final static int  RESPONSE_LATEST_BLOCK=9;
    //处理最后一个区块
    public final static int  RESPONSE_All_BLOCK=10;

    public ProcessingServer(P2PController blockController){
        this.blockController =blockController;
        this.setSockets(new ArrayList<WebSocket>());
        listenEvents le = new listenEvents();
        Thread thread1 = new Thread(le);
        thread1.start();
    }
    public List<WebSocket> getSockets() {
        return sockets;
    }

    public void setSockets(List<WebSocket> sockets) {
        this.sockets = sockets;
    }

    public final static Logger logger = LoggerFactory.getLogger(ProcessingServer.class);

    //客户端列表
    private List<WebSocket> sockets;

    public  void handleBlockChainResponse(String message,List<WebSocket> socket){
        if(!ObjectUtils.notEmpty(message) || message.equals("[]"))
            return;
        List<Block> receiverBlockChain = JSON.parseArray(message,Block.class);
        Collections.sort(receiverBlockChain, new Comparator<Block>() {
            @Override
            public int compare(Block b1, Block b2) {
                //从小到大排序
                return b1.getIndex().compareTo(b2.getIndex());
            }
        });
        //记录对应
        boolean falg =false;
        //找到最后一块区块
        Block lastestBlockReceived = receiverBlockChain.get(receiverBlockChain.size() -1);
        //找到本地最后一块区块
        Block lastestBlock =blockController.getLastestBlock();
        List<Block> replaceBlockChian =null;
        //如果接收到的区块链长度 大于当前区块链
        if(lastestBlockReceived.getIndex().compareTo(lastestBlock.getIndex())==1){
            //如果 接收到的区块 在  我现在这条链的 6个节点之内 就接收
            List<Block> block = blockController.getBlockChain();
            for(int i=0;i<6;i++){
                replaceBlockChian = new ArrayList<Block>();
               Block  _lastblock = block.get(block.size()-1);
                for(int m=0;m<receiverBlockChain.size();m++){
                    replaceBlockChian.add(receiverBlockChain.get(receiverBlockChain.size() -1 -m ));
                    if(receiverBlockChain.get(receiverBlockChain.size() -1 -m ).getPreviousHash().equals(_lastblock.gethash())){
                        falg = true;
                        break;
                    }
                }
                if(falg)
                    break;
                else {//如果当前区块没有在收到的区块里
                    blockController.getBlockChain().remove(_lastblock);
                    if(blockController.getBlockChain().size()==0)
                        break;
                }
            }
            if(falg){

                Collections.sort(replaceBlockChian, new Comparator<Block>() {
                    @Override
                    public int compare(Block b1, Block b2) {
                        //从小到大排序
                        return b1.getIndex().compareTo(b2.getIndex());
                    }
                });
                ListIterator<Block> replaceIter = replaceBlockChian.listIterator();
                while(replaceIter.hasNext()){
                    blockController.addBlock(replaceIter.next());
                }
                broatcast(responseBlockChainMsg());
            }
//            //如果本地最后一块区块大于的hash 是 收到的最后一块区块的前一块
//            if(lastestBlock.gethash().equals(lastestBlockReceived.getPreviousHash())){
//                logger.info("将接收到的新区块加入到本地区块链中~");
//                //将区块链存储到本地区块中 加入时需要验证
//                if(blockController.addBlock(lastestBlockReceived)){
//                    //加入并且验证通过 向所有连接节点广播这条消息
//                    broatcast(responseLatestBlockMsg());
//                    //否则收到的节点就1个创世区块的话
//                }else if(receiverBlockChain.size() ==1){
//                    logger.info("查询所有通讯节点上的区块链~");
//                    //向所有连接节点发送查询
//                    broatcast(queryBlockChainMsg());
//                }else{
//                    //用长的list替换 当前区块
//                    blockController.replaceChain(receiverBlockChain);
//                }
//            }
        }else{
            logger.info("收到的区块链长度比本地小故抛弃~");
        }
    }

    public synchronized void handleMessage(WebSocket websocket,String msg,List<WebSocket> sockets)  {
        if(ObjectUtils.isEmpty(msg))
            return;
        if(!msg.startsWith("{")) {
            System.out.println("xxxxxxxxx");
            return;
        }
        try{
            Message message = JSON.parseObject(msg,Message.class);
            logger.info("接收到" + websocket.getRemoteSocketAddress().getPort() +"的p2p消息" );
            if(!ObjectUtils.notEmpty(message.getHead()))
                return;
            switch(message.getHead()){
                case QUERY_LATEST_BLOCK:
                    write(websocket,responseLatestBlockMsg());
                    break;
                case QUERY_BLOCKCHAIN:
                    write(websocket,responseBlockChainMsg());
                    break;
                case QUERY_TRANSACTION:
                    write(websocket,responseTransaction());
                    break;
                case QUERY_PACKED_TRANSACTION:
                    write(websocket,responsePackagedTransaction());
                    break;
                case QUERY_WALLET:
                    //返回钱包账号和地址
                    write(websocket,responseWallets());
                    break;
                case RESPONSE_BLOCKCHAIN:
                    //返回区块链
                    handleBlockChainResponse(message.getContent(),sockets);
                    break;
                case RESPONSE_TRANSACTION:
                    //返回交易
                    handleTransactionResponse(message.getContent());
                    break;
                case RESPONSE_PACKED_TRANSACTION:
                    //返回打包的交易
                    handlePackedTransactionResponse(message.getContent());
                    break;
                case RESPONSE_WALLET:
                    handleWalletResponse(message.getContent());
                    break;
                case RESPONSE_LATEST_BLOCK:
                    handleLasteBlockChain(message.getContent(),sockets);
                    break;

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleLasteBlockChain(String content, List<WebSocket> sockets) {
        if(!ObjectUtils.notEmpty(content))
            return;
        List<Block> receiverBlockChain = JSON.parseArray(content,Block.class);
        Block lasreceivetBlock = receiverBlockChain.get(0);
        //找到最后一块区块
        Block lastestBlock= blockController.getLastestBlock();
        if(lasreceivetBlock.getPreviousHash().equals(lastestBlock.gethash())) {
            logger.info("将接收到的新区块加入到本地区块链中~");
            //将区块链存储到本地区块中 加入时需要验证
            if (blockController.addBlock(lasreceivetBlock)) {
                broatcast(responseLatestBlockMsg());
            }
        }
//        }else{
//            //验证失败 请求获取全节点
//            broatcast(queryBlockChainMsg());
//        }
    }

    /**
     *
     * @param content 处理从其他人那获取到的地址
     */
    private void handleWalletResponse(String content) {
        if(!ObjectUtils.notEmpty(content))
            return;
        Map<String,byte[]> walletss = (Map) JSONObject.parse(content);
        //存放其他人的地址
        blockController.setWallet(walletss);
    }
    /**
     *
     * @param content 处理得到的打包交易添加到 自己的交易池里
     */
    private void handlePackedTransactionResponse(String content) {
        if(!ObjectUtils.notEmpty(content))
            return;
        List<Transaction> txs = JSON.parseArray(content,Transaction.class);
        blockController.getAllTransation().addAll(txs);

    }

    /**
     *
     * @param content 处理接收来的 交易 加入交易池
     */
    private void handleTransactionResponse(String content) {
        if(!ObjectUtils.notEmpty(content))
            return;
       List<Transaction> txs =  JSON.parseArray(content,Transaction.class);
       if(!ObjectUtils.notEmpty( blockController.getAllTransation()))
           blockController.setAllTransation(txs);
       else
           blockController.getAllTransation().addAll(txs);
    }
    /**
     * 从钱包中获取 钱包名字 和 地址
     * @return
     */
    private String responseWallets()  {
        Map<String,byte[]> walletAddress = new HashMap<String,byte[]>();
        blockController.getWallet().forEach((name,address) -> {
            walletAddress.put(name,address);
        });
            return JSON.toJSONString(walletAddress);
    }
    /**
     *
     * @return 返回打包的交易
     */

    private String responsePackagedTransaction() {
        return JSON.toJSONString(new Message(RESPONSE_PACKED_TRANSACTION,JSON.toJSONString(blockController.getPackagedTransaction())));
    }

    /**
     *
     * @return 返回所有交易
     */
    private String responseTransaction() {
        return JSON.toJSONString(new Message(RESPONSE_TRANSACTION,JSON.toJSONString(blockController.getAllTransation())));
    }



    /**
     *
     * @return 返回 blockchain 数组
     */
    private String responseBlockChainMsg() {
        return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN,JSON.toJSONString(blockController.getBlockChain())));
    }

    /**
     *
     * @return 连接建立最初,查询最新的区块
     */
    private String responseLatestBlockMsg() {
        Block[] blocks ={blockController.getLastestBlock()};
        return JSON.toJSONString(new Message(RESPONSE_LATEST_BLOCK,JSON.toJSONString(blocks)));
    }
    /**
     *
     * @return  查询区整条块链
     */
    public String queryBlockChainMsg() {
        return JSON.toJSONString(new Message(QUERY_BLOCKCHAIN));
    }
    /**
     * 查询最后一个区块链
     */
    public String queryLastestBlockMsg() { return JSON.toJSONString(new Message(QUERY_LATEST_BLOCK)); }
    /**
     * 查询交易
     */
    public String queryTransactionMsg() { return JSON.toJSONString(new Message(QUERY_TRANSACTION)); }
    /**
     * 查询交易集合
     * @return
     */
    public String queryPackedTransactionMsg() {return JSON.toJSONString(new Message(QUERY_PACKED_TRANSACTION)); }
    /**
     * 查询钱包集合
     * @return
     */
    public String queryWalletMsg() {return JSON.toJSONString(new Message(QUERY_WALLET)); }



    /**
     * 向服务端发送消息
     * 当前WebSocket的远程Socket地址，就是服务器端
     * @param ws：
     * @param message
     */
    public void write(WebSocket ws, String message) {
        logger.info("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息:" + message);
        ws.send(message);
    }
    /**
     * 向所有服务端广播消息
     * @param message
     */
    public void broatcast(String message) {
        if (sockets.size() == 0) {
            return;
        }
        logger.info("======广播消息开始：");
        for (WebSocket socket : sockets) {
            this.write(socket, message);
        }
        logger.info("======广播消息结束");
    }
}

