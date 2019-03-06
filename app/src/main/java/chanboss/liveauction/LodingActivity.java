package chanboss.liveauction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.login.Login;
import chanboss.liveauction.main.Post;
import chanboss.liveauction.profile.ProfileMarge;

public class LodingActivity extends AppCompatActivity {
    ImageView logo;

    //프로필 존재 여부를 확인할 변수
    String fileName;
    String param;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loding_lay);

        logo = findViewById(R.id.logo);



        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_alpha); //Context와 Animation xml파일
        animation.setAnimationListener(new Animation.AnimationListener() {  //Animation Listener 순서대로 시작할때, 끝날때, 반복될때
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
                String sessionId = pref.getString("sessionId", "");
                //로그인 정보가 존재하지 않을때
                System.out.println(sessionId);
                if(sessionId==""){
                    Intent intent = new Intent(LodingActivity.this,Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }else{
                    //존재할 경우 프로필 존재여부 체크
                    PhpConnect phpConnect = new PhpConnect();
                    String result;
                    try{
                        fileName = "profile_check.php";
                        param = "userId="+sessionId;
                        result = phpConnect.execute(fileName,param).get();
                        if(result.equals("mainPage")){
                            Intent intent = new Intent(LodingActivity.this,Post.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }else if(result.equals("insertPage")){
                            Intent intent = new Intent(LodingActivity.this,ProfileMarge.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }else{
                            //아이디 강제 삭제할 경우 로그인 페이지로
                            //쉐어드에서도 제거

                            SharedPreferences pref1 = getSharedPreferences("sessionId", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref1.edit();
                            editor.clear();
                            editor.commit();

                            Intent intent = new Intent(LodingActivity.this,Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }catch (Exception e){

                    }


                }


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation.setFillEnabled(true);    //애니메이션 이 끝난곳에 고정할지 아닐지
        logo.startAnimation(animation);    //애니메이션 시작


    }

    /*
    //로그아웃
    com.facebook.login.LoginManager.getInstance().logOut();
    FirebaseAuth.getInstance().signOut();
    SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
    SharedPreferences.Editor editor = pref.edit();
    editor.remove("sessionId");
    editor.commit();
    */
}
