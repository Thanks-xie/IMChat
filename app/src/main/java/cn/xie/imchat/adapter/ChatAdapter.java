package cn.xie.imchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.domain.ChatMessage;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

/**
 * @author xiejinbo
 * @date 2019/9/25 0025 15:10
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private Context context;
    private DBManager dbManager;
    private List<ChatMessage> chatMessageList;

    public ChatAdapter(Context context,List<ChatMessage> chatMessages){
        this.context = context;
        this.dbManager = new DBManager(context);
        this.chatMessageList = chatMessages;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_chat,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessageList.get(position);
        if (chatMessage.getMyself().equals("IN")){
            holder.rightMsg.setVisibility(View.GONE);
            holder.leftMsg.setVisibility(View.VISIBLE);
            if (chatMessage.getType().equals("groupchat")){
                ChatUser chatUser = dbManager.queryChatUserByName(chatMessage.getSendId());
                holder.sendName.setVisibility(View.VISIBLE);
                Util.showName(context, holder.sendName, chatUser.getUserName(), chatUser.getNickName());
            }
            holder.leftChatTime.setText(Util.setChatMessageDate(chatMessage.getSendtime(),context));
            holder.leftChatData.setText(chatMessage.getData());
        }else {
            holder.leftMsg.setVisibility(View.GONE);
            holder.rightMsg.setVisibility(View.VISIBLE);
            holder.rightChatTime.setText(Util.setChatMessageDate(chatMessage.getSendtime(),context));
            holder.rightChatData.setText(chatMessage.getData());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout leftMsg,rightMsg;
        TextView leftChatTime,rightChatTime,sendName;
        ImageView leftChatHeadIcon,rightChatHeadIcon;
        TextView leftChatData,rightChatData;
        public ViewHolder(View itemView) {
            super(itemView);
            leftMsg = itemView.findViewById(R.id.left_msg);
            leftChatTime = itemView.findViewById(R.id.left_chat_time);
            leftChatHeadIcon = itemView.findViewById(R.id.left_chat_head_icon);
            leftChatData = itemView.findViewById(R.id.left_chat_data);
            sendName = itemView.findViewById(R.id.send_name);
            rightMsg = itemView.findViewById(R.id.right_msg);
            rightChatTime = itemView.findViewById(R.id.right_chat_time);
            rightChatHeadIcon = itemView.findViewById(R.id.left_chat_head_icon);
            rightChatData = itemView.findViewById(R.id.right_chat_data);
        }
    }
}
