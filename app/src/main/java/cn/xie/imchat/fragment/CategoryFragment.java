package cn.xie.imchat.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.activity.AddFriendActivity;
import cn.xie.imchat.activity.ChatActivity;
import cn.xie.imchat.activity.ChatGroupListActivity;
import cn.xie.imchat.activity.ChooseFriendsToRoomActivity;
import cn.xie.imchat.adapter.ContractsAdapter;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFragment extends Fragment {
    private DBManager dbManager;
    private Activity context;
    private List<ChatUser> chatUsers;
    private RecyclerView recyclerView;
    private ContractsAdapter adapter;
    private EditText searchFriend;
    private ImageView add;
    private PopupWindow pwindow;
    private RelativeLayout reGroup;


    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        add = view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPopupWindow();
            }
        });
        context = getActivity();
        dbManager = new DBManager(context);
        initData();
        searchFriend = view.findViewById(R.id.search);
        //搜索监听
        searchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString())) {
                    List<ChatUser> chatUserList = dbManager.queryChatUserByLikeName(s.toString());
                    for (ChatUser chatUser : chatUserList) {
                        if (chatUser.getUserName().equals(Util.getLoginInfo(context).getUserName())) {
                            chatUserList.remove(chatUser);
                            break;
                        }
                    }
                    adapter = new ContractsAdapter(context, chatUserList);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter = new ContractsAdapter(context, chatUsers);
                    recyclerView.setAdapter(adapter);
                }


            }
        });
        //进入群聊列表
        reGroup = view.findViewById(R.id.re_group);
        reGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatGroupListActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    /**
     * 获取所有好友
     */
    private void initData() {
        chatUsers = new ArrayList<>();
        chatUsers = dbManager.queryAllChatUser();
        for (int i=0;i<chatUsers.size();i++){
            if (chatUsers.get(i).getUserName().equals(Util.getLoginInfo(context).getUserName())) {
                chatUsers.remove(i);
            }
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        //禁止滑动，解决滑动冲突
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new ContractsAdapter(context, chatUsers);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new ContractsAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                if (Util.isNotFastClick()) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("sendName", chatUsers.get(position).getUserName());
                    intent.putExtra("type", "chat");
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 添加好友或群聊弹出框
     */
    private void showAddPopupWindow() {
        try {

            View contentview = LayoutInflater.from(context).inflate(R.layout.popupwindow_add, null);
            pwindow = new PopupWindow(contentview, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            LinearLayout createGroup = (LinearLayout) contentview.findViewById(R.id.create_group);
            LinearLayout addFriend = (LinearLayout) contentview.findViewById(R.id.add_friend);
            createGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Util.isNotFastClick()){
                        Intent intent = new Intent(context, ChooseFriendsToRoomActivity.class);
                        startActivity(intent);
                        pwindow.dismiss();
                    }
                }
            });
            addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Util.isNotFastClick()) {
                        Intent intent = new Intent(context, AddFriendActivity.class);
                        startActivity(intent);
                        pwindow.dismiss();
                    }
                }
            });
            pwindow.setFocusable(true);
            pwindow.setOutsideTouchable(true);
            pwindow.setBackgroundDrawable(new BitmapDrawable());
            pwindow.showAsDropDown(add);
            pwindow.update();
        } catch (Exception e) {
            Log.e("showChoiceUnfollow", "Exception: " + e);
        }
    }

}
