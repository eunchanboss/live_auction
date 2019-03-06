package chanboss.liveauction.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import chanboss.liveauction.LodingActivity;
import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.login.Login;

public class ProfileDetail extends AppCompatActivity {

    /*조회 항목
    **userName
    **userGrade
    **userItemCount
    **follower
    **following
    **userImgPath
    **userComment
    */

    //불러올 데이터의 뷰선언
    TextView userName;
    TextView userGrade;
    TextView userItemCount;
    TextView follower;
    TextView following;

    //버튼
    ImageButton userImg;
    ImageButton settingBtn;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_detail_lay);
       // CLASS =  ItemChat
        //뷰연동
        userName = findViewById(R.id.userName);
        userGrade = findViewById(R.id.userGrade);
        follower = findViewById(R.id.follower);
        following = findViewById(R.id.following);
        userItemCount = findViewById(R.id.userItemCount);

        userImg = findViewById(R.id.userImg);
        settingBtn = findViewById(R.id.settingBtn);
        settingBtn.setVisibility(View.GONE);
        //세션 아이디로 유저 관련정보 조회하기
        Intent gIntent = getIntent();
        String userId = gIntent.getStringExtra("userId");

        SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
        String sessionId = pref.getString("sessionId", "");
        if(userId!=null){

            if(userId.equals(sessionId)){
                settingBtn.setVisibility(View.VISIBLE);
                settingBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(),ProfileMarge.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
            }else{
                sessionId = userId;
            }
        }else{
                settingBtn.setVisibility(View.VISIBLE);
                settingBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(),ProfileMarge.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
        }


        PhpConnect phpConnect = new PhpConnect();
        String result;
        String fileName = "profile_detail.php";
        String param = "userId="+sessionId;
        try {
            result = phpConnect.execute(fileName,param).get();
            JSONArray jarray = new JSONArray(result);
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                String suserName = jObject.getString("userName");
                String suserGrade = jObject.getString("userGrade");
                String suserImg = jObject.getString("userImg");
                String sfollower = jObject.getString("follower");
                String sfollowing = jObject.getString("following");
                String suserItemCount =  jObject.getString("userItemCount");
                int grade = Integer.parseInt(suserGrade);
                if(grade>50){
                    userGrade.setText("다이아");
                }else if(grade>40) {
                    userGrade.setText("플레티넘");
                }else if(grade>30){
                    userGrade.setText("골드");
                }else if(grade>20){
                    userGrade.setText("실버");
                }else{
                    userGrade.setText("브론즈");
                }
                userName.setText(suserName);
                follower.setText(sfollower);
                following.setText(sfollowing);
                userItemCount.setText(suserItemCount);
                Glide.with(this).load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/user_img/"+suserImg).apply(new RequestOptions().circleCrop()).into(userImg);


            }
        }catch (Exception e){

        }


    }
}
