package cn.xie.imchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.xie.imchat.R;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.LoginUser;
import cn.xie.imchat.utils.Util;

public class LoginActivity extends BaseActivity {
    private TextView inputName,inputPassword,toRegister;
    private Button loginBtn;
    private Context context;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        initView();
    }

    private void initView() {
        inputName = findViewById(R.id.name_text);
        if (!TextUtils.isEmpty(name)){
            inputName.setText(name);
        }
        inputPassword = findViewById(R.id.password_text);
        loginBtn = findViewById(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = inputName.getText().toString();
                String userPassWord = inputPassword.getText().toString();
                boolean isLogin = XmppConnection.getInstance(context).loginXmpp(context,userName,userPassWord);
                if (isLogin){
                    LoginUser loginUser = new LoginUser();
                    loginUser.setUserName(userName);
                    loginUser.setPassword(userPassWord);
                    Util.saveLoginStatic(context,loginUser);
                    Util.startChatService(context);
                    Intent intent = new Intent(context,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        toRegister = findViewById(R.id.register);
        toRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(context,RegisterActivity.class);
                    startActivity(intent);
            }
        });
    }
}
