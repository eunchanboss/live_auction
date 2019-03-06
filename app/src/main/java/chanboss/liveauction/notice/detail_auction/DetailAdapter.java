package chanboss.liveauction.notice.detail_auction;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import chanboss.liveauction.R;
import chanboss.liveauction.notice.img.ImgVO;

public class DetailAdapter extends RecyclerView.Adapter<DetailHoler> {
    private ArrayList<ImgVO> imgList;
    Context context;






    public DetailAdapter(ArrayList itemList){
        this.imgList = itemList;
    }

    @NonNull
    @Override
    public DetailHoler onCreateViewHolder(@NonNull ViewGroup parant, int i) {
        View view = LayoutInflater.from(parant.getContext()).inflate(R.layout.fragment_post_detail_item,parant,false);
        //뷰그룹에 접근
        context = parant.getContext();
        DetailHoler holder = new DetailHoler(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DetailHoler reHolder, final int position) {
        Glide.with(context)
                .load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/post_img/"+imgList.get(position).getImgUri())
                .into(reHolder.postImg);

    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }
}
