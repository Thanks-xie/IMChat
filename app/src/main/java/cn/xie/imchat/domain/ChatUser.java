package cn.xie.imchat.domain;

/**
 * @author xiejinbo
 * @date 2019/9/20 0020 11:19
 */
public class ChatUser {
    private String nickName;
    private String jid;
    private String userName;
    private String email;
    private boolean checkbox;

    public ChatUser(){

    }

    public ChatUser(LoginUser loginUser){
        this.userName = loginUser.getUserName();
        this.nickName = loginUser.getNickName();
        this.jid = loginUser.getJid();
        this.email = loginUser.getEmail();
    }

    public boolean isCheckbox() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
