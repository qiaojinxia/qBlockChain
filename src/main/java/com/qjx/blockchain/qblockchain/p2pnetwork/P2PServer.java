package com.qjx.blockchain.qblockchain.p2pnetwork;

/**
 * Created by caomaoboy 2019-11-04
 **/


import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jack on 2018/4/1.
 */
public class P2PServer {
    public final static Logger logger = LoggerFactory.getLogger(P2PServer.class);

    public ProcessingServer getProcessingServer() {
        return processingServer;
    }

    private ProcessingServer processingServer;

    public P2PServer(P2PController bc) {
        this.processingServer = new ProcessingServer(bc);
        //this.processingServer.setSockets(sockets);
    }
    public void initP2PServer(int port) {
        final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                //将当前客户端加入websocket池
                //processeServer.getSockets().add(this);
                processingServer.getSockets().add(webSocket);

            }

            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
                processingServer.getSockets().remove(webSocket);
            }

            public void onMessage(WebSocket webSocket, String msg) {
                System.out.println("接收到客户端消息：" + msg);
                processingServer.handleMessage(webSocket,msg,processingServer.getSockets());
            }

            public void onError(WebSocket webSocket, Exception e) {
                System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
                processingServer.getSockets().remove(webSocket);
            }

            @Override
            public void onStart() {
                System.out.println("WebSocket Server端启动...");
            }

        };
        socketServer.start();
        System.out.println("listening websocket p2p port on: " + port);
    }
}


