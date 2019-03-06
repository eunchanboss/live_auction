package chanboss.liveauction.notice;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import org.w3c.dom.Text;

import chanboss.liveauction.R;

public class PostRecyclerHolder extends RecyclerView.ViewHolder {
    public ImageView userImg;
    public TextView userName;
    public TextView userId;
    public TextView auctionId;
    public TextView pdTitle;
    public TextView pdStartPrice;
    public TextView pdNowPrice;
    public TextView pdNowBuy;
    public TextView time;
    public ImageView pdImg;
    public ConstraintLayout listItem;
    public TextView pdNowPricett;




    public PostRecyclerHolder(@NonNull View itemView) {

        super(itemView);
        userImg = itemView.findViewById(R.id.userImg);
        userName = itemView.findViewById(R.id.userName);
        userId = itemView.findViewById(R.id.userId);
        auctionId = itemView.findViewById(R.id.auctionId);
        pdTitle = itemView.findViewById(R.id.pdTitle);
        pdStartPrice = itemView.findViewById(R.id.pdStartPrice);
        pdNowPrice = itemView.findViewById(R.id.pdNowPrice);
        pdNowBuy = itemView.findViewById(R.id.pdNowBuy);
        time = itemView.findViewById(R.id.time);
        pdImg = itemView.findViewById(R.id.pdImg);
        listItem = itemView.findViewById(R.id.listItem);
        pdNowPricett = itemView.findViewById(R.id.pdNowPricett);

    }
}
