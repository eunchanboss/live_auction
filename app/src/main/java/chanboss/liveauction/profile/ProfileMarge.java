package chanboss.liveauction.profile;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import chanboss.liveauction.LodingActivity;
import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.main.Post;

public class ProfileMarge extends AppCompatActivity implements View.OnClickListener {

    //프로필 이미지 변경 버튼
    ImageButton imgBtn;

    //프로필 저장 버튼
    Button updateBtn;

    //프로필 정보
    EditText userName;
    EditText userNum;

    //정보를 담을 변수
    String suserName;
    String suserNum;
    String suserId;

    //디비연동 객체 생성
    PhpConnect phpConnect;

    //디비연동시 파라미터담을 변수
    String fileName;
    String param;

    //권한 처리
    final int MY_PERMISSION_REQUEST_STORAGE =101;
    private static final String TAG = "ProfileMarge";

    //카메라 호출& 갤러리 호출 관련 변수
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    static final int REQUEST_IMAGE_CROP = 3;
    //경로관련 변수 초기화
    String imgPath = null;
    Uri photoUri = null;
    Uri albumUri = null;
    Boolean albumYn = false;

    //파일 업로드
    int serverResponseCode = 0;
    String upLoadServerUri = null;
    ProgressDialog dialog = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_marge_lay);
        //시작시 사진과 관련한 권한 획득
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;   //SDK의 레벨을 받아온다.
        if (currentapiVersion > 22){
            checkPermission();
        } else{
            // 현재 디바이스의 버전이 롤리팝 미만일 경우.
        }
        upLoadServerUri = "http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/UploadToServer.php";//서버컴퓨터의 ip주소

        //뷰 연동
        userName = findViewById(R.id.userName);
        userNum = findViewById(R.id.userNum);
        updateBtn = findViewById(R.id.updateBtn);
        imgBtn = findViewById(R.id.imgBtn);
        if(Build.VERSION.SDK_INT >= 21) {
            imgBtn.setBackground(new ShapeDrawable(new OvalShape()));
            imgBtn.setClipToOutline(true);
        }
        UserVO userVO = new UserVO();
        //기존 정보 불러서 출력
        SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
        String sessionId = pref.getString("sessionId", "");
        PhpConnect phpConnect1 = new PhpConnect();
        String fileName1 = "profile_select.php";
        String param1 = "userId="+sessionId;
        try {
            String result = phpConnect1.execute(fileName1,param1).get();
            JSONArray jarray = new JSONArray(result);
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                String suserName = jObject.getString("userName");
                String suserImg = jObject.getString("userImg");
                String suserNum =  jObject.getString("userNum");
                userName.setText(suserName);
                userNum.setText(suserNum);
                Glide.with(this).load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/user_img/"+suserImg).apply(new RequestOptions().circleCrop()).into(imgBtn);

            }
        }catch (Exception e){

        }


        //프로필 저장 버튼 이벤트
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //값을 가져와서 변수에 담기
                suserName = userName.getText().toString();
                suserNum = userNum.getText().toString();

                //업데이트를 위해 저장된 아이디 값을 가져온다.
                SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
                String sessionId = pref.getString("sessionId", "");
                suserId = sessionId;

                //가져온 값이 없을경우 처리
                if(!suserName.equals("")||!suserNum.equals("")) {
                    phpConnect = new PhpConnect();
                    String result;
                    try{
                        fileName = "profile_update.php";
                        param = "userId="+suserId+"&userName="+suserName+"&userNum="+suserNum;
                        result = phpConnect.execute(fileName,param).get();
                        if(result.equals("success")){
                            Toast.makeText(getApplicationContext(),"저장완료!",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(),ProfileDetail.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(),"저장실패!",Toast.LENGTH_LONG).show();
                            return;
                        }
                    }catch (Exception e){

                    }
                }else{
                    Toast.makeText(getApplicationContext(), "빈칸을 채워주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        //이미지 버튼 이벤트
        imgBtn.setOnClickListener(this);

    }

    /*
     * 카메라에서 이미지 가져오기
     */
    private void doTakePhotoAction()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 임시로 사용할 파일의 경로를 생성
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    /*
     * 앨범에서 이미지 가져오기
     */
    private void doTakeAlbumAction()
    {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    public String getBase64String(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }







    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK)
        {
            return;
        }
        switch(requestCode)
        {
            case REQUEST_IMAGE_CAPTURE:
            {

                final Bundle extras = data.getExtras();
                    if (extras != null) {
                        final Bitmap photo = extras.getParcelable("data");
                        //받아온 데이터 set하기
                        imgBtn.setImageBitmap(photo);

                        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                        File myDir = new File(root);
                        myDir.mkdirs();
                        String fname = UUID.randomUUID().toString() + ".png";
                        File file = new File(myDir, fname);
                        if (file.exists()) file.delete();
                        Log.i("LOAD", root + fname);
                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            photo.compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);

                        final String photoPath = root+"/"+fname;

                        //저장된 이미지 파일 업로드
                        PhpConnect phpConnect = new PhpConnect();
                        SharedPreferences pref1 = getSharedPreferences("sessionId", MODE_PRIVATE);
                        String sessionId1 = pref1.getString("sessionId", "");
                        try{
                            fileName = "img_update.php";
                            String[] array = photoPath.split("/");
                            String path = array[array.length-1];
                            param = "userId="+sessionId1+"&imgPath="+path;
                            phpConnect.execute(fileName,param);

                        }catch (Exception e){

                        }
                        //File file = new File(photoUri.getPath());

                        new Thread(new Runnable() {
                            public void run() {

                                uploadFile(photoPath);
                            }
                        }).start();
                    }

                // 임시 파일 삭제

                break;
            }

            case REQUEST_TAKE_PHOTO:
            {

                photoUri = data.getData();
                final String photoPath = getPath(photoUri);
                System.out.println("111111111111111");
                System.out.println(photoPath);
                imgBtn.setImageURI(photoUri);
                PhpConnect phpConnect = new PhpConnect();
                SharedPreferences pref1 = getSharedPreferences("sessionId", MODE_PRIVATE);
                String sessionId1 = pref1.getString("sessionId", "");
                try{
                    fileName = "img_update.php";
                    String[] array = photoPath.split("/");
                    String path = array[array.length-1];
                    param = "userId="+sessionId1+"&imgPath="+path;
                    phpConnect.execute(fileName,param);

                }catch (Exception e){

                }
                //File file = new File(photoUri.getPath());

                new Thread(new Runnable() {
                    public void run() {
                        uploadFile(photoPath);
                    }
                }).start();
            }

        }
    }

    //이미지 업로드
    public int uploadFile(String sourceFileUri) {
        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            runOnUiThread(new Runnable() {
                public void run() {

                }
            });
            return 0;
        }
        else
        {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);


                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {

                        }
                    });
                }
                //close the streams //
                fileInputStream.close();

                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                dialog.dismiss();
                ex.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
                runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });

            }
            return serverResponseCode;
        } // End else block
    }


    //권한처리
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

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
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

    @Override
    public void onClick(View v){
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                doTakePhotoAction();
            }
        };
        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                doTakeAlbumAction();
            }
        };
        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("업로드할 이미지 선택")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("앨범선택", albumListener)
                .setNegativeButton("취소", cancelListener)
                .show();
    }

    //이미지 절대 경로 구하기
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

}
