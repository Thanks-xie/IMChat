package cn.xie.imchat.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author xiejinbo
 * @date 2019/9/20 0020 11:08
 */
public class DBHelper extends SQLiteOpenHelper {
    private static String DEFAULT_DATA = "chat.db";
    private static int DEFAULT_VERSION = 1;
    private Context mContext;

    public DBHelper(Context context){
        super(context,DEFAULT_DATA, null,DEFAULT_VERSION,null);
        mContext = context;
    }

    /**
     * 数据表的创建
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String create_user="create table user ("
                + "id integer primary key autoincrement, "
                + "username text, "
                + "nickname text, "
                + "email text, "
                + "jid text)";
        //执行建表语句
        db.execSQL(create_user);
        final String create_chatMsg="create table chatMessage ("
                + "history_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username text, "
                + "sendname text, "
                + "data text, "
                + "myself text, "
                + "sendtime text, "
                + "messageId text, "
                + "sendId text, "
                + "type text)";
        //执行建表语句
        db.execSQL(create_chatMsg);
        final String create_room="create table chatRoom ("
                + "id integer primary key autoincrement, "
                + "roomName text, "
                + "jid text)";
        //执行建表语句
        db.execSQL(create_room);
    }

    /**
     * 数据表的更新
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
