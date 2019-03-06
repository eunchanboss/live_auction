package videortc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import chanboss.liveauction.R;
import chanboss.liveauction.chatting.chatin.ChatInList;
import chanboss.liveauction.chatting.chatin.ChatInVO;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.database.SessionManager;
import chanboss.liveauction.profile.UserVO;

public class ReceptionActivity extends AppCompatActivity {

    String senderName;
    String senderImg;

    ImageView senderImgIV;
    TextView senderNameTV;
    ImageButton callBtn;
    ImageButton rejectBtn;

    PhpConnect phpConnect;
    String fileName;
    String param;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_waiting);

        Intent gIntent = getIntent();

        //뷰연동
        senderImgIV=findViewById(R.id.senderImgIV);
        senderNameTV=findViewById(R.id.senderNameTV);
        callBtn=findViewById(R.id.callBtn);
        rejectBtn=findViewById(R.id.rejectBtn);

        phpConnect = new PhpConnect();
        fileName = "chat_user_select.php";

        param = "userId="+gIntent.getStringExtra("senderId");
        try {
            String userResult = phpConnect.execute(fileName, param).get();
            JSONArray jarray = new JSONArray(userResult);
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jObject = jarray.getJSONObject(0);  // JSONObject 추출
                //채팅정보 조회결과
                senderName = jObject.getString("userName");
                senderImg = jObject.getString("userImg");
                senderNameTV.setText(senderName);
                Glide
                        .with(getApplicationContext())
                        .load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/user_img/"+senderImg)
                        .apply(new RequestOptions().circleCrop())
                        .into(senderImgIV);
            }
        }catch (Exception e){
            System.out.println(e);
        }

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CallActivity.class);
                intent.putExtra("randomRoomId",gIntent.getStringExtra("randomRoomId"));
                System.out.println(gIntent.getStringExtra("randomRoomId"));
                System.out.println("test02");
                intent.putExtra("userId",gIntent.getStringExtra("senderId"));
                intent.putExtra("chatroomId",gIntent.getStringExtra("chatroomId"));
                startActivity(intent);
                finish();
            }
        });

        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceptionActivity.this,ChatInList.class);
                intent.putExtra("userId",gIntent.getStringExtra("senderId"));
                intent.putExtra("chatroomId",gIntent.getStringExtra("chatroomId"));
                startActivity(intent);
                finish();
            }
        });

    }
}
