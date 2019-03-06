package chanboss.liveauction.notice;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
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

public class PostAdapter extends RecyclerView.Adapter<PostRecyclerHolder> {
    private ArrayList<PostVO> postList;
    Context context;
    //시간계산
    final static int MIN = 60;
    final static int HOUR = MIN *60;

    String timer;



    public PostAdapter(ArrayList itemList){
        this.postList = itemList;
    }

    @NonNull
    @Override
    public PostRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parant, int i) {
        View view = LayoutInflater.from(parant.getContext()).inflate(R.layout.fragment_post_item_list,parant,false);
        //뷰그룹에 접근
        context = parant.getContext();
        PostRecyclerHolder holder = new PostRecyclerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PostRecyclerHolder postRecyclerHolder, final int position) {

        //유저정보는 유저 아이디에서 추출
        //사용자 이미지 경로
        //postRecyclerHolder.userImg.setText(postList.get(position).getUserImg());
        postRecyclerHolder.userId.setText(postList.get(position).getUserId());
        PhpConnect phpConnect1 = new PhpConnect();
        String fileName1 = "profile_select.php";
        String param1 = "userId="+postList.get(position).getUserId();
        try {
            String result = phpConnect1.execute(fileName1,param1).get();
            JSONArray jarray = new JSONArray(result);
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                String suserName = jObject.getString("userName");
                String suserImg = jObject.getString("userImg");
                Glide.with(context).load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/user_img/"+suserImg).apply(new RequestOptions().circleCrop()).into(postRecyclerHolder.userImg);
                postRecyclerHolder.userName.setText(suserName);

            }
        }catch (Exception e){

        }
        postRecyclerHolder.userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ProfileDetail.class);;
                intent.putExtra("userId",postList.get(position).getUserId());
                context.startActivity(intent);
            }
        });


        postRecyclerHolder.auctionId.setText(postList.get(position).getAuctionContentsId());
        postRecyclerHolder.pdTitle.setText(postList.get(position).getAuctionTitle());
        postRecyclerHolder.pdStartPrice.setText(String.format("%,d", intChanger(postList.get(position).getStartPrice())));
        postRecyclerHolder.pdNowPrice.setText(String.format("%,d", intChanger(postList.get(position).getLastPrice())));
        postRecyclerHolder.pdNowBuy.setText(String.format("%,d", intChanger(postList.get(position).getNowBuy())));


        Glide.with(context)
                .load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/post_img/"+postList.get(position).getPdImg())
                .into(postRecyclerHolder.pdImg);
        //쓰레드로 (완료시간-현재시간)
//        int start = Integer.parseInt(postList.get(position).getLastTime());
//        start = start * 24;
        int numInt = Integer.parseInt(postList.get(position).getLastTime());


        if(numInt>0){
            final int[] i = {numInt};
            new CountDownTimer(3600*60*12, 1000){
                @Override
                public void onTick(long millisUntilFinished) { // 총 시간과 주기
                    for(int j = 0; j < i.length; j++){
                        i[j]--;
                        if(i[j] >0){
                            int s = i[j] % 3600 % 60;
                            int m = i[j] % 3600 / 60;
                            int h = i[j] / 3600;
                            String ss = String.valueOf(s);
                            String ms = String.valueOf(m);
                            String hs = String.valueOf(h);
                            postRecyclerHolder.time.setText(hs+"시간"+ms+"분"+ss+"초 남음");

                        }else{
                            postRecyclerHolder.time.setText("종료");
                        }
                    }
                }

                @Override
                public void onFinish() {

                }
            }.start();  // 타이머 시작
        }else{
            postRecyclerHolder.time.setText("종료");
            postRecyclerHolder.pdNowPrice.setText(String.format("%,d", intChanger(postList.get(position).getLastPrice())));
            postRecyclerHolder.pdNowPricett.setText("최종가격");
            if(postList.get(position).getLastPrice().equals("")||postList.get(position).getLastPrice().equals(null)){
                postRecyclerHolder.pdNowBuy.setText("유찰");
            }else{
                postRecyclerHolder.pdNowBuy.setText("낙찰");
            }
        }



        postRecyclerHolder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,PostDetail.class);
                intent.putExtra("postId",postList.get(position).getAuctionContentsId());
                context.startActivity(intent);
            }
        });





    }

    //스트링을 인트로 변환해주는 메소드
    public int intChanger(String priceInput){

        if(priceInput.equals("")||priceInput.equals(null)){
            return 0;
        }else{
            int return_int = Integer.parseInt(priceInput.replaceAll(",",""));
            return return_int;
        }
    }

    //인트를 스트링으로 변환
    public String stringChanger(int priceInput){
        String return_string = String.valueOf(priceInput);
        return return_string;
    }
    @Override
    public int getItemCount() {
        return postList.size();
    }
}
