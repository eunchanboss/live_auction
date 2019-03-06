package chanboss.liveauction.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnect;

public class SignUp extends AppCompatActivity {

    //회원가입 정보
    EditText userId;
    EditText userPwd;
    EditText userPwdCh;

    //버튼 정의
    Button signUpBtn;

    //회원가입정보 담을 변수
    String suserId;
    String suserPwd;
    String suserPwdCh;

    //mysql 데이터 저장 클래스
    PhpConnect phpConnect;

    //데이터 저장에 필요한 파일명을 담을 변수&&파라미터
    String fileName;
    String param;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_lay);

        //layout view와 연결
        userId = findViewById(R.id.userId);
        userPwd = findViewById(R.id.userPwd);
        userPwdCh = findViewById(R.id.userPwdCh);

        signUpBtn = findViewById(R.id.signUpBtn);

        //가입 버튼 이벤트
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //view에서 값 가져와서 변수에 담기
                suserId = userId.getText().toString();
                suserPwd = userPwd.getText().toString();
                suserPwdCh = userPwdCh.getText().toString();

                //빈칸 여부 체크
                if(suserId.equals("")||suserPwd.equals("")||suserPwdCh.equals("")){
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

                //비밀번호 동일여부 체크
                if(suserPwd.equals(suserPwdCh)) {
                    //성공
                    phpConnect = new PhpConnect();
                    String result;
                    try{
                        fileName = "signUp.php";
                        param = "userId="+suserId+"&userPwd="+suserPwd;
                        result = phpConnect.execute(fileName,param).get();

                        //insert문 성공 여부
                        if(result.equals("success")){
                            Toast.makeText(getApplicationContext(),"가입 완료",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(),Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(),"중목된 아이디 입니다.",Toast.LENGTH_LONG).show();
                            return;
                        }
                    }catch (Exception e){

                    }
                }else{
                    //실패
                    Toast.makeText(getApplicationContext(),"비밀번호가 일치하지 않습니다.",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

    }

}
