package cn.xie.imchat.utils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.xie.imchat.R;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.domain.LoginUser;
import cn.xie.imchat.service.ChatService;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author xiejinbo
 * @date 2019/9/19 0019 13:27
 */
public class Util {
    private static String format = "yyyy-MM-dd HH:mm:ss";
    private static SimpleDateFormat sdf = new SimpleDateFormat(format);
    private static Date dateTime;
    public static final int DELAY = 1000;
    private static long lastClickTime = 0;
    public static List<ChatUser> chooseFriends = new ArrayList<>();

    /**
     * 保存登录状态
     * @param context
     * @param loginUser
     */
    public static void saveLoginStatic(Context context,LoginUser loginUser){
        SharedPreferences sharedPreferences =context.getSharedPreferences("login_data",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName",loginUser.getUserName());
        editor.putString("password",loginUser.getPassword());
        editor.putString("jid",loginUser.getJid());
        editor.putString("nickName",loginUser.getNickName());
        editor.putString("email",loginUser.getEmail());
        editor.apply();
    }

    /**
     * 获取登录状态
     * @param context
     * @return
     */
    public static boolean getLoginStatic(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("login_data", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName","");
        if (TextUtils.isEmpty(userName)){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 获取登录用户信息
     * @param context
     * @return
     */
    public static LoginUser getLoginInfo(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("login_data",MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName","");
        String password = sharedPreferences.getString("password","");
        String jid = sharedPreferences.getString("jid","");
        String nickName = sharedPreferences.getString("nickName","");
        String email = sharedPreferences.getString("email","");
        LoginUser user = new LoginUser();
        user.setUserName(userName);
        user.setPassword(password);
        user.setJid(jid);
        user.setNickName(nickName);
        user.setEmail(email);
        return user;
    }

    /**
     * 清除登录数据
     * @param context
     */
    public static void cleanLoginData(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("login_data",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 启动 ChatService服务
     * @param context
     */
    public static void startChatService(Context context){
        if (!isServiceRunning(context, "cn.xie.imchat.service.ChatService")) {
            Intent intent = new Intent(context, ChatService.class);
            context.startService(intent);
        }
    }

    /**
     * 判断服务是否已经启动
     * @param context
     * @param ServiceName
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
        {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证原密码
     * @param context
     * @param oldPassword
     * @return
     */
    public static boolean checkOldPassword(Context context,String oldPassword){
        LoginUser loginUser = getLoginInfo(context);
        if (loginUser.getPassword().equals(oldPassword)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 验证两次输入的密码
     * @param context
     * @param newPassword
     * @param reNewPassword
     * @return
     */
    public static boolean checkNewPassword(Context context,String newPassword,String reNewPassword){
        if (newPassword.equals(reNewPassword)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 设置消息时间格式
     * @param messageDate
     * @param context
     * @return
     */
    public static String setChatMessageDate(String messageDate,Context context) {

        if (!TextUtils.isEmpty(messageDate)){
            messageDate = messageDate.replace("T", " ");
        }
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date dateTime = new Date();
        String date = "";
        String dataNow = "";
        try {
            dataNow = sdf.format(dateTime);
            date = sdf.format(sdf.parse(messageDate));
            String format_ = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf_ = new SimpleDateFormat(format_);
            Date date_ = sdf_.parse(messageDate);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,-1);
            String yesterday = new SimpleDateFormat( "yyyy-MM-dd").format(cal.getTime());
            if (date.equals(dataNow)) { //今天

                String format1 = "HH:mm";
                SimpleDateFormat sdf1 = new SimpleDateFormat(format1);
                return sdf1.format(date_);
            } else if(date.equals(yesterday)){ //昨天
                String format2 = "  HH:mm";
                SimpleDateFormat sdf2 = new SimpleDateFormat(format2);
                return context.getResources().getString(R.string.yesterday)+sdf2.format(date_);
            }else {                         //昨天再往前
                String format3 = "MM/dd HH:mm";
                SimpleDateFormat sdf3 = new SimpleDateFormat(format3);
                return sdf3.format(date_);
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
            return "";
        }
    }

    /**
     * 获取当前时间
     * @return
     */
    public static String getNewDate(){
        dateTime = new Date();
        //由于时间少了八个小时
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return sdf.format(dateTime);
    }

    /**
     * 如果nickName有值，优先显示nickName
     * @param context
     * @param textView
     * @param userName
     * @param nickName
     */
    public static void showName(Context context, TextView textView,String userName,String nickName){
        if (!TextUtils.isEmpty(nickName)){
            textView.setText(nickName);
            return ;
        }
        textView.setText(userName);

    }

    /**
     * 防止快速连点
     * @return
     */
    public static boolean isNotFastClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > DELAY) {
            lastClickTime = currentTime;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 带输入框的弹出框
     */
    public static String inputDialog(final Context context){
        final EditText inputServer = new EditText(context);
        final String[] inputRoomName = {null};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.room_title)).setIcon(android.R.drawable.ic_dialog_info).setView(inputServer);

        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputRoomName[0] = inputServer.getText().toString();
                if (TextUtils.isEmpty(inputRoomName[0])){
                    Toast.makeText(context,context.getResources().getString(R.string.empty_room_name),Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
        return inputRoomName[0];
    }

    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    // InputStream转换成Drawable
    public static Drawable InputStream2Drawable(InputStream is) {
        Bitmap bitmap = InputStream2Bitmap(is);
        return bitmap2Drawable(bitmap);
    }

    // 将InputStream转换成Bitmap
    public static Bitmap InputStream2Bitmap(InputStream is) {
        return BitmapFactory.decodeStream(is);
    }
    // Bitmap转换成Drawable
    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        Drawable d = (Drawable) bd;
        return d;
    }

    /**
     * 数据持久化，保存选择的线路
     * @param context
     */
    public static void saveServiceLine(Context context,String line){
        SharedPreferences.Editor editor = context.getSharedPreferences("data",MODE_PRIVATE).edit();
        editor.putString("serviceLine",line);
        editor.apply();
        XmppConnection.getInstance(context).logOut(context);
    }

    /**
     * 服务器线路获取持久化数据
     */
    public static String getServiceHost(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("data", MODE_PRIVATE);
        String serviceLine = preferences.getString("serviceLine","");
        if (TextUtils.isEmpty(serviceLine)){
            return "im.ezcarry.com";
        }else {
            return serviceLine;
        }

    }

}
