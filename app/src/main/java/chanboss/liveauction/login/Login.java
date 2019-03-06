package chanboss.liveauction.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.CallbackManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chanboss.liveauction.LodingActivity;
import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.main.Post;
import chanboss.liveauction.profile.ProfileMarge;

public class Login extends AppCompatActivity {

    //로그인 정보
    EditText userId;
    EditText userPwd;


    //버튼 정의
    Button signInBtn;
    Button signUpBtn;

    // 구글로그인 result 상수
    private static final int RC_SIGN_IN = 900;
    // 구글api클라이언트
    private GoogleSignInClient googleSignInClient;
    // 파이어베이스 인증 객체 생성
    private FirebaseAuth firebaseAuth;
    // 구글  로그인 버튼
    private SignInButton buttonGoogle;

    //페북 로그인
    private CallbackManager callbackManager;
    LoginButton btn_facebookSignIn;

    //로그인정보 담을 변수
    String suserId;
    String suserPwd;

    //mysql 데이터 저장 클래스
    PhpConnect phpConnect;
    //php 연동 파라미터
    String param;
    String fileName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_lay);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("TEST", "key_hash=" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //페이스북 sdk 활성화
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        //layout view와 연결
        userId = findViewById(R.id.userId);
        userPwd = findViewById(R.id.userPwd);

        signInBtn = findViewById(R.id.signInBtn);
        signUpBtn = findViewById(R.id.signUpBtn);

        //회원가입 화면 이동 이벤트
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignUp.class);
                startActivity(intent);
            }
        });

        //파이어베이스 인스턴스 생성
        firebaseAuth = FirebaseAuth.getInstance();

        //구글 로그인 인증 객체 생성
        buttonGoogle = findViewById(R.id.btn_googleSignIn);

        // Google 로그인을 앱에 통합
        // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


        // 페이스북 콜백 함수 등록
        callbackManager = CallbackManager.Factory.create();

        btn_facebookSignIn = findViewById(R.id.btn_facebookSignIn);
        btn_facebookSignIn.setReadPermissions("email", "public_profile");
        btn_facebookSignIn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("토큰",loginResult.getAccessToken().getToken());
                Log.e("유저아이디",loginResult.getAccessToken().getUserId());
                Log.e("퍼미션 리스트",loginResult.getAccessToken().getPermissions()+"");
                handleFacebookAccessToken(loginResult.getAccessToken());

                SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("sessionId", loginResult.getAccessToken().getUserId());
                editor.commit();

                //mysql 데이터 베이스에 회원정보가 있는지 체크
                PhpConnect phpConnect1 = new PhpConnect();
                String result;
                try{
                    fileName = "sns_login.php";
                    param = "userId="+loginResult.getAccessToken().getUserId();
                    result = phpConnect1.execute(fileName,param).get();
                    if(result.equals("success")){

                        //존재할 경우 프로필 존재여부 체크(페이스북)
                        PhpConnect phpConnect = new PhpConnect();
                        String result1;
                        try{
                            fileName = "profile_check.php";
                            param = "userId="+loginResult.getAccessToken().getUserId();
                            result1 = phpConnect.execute(fileName,param).get();
                            if(result1.equals("mainPage")){
                                Intent intent = new Intent(getApplicationContext(),Post.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }else if((result1.equals("insertPage"))){
                                Intent intent = new Intent(getApplicationContext(),ProfileMarge.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(),"테스트"+result1,Toast.LENGTH_LONG).show();

                            }
                        }catch (Exception e){

                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"로그인 실패! 다시 시도해 주세요",Toast.LENGTH_LONG).show();
                        return;
                    }
                }catch (Exception e){
                    // 로그인 실패
                    Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                    return;
                }


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("에러",error.toString());
            }
        });


        //일반 로그인 이벤트
        signInBtn = findViewById(R.id.signInBtn);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suserId = userId.getText().toString();
                suserPwd = userPwd.getText().toString();

                //빈칸 체크
                if(suserId.equals("")||suserPwd.equals("")){
                    Toast.makeText(getApplicationContext(),"빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
                    return;
                }

                //이메일 형식인지 체크
                String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(suserId);
                boolean b = m.matches();
                if(!b){
                    Toast.makeText(getApplicationContext(),"올바른 이메일 형식이 아닙니다.",Toast.LENGTH_LONG).show();
                    return;
                }

                //아이디 패스워드 정상 여부
                phpConnect = new PhpConnect();
                String result;
                try{
                    fileName = "login.php";
                    param = "userId="+suserId+"&userPwd="+suserPwd;
                    result = phpConnect.execute(fileName,param).get();
                    if(result.equals("success")){
                        SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("sessionId", suserId);
                        editor.commit();

                        //존재할 경우 프로필 존재여부 체크(일반)
                        SharedPreferences pref22 = getSharedPreferences("sessionId", MODE_PRIVATE);
                        String sessionId = pref22.getString("sessionId", "");
                        PhpConnect phpConnect = new PhpConnect();
                        String result1;
                        try{
                            fileName = "profile_check.php";
                            param = "userId="+sessionId;
                            result1 = phpConnect.execute(fileName,param).get();
                            if(result1.equals("mainPage")){
                                Intent intent = new Intent(getApplicationContext(),Post.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }else if((result1.equals("insertPage"))){
                                Intent intent = new Intent(getApplicationContext(),ProfileMarge.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }catch (Exception e){

                        }

                    }else{
                        Toast.makeText(getApplicationContext(),"ID와 PASSWORD가 일치하지 않습니다.",Toast.LENGTH_LONG).show();
                        return;
                    }
                }catch (Exception e){

                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 페이스북 콜백 등록
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // 구글로그인 버튼 응답
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // 구글 로그인 성공
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {

            }
        }
    }

     /*
     페이스북 로그인 이벤트
     사용자가 정상적으로 로그인한 후 페이스북 로그인 버튼의 onSuccess 콜백 메소드에서 로그인한 사용자의
     액세스 토큰을 가져와서 Firebase 사용자 인증 정보로 교환하고,
     Firebase 사용자 인증 정보를 사용해 Firebase에 인증.
     */
    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                        } else {
                            // 로그인 실패
                            Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    /*
     사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID 토큰을 가져와서
     Firebase 사용자 인증 정보로 교환하고 Firebase 사용자 인증 정보를 사용해 Firebase에 인증합니다.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            String email = firebaseAuth.getCurrentUser().getEmail();
                            SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("sessionId", email);
                            editor.commit();
                            //mysql 데이터 베이스에 회원정보가 있는지 체크
                            PhpConnect phpConnect1 = new PhpConnect();
                            String result;
                            try{
                                fileName = "sns_login.php";
                                param = "userId="+email;
                                result = phpConnect1.execute(fileName,param).get();
                                if(result.equals("success")){
                                    //존재할 경우 프로필 존재여부 체크(페이스북)
                                    PhpConnect phpConnect = new PhpConnect();
                                    String result1;
                                    try{
                                        fileName = "profile_check.php";
                                        param = "userId="+email;
                                        result1 = phpConnect.execute(fileName,param).get();
                                        if(result1.equals("mainPage")){
                                            Intent intent = new Intent(getApplicationContext(),Post.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }else if((result1.equals("insertPage"))){
                                            Intent intent = new Intent(getApplicationContext(),ProfileMarge.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }catch (Exception e){

                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(),"로그인 실패! 다시 시도해 주세요",Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }catch (Exception e){
                                // 로그인 실패
                                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // 로그인 실패
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
