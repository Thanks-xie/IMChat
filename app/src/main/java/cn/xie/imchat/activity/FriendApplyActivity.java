package cn.xie.imchat.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.PresenceTypeFilter;
import org.jivesoftware.smack.packet.Stanza;

import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.adapter.FriendApplyAdapter;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatMessage;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

public class FriendApplyActivity extends BaseActivity {
    private ImageView back;
    private RecyclerView recyclerView;
    private DBManager dbManager;
    private Context context;
    private FriendApplyAdapter friendApplyAdapter;
    private List<ChatMessage> applyList;
    private String userName;
    private StanzaListener packetApplyListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_apply);
        context = this;
        dbManager = new DBManager(context);
        userName = Util.getLoginInfo(context).getUserName();
        initView();
        initData();
    }

    private void initData() {
        applyList = dbManager.queryChatMessageByName(userName,"APPLY");
        friendApplyAdapter = new FriendApplyAdapter(context,applyList);
        recyclerView.setAdapter(friendApplyAdapter);
    }

    private void initView() {
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        recyclerView = findViewById(R.id.recycler_apply);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        //禁止滑动，解决滑动冲突
        recyclerView.setNestedScrollingEnabled(false);
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
    private void setProcessPacket(Stanza packet) {
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
                                setProcessPacket(packet);
                            }
                        };
                    }
                    XmppConnection.getInstance(context).addSyncStanzaListener(packetApplyListener, PresenceTypeFilter.SUBSCRIBE);
                }
            });
            thread.start();
        } catch (Exception e) {
            Log.e("initPacket", e.toString());
        }
    }
}
