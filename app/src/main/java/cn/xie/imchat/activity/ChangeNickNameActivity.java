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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_nick_name);
        context = this;
        dbManager = new DBManager(context);
        Intent intent = getIntent();
        oldNickName = intent.getStringExtra("nickname");
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
        if (TextUtils.isEmpty(inputNickname.getText().toString())){
            Toast.makeText(context,"输入备注名不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        LoginUser loginUser = Util.getLoginInfo(context);
        boolean result = XmppConnection.getConnection().modifyNickName(inputNickname.getText().toString(),loginUser.getUserName(),loginUser.getUserName());
        if (result){
            Toast.makeText(context,"修改成功",Toast.LENGTH_SHORT).show();
            loginUser.setNickName(inputNickname.getText().toString());
            Util.saveLoginStatic(context,loginUser);
            onBackPressed();
        }else {
            Toast.makeText(context,"修改失败",Toast.LENGTH_SHORT).show();
        }

    }
}
