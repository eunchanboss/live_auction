package chanboss.liveauction.streaming;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chanboss.liveauction.R;

public class LiveroomRecylerHolder extends RecyclerView.ViewHolder {
    public ImageView userImgIV;
    public TextView userNameTV;
    public TextView pdNowPriceTV;
    public ImageView pdImgIV;
    public TextView liveNameTV;
    public ConstraintLayout listItem;
    public TextView liveStat;




    public LiveroomRecylerHolder(@NonNull View itemView) {
        super(itemView);
        userImgIV = itemView.findViewById(R.id.userImg);
        userNameTV = itemView.findViewById(R.id.userName);
        pdNowPriceTV = itemView.findViewById(R.id.pdNowPrice);
        pdImgIV = itemView.findViewById(R.id.pdImg);
        liveNameTV = itemView.findViewById(R.id.liveName);
        listItem = itemView.findViewById(R.id.listItem);
        liveStat = itemView.findViewById(R.id.liveStat);
    }
}
