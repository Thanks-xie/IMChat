package cn.xie.imchat.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.adapter.ChooseFriendsAdapter;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatRoom;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

/**
 * 创建聊天室
 */
public class ChooseFriendsToRoomActivity extends BaseActivity {
    private Context context;
    private RecyclerView recyclerView;
    private ImageView back;
    private TextView choose;
    private List<ChatUser> chatUserList;
    private ChooseFriendsAdapter chooseFriendsAdapter;
    private DBManager dbManager;
    private PopupWindow pwindow;
    private String roomNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_friends_to_room);
        context = this;
        chatUserList = new ArrayList<>();
        Util.chooseFriends = new ArrayList<>();
        dbManager = new DBManager(context);
        initView();
        initData();
    }

    private void initData() {
        chatUserList = dbManager.queryAllChatUser();
        for (int i=0;i<chatUserList.size();i++){
            if (chatUserList.get(i).getUserName().equals(Util.getLoginInfo(context).getUserName())) {
                chatUserList.remove(i);
            }
        }
        chooseFriendsAdapter = new ChooseFriendsAdapter(context,chatUserList);
        recyclerView.setAdapter(chooseFriendsAdapter);
        chooseFriendsAdapter.setItemOnClickListener(new ChooseFriendsAdapter.ItemClickListener() {
            @Override
            public void onClick(int position,View view) {
                CheckBox checkBox = view.findViewById(R.id.checkbox);
                checkBox.toggle();
                chatUserList.get(position).setCheckbox(checkBox.isChecked());
                chooseFriendsAdapter.notifyDataSetChanged();
                if (checkBox.isChecked()){
                    Util.chooseFriends.add(chatUserList.get(position));
                }else {
                    Util.chooseFriends.remove(chatUserList.get(position));
                }
                if (Util.chooseFriends.size()>0){
                    choose.setVisibility(View.VISIBLE);
                    choose.setText(getResources().getString(R.string.choose)+getResources().getString(R.string.left)+Util.chooseFriends.size()+getResources().getString(R.string.right));
                }else {
                    choose.setVisibility(View.GONE);
                    choose.setText(getResources().getString(R.string.choose));
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
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        choose = findViewById(R.id.choose);
        if (Util.chooseFriends==null||Util.chooseFriends.size()==0){
            choose.setVisibility(View.GONE);
        }
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChatGroupPopupWindos();
            }
        });
    }

    /**
     * 显示聊天室名称弹出框
     */
    private void showChatGroupPopupWindos() {
        try {
            View contentview = LayoutInflater.from(this).inflate(R.layout.popupwindow_chat_group, null);
            pwindow = new PopupWindow(contentview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            Button confirmBtn = (Button) contentview.findViewById(R.id.confirmBtn);
            Button closeBtn = (Button) contentview.findViewById(R.id.closeBtn);
            final EditText roomName = (EditText) contentview.findViewById(R.id.room_name);
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwindow.dismiss();
                }
            });

            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String roomNameStr = roomName.getText().toString();
                    if (TextUtils.isEmpty(roomNameStr)){
                        Toast.makeText(context,context.getResources().getString(R.string.empty_room_name),Toast.LENGTH_SHORT).show();
                    }else {
                        roomNameText = roomNameStr;
                        createRoomAndAddMember.sendEmptyMessage(1);
                        pwindow.dismiss();
                    }
                }
            });
            pwindow.setFocusable(true);
            pwindow.setOutsideTouchable(true);
            pwindow.setBackgroundDrawable(new BitmapDrawable());
            pwindow.showAsDropDown(choose);
            pwindow.update();
        } catch (Exception e) {

        }
    }

    /**
     * 创建聊天室以及加入聊天成员
     */
    @SuppressLint("HandlerLeak")
    private Handler createRoomAndAddMember = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            boolean result = XmppConnection.getInstance(context).createChatRoom(context,roomNameText,Util.chooseFriends);
            if (result){
                //创建聊天室成功就保存到本地
                List<ChatRoom> chatRooms = new ArrayList<>();
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setRoomName(roomNameText);
                chatRoom.setJid(roomNameText+"@conference.xie-pc");
                chatRooms.add(chatRoom);
                dbManager.addChatRoomData(chatRooms);
                Intent intent = new Intent(context,ChatActivity.class);
                intent.putExtra("sendName", roomNameText);
                intent.putExtra("type", "groupchat");
                startActivity(intent);
                finish();
            }
        }
    };
}
