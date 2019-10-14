package cn.xie.imchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.utils.Util;

/**
 * @author xiejinbo
 * @date 2019/9/20 0020 13:40
 */
public class ContractsAdapter extends RecyclerView.Adapter<ContractsAdapter.ViewHolder> {
    private Context mContext;
    private List<ChatUser> chatUserList;
    private ItemClickListener itemClickListener;
    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onClick(int position);

    }

    public ContractsAdapter(Context context, List<ChatUser> chatUsers){
        this.mContext = context;
        this.chatUserList = chatUsers;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View mItemView=  LayoutInflater.from(mContext).inflate(R.layout.contract_list_adapter,parent,false);
        return new ViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        final ChatUser chatUser = chatUserList.get(position);
        Util.showName(mContext,holder.name,chatUser.getUserName(),chatUser.getNickName());
        Message msg = new Message();
        msg.what = position;
        msg.obj = holder;
        showOnline.sendMessage(msg);
    }

    @Override
    public int getItemCount() {
        return chatUserList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name,online;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.name);
            online = itemView.findViewById(R.id.online);
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
            int result = XmppConnection.getInstance().IsUserOnLine(chatUserList.get(msg.what).getJid());
            ViewHolder viewHolder = (ViewHolder) msg.obj;
            if (1==result){
                viewHolder.online.setText(R.string.online_text);
            }else {
                viewHolder.online.setText(R.string.offline_text);
            }
        }
    };

}
