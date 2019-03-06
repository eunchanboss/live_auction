package chanboss.liveauction.streaming;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import chanboss.liveauction.R;
import io.antmedia.android.liveVideoPlayer.LiveVideoPlayerActivity;


public class LiveroomAdapter extends RecyclerView.Adapter<LiveroomRecylerHolder> {
    //컨텍스트 생성
    Context context;
    private ArrayList<LiveAuctionVO> liveAuctionList;

    public LiveroomAdapter(ArrayList<LiveAuctionVO> liveAuctionList) {
        this.liveAuctionList = liveAuctionList;
    }

    @NonNull
    @Override
    public LiveroomRecylerHolder onCreateViewHolder(@NonNull ViewGroup parant, int i) {
        View view = LayoutInflater.from(parant.getContext()).inflate(R.layout.fragment_streamroom_item_list,parant,false);
        context = parant.getContext();
        LiveroomRecylerHolder holder = new LiveroomRecylerHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull LiveroomRecylerHolder liveroomRecylerHolder, int position) {
        //받아온값 itemview에 매칭
        //1.텍스트뷰 부터
        liveroomRecylerHolder.liveNameTV.setText(liveAuctionList.get(position).getLiveName());      //방송제목
        liveroomRecylerHolder.pdNowPriceTV.setText(Integer.toString(liveAuctionList.get(position).getNowPrice()));    //현재가격(쉼표단위 셋팅)
        liveroomRecylerHolder.userNameTV.setText(liveAuctionList.get(position).getUserName());      //유저이름
        switch (liveAuctionList.get(position).getLiveStat()){
            case "WAIT" :
                liveroomRecylerHolder.liveStat.setText("준비중");
                break;
            case "START" :
                liveroomRecylerHolder.liveStat.setText("경매중");
                break;
            case "STOP" :
                liveroomRecylerHolder.liveStat.setText("종료");
                break;
        }


        //2.이미지뷰
        Glide   //유저
                .with(context)
                .load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/user_img/"+liveAuctionList.get(position).getUserImg())
                .apply(new RequestOptions().circleCrop())
                .into(liveroomRecylerHolder.userImgIV);

        Glide   //유저
                .with(context)
                .load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/post_img/"+liveAuctionList.get(position).getPdImg())
            .into(liveroomRecylerHolder.pdImgIV);

    //클릭이벤트 필요함
        liveroomRecylerHolder.listItem.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (liveAuctionList.get(position).getLiveStat()){
                case "WAIT" :
                    Toast.makeText(context,"준비중 입니다.",Toast.LENGTH_LONG).show();
                    break;
                case "START" :
                    Intent intent = new Intent(context,LiveVideoPlayerActivity.class);
                    intent.putExtra("liveId",liveAuctionList.get(position).getLiveId());
                    intent.putExtra("liveName",liveAuctionList.get(position).getLiveName());
                    context.startActivity(intent);
                    break;
                case "STOP" :
                    Toast.makeText(context,"종료된 경매 입니다.",Toast.LENGTH_LONG).show();
                    break;
            }

        }
    });
}

    @Override
    public int getItemCount() {
        return liveAuctionList.size();
    }
}
