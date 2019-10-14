package cn.xie.imchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.adapter.ChatGroupListAdapter;
import cn.xie.imchat.domain.ChatRoom;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

/**
 * 聊天房间列表
 */
public class ChatGroupListActivity extends BaseActivity {

    private Context context;
    private ImageView back;
    private DBManager dbManager;
    private List<ChatRoom> chatRoomList;
    private ChatGroupListAdapter chatGroupListAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group_list);
        context = this;
        dbManager = new DBManager(context);
        initView();
        initData();
    }

    private void initData() {
        chatRoomList = dbManager.queryAllChatRoom();
        chatGroupListAdapter = new ChatGroupListAdapter(context,chatRoomList);
        recyclerView.setAdapter(chatGroupListAdapter);
        chatGroupListAdapter.setItemOnclickListener(new ChatGroupListAdapter.ItemOnclickListener() {
            @Override
            public void Click(int position) {
                if (Util.isNotFastClick()){
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("sendName",chatRoomList.get(position).getRoomName());
                    intent.putExtra("type","groupchat");
                    startActivity(intent);
                }
            }
        });
    }

    private void initView() {
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }
}
