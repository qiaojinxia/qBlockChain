package com.qjx.blockchain.qblockchain.p2pnetwork;

/**
 * Created by caomaoboy 2019-11-04
 **/

import com.qjx.blockchain.qblockchain.basemodel.BlockChain;
import com.qjx.blockchain.qblockchain.basemodel.Wallet;
import com.qjx.blockchain.qblockchain.blockprocessing.BlockServices;
import com.qjx.blockchain.qblockchain.blockprocessing.TransactionsPool;
import com.qjx.blockchain.qblockchain.cli.Main;

/**
 * P2P网络中每个节点即是服务端又是客户端
 */
public class Inital implements Runnable {
    private String listenport;
    private String conPort;
    private BlockServices blockServices;
    private TransactionsPool tp;
    public Inital(String listenport, String conPort, BlockServices blockServices, TransactionsPool tp){
        this.listenport =listenport;
        this.conPort =conPort;
        this.blockServices = blockServices;
        this.tp = tp;
    }
    @Override
    public void run() {
        synchronized (this){
            Wallet wallet = Wallet.loadMyWallet();
            P2PController bc = new P2PController(blockServices,wallet,tp);
            P2PServer p2pServer = new P2PServer(bc);
            P2PClient p2pClient = new P2PClient(bc);
            int p2pPort = Integer.parseInt(listenport);
            // 启动p2p服务端
            p2pServer.initP2PServer(p2pPort);
            if (conPort!= null) {
                // 作为p2p客户端连接p2p服务端
                p2pClient.connectToPeer(conPort);
            }
        }
    }
    public static void main(String[] args) {
        BlockServices blockServices =new BlockServices(BlockChain.loadBlockChain().getBlockchain());
        TransactionsPool tp = new TransactionsPool(blockServices.getBlockChain());
       // Inital a = new Inital("52112","ws://localhost:52111",blockServices,tp);
        Inital a = new Inital("52111",null,blockServices,tp);
        Thread xx = new Thread(a);
        xx.start();
    }


}
