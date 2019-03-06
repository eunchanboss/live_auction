package chanboss.liveauction.chatting.chatin;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chanboss.liveauction.R;

public class ChatInRecylerHolder extends RecyclerView.ViewHolder {
    //item뷰와 연동
    public ConstraintLayout reciverLayout;
    public ImageView reciverImgIV;
    public TextView reciverNmTV;
    public TextView reciverMsgTV;
    public TextView reciverDateTV;
    public ConstraintLayout senderLayout;
    public TextView senderMsgTV;
    public TextView senderDateTV;
    public TextView checkYnTv;
    public ImageView reciverImgMsgIV;
    public ImageView senderImgMsgIV;





    public ChatInRecylerHolder(@NonNull View itemView) {
        super(itemView);
        //상대방
        reciverLayout = itemView.findViewById(R.id.reciverLayout);
        reciverImgIV = itemView.findViewById(R.id.reciverImgIV);
        reciverNmTV = itemView.findViewById(R.id.reciverNmTV);
        reciverMsgTV = itemView.findViewById(R.id.reciverMsgTV);
        reciverDateTV = itemView.findViewById(R.id.reciverDateTV);
        //me
        senderLayout = itemView.findViewById(R.id.senderLayout);
        senderMsgTV = itemView.findViewById(R.id.senderMsgTV);
        senderDateTV = itemView.findViewById(R.id.senderDateTV);
        checkYnTv = itemView.findViewById(R.id.checkYnTv);
        reciverImgMsgIV = itemView.findViewById(R.id.reciverImgMsgIV);
        senderImgMsgIV = itemView.findViewById(R.id.senderImgMsgIV);
    }
}
