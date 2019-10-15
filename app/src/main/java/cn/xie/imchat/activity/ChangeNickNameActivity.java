package cn.xie.imchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import cn.xie.imchat.R;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.domain.LoginUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

public class ChangeNickNameActivity extends BaseActivity {
    private Context context;
    private DBManager dbManager;
    private ImageView back;
    private EditText inputNickname;
    private Button button;
    private String oldNickName;
    private ChatUser changeUser;
    private LoginUser loginUser;
    private boolean isMe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_nick_name);
        context = this;
        dbManager = new DBManager(context);
        Intent intent = getIntent();
        oldNickName = intent.getStringExtra("nickname");
        isMe = intent.getBooleanExtra("isMySelf",false);
        if (isMe){
            loginUser = (LoginUser) intent.getSerializableExtra("changeUser");
        }else {
            changeUser = (ChatUser) intent.getSerializableExtra("changeUser");
        }

        initView();
    }

    private void initView() {
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        inputNickname = findViewById(R.id.nickname_edit);
        inputNickname.setText(oldNickName);
        button = findViewById(R.id.submit_nickname);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNickName();
            }
        });
    }

    /**
     * 修改备注名
     */
    private void changeNickName(){
        String newNickName = inputNickname.getText().toString();
        if (TextUtils.isEmpty(newNickName)){
            Toast.makeText(context,"输入备注名不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        boolean result = false;
        if (isMe){
            result = XmppConnection.getConnection().modifyNickName(newNickName,loginUser.getUserName(),loginUser.getJid());
        }else {
            result = XmppConnection.getConnection().modifyNickName(newNickName,Util.getLoginInfo(context).getUserName(),changeUser.getJid());
        }
        if (result){
            Toast.makeText(context,"修改成功",Toast.LENGTH_SHORT).show();
            if (isMe){
                loginUser.setNickName(newNickName);
                Util.saveLoginStatic(context,loginUser);
            }else {
                changeUser.setNickName(newNickName);
                dbManager.updateChatUserData(changeUser);
            }

            onBackPressed();
        }else {
            Toast.makeText(context,"修改失败",Toast.LENGTH_SHORT).show();
        }

    }
}
