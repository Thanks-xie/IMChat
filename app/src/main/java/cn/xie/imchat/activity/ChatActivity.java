package cn.xie.imchat.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.adapter.ChatAdapter;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatMessage;
import cn.xie.imchat.domain.LoginUser;
import cn.xie.imchat.utils.AddMessageDate;
import cn.xie.imchat.utils.AddMessageInfo;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.MyMessage;
import cn.xie.imchat.utils.Util;
import cn.xie.imchat.view.MyEditTextView;

public class ChatActivity extends BaseActivity {
    private ImageView back, more;
    private Context context;
    private TextView chatName, send;
    private String sendName, type, userName;
    private DBManager dbManager;
    private ChatAdapter adapter;
    private RecyclerView recyclerView;
    private List<ChatMessage> chatMessageList;
    private MyEditTextView inputText;
    private StanzaListener packetListener;
    private Chat chat;
    private LinearLayoutManager manager;
    private MultiUserChat muc;
    private LoginUser loginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = this;
        dbManager = new DBManager(context);
        Intent intent = getIntent();
        sendName = intent.getStringExtra("sendName");
        type = intent.getStringExtra("type");
        loginUser = Util.getLoginInfo(context);
        if (type.equals("chat")){
            //创建个人聊天窗口
            String sendJid = sendName + "@xie-pc";
            chat = XmppConnection.getInstance(context).getFriendChat(context,sendJid);
        }else {
            //创建聊天室窗口
            String roomJid = sendName + "@conference.xie-pc";
            muc = XmppConnection.getInstance(context).joinMultiUserChat(context,loginUser.getUserName(),sendName);
        }

        userName = Util.getLoginInfo(context).getUserName();

        initView();
        initData();
    }

    /**
     * 加载聊天记录
     */
    private void initData() {
        chatMessageList = dbManager.queryHistoryChatMessageByName(sendName);
        adapter = new ChatAdapter(context, chatMessageList);
        recyclerView.setAdapter(adapter);
        //是否铺满屏幕监听
        addOnGlobalLayoutItem();

    }


    /**
     * 初始化布局
     */
    private void initView() {
        recyclerView = findViewById(R.id.recycler_chat);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        //禁止滑动，解决滑动冲突
        recyclerView.setNestedScrollingEnabled(false);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        chatName = findViewById(R.id.chat_name);
        if (type.equals("chat")){
            chatName.setVisibility(View.GONE);
        }else {
            chatName.setVisibility(View.VISIBLE);
            chatName.setText(sendName);
        }
        send = findViewById(R.id.send);
        more = findViewById(R.id.more);
        inputText = findViewById(R.id.input_text);
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //这里控制输入框里面没有值时显示+按钮，有值时显示发送按钮
                if (!TextUtils.isEmpty(s.toString())) {
                    more.setVisibility(View.GONE);
                    send.setVisibility(View.VISIBLE);
                } else {
                    more.setVisibility(View.VISIBLE);
                    send.setVisibility(View.GONE);
                }
            }
        });
        //点击输入框时，滚动到最新消息
        inputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //是否铺满屏幕监听
                addOnGlobalLayoutItem();
            }
        });

        //输入框获取焦点时监听（此时软件盘打开）
        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //是否铺满屏幕监听
                addOnGlobalLayoutItem();
            }
        });
        //软件盘关闭监听
        inputText.setOnKeyBoardHideListener(new MyEditTextView.OnKeyBoardHideListener() {
            @Override
            public void onKeyHide(int keyCode, KeyEvent event) {
                //是否铺满屏幕监听
                addOnGlobalLayoutItem();
            }
        });

        //这里控制输入值的发送，当发送后清空输入框
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputMsg = inputText.getText().toString();
                if (TextUtils.isEmpty(inputMsg)){
                    Toast.makeText(context,"发送内容不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(XmppConnection.getInstance(context).checkConnection()&&XmppConnection.getInstance(context).checkAuthenticated())){
                    Toast.makeText(context,"对不起，你暂处于离线状态，不能发送消息",Toast.LENGTH_SHORT).show();
                    return;
                }
                sendChatMessageListener(inputMsg);
                inputText.setText("");
            }
        });
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

    /**
     * 添加聊天消息的监听
     */
    private void sendChatMessageListener(String inputMsg) {
        //构造自定义消息格式(用户信息)
        AddMessageInfo addMessageInfo = new AddMessageInfo();
        addMessageInfo.setNickNameText(userName);
        addMessageInfo.setIconText("http://image.baidu.com/search/detail?ct=503316480&z=0&ipn=false&word=%E5%A4%B4%E5%83%8F&hs=0&pn=9&spn=0&di=207790&pi=0&rn=1&tn=baiduimagedetail&is=0%2C0&ie=utf-8&oe=utf-8&cl=2&lm=-1&cs=3583433020%2C118316633&os=1895134963%2C172187008&simid=0%2C0&adpicid=0&lpn=0&ln=30&fr=ala&fm=&sme=&cg=head&bdtype=0&oriquery=%E5%A4%B4%E5%83%8F&objurl=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201810%2F18%2F20181018162951_kgwzm.thumb.700_0.jpeg&fromurl=ippr_z2C%24qAzdH3FAzdH3Fooo_z%26e3B17tpwg2_z%26e3Bv54AzdH3Fks52AzdH3F%3Ft1%3D8aa0mddbb8&gsm=&islist=&querylist=");

        //构造自定义消息格式(消息时间)
        AddMessageDate addMessageDate = new AddMessageDate();
        addMessageDate.setDateText(Util.getNewDate());

        //自定义消息
        MyMessage myMessage = new MyMessage();
        myMessage.data = inputMsg;
        myMessage.type = type;
        myMessage.sendtime = Util.getNewDate();
        myMessage.source = "Android";

        //添加扩展到message
        Message message = new Message();
        //message.setType(Message.Type.chat);
        message.setBody(JSON.toJSONString(myMessage));
        //message.addExtension(addMessageDate);
        //message.addExtension(addMessageInfo);

        //发送消息
        XmppConnection.getInstance(context).sendMessage(chat, muc, message);
        List<ChatMessage> chatMessages = new ArrayList<>();
        if (type.equals("groupchat")){
            ChatMessage chatMessage = new ChatMessage(userName, sendName, inputMsg, "OUT", myMessage.sendtime, null, type,userName);
            chatMessages.add(chatMessage);
            chatMessageList.add(chatMessage);
        }else {
            ChatMessage chatMessage = new ChatMessage(userName, sendName, inputMsg, "OUT", myMessage.sendtime, null, type,"");
            chatMessages.add(chatMessage);
            chatMessageList.add(chatMessage);
        }
        dbManager.addChatMessageData(chatMessages);
        adapter.notifyDataSetChanged();
        //是否铺满屏幕监听
        addOnGlobalLayoutItem();

    }

    /**
     * item是否铺满屏幕监听
     */
    private void addOnGlobalLayoutItem() {
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (manager.findLastCompletelyVisibleItemPosition() < adapter.getItemCount() - 1) {
                    manager.setStackFromEnd(true);
                    recyclerView.setLayoutManager(manager);
                    recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    manager.setStackFromEnd(false);
                    recyclerView.setLayoutManager(manager);
                }
            }
        });
        if (chatMessageList.size() > 0) {
            recyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
        }
    }

}
