package com.qjx.blockchain.qblockchain.basemodel;

import java.io.Serializable;

/**
 * Created by caomaoboy 2019-11-05
 **/
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    public Integer getHead() {
        return head;
    }

    public void setHead(Integer head) {
        this.head = head;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Message(){}
    public Message(Integer head){ this(head,null);};
    public Message(Integer head,String content){
        this.head = head;
        this.content =content;
        this.timeStamp = String.valueOf(System.currentTimeMillis());

    }
    /**
     * 用于发送消息 包含消息时间戳 消息内容 消息头
     */
    private Integer head;
    private String timeStamp;
    private String content;
}
