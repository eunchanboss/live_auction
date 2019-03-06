package chanboss.liveauction.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;

import chanboss.liveauction.R;
import chanboss.liveauction.login.Login;
import chanboss.liveauction.profile.ProfileDetail;
import chanboss.liveauction.service.ChatService;

public class Post extends AppCompatActivity {

    //권한
    final int MY_PERMISSION_REQUEST_STORAGE =101;
    private static final String TAG = Post.class.getSimpleName();


    //프레그 먼트 사용 준비
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;

    //화면 번호 받아올 플래그
    int currentItem;

    //서비스 관련 변수
    private ChatService chatService;
    private boolean isBind;

    //어플실행시 서비스 종료
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            chatService = null;
            isBind = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_lay);
        stopService(new Intent(getApplicationContext(),ChatService.class));
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;   //SDK의 레벨을 받아온다.
        if (currentapiVersion > 22){
            checkPermission();
        } else{
            // 현재 디바이스의 버전이 롤리팝 미만일 경우.
        }
        //뷰페이저 생성
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager=findViewById(R.id.viewpager);
        viewPager.setAdapter(pagerAdapter);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //툴바 생성
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
    }

    //toolbar에 메뉴 레이아웃 넣어주기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);

        return true;
    }

    //toolbar레이아웃 셀렉트 이벤트 발생
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.profileMenuBtn:
                intent = new Intent(getApplicationContext(),ProfileDetail.class);
                startActivity(intent);
                break;

            case R.id.logoutBtn:
                SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();

                FirebaseAuth.getInstance().signOut();
                intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
                break;


        }
        return super.onOptionsItemSelected(item);
    }
    //뒤로가기 버튼 컨트롤
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("종료")
                .setMessage("Yes를 누르시면 어플이 종료됩니다.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(getApplicationContext(),ChatService.class);
        startService(intent);
        super.onDestroy();
    }

    //권한 체크
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSION_REQUEST_STORAGE);

            // MY_PERMISSION_REQUEST_STORAGE is an
            // app-defined int constant

        } else {
            // 다음 부분은 항상 허용일 경우에 해당이 됩니다.
            writeFile();
        }
    }
    private void writeFile() {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "temp.txt");
        try {
            Log.d(TAG, "create new File : " + file.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    writeFile();

                    // permission was granted, yay! do the
                    // calendar task you need to do.

                } else {

                    Log.d(TAG, "Permission always deny");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

}
