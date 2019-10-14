package cn.xie.imchat.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.PresenceTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatMessage;
import cn.xie.imchat.domain.ChatRoom;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.domain.LoginUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.MyMessage;
import cn.xie.imchat.utils.Util;

public class ChatService extends Service {
    private Context context;
    private DBManager dbManager;
    private StanzaListener packetMessageListener, packetUnsubscribeListener, packetUnsubscribedListener, packetSubscribedListener,packetSubscribeListener;
    private Message message;
    private String fromId;
    public ChatService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        dbManager = new DBManager(context);
        judgeLoginXMPP();
        createThreadPool();
        return START_STICKY;
    }

    /**
     * 启动线程池
     */
    private void createThreadPool(){
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                handlerInitAllFriends.sendEmptyMessage(1);
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                handlerGetAllRooms.sendEmptyMessage(1);
            }
        });
        pool.execute(thread1);
        pool.execute(thread2);
        pool.shutdown();

    }

    /**
     * 判断是否连接并登录了XMpp
     */
    private void judgeLoginXMPP() {
        if (!XmppConnection.getInstance().checkConnection()){
            XmppConnection.getConnection();
        }
        if (!XmppConnection.getInstance().checkAuthenticated()){

            LoginUser user = new LoginUser();
            user = Util.getLoginInfo(context);
            XmppConnection.getInstance().loginXmpp(user.getUserName(),user.getPassword());

        }
        XmppConnection.getInstance().getOfflineMessage(context);
        processPacket();

    }

    /**
     * 加载所有好友
     */
    @SuppressLint("HandlerLeak")
    private Handler handlerInitAllFriends = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            List<ChatUser> chatUsers = dbManager.queryAllChatUser();
            if (chatUsers==null||chatUsers.size()==0){
                getAllFriends();
            }
            //更新登录用户信息
            List<ChatUser> chatUsers1 = XmppConnection.getInstance().searchUsers(Util.getLoginInfo(context).getUserName());
            LoginUser loginUser = Util.getLoginInfo(context);
            for (ChatUser chatUser:chatUsers1){
                if (chatUser.getUserName().equals(loginUser.getUserName())){
                    loginUser.setJid(chatUser.getJid());
                    loginUser.setNickName(chatUser.getNickName());
                    loginUser.setEmail(chatUser.getEmail());
                    Util.saveLoginStatic(context,loginUser);
                    break;
                }
            }
        }
    };

    /**
     * 加载所有好友
     */
    private void getAllFriends() {
        List<RosterEntry> entryList = XmppConnection.getInstance().getAllEntries();

        if (entryList!=null){
            List<ChatUser> chatUsers = new ArrayList<>();
            for (RosterEntry rosterEntry:entryList){
                if ("both".equals(rosterEntry.getType().toString())&&!Util.getLoginInfo(context).getUserName().equals(rosterEntry.getUser().split("@")[0])){
                    List<ChatUser> userInfos = XmppConnection.getInstance().searchUsers(rosterEntry.getUser().split("@")[0]);
                    for (ChatUser chatUser:userInfos){
                        if (rosterEntry.getUser().split("@")[0].equals(chatUser.getUserName())){
                            chatUsers.add(userInfos.get(0));
                            break;
                        }
                    }

                }
            }
            //保存所有好友到数据库
            if (chatUsers!=null&&chatUsers.size()>0){
                dbManager.addChatUserData(chatUsers);
            }
        }

    }

    /**
     * 新消息监控
     */
    private void processPacket() {
        if (packetMessageListener == null) {
            packetMessageListener = new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    setMessagePacket(packet);
                }
            };
        }
        //对方拒绝加为好友
        if (packetUnsubscribeListener == null) {
            packetUnsubscribeListener = new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    setUnsubscribedPacket(packet);
                }
            };
        }
        //对方拒绝加为好友
        if (packetUnsubscribedListener == null) {
            packetUnsubscribedListener = new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    setUnsubscribedPacket(packet);
                }
            };
        }

        //对方同意加为好友
        if (packetSubscribedListener == null) {
            packetSubscribedListener = new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    setSubscribedPacket(packet);
                }
            };
        }
        //收到好友添加申请
        if (packetSubscribeListener == null) {
            packetSubscribeListener = new StanzaListener() {
                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                    getFriendApplyPacket(packet);
                }
            };
        }

        XmppConnection.getInstance().removeAsyncStanzaListener(packetMessageListener);
        XmppConnection.getInstance().addAsyncStanzaListener(packetMessageListener, StanzaTypeFilter.MESSAGE);

        XmppConnection.getInstance().removeSyncStanzaListener(packetUnsubscribeListener);
        XmppConnection.getInstance().addSyncStanzaListener(packetUnsubscribeListener, PresenceTypeFilter.UNSUBSCRIBE);

        XmppConnection.getInstance().removeSyncStanzaListener(packetUnsubscribedListener);
        XmppConnection.getInstance().addSyncStanzaListener(packetUnsubscribedListener, PresenceTypeFilter.UNSUBSCRIBED);

        XmppConnection.getInstance().removeSyncStanzaListener(packetSubscribedListener);
        XmppConnection.getInstance().addSyncStanzaListener(packetSubscribedListener, PresenceTypeFilter.SUBSCRIBED);

        XmppConnection.getInstance().removeSyncStanzaListener(packetSubscribeListener);
        XmppConnection.getInstance().addSyncStanzaListener(packetSubscribeListener, PresenceTypeFilter.SUBSCRIBE);
    }

    /**
     * 对方拒绝添加好友
     * @param packet
     */
    private void setUnsubscribedPacket(Stanza packet) {
        Toast.makeText(context,packet.getFrom().toString().split("@")[0]+": 拒绝添加好友",Toast.LENGTH_SHORT).show();
    }

    /**
     * 对方同意添加好友
     * @param packet
     */
    private void setSubscribedPacket(Stanza packet) {
        final String applyJid = packet.getFrom().toString();
        LoginUser loginUser = Util.getLoginInfo(context);
        XmppConnection.getInstance().acceptFriendApply(applyJid);
        XmppConnection.getInstance().addUser(applyJid,loginUser.getUserName());
        dbManager.deleteApplyMessage("chatMessage","sendname=? and myself=?", new String[]{applyJid.split("@")[0],"APPLY"});
        List<ChatUser> userInfos = XmppConnection.getInstance().searchUsers(applyJid.split("@")[0]);
        List<ChatUser> friendList = new ArrayList<>();
        for (ChatUser chatUser:userInfos){
            if (applyJid.split("@")[0].equals(chatUser.getUserName())){
                friendList.add(chatUser);
                break;
            }
        }
        if (friendList!=null&&friendList.size()>0){
            dbManager.addChatUserData(friendList);
        }
        ChatMessage chatMessage = new ChatMessage(loginUser.getUserName(), applyJid.split("@")[0], "对方同意添加好友", "IN", Util.getNewDate(), packet.getPacketID(),"chat","");
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(chatMessage);
        dbManager.addChatMessageData(chatMessages);
    }

    /**
     * 接收到好友申请
     * @param packet
     */
    private void getFriendApplyPacket(Stanza packet) {
        String res1[] = packet.getFrom().toString().split("@");
        String applyName="";
        if (res1.length == 2) {
            applyName = res1[0];
        }
        String username = Util.getLoginInfo(context).getUserName();
        ChatMessage chatMessage = new ChatMessage(username, applyName, "", "APPLY", Util.getNewDate(), packet.getPacketID(), "subscribe","");
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(chatMessage);
        dbManager.addChatMessageData(chatMessages);
    }

    /**
     * 后台聊天消息监控
     * @param packet
     */
    private void setMessagePacket(Stanza packet) {
        message = (Message) packet;
        if (!TextUtils.isEmpty(message.getBody())) {
            String res1[] = packet.getFrom().toString().split("@");
            String res2[] = packet.getFrom().toString().split("/");
            if (res1.length == 2) {
                fromId = res1[0];
            }
            if (message.getType().toString().equals("groupchat")) {
                sendNotification(message, fromId, res2[1]);
            } else {
                sendNotification(message, fromId, "");
            }
        }
    }

    /**
     * 后台聊天消息处理
     * @param message
     * @param fromId
     * @param sendId
     */
    private void sendNotification(Message message,String fromId,String sendId){
        MyMessage myMessage = JSON.parseObject(message.getBody(), MyMessage.class);
        if (TextUtils.isEmpty(myMessage.sendtime)){
            return;
        }
        Log.e("xjbo","myMessage: "+JSON.toJSON(myMessage));
        if (TextUtils.isEmpty(sendId)) {
            //单聊
            ChatUser chatUser = dbManager.queryChatUserByName(fromId);
            if (chatUser != null) {
                String username = Util.getLoginInfo(context).getUserName();
                ChatMessage chatMessage = new ChatMessage(username, fromId, myMessage.data.toString(), "IN",myMessage.sendtime, message.getPacketID(), myMessage.type,"");
                List<ChatMessage> chatMessages = new ArrayList<>();
                chatMessages.add(chatMessage);
                dbManager.addChatMessageData(chatMessages);
            }
        }else {
            //群聊
            ChatRoom chatRoom = dbManager.queryAllChatRoomByJid(fromId+"@conference.xie-pc");
            if (chatRoom != null) {
                String username = Util.getLoginInfo(context).getUserName();
                //不是自己发送的消息
                if (!username.equals(sendId)){
                    ChatMessage chatMessage = new ChatMessage(username, fromId, myMessage.data.toString(), "IN",myMessage.sendtime, message.getPacketID(), myMessage.type,sendId);
                    List<ChatMessage> chatMessages = new ArrayList<>();
                    chatMessages.add(chatMessage);
                    dbManager.addChatMessageData(chatMessages);
                }

            }
        }

    }

    /**
     * 加载所有房间
     */
    @SuppressLint("HandlerLeak")
    private Handler handlerGetAllRooms = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            XmppConnection.getInstance().getAllRooms(context);
        }
    };


}
