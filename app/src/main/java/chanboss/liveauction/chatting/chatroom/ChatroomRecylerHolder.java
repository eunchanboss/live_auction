package chanboss.liveauction.chatting.chatroom;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chanboss.liveauction.R;

public class ChatroomRecylerHolder extends RecyclerView.ViewHolder {
    public ImageView userImgIV;
    public TextView userNameTV;
    public TextView lastMsgTV;
    public TextView lastMsgDateTV;
    public ConstraintLayout chatroomEnterBtn;




    public ChatroomRecylerHolder(@NonNull View itemView) {
        super(itemView);

        userImgIV = itemView.findViewById(R.id.userImgIV);
        userNameTV = itemView.findViewById(R.id.userNameTV);
        lastMsgTV = itemView.findViewById(R.id.lastMsgTV);
        lastMsgDateTV = itemView.findViewById(R.id.lastMsgDateTV);
        chatroomEnterBtn = itemView.findViewById(R.id.chatroomEnterBtn);
    }
}
