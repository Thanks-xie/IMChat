package cn.xie.imchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.xie.imchat.R;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

public class FriendDetailActivity extends BaseActivity {

    private Context context;
    private DBManager dbManager;
    private ImageView back,headIcon;
    private TextView userName,nickName,email;
    private Button deleteFriend;
    private RelativeLayout reNickName;
    private ChatUser chatUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);
        context = this;
        dbManager = new DBManager(context);
        Intent intent = getIntent();
        chatUser = (ChatUser) intent.getSerializableExtra("friend");
        initView();
        initData();
    }

    private void initData() {
        if (chatUser!=null){
            userName.setText(chatUser.getUserName());
            nickName.setText(chatUser.getNickName());
            email.setText(chatUser.getEmail());
        }
    }

    private void initView() {
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        headIcon = findViewById(R.id.head_icon);
        userName = findViewById(R.id.userName_text);
        email = findViewById(R.id.email);
        nickName = findViewById(R.id.nickname_text);
        reNickName = findViewById(R.id.nickname_re);
        reNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isNotFastClick()){
                    Intent intent = new Intent(context,ChangeNickNameActivity.class);
                    intent.putExtra("nickname",chatUser.getNickName());
                    intent.putExtra("isMySelf",false);
                    intent.putExtra("changeUser",chatUser);
                    startActivity(intent);
                }

            }
        });
        deleteFriend = findViewById(R.id.delete_btn);
        deleteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        chatUser = dbManager.queryChatUserByName(chatUser.getUserName());
        initData();
    }
}
