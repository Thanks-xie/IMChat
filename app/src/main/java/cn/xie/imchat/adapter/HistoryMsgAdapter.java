package cn.xie.imchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.domain.ChatMessage;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

/**
 * @author xiejinbo
 * @date 2019/9/23 0023 11:41
 */
public class HistoryMsgAdapter extends RecyclerView.Adapter<HistoryMsgAdapter.ViewHolder> {

    private Context context;
    private List<ChatMessage> chatMessageList;
    private ItemClickListener itemClickListener;
    private DBManager dbManager;

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onClick(int position);
    }

    public HistoryMsgAdapter(Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessageList = chatMessages;
        dbManager = new DBManager(context);
    }

    @Override
    public HistoryMsgAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_message_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ChatMessage chatMessage = chatMessageList.get(position);
        if (chatMessage.getType().equals("chat")) {
            ChatUser chatUser = dbManager.queryChatUserByName(chatMessage.getSendName());
            if (chatUser != null) {

                Util.showName(context, holder.chatName, chatUser.getUserName(), chatUser.getNickName());
                holder.chatData.setText(chatMessage.getData());
            }
        } else if (chatMessage.getType().equals("groupchat")) {
            if (chatMessage.getMyself().equals("OUT")){
                holder.chatData.setText(context.getResources().getString(R.string.send_out) + ":" + chatMessage.getData());
            }else {
                ChatUser chatUser = dbManager.queryChatUserByName(chatMessage.getSendId());
                holder.chatData.setText(chatUser.getNickName() + ":" + chatMessage.getData());

            }
            holder.chatName.setText(chatMessage.getSendName());
            holder.head.setImageResource(R.mipmap.group);

        }
        holder.chatTime.setText(Util.setChatMessageDate(chatMessage.getSendtime(), context));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView chatName, chatData, chatTime;
        ImageView head;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            chatName = itemView.findViewById(R.id.userName);
            chatData = itemView.findViewById(R.id.msg_data);
            chatTime = itemView.findViewById(R.id.msg_date);
            head = itemView.findViewById(R.id.head);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onClick(getAdapterPosition());    //getAdapterPosition()获取RecyclerView的item position，getPosition()方法已废弃
            }
        }
    }
}
