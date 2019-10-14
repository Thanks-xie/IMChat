package cn.xie.imchat.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import cn.xie.imchat.R;
import cn.xie.imchat.activity.ChangeNickNameActivity;
import cn.xie.imchat.activity.ChangePasswordActivity;
import cn.xie.imchat.activity.LoginActivity;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.LoginUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class MineFragment extends Fragment {
    private Button logout;
    private Context context;
    private RelativeLayout change_password,reNickName;
    private TextView userName,nickName;
    private DBManager dbManager;
    private LoginUser chatUser;


    public MineFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        context = getActivity();
        dbManager = new DBManager(context);
        chatUser = new LoginUser();
        initView(view);
        initData();
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chatUser==null){
            initData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        chatUser=null;
    }

    /**
     * 获取登录用户详细信息
     */
    private void initData() {
        chatUser = Util.getLoginInfo(context);
        userName.setText(chatUser.getUserName());
        nickName.setText(chatUser.getNickName());
    }

    /**
     * 初始化
     * @param view
     */
    private void initView(View view) {
        userName = view.findViewById(R.id.userName_show);
        nickName = view.findViewById(R.id.name_show);
        //退出登录
        logout = view.findViewById(R.id.logout_btn);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.cleanLoginData(context);
                XmppConnection.getInstance().setPresence(5);
                XmppConnection.getInstance().closeConnection();
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        //修改密码
        change_password = view.findViewById(R.id.change_password);
        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        reNickName = view.findViewById(R.id.name);
        reNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChangeNickNameActivity.class);
                intent.putExtra("nickname",chatUser.getNickName());
                startActivity(intent);
            }
        });

    }

}
