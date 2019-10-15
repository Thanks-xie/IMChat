package cn.xie.imchat.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.xie.imchat.domain.ChatMessage;
import cn.xie.imchat.domain.ChatRoom;
import cn.xie.imchat.domain.ChatUser;

/**
 * @author xiejinbo
 * @date 2019/9/20 0020 11:17
 */
public class DBManager {
    private DBHelper dbHelper;
    private SQLiteDatabase database;

    public DBManager(Context context){
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    /**
     * 添加好友到数据库
     * @param chatUsers
     * @return
     */
    public boolean addChatUserData(List<ChatUser> chatUsers){
        try {
            database.beginTransaction();
            for (ChatUser chatUser: chatUsers){
                String sql = "INSERT INTO user VALUES(NULL,?,?,?,?)";
                Object[] objects = new Object[]{chatUser.getUserName(),chatUser.getNickName(),chatUser.getEmail(),chatUser.getJid()};
                database.execSQL(sql,objects);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            return false;
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
        return true;
    }

    /**
     * 更新表中数据
     * @param chatUser
     * @return
     */
    public boolean updateChatUserData(ChatUser chatUser){
        try {
            ContentValues values = new ContentValues();
            values.put("jid", chatUser.getJid());
            values.put("username",chatUser.getUserName());
            values.put("nickname",chatUser.getNickName());
            values.put("email",chatUser.getEmail());
            database.beginTransaction();
            database.update("user", values, "username=?", new String[]{chatUser.getUserName()});
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("updateChatUserData","Exception: "+e.toString());
            return false;
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
        return true;
    }

    /**
     * 根据用户名查询用户
     * @param userName
     * @return
     */
    public ChatUser queryChatUserByName(String userName){
        ChatUser chatUser = new ChatUser();
        try {
            if (TextUtils.isEmpty(userName)){
              return null;
            }
            String sql = "select * from user where username=? ";
            Cursor cursor = database.rawQuery(sql, new String[]{userName});
            while (cursor.moveToNext()) {
                chatUser.setUserName(userName);
                chatUser.setNickName(cursor.getString(cursor.getColumnIndex("nickname")));
                chatUser.setEmail(cursor.getString(cursor.getColumnIndex("email")));
                chatUser.setJid(cursor.getString(cursor.getColumnIndex("jid")));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("queryChatUserByName",e.toString());
        }
        return chatUser;
    }
    /**
     * 查询所有好友
     * @return
     */
    public List<ChatUser> queryAllChatUser(){
        List<ChatUser> chatUsers = new ArrayList<>();
        try {
            String sql = "select * from user ";
            Cursor cursor = database.rawQuery(sql,new String[]{});
            while (cursor.moveToNext()) {
                ChatUser chatUser = new ChatUser();
                chatUser.setUserName(cursor.getString(cursor.getColumnIndex("username")));
                chatUser.setNickName(cursor.getString(cursor.getColumnIndex("nickname")));
                chatUser.setEmail(cursor.getString(cursor.getColumnIndex("email")));
                chatUser.setJid(cursor.getString(cursor.getColumnIndex("jid")));
                chatUsers.add(chatUser);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("queryAllChatUser",e.toString());
        }
        return chatUsers;
    }

    /**
     * 模糊查询，包含likeName的用户
     * @param likeName
     * @return
     */
    public List<ChatUser> queryChatUserByLikeName(String likeName){
        List<ChatUser> chatUsers = new ArrayList<>();
        try {
            String sql = "select * from user where username like '%"+likeName+"%'";
            Cursor cursor  = database.rawQuery(sql,new String[]{});
            while (cursor.moveToNext()) {
                ChatUser chatUser = new ChatUser();
                chatUser.setUserName(cursor.getString(cursor.getColumnIndex("username")));
                chatUser.setNickName(cursor.getString(cursor.getColumnIndex("nickname")));
                chatUser.setEmail(cursor.getString(cursor.getColumnIndex("email")));
                chatUser.setJid(cursor.getString(cursor.getColumnIndex("jid")));
                chatUsers.add(chatUser);
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return chatUsers;
    }

    /**
     * 添加聊天信息到数据库
     * @param chatMessages
     * @return
     */
    public boolean addChatMessageData(List<ChatMessage> chatMessages){
        try {
            database.beginTransaction();
            for (ChatMessage chatMessage: chatMessages){
                String sql = "INSERT INTO chatMessage VALUES(NULL,?,?,?,?,?,?,?,?)";
                Object[] objects = new Object[]{chatMessage.getUserName(),chatMessage.getSendName(),chatMessage.getData(),chatMessage.getMyself(),
                chatMessage.getSendtime(),chatMessage.getMessageId(),chatMessage.getSendId(),chatMessage.getType()};
                database.execSQL(sql,objects);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            return false;
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
        return true;
    }

    /**
     * 根据用户名查询最近消息
     * @param userName
     * @return
     */
    public List<ChatMessage> queryChatMessageByName(String userName){
            List<ChatMessage> chatMessages = new ArrayList<>();
        try {
            //此处的group by username,sendname是把同一个人发的消息合并，取最新一条
            String sql = "select * from chatMessage where username=? group by username,sendname order by history_id desc ";
            Cursor cursor = database.rawQuery(sql, new String[]{userName});
            while (cursor.moveToNext()) {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setUserName(userName);
                chatMessage.setSendName(cursor.getString(cursor.getColumnIndex("sendname")));
                chatMessage.setData(cursor.getString(cursor.getColumnIndex("data")));
                chatMessage.setMyself(cursor.getString(cursor.getColumnIndex("myself")));
                chatMessage.setSendtime(cursor.getString(cursor.getColumnIndex("sendtime")));
                chatMessage.setMessageId(cursor.getString(cursor.getColumnIndex("messageId")));
                chatMessage.setSendId(cursor.getString(cursor.getColumnIndex("sendId")));
                chatMessage.setType(cursor.getString(cursor.getColumnIndex("type")));
                chatMessages.add(chatMessage);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("queryChatMessageByName",e.toString());
        }
        return chatMessages;
    }
    /**
     * 根据用户名查询最近消息
     * @param userName
     * @return
     */
    public List<ChatMessage> queryChatMessageByName(String userName,String myself){
        List<ChatMessage> chatMessages = new ArrayList<>();
        try {
            //此处的group by username,sendname是把同一个人发的消息合并，取最新一条
            String sql = "select * from chatMessage where username=? and myself=? group by username,sendname order by history_id desc ";
            Cursor cursor = database.rawQuery(sql, new String[]{userName,myself});
            while (cursor.moveToNext()) {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setUserName(userName);
                chatMessage.setSendName(cursor.getString(cursor.getColumnIndex("sendname")));
                chatMessage.setData(cursor.getString(cursor.getColumnIndex("data")));
                chatMessage.setMyself(cursor.getString(cursor.getColumnIndex("myself")));
                chatMessage.setSendtime(cursor.getString(cursor.getColumnIndex("sendtime")));
                chatMessage.setMessageId(cursor.getString(cursor.getColumnIndex("messageId")));
                chatMessage.setSendId(cursor.getString(cursor.getColumnIndex("sendId")));
                chatMessage.setType(cursor.getString(cursor.getColumnIndex("type")));
                chatMessages.add(chatMessage);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("queryChatMessageByName",e.toString());
        }
        return chatMessages;
    }
    /**
     * 根据用户名查询历史消息
     * @param sendName
     * @return
     */
    public List<ChatMessage> queryHistoryChatMessageByName(String sendName){
        List<ChatMessage> chatMessages = new ArrayList<>();
        try {
            //此处的group by username,sendname是把同一个人发的消息合并，取最新一条
            String sql = "select * from chatMessage where  sendname=? order by history_id ";
            Cursor cursor = database.rawQuery(sql, new String[]{sendName});
            while (cursor.moveToNext()) {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setUserName(cursor.getString(cursor.getColumnIndex("username")));
                chatMessage.setSendName(cursor.getString(cursor.getColumnIndex("sendname")));
                chatMessage.setData(cursor.getString(cursor.getColumnIndex("data")));
                chatMessage.setMyself(cursor.getString(cursor.getColumnIndex("myself")));
                chatMessage.setSendtime(cursor.getString(cursor.getColumnIndex("sendtime")));
                chatMessage.setMessageId(cursor.getString(cursor.getColumnIndex("messageId")));
                chatMessage.setSendId(cursor.getString(cursor.getColumnIndex("sendId")));
                chatMessage.setType(cursor.getString(cursor.getColumnIndex("type")));
                chatMessages.add(chatMessage);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("queryHistoryByName",e.toString());
        }
        return chatMessages;
    }

    /**
     * 删除已操作后的申请信息
     * @param table
     * @param whereClause
     * @param parameter
     * @return
     */
    public boolean deleteData(String table, String whereClause, String[] parameter) {
        try {
            if (table != "" ) {
                database.beginTransaction();
                database.delete(table, whereClause, parameter);
                database.setTransactionSuccessful();
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
        return true;
    }

    /**
     * 添加聊天房间到数据库
     * @param chatRooms
     * @return
     */
    public boolean addChatRoomData(List<ChatRoom> chatRooms){
        try {
            database.beginTransaction();
            for (ChatRoom chatRoom: chatRooms){
                ChatRoom chatRoom1 = queryAllChatRoomByJid(chatRoom.getJid());
                if (chatRoom1!=null&&!TextUtils.isEmpty(chatRoom1.getJid())){
                    ContentValues values = new ContentValues();
                    values.put("roomName",chatRoom.getRoomName());
                    database.update("chatRoom",values,"jid=?" ,new String[]{chatRoom.getJid()});
                }else {
                    String sql = "INSERT INTO chatRoom VALUES(NULL,?,?)";
                    Object[] objects = new Object[]{chatRoom.getRoomName(),chatRoom.getJid()};
                    database.execSQL(sql,objects);
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            return false;
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
        return true;
    }
    /**
     * 根据Jid查询所有聊天室
     * @return
     */
    public ChatRoom queryAllChatRoomByJid(String jid){

        try {
            String sql = "select * from chatRoom where jid=? ";
            Cursor cursor = database.rawQuery(sql,new String[]{jid});
            ChatRoom chatRoom = new ChatRoom();
            while (cursor.moveToNext()) {
                chatRoom.setJid(cursor.getString(cursor.getColumnIndex("jid")));
                chatRoom.setRoomName(cursor.getString(cursor.getColumnIndex("roomName")));
            }
            cursor.close();
            return chatRoom;
        } catch (Exception e) {
            Log.e("queryAllChatRoomByJid",e.toString());
            return null;
        }

    }
    /**
     * 查询所有聊天室
     * @return
     */
    public List<ChatRoom> queryAllChatRoom(){
        List<ChatRoom> chatRooms = new ArrayList<>();
        try {
            String sql = "select * from chatRoom ";
            Cursor cursor = database.rawQuery(sql,new String[]{});
            while (cursor.moveToNext()) {
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setRoomName(cursor.getString(cursor.getColumnIndex("roomName")));
                chatRoom.setJid(cursor.getString(cursor.getColumnIndex("jid")));
                chatRooms.add(chatRoom);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("queryAllChatRoom",e.toString());
        }
        return chatRooms;
    }

}
