package cn.xie.imchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.xie.imchat.R;
import cn.xie.imchat.domain.ChatRoom;

/**
 * @author xiejinbo
 * @date 2019/10/8 0008 14:57
 */
public class ChatGroupListAdapter extends RecyclerView.Adapter<ChatGroupListAdapter.ViewHolder> {

    private Context context;
    private List<ChatRoom> chatRoomList;
    private ItemOnclickListener itemOnclickListener;

    public void setItemOnclickListener(ItemOnclickListener itemOnclickListener){
        this.itemOnclickListener = itemOnclickListener;
    }

    public interface ItemOnclickListener{
        void Click(int position);
    }

    public ChatGroupListAdapter(Context context,List<ChatRoom> chatRooms){
        this.context = context;
        this.chatRoomList = chatRooms;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chatgroup_list_adapter,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomList.get(position);
        holder.roomName.setText(chatRoom.getRoomName());
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView roomName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.group_name);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if (itemOnclickListener!=null){
                itemOnclickListener.Click(getAdapterPosition());
            }
        }
    }
}
