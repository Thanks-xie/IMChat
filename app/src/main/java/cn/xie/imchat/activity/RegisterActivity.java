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
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

public class RegisterActivity extends BaseActivity {
    private ImageView back;
    private Context context;
    private DBManager dbManager;
    private EditText registerName,registerPassword,reRegisterPassword;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;
        dbManager = new DBManager(context);
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
        registerName = findViewById(R.id.name_text);
        registerPassword = findViewById(R.id.password_text);
        reRegisterPassword = findViewById(R.id.password_text_re);
        button = findViewById(R.id.summit_register);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    /**
     * 提交注册
     */
    private void register(){
        String name = registerName.getText().toString();
        String password = registerPassword.getText().toString();
        String rePassword = reRegisterPassword.getText().toString();
        if (TextUtils.isEmpty(name)||TextUtils.isEmpty(password)){
            Toast.makeText(context,"用户名或密码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Util.checkNewPassword(context,password,rePassword)){
            Toast.makeText(context,"两次输入密码不一致",Toast.LENGTH_SHORT).show();
            return;
        }
        boolean result = XmppConnection.getInstance().register(name,password);
        if (result){
            Intent intent = new Intent(context,LoginActivity.class);
            intent.putExtra("name",name);
            startActivity(intent);
            finish();

        }else {
            Toast.makeText(context,"注册失败",Toast.LENGTH_SHORT).show();
        }
    }
}
