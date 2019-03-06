package chanboss.liveauction.notice.img;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.profile.ProfileDetail;

public class Adapter extends RecyclerView.Adapter<ReHolder> {
    private ArrayList<ImgVO> imgList;
    Context context;






    public Adapter(ArrayList itemList){
        this.imgList = itemList;
    }

    @NonNull
    @Override
    public ReHolder onCreateViewHolder(@NonNull ViewGroup parant, int i) {
        View view = LayoutInflater.from(parant.getContext()).inflate(R.layout.fragment_post_img_item_list,parant,false);
        //뷰그룹에 접근
        context = parant.getContext();
        ReHolder holder = new ReHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ReHolder reHolder, final int position) {
        if(position==0){
            reHolder.textView22.setVisibility(View.VISIBLE);
        }
        Glide.with(context).load(imgList.get(position).getImgUri())
                .into(reHolder.postImg);

        reHolder.postImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                alert_confirm.setMessage("삭제하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                imgList.remove(position);
                                //새로고침
                                notifyDataSetChanged();

                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }
}
