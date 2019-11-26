package com.qjx.blockchain.qblockchain.p2pnetwork;

/**
 * Created by caomaoboy 2019-11-03
 **/

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * p2p客户端
 *
 */
/**
 * Created by jack on 2018/4/1.
 */
public class P2PClient {
    public final static Logger logger = LoggerFactory.getLogger(P2PClient.class);
    private ProcessingServer processingServer;
    public P2PClient(BlockController ps) {
        this.processingServer =new ProcessingServer(ps);
    }

    public void connectToPeer(String peer) {
        try {
            final WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    processingServer.write(this, "客户端连接成功");
                    //查询节点的上一个区块链
                    processingServer.write(this,processingServer.queryLastestBlockMsg());
                    //查询交易消息
                    processingServer.write(this,processingServer.queryTransactionMsg());
                    //查询打包交易
                    processingServer.write(this,processingServer.queryPackedTransactionMsg());
                    //查询钱包
                    processingServer.write(this,processingServer.queryWalletMsg());
                    processingServer.getSockets().add(this);
                }

                @Override
                public void onMessage(String msg) {
                    processingServer.handleMessage(this,msg,processingServer.getSockets());
                }

                @Override
                public void onClose(int i, String msg, boolean b) {
                    System.out.println("connection failed");
                    processingServer.getSockets().remove(this);
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("connection failed");
                    processingServer.getSockets().remove(this);
                }
            };
            socketClient.connect();
        } catch (URISyntaxException e) {
            System.out.println("p2p connect is error:" + e.getMessage());
        }
    }
}


