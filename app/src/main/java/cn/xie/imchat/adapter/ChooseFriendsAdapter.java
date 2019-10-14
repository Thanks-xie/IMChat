package cn.xie.imchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.domain.ChatUser;
import cn.xie.imchat.utils.Util;

/**
 * @author xiejinbo
 * @date 2019/10/9 0009 9:48
 */
public class ChooseFriendsAdapter extends RecyclerView.Adapter<ChooseFriendsAdapter.ViewHolder> {
    private Context context;
    private List<ChatUser> chatUserList;
    private ItemClickListener itemClickListener;

    public void setItemOnClickListener(ItemClickListener itemOnClickListener){
        this.itemClickListener = itemOnClickListener;
    }

    public interface ItemClickListener{
        void onClick(int position,View view);
    }

    public ChooseFriendsAdapter(Context context,List<ChatUser> chatUsers){
        this.context = context;
        this.chatUserList = chatUsers;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.choose_friends_list_adapter,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatUser chatUser = chatUserList.get(position);
        holder.checkBox.setChecked(chatUser.isCheckbox());
        Util.showName(context,holder.textView,chatUser.getUserName(),chatUser.getNickName());

    }

    @Override
    public int getItemCount() {
        return chatUserList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CheckBox checkBox;
        TextView textView;
        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            textView = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if (itemClickListener!=null){
                itemClickListener.onClick(getAdapterPosition(),v);
            }
        }
    }
}
