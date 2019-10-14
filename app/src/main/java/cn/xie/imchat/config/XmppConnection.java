package cn.xie.imchat.config;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterLoadedListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.xie.imchat.domain.ChatMessage;
import cn.xie.imchat.domain.ChatRoom;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.domain.LoginUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.NickNameIQ;
import cn.xie.imchat.utils.Util;

/**
 * @author xiejinbo
 * @date 2019/9/19 0019 14:30
 */
public class XmppConnection extends XMPPTCPConnection {
    private static XmppConnection connection = null;
    private static int SERVER_PORT = 5222;
    private static String SERVER_HOST = "192.168.0.130";
    private static String SERVER_NAME = "xie-pc";
    private ConnectionListener connectionListener;
    private DBManager dbManager;

    public XmppConnection(XMPPTCPConnectionConfiguration config) {
        super(config);
    }


    /**
     * 单例模式
     *
     * @return
     */
    public synchronized static XmppConnection getInstance() {

        return getConnection();
    }

    /**
     * 创建连接
     *
     * @return
     */
    public static XmppConnection getConnection() {
        if (connection == null) {
            // 开线程打开连接，避免在主线程里面执行HTTP请求
            // Caused by: android.os.NetworkOnMainThreadException
            /*new Thread(new Runnable() {
                @Override
                public void run() {*/
            openConnection();
               /* }
            }).start();
*/
        }
        return connection;
    }

    /**
     * 判断是否已连接
     *
     * @return
     */
    public boolean checkConnection() {
        return null != connection && connection.isConnected();
    }

    /**
     * 打开连接
     *
     * @return
     */
    public static boolean openConnection() {
        try {
            if (null == connection || !connection.isAuthenticated()) {
                SmackConfiguration.DEBUG = true;
                XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
                //设置openfire主机IP
                config.setHostAddress(InetAddress.getByName(SERVER_HOST));
                //设置openfire服务器名称
                config.setXmppDomain(SERVER_NAME);
                //设置端口号：默认5222
                config.setPort(SERVER_PORT);
                //设置客服端类型
                config.setResource("Android");
                //禁用SSL连接
                config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).setCompressionEnabled(false);

                //设置离线状态
                config.setSendPresence(false);
                //设置开启压缩，可以节省流量
                config.setCompressionEnabled(true);


                //需要经过同意才可以添加好友
                Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

                // 将相应机制隐掉
                //SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
                //SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

                connection = new XmppConnection(config.build());
                connection.connect();// 连接到服务器
                Roster.getInstanceFor(connection).setSubscriptionMode(Roster.SubscriptionMode.manual);
                ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
                // 重联间隔5秒
                reconnectionManager.setFixedDelay(5);
                reconnectionManager.enableAutomaticReconnection();//开启重联机制

                // 维持ping
                PingManager.setDefaultPingInterval(30);
                PingManager pingManager = PingManager.getInstanceFor(connection);
                pingManager.registerPingFailedListener(new PingFailedListener() {
                    @Override
                    public void pingFailed() {
                        Log.e("xjbo", "pingFailed");
                    }
                });
                return true;
            }
        } catch (SmackException | IOException | XMPPException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭连接
     *
     * @return
     */
    public void closeConnection() {
        if (connection != null) {
            connection.removeConnectionListener(connectionListener);
            if (connection.isConnected()) {
                connection.disconnect();
            }
            connection = null;
        }
        Log.d("closeConnection", "关闭连接");
    }

    /**
     * 判断是否已经通过身份验证，是否已经完成登录
     *
     * @return
     */
    public boolean checkAuthenticated() {
        return null != connection && connection.isAuthenticated();
    }

    /**
     * xmpp登录
     *
     * @param userName 用户名
     * @param password 密码
     * @return
     */
    public boolean loginXmpp(String userName, String password) {
        try {
            if (getConnection() == null) {
                return false;
            }
            getConnection().login(userName, password);
            // 更改在线状态
            setPresence(5);

            // 添加连接监听
            connectionListener = new ConnectionListener() {
                @Override
                public void connected(XMPPConnection connection) {
                    Log.d("xjbo connected", "已经连接");
                }

                @Override
                public void authenticated(XMPPConnection connection, boolean resumed) {
                    Log.d("xjbo authenticated", "已经登录");
                }

                @Override
                public void connectionClosed() {
                    Log.d("xjbo connectionClosed", "连接关闭");
                }

                @Override
                public void connectionClosedOnError(Exception e) {
                    Log.e("connectionClosedOnError", "Exception: " + e.toString());
                    Log.d("connectionClosedOnError", "连接错误");
                }
            };
            getConnection().addConnectionListener(connectionListener);
            return true;

        } catch (IOException | InterruptedException | SmackException | XMPPException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 注册
     *
     * @param account  注册帐号
     * @param password 注册密码
     * @return 1、注册成功 0、注册失败
     */
    public boolean register(String account, String password) {
        if (getConnection() == null) {
            return false;
        }
        try {
            AccountManager accountManager = AccountManager.getInstance(connection);
            if (accountManager.supportsAccountCreation()) {
                accountManager.sensitiveOperationOverInsecureConnection(true);
                accountManager.createAccount(Localpart.from(account), password);
                return true;
            }
        } catch (XmppStringprepException | InterruptedException | XMPPException | SmackException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 更改用户状态
     */
    public void setPresence(int code) {
        XMPPConnection con = getConnection();
        if (con == null) {
            return;
        }
        Presence presence;
        try {
            switch (code) {
                case 0:
                    presence = new Presence(Presence.Type.available);
                    con.sendStanza(presence);
                    Log.v("state", "设置在线");
                    break;
                case 1:
                    presence = new Presence(Presence.Type.available);
                    presence.setMode(Presence.Mode.chat);
                    con.sendStanza(presence);
                    Log.v("state", "设置Q我吧");
                    break;
                case 2:
                    presence = new Presence(Presence.Type.available);
                    presence.setMode(Presence.Mode.dnd);
                    con.sendStanza(presence);
                    Log.v("state", "设置忙碌");
                    break;
                case 3:
                    presence = new Presence(Presence.Type.available);
                    presence.setMode(Presence.Mode.away);
                    con.sendStanza(presence);
                    Log.v("state", "设置离开");
                    break;
                case 4:
//                    Roster roster = con.getRoster();
//                    Collection<RosterEntry> entries = roster.getEntries();
//                    for (RosterEntry entry : entries) {
//                        presence = new Presence(Presence.Type.unavailable);
//                        presence.setPacketID(Packet.ID_NOT_AVAILABLE);
//                        presence.setFrom(con.getUser());
//                        presence.setTo(entry.getUser());
//                        con.sendPacket(presence);
//                        Log.v("state", presence.toXML());
//                    }
//                    // 向同一用户的其他客户端发送隐身状态
//                    presence = new Presence(Presence.Type.unavailable);
//                    presence.setPacketID(Packet.ID_NOT_AVAILABLE);
//                    presence.setFrom(con.getUser());
//                    presence.setTo(StringUtils.parseBareAddress(con.getUser()));
//                    con.sendStanza(presence);
//                    Log.v("state", "设置隐身");
//                    break;
                case 5:
                    presence = new Presence(Presence.Type.unavailable);
                    con.sendStanza(presence);
                    Log.v("state", "设置离线");
                    break;
                default:
                    break;
            }
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有分组
     *
     * @return 所有分组集合
     */
    public List<RosterGroup> getGroups() {
        if (getConnection() == null) {
            return null;
        }
        List<RosterGroup> groupList = new ArrayList<>();
        Collection<RosterGroup> rosterGroup = Roster.getInstanceFor(connection).getGroups();
        for (RosterGroup aRosterGroup : rosterGroup) {
            groupList.add(aRosterGroup);
        }
        return groupList;
    }

    /**
     * 获取某个分组里面的所有好友
     *
     * @param groupName 组名
     * @return List<RosterEntry>
     */
    public List<RosterEntry> getEntriesByGroup(String groupName) {
        if (getConnection() == null) {
            return null;
        }
        List<RosterEntry> EntriesList = new ArrayList<>();
        RosterGroup rosterGroup = Roster.getInstanceFor(connection).getGroup(groupName);
        Collection<RosterEntry> rosterEntry = rosterGroup.getEntries();
        for (RosterEntry aRosterEntry : rosterEntry) {
            EntriesList.add(aRosterEntry);
        }
        return EntriesList;
    }

    /**
     * 获取所有好友信息
     *
     * @return List<RosterEntry>
     */
    public List<RosterEntry> getAllEntries() {
        if (getConnection() == null) {
            return null;
        }
        final List<RosterEntry> enlist = new ArrayList<>();
        if (connection.isAuthenticated()) {
            Collection<RosterEntry> rosterEntry = Roster.getInstanceFor(connection).getEntries();
            for (RosterEntry aRosterEntry : rosterEntry) {
                enlist.add(aRosterEntry);
            }

        } else {
            Roster roster = Roster.getInstanceFor(connection);
            roster.addRosterLoadedListener(new RosterLoadedListener() {
                @Override
                public void onRosterLoaded(Roster roster1) {
                    Collection<RosterEntry> entries = roster1.getEntries();
                    for (RosterEntry aRosterEntry : entries) {
                        enlist.add(aRosterEntry);
                    }
                }

                @Override
                public void onRosterLoadingFailed(Exception exception) {

                }
            });
        }
        return enlist;
    }

    /**
     * 获取用户VCard信息
     *
     * @param user user
     * @return VCard
     */
    public VCard getUserVCard(String user) {
        if (getConnection() == null) {
            return null;
        }
        VCard vcard = new VCard();
        try {
            vcard = VCardManager.getInstanceFor(getConnection()).loadVCard(JidCreate.entityBareFrom(user));
        } catch (XmppStringprepException | SmackException | InterruptedException | XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        }

        return vcard;
    }

    /**
     * 获取用户头像信息
     *
     * @param user user
     * @return Drawable
     */
    public Drawable getUserImage(String user) {
        if (getConnection() == null)
        {
            return null;
        }
        ByteArrayInputStream bais = null;
        try {
            VCard vcard = new VCard();
            // 加入这句代码，解决No VCard for
            ProviderManager.addIQProvider("vCard", "vcard-temp",
                    new org.jivesoftware.smackx.vcardtemp.provider.VCardProvider());
            if (user == null || user.equals("") || user.trim().length() <= 0) {
                return null;
            }
            try {
                VCardManager.getInstanceFor(getConnection()).loadVCard(JidCreate.entityBareFrom(user));
            } catch (XmppStringprepException | SmackException | InterruptedException | XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            }

            if (vcard.getAvatar() == null)
            {
                return null;
            }
            bais = new ByteArrayInputStream(vcard.getAvatar());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return Util.InputStream2Drawable(bais);
    }

    /**
     * 创建一个群
     *
     * @param groupName groupName
     * @return boolean
     */
    public boolean addGroup(String groupName) {
        if (getConnection() == null) {
            return false;
        }
        try {
            Roster.getInstanceFor(connection).createGroup(groupName);
            Log.v("addGroup", groupName + "創建成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除分组
     *
     * @param groupName groupName
     * @return boolean
     */
    public boolean removeGroup(String groupName) {
        return true;
    }

    /**
     * 添加好友 无分组
     *
     * @param userName userName
     * @param jid      name
     * @return boolean
     */
    public boolean addUser(String jid, String userName) {
        if (getConnection() == null) {
            return false;
        }
        try {
            Roster.getInstanceFor(connection).createEntry(JidCreate.entityBareFrom(jid), userName, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 添加好友 有分组
     *
     * @param userName  userName
     * @param name      name
     * @param groupName groupName
     * @return boolean
     */
    public boolean addUser(String userName, String name, String groupName) {
        if (getConnection() == null) {
            return false;
        }
        try {
            Presence subscription = new Presence(Presence.Type.subscribed);
            subscription.setTo(JidCreate.entityBareFrom(userName));
            userName += "@" + getConnection().getXMPPServiceDomain();
            getConnection().sendStanza(subscription);
            Roster.getInstanceFor(connection).createEntry(JidCreate.entityBareFrom(userName), name,
                    new String[]{groupName});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除好友
     *
     * @param userName userName
     * @return boolean
     */
    public boolean removeUser(String userName) {
        if (getConnection() == null) {
            return false;
        }
        try {
            RosterEntry entry = null;
            if (userName.contains("@")) {
                entry = Roster.getInstanceFor(connection).getEntry(JidCreate.entityBareFrom(userName));
            } else {
                entry = Roster.getInstanceFor(connection).getEntry(JidCreate.entityBareFrom(
                        userName + "@" + getConnection().getXMPPServiceDomain()));
            }
            if (entry == null) {
                entry = Roster.getInstanceFor(connection).getEntry(JidCreate.entityBareFrom(userName));
            }
            Roster.getInstanceFor(connection).removeEntry(entry);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 查询用户
     *
     * @param userName userName
     * @return List<HashMap < String, String>>
     */
    public List<ChatUser> searchUsers(String userName) {
        if (getConnection() == null) {
            return null;
        }
        ChatUser user;  //自定义的用户实体类
        List<ChatUser> userInfos = new ArrayList<>();
        try {
            UserSearchManager usm = new UserSearchManager(getConnection());
            //本例用的smack:4.3.4版本，getSearchForm方法传的是DomainBareJid类型，而之前的版本是String类型，大家在使用的时候需要特别注意
            //而转换DomainBareJid的方式如下面的例子所示：JidCreate.domainBareFrom("search." + getConnection().getXMPPServiceDomain())
            Form searchForm = usm.getSearchForm(JidCreate.domainBareFrom("search." + getConnection().getXMPPServiceDomain()));
            if (searchForm == null) {
                return null;
            }
            //这里设置了Username为true代码是根据用户名查询用户，search代表查询字段
            //smack:4.3.4版本是下面的字段，但之前的版本会有些不一样，所以在用的时候最好看下xmpp交互的log，里面有相应的字段值
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", userName);
            ReportedData data = usm.getSearchResults(answerForm, JidCreate.domainBareFrom("search." + getConnection().getXMPPServiceDomain()));
            List<ReportedData.Row> rowList = data.getRows();

            //此处返回的字段名如下所示，之前的版本可能有所变化，使用的时候需要注意
            for (ReportedData.Row row : rowList) {
                user = new ChatUser();
                String jid = row.getValues("jid").toString();
                String username = row.getValues("Username").toString();
                String name = row.getValues("Name").toString();
                String email = row.getValues("Email").toString();
                user.setJid(jid.substring(jid.indexOf("[") + 1, jid.indexOf("]")));
                user.setUserName(username.substring(username.indexOf("[") + 1, username.indexOf("]")));
                user.setNickName(name.substring(name.indexOf("[") + 1, name.indexOf("]")));
                user.setEmail(email.substring(email.indexOf("[") + 1, email.indexOf("]")));
                userInfos.add(user);
                // 若存在，则有返回,UserName一定非空，其他两个若是有设，一定非空
            }
        } catch (SmackException | InterruptedException | XmppStringprepException | XMPPException e) {
            e.printStackTrace();
        }
        return userInfos;
    }

    /**
     * 修改心情
     *
     * @param status
     */
    public void changeStateMessage(String status) {
        if (getConnection() == null) {
            return;
        }
        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus(status);
        try {
            getConnection().sendStanza(presence);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改密码
     *
     * @return true成功
     */
    public boolean changePassword(String pwd) {
        if (getConnection() == null) {
            return false;
        }
        try {
            AccountManager accountManager = AccountManager.getInstance(connection);
            if (accountManager.supportsAccountCreation()) {
                accountManager.sensitiveOperationOverInsecureConnection(true);
                accountManager.changePassword(pwd);

            }
            return true;
        } catch (SmackException | InterruptedException | XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取所有聊天室
     */
    public void getAllRooms(Context context) {
        if (getConnection() == null) {
            return;
        }
        Collection<HostedRoom> hostrooms;
        List<ChatRoom> chatRooms = new ArrayList<>();
        LoginUser loginUser = Util.getLoginInfo(context);
        try {
            hostrooms = MultiUserChatManager.getInstanceFor(getConnection()).getHostedRooms(
                    JidCreate.domainBareFrom("conference." + getConnection().getXMPPServiceDomain()));

            for (HostedRoom entry : hostrooms) {
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setJid(entry.getJid().toString());
                chatRoom.setRoomName(entry.getName());
                chatRooms.add(chatRoom);
                joinMultiUserChat(loginUser.getUserName(),entry.getName());

            }
            if (chatRooms != null && chatRooms.size() > 0) {
                dbManager.addChatRoomData(chatRooms);
            }
        } catch (XMPPException | XmppStringprepException | InterruptedException | SmackException e) {
            e.printStackTrace();
            return;
        }
    }
    /**
     * 创建群聊房间
     *
     * @param roomName 群名称
     * @param users    创建群完成后添加的群成员
     * @return
     */
    public boolean createChatRoom(Context context, String roomName, List<ChatUser> users) {
        try {//组装群聊jid,这里需要注意一下,群jid的格式就是  群名称@conference.openfire服务器名称
            String jid = roomName + "@conference." + getConnection().getXMPPServiceDomain();
            EntityBareJid groupJid = null;

            groupJid = JidCreate.entityBareFrom(jid);

            MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(getInstance());
            MultiUserChat muc = manager.getMultiUserChat(groupJid);
            muc.create(Resourcepart.from(roomName));
            // 获得聊天室的配置表单
            Form form = muc.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();

            // 设置聊天室的新拥有者
            List<String> owners = new ArrayList<>();
            owners.add(Util.getLoginInfo(context).getJid());

            //这里的用户实体我要说一下，因为这是我这个项目的实体，实际上这里只需要知道用户的jid获者名称就可以了
            if (users != null && !users.isEmpty()) {
                for (int i = 0; i < users.size(); i++) {  //添加群成员,用户jid格式和之前一样 用户名@openfire服务器名称
                    EntityBareJid userJid = JidCreate.entityBareFrom(users.get(i).getJid());
                    owners.add(userJid + "");
                }
            }


            submitForm.setAnswer("muc#roomconfig_roomowners", owners);
            //设置为公共房间
            submitForm.setAnswer("muc#roomconfig_publicroom", true);
            // 设置聊天室是持久聊天室，即将要被保存下来
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            // 房间仅对成员开放
            submitForm.setAnswer("muc#roomconfig_membersonly", false);
            // 允许占有者邀请其他人
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            //进入不需要密码
            submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", false);


            // 能够发现占有者真实 JID 的角色
            // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
            // 登录房间对话
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // 仅允许注册的昵称登录
            submitForm.setAnswer("x-muc#roomconfig_reservednick", false);
            // 允许使用者修改昵称
            submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);
            // 允许用户注册房间
            submitForm.setAnswer("x-muc#roomconfig_registration", false);
            // 发送已完成的表单（有默认值）到服务器来配置聊天室
            muc.sendConfigurationForm(submitForm);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 加入会议室
     *
     * @param user      昵称
     * @param roomsName 会议室名
     */
    public MultiUserChat joinMultiUserChat(String user, String roomsName) {
        if (getConnection() == null) {
            return null;
        }
        try {
            //获取群管理对象
            MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
            //通过群管理对象获取该群房间对象
            MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(JidCreate.entityBareFrom(roomsName + "@conference." + getConnection().getXMPPServiceDomain()));

            MucEnterConfiguration.Builder builder = multiUserChat.getEnterConfigurationBuilder(Resourcepart.from(user));
            //只获取最后0条历史记录
            builder.requestMaxCharsHistory(0);
            MucEnterConfiguration mucEnterConfiguration = builder.build();
            //加入群
            multiUserChat.join(mucEnterConfiguration);
            Log.i("MultiUserChat", "会议室【" + roomsName + "】加入成功........");
            return multiUserChat;
        } catch (XMPPException | XmppStringprepException | InterruptedException | SmackException e) {
            e.printStackTrace();
            Log.i("MultiUserChat", "会议室【" + roomsName + "】加入失败........");
            return null;
        }
    }

    /**
     * 发送群组聊天消息
     *
     * @param muc     muc
     * @param message 消息文本
     */
        public void sendGroupMessage(MultiUserChat muc, Message message) {
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询会议室成员名字
     *
     * @param muc
     */
    public List<String> findMulitUser(MultiUserChat muc) {
        if (getConnection() == null) {
            return null;
        }
        List<String> listUser = new ArrayList<>();
        List<EntityFullJid> it = muc.getOccupants();
        // 遍历出聊天室人员名称
        for (EntityFullJid entityFullJid : it) {
            // 聊天室成员名字
            String name = entityFullJid.toString();
            listUser.add(name);
        }
        return listUser;
    }

    /**
     * 创建聊天窗口
     *
     * @param JID JID
     * @return Chat
     */
    public Chat getFriendChat(String JID) {
        try {
            return ChatManager.getInstanceFor(XmppConnection.getConnection())
                    .chatWith(JidCreate.entityBareFrom(JID));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送单人聊天消息
     *
     * @param chat    chat
     * @param message 消息文本
     */
    public void sendSingleMessage(Chat chat, Message message) {
        try {
            chat.send(message);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发消息
     *
     * @param chat    chat
     * @param muc     muc
     * @param message message
     */
    public void sendMessage(Chat chat, MultiUserChat muc, Message message) {


        if (chat != null) {
            sendSingleMessage(chat, message);
        } else if (muc != null) {
            sendGroupMessage(muc, message);
        }
    }

    /**
     * 发送文件
     *
     * @param user
     * @param filePath
     */
    public void sendFile(String user, String filePath) {
        if (getConnection() == null) {
            return;
        }
        // 创建文件传输管理器
        FileTransferManager manager = FileTransferManager.getInstanceFor(getConnection());

        // 创建输出的文件传输
        OutgoingFileTransfer transfer = null;
        try {
            transfer = manager.createOutgoingFileTransfer(JidCreate.entityFullFrom(user));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        // 发送文件
        try {
            if (transfer != null) {
                transfer.sendFile(new File(filePath), "You won't believe this!");
            }
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取离线消息
     *
     * @return
     */
    public void getOfflineMessage(Context context) {
        if (getConnection() == null) {
            return;
        }
        try {
            dbManager = new DBManager(context);
            OfflineMessageManager offlineManager = new OfflineMessageManager(getConnection());
            List<Message> messageList = offlineManager.getMessages();
            String username = Util.getLoginInfo(context).getUserName();
            List<ChatMessage> chatMessages = new ArrayList<>();
            for (Message message : messageList) {
                if (!TextUtils.isEmpty(message.getBody())) {
                    ChatMessage chatMessage = new ChatMessage(username, message.getFrom().toString().split("@")[0], message.getBody(), "IN", Util.getNewDate(), message.getPacketID(), message.getType().toString(),"");
                    chatMessages.add(chatMessage);
                }
            }
            if (chatMessages != null && chatMessages.size() > 0) {
                dbManager.addChatMessageData(chatMessages);
            }
            setPresence(0);
            offlineManager.deleteMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断用户是否在线
     *
     * @param jid
     * @return 0代表不在线，1代表在线
     */
    public int IsUserOnLine(String jid) {
        Roster roster = Roster.getInstanceFor(connection);
        try {
            Presence presence = roster.getPresence(JidCreate.bareFrom(jid));
            if (presence.isAvailable()) {
                return 1;
            } else {
                return 0;
            }
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 同意添加好友
     *
     * @param applyJid
     */
    public void acceptFriendApply(String applyJid) {
        try {
            if (getConnection() == null) {
                return;
            }
            Presence presenceRes = new Presence(Presence.Type.subscribed);
            presenceRes.setTo(applyJid);
            getConnection().sendStanza(presenceRes);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拒绝添加好友
     *
     * @param applyJid
     */
    public void refuseFriendApply(String applyJid) {
        try {
            if (getConnection() == null) {
                return;
            }
            Presence presenceRes = new Presence(Presence.Type.unsubscribe);
            presenceRes.setTo(applyJid);
            getConnection().sendStanza(presenceRes);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改好友备注名
     *
     * @param newNickName
     * @param loginId     登录者id
     * @param changeId    被修改者id
     * @return
     */
    public boolean modifyNickName(String newNickName, String loginId, String changeId) {
        try {
            if (getConnection() == null) {
                return false;
            }
            NickNameIQ nickNameIQ = new NickNameIQ("query", "jabber:iq:roster", changeId + "@xie-pc", newNickName);
            nickNameIQ.setType(IQ.Type.set);
            nickNameIQ.setStanzaId(loginId + "@xie-pc/" + changeId);
            getConnection().sendStanza(nickNameIQ);
            return true;
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }

    }




}
