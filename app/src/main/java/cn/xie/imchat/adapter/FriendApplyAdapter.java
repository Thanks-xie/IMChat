package cn.xie.imchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.config.XmppConnection;
import cn.xie.imchat.domain.ChatMessage;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.utils.DBManager;
import cn.xie.imchat.utils.Util;

/**
 * @author xiejinbo
 * @date 2019/9/27 0027 10:40
 */
public class FriendApplyAdapter extends RecyclerView.Adapter<FriendApplyAdapter.ViewHolder> {
    private Context mContext;
    private List<ChatMessage> applyList;
    private DBManager dbManager;

    public FriendApplyAdapter(Context context,List<ChatMessage> chatMessages){
        this.mContext = context;
        this.applyList = chatMessages;
        this.dbManager = new DBManager(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_friend_apply,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ChatMessage chatMessage = applyList.get(position);
        holder.applyTime.setText(Util.setChatMessageDate(chatMessage.getSendtime(),mContext));
        holder.applyName.setText(chatMessage.getSendName());
        final String applyJid = chatMessage.getSendName()+"@xie-pc";
        //同意好友申请
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XmppConnection.getInstance().acceptFriendApply(applyJid);
                XmppConnection.getInstance().addUser(applyJid,chatMessage.getUserName());
                dbManager.deleteData("chatMessage","sendname=? and myself=?", new String[]{chatMessage.getSendName(),"APPLY"});
                List<ChatUser> userInfos = XmppConnection.getInstance().searchUsers(chatMessage.getSendName());
                List<ChatUser> friendList = new ArrayList<>();
                for (ChatUser chatUser:userInfos){
                    if (chatMessage.getSendName().equals(chatUser.getUserName())){
                        friendList.add(chatUser);
                        break;
                    }
                }
                if (friendList!=null&&friendList.size()>0){
                    dbManager.addChatUserData(friendList);
                }
                applyList.remove(position);
                notifyDataSetChanged();
            }
        });
        //拒绝添加好友
        holder.refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XmppConnection.getInstance().refuseFriendApply(applyJid);
                dbManager.deleteData("chatMessage","sendname=? and myself=?", new String[]{chatMessage.getSendName(),"APPLY"});
                applyList.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return applyList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView applyTime,applyName;
        ImageView accept,refuse;
        public ViewHolder(View itemView) {
            super(itemView);
            applyTime = itemView.findViewById(R.id.apply_time);
            applyName = itemView.findViewById(R.id.apply_name);
            accept = itemView.findViewById(R.id.accept);
            refuse = itemView.findViewById(R.id.refuse);
        }
    }
}
