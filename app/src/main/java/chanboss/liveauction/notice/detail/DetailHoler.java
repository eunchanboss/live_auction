package chanboss.liveauction.notice.detail;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chanboss.liveauction.R;

public class DetailHoler extends RecyclerView.ViewHolder {
    public ImageView postImg;



    public DetailHoler(@NonNull View itemView) {

        super(itemView);
        //이미지 리스트
        postImg = itemView.findViewById(R.id.postImg);


    }
}
