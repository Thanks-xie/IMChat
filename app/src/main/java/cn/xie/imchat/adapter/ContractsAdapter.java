package cn.xie.imchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.activity.FriendDetailActivity;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

/**
 * @author xiejinbo
 * @date 2019/9/20 0020 13:40
 */
public class ContractsAdapter extends RecyclerView.Adapter<ContractsAdapter.ViewHolder> {
    private Context mContext;
    private List<ChatUser> chatUserList;
    private ItemClickListener itemClickListener;
    private DBManager dbManager;
    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onClick(int position);

    }

    public ContractsAdapter(Context context, List<ChatUser> chatUsers){
        this.mContext = context;
        this.chatUserList = chatUsers;
        this.dbManager = new DBManager(context);
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View mItemView=  LayoutInflater.from(mContext).inflate(R.layout.contract_list_adapter,parent,false);
        return new ViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ChatUser chatUser = chatUserList.get(position);
        Util.showName(mContext,holder.name,chatUser.getUserName(),chatUser.getNickName());
        holder.headIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isNotFastClick()){
                    Intent intent = new Intent(mContext, FriendDetailActivity.class);
                    intent.putExtra("friend",chatUser);
                    mContext.startActivity(intent);
                }
            }
        });
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isNotFastClick()){
                    boolean result = XmppConnection.getInstance(mContext).removeUser(mContext,chatUser.getUserName());
                    if (result){
                        Toast.makeText(mContext,"删除成功",Toast.LENGTH_SHORT).show();
                        dbManager.deleteData("user","jid=?", new String[]{chatUser.getJid()});
                        dbManager.deleteData("chatMessage","sendname=? and username=?", new String[]{chatUser.getUserName(),Util.getLoginInfo(mContext).getUserName()});
                        chatUserList.remove(position);
                        notifyDataSetChanged();
                    }else {
                        Toast.makeText(mContext,"删除失败",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        Message msg = new Message();
        msg.what = position;
        msg.obj = holder;
        //showHeadImage.sendMessage(msg);
        Message msg1 = new Message();
        msg1.what = position;
        msg1.obj = holder;
        showOnline.sendMessage(msg1);
    }

    @Override
    public int getItemCount() {
        return chatUserList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name,online;
        ImageView headIcon,deleteIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.name);
            online = itemView.findViewById(R.id.online);
            headIcon = itemView.findViewById(R.id.head);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onClick(getAdapterPosition());	//getAdapterPosition()获取RecyclerView的item position，getPosition()方法已废弃
            }
        }
    }

    /**
     * 获取好友在线状态
     */
    @SuppressLint("HandlerLeak")
    private Handler showOnline = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int result = XmppConnection.getInstance(mContext).IsUserOnLine(chatUserList.get(msg.what).getJid());
            ViewHolder viewHolder = (ViewHolder) msg.obj;
            if (1==result){
                viewHolder.online.setText(R.string.online_text);
            }else {
                viewHolder.online.setText(R.string.offline_text);
            }
        }
    };
    /**
     * 获取好友头像
     */
    @SuppressLint("HandlerLeak")
    private Handler showHeadImage = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Drawable drawable = XmppConnection.getInstance(mContext).getUserImage(mContext,chatUserList.get(msg.what).getJid());
            ViewHolder viewHolder = (ViewHolder) msg.obj;
            Log.e("xjbo","drawable："+drawable);
            /*if (drawable!=null){
                viewHolder.online.setText(R.string.online_text);
            }else {
                viewHolder.online.setText(R.string.offline_text);
            }*/
        }
    };


}
