package chanboss.liveauction.notice.img;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chanboss.liveauction.R;

public class ReHolder extends RecyclerView.ViewHolder {
    public ImageView postImg;
    public TextView textView22;



    public ReHolder(@NonNull View itemView) {

        super(itemView);
        postImg = itemView.findViewById(R.id.postImg);
        textView22 = itemView.findViewById(R.id.textView22);


    }
}
