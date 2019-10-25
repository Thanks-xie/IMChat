package cn.xie.imchat.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Stanza;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.activity.AddFriendActivity;
import cn.xie.imchat.activity.ChatActivity;
import cn.xie.imchat.activity.ChooseFriendsToRoomActivity;
import cn.xie.imchat.activity.FriendApplyActivity;
import cn.xie.imchat.adapter.HistoryMsgAdapter;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatMessage;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private ImageView add;
    private PopupWindow pwindow;
    private Activity context;
    private RecyclerView recycler_chat;
    private DBManager dbManager;
    private HistoryMsgAdapter adapter;
    private StanzaListener packetListener,packetApplyListener;
    private List<ChatMessage> chatMessages,applyList;
    private RelativeLayout reNewFriend;
    private TextView applyCount;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initView(view);
        initData();
        return view;
    }

    /**
     * 加载布局
     * @param view
     */
    private void initView(View view) { add = view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showAddPopupWindow();
                XmppConnection.getConnection(context).logOut(context);

            }
        });
        context = getActivity();
        recycler_chat = view.findViewById(R.id.recycler_chat);
        dbManager = new DBManager(context);
        recycler_chat.setLayoutManager(new LinearLayoutManager(context));
        recycler_chat.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        //禁止滑动，解决滑动冲突
        recycler_chat.setNestedScrollingEnabled(false);

        //好友申请
        applyCount = view.findViewById(R.id.apply_count);
        reNewFriend = view.findViewById(R.id.re_new_apply);
        reNewFriend.setVisibility(View.GONE);
        reNewFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FriendApplyActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 加载最近聊天消息
     */
    private void initData() {
        String username = Util.getLoginInfo(context).getUserName();
        chatMessages = dbManager.queryChatMessageByName(username);
        applyList = dbManager.queryChatMessageByName(username,"APPLY");
        for (int i=0;i<chatMessages.size();i++){
            if ("APPLY".equals(chatMessages.get(i).getMyself())){
                chatMessages.remove(i);
            }
        }
        adapter = new HistoryMsgAdapter(context,chatMessages);
        recycler_chat.setAdapter(adapter);
        adapter.setOnItemClickListener(new HistoryMsgAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                if (Util.isNotFastClick()){
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("sendName",chatMessages.get(position).getSendName());
                    intent.putExtra("type",chatMessages.get(position).getType());
                    context.startActivity(intent);
                }
            }
        });

        if (applyList!=null&&applyList.size()>0){
            reNewFriend.setVisibility(View.VISIBLE);
            applyCount.setText(String.valueOf(applyList.size()));
        }else {
            reNewFriend.setVisibility(View.GONE);
        }
    }


    /**
     *添加好友或群聊弹出框
     */
    private void showAddPopupWindow(){
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

    @SuppressLint("HandlerLeak")
    private Handler handlerInitData = new Handler() {
    };
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            initData();
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                handlerInit.sendEmptyMessage(1);
                initPacket();
                initApply();
            }
        });
        thread.start();

    }
    @SuppressLint("HandlerLeak")
    private Handler handlerInit = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            handlerInitData.postDelayed(task, 1000);
        }
    };
    /**
     * 监控最新聊天消息
     */
    public void initPacket() {
        try {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (packetListener == null) {
                        packetListener = new StanzaListener() {
                            @Override
                            public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                                setProcessPacket(packet);
                            }
                        };
                    }
                    XmppConnection.getInstance(context).addAsyncStanzaListener(packetListener, StanzaTypeFilter.MESSAGE);
                }
            });
            thread.start();
        } catch (Exception e) {
            Log.e("initPacket", e.toString());
        }
    }
    private void setProcessPacket(Stanza packet) {
        try {
            handlerInit.sendEmptyMessage(1);
        } catch (Exception e) {
            Log.e("setProcessPacket", e.toString());
        }
    }
    private void setApplyProcessPacket(Stanza packet) {
        try {
            handlerInit.sendEmptyMessage(1);
        } catch (Exception e) {
            Log.e("setProcessPacket", e.toString());
        }
    }

    /**
     * 监控最新好友申请
     */
    public void initApply() {
        try {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (packetApplyListener == null) {
                        packetApplyListener = new StanzaListener() {
                            @Override
                            public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
                                setApplyProcessPacket(packet);
                            }
                        };
                    }
                    XmppConnection.getInstance(context).addSyncStanzaListener(packetApplyListener, StanzaTypeFilter.PRESENCE);
                }
            });
            thread.start();
        } catch (Exception e) {
            Log.e("initPacket", e.toString());
        }
    }

}
