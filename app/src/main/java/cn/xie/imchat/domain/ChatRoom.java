package cn.xie.imchat.domain;

/**
 * @author xiejinbo
 * @date 2019/10/8 0008 15:39
 */
public class ChatRoom {
    private String jid;
    private String roomName;

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
