package cn.xie.imchat.domain;

import java.io.Serializable;

/**
 * @author xiejinbo
 * @date 2019/9/24 0024 16:19
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = -2790672475566638165L;
    private String sendName;
    private String userName;
    private String type;
    private String sendtime;
    private String messageId;
    private int subType;
    private String myself;
    private String data;
    private String sendId;
    public ChatMessage(){

    }

    public ChatMessage(String userName, String sendName,String msgData,String myself,String sendtime,String messageId,String type,String sendId){
        this.userName = userName;
        this.sendName = sendName;
        this.data = msgData;
        this.myself = myself;
        this.sendtime = sendtime;
        this.messageId = messageId;
        this.type = type;
        this.sendId = sendId;
    }

    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    public String getMyself() {
        return myself;
    }

    public void setMyself(String myself) {
        this.myself = myself;
    }



    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSendtime() {
        return sendtime;
    }

    public void setSendtime(String sendtime) {
        this.sendtime = sendtime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }
}
