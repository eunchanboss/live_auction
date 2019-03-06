package chanboss.liveauction.notice;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.main.Post;
import chanboss.liveauction.notice.img.Adapter;
import chanboss.liveauction.notice.img.ImgVO;

public class PostMerge extends AppCompatActivity {

    //상단바
    private Toolbar toolbar;
    //셀렉트 박스 연결
    Spinner spinner;
    ArrayAdapter arrayAdapter;
    // 세자리로 끊어서 쉼표 보여주고, 소숫점 셋째짜리까지 보여준다.
    DecimalFormat df = new DecimalFormat("###,###.####");
    // 값 셋팅시, StackOverFlow를 막기 위해서, 바뀐 변수를 저장해준다.
    String result="";
    String result1="";

    //수정시 뷰안보이기
    ConstraintLayout constraintLayout;
    LinearLayout linearLayout5;

    String imgData;
    EditText nowBuyPrice;
    EditText startPrice;
    EditText pdTitle;
    EditText pdContents;

    ImageView postImg;

    Button saveBtn;
    Button updateBtn;

    String iuserId;
    String iuserName;
    String iuserImg;
    String ipostId;
    String ipostContents;
    String ipostImg;
    String inowBuyPrice;
    String istartPrice;
    String iterm;
    String ipostTitle;

    String sessionId;


    //param
    String fileName;
    String param;

    //연결객체
    PhpConnect phpConnect;

    //constant
    final int PICTURE_REQUEST_CODE = 100;

    //이미지 리사이클러뷰
    ArrayList<ImgVO> imgList;
    ImgVO imgVO;

    private Adapter adapter;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    //이미지 업로드
    //파일 업로드
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String upLoadServerUri = null;
    ArrayList<ImgVO> upImgList;

    String postImgList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_merge);

        //전송용 데이터
        SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
        sessionId = pref.getString("sessionId", "");

        upLoadServerUri = "http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/img_upload.php";//서버컴퓨터의 ip주소

        //뷰연동
        pdTitle = findViewById(R.id.pdTitle);
        pdContents = findViewById(R.id.pdContents);

        postImg = findViewById(R.id.postImg);
        saveBtn = findViewById(R.id.saveBtn);

        updateBtn = findViewById(R.id.updateBtn);

        //수정시 뷰
        constraintLayout = findViewById(R.id.constraintLayout);
        linearLayout5 = findViewById(R.id.linearLayout5);

        //리사이클러뷰 뷰연동
        //리사이클러뷰 생성
        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        imgList = new ArrayList<>();
        imgList.clear();



        postImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                //사진을 여러개 선택할수 있도록 한다
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),  PICTURE_REQUEST_CODE);
            }
        });



        //완료 버튼 이벤트
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //유저정보 받아오기
                phpConnect = new PhpConnect();
                iuserId = sessionId;

                String result;
                try{
                    fileName = "user_select.php";
                    param = "userId="+iuserId;
                    result = phpConnect.execute(fileName,param).get();
                    JSONArray jarray = new JSONArray(result);
                    for(int i=0; i < jarray.length(); i++){
                        JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                        iuserName = jObject.getString("userName");
                        iuserImg = jObject.getString("userImg");
                    }

                }catch (Exception e){

                }
                //post테이블에 데이터 넣기
                ipostTitle = pdTitle.getText().toString();
                ipostContents = pdContents.getText().toString();
                istartPrice = startPrice.getText().toString();
                inowBuyPrice = nowBuyPrice.getText().toString();
                iterm = spinner.getSelectedItem().toString();
                iterm = iterm.substring(0,1);
                String uuid = UUID.randomUUID().toString();
                String test =getPath(imgList.get(0).getImgUri());
                int test2 = test.split("/").length-1;
                ipostImg = getPath(imgList.get(0).getImgUri()).split("/")[test2];



                if(!ipostTitle.equals("")||!ipostContents.equals("")||!istartPrice.equals("")||!inowBuyPrice.equals("")||imgList.get(0).getImgUri()!=null){
                    //숫자변환 가격체크
                    String istartPrice2 = istartPrice.replace(",","");
                    String inowBuyPrice2 = inowBuyPrice.replace(",","");
                    int start = Integer.parseInt(istartPrice2);
                    int last =Integer.parseInt(inowBuyPrice2);


                    if(start>1000) {
                        if (start < last) {
                            PhpConnect phpConnect2 = new PhpConnect();
                            fileName = "post_insert.php";
                            param = "userId="+iuserId+"&auctionId="+uuid+"&userName="+null+"&userImg="+null+"&pdImg="+ipostImg+"&pdTitle="+ipostTitle+"&pdContents="+ipostContents+"&pdStartPrice="+istartPrice2+"&pdNowBuy="+inowBuyPrice2+"&pdLastTime="+iterm;
                            try{
                                String result2 = phpConnect2.execute(fileName,param).get();
                                if(result2.equals("success")){
                                    //파일 업로드
                                    for(int i =0;i<imgList.size();i++){
                                        final String photoPath = getPath(imgList.get(i).getImgUri());
                                        new Thread(new Runnable() {
                                            public void run() {
                                                uploadFile(photoPath);
                                            }
                                        }).start();
                                        String test5 =photoPath;
                                        int test6 = test5.split("/").length-1;
                                        ipostImg = photoPath.split("/")[test6];

                                        fileName = "post_img_insert.php";
                                        param = "auctionId="+uuid+"&imgPath="+ipostImg;
                                        PhpConnect phpConnect6 = new PhpConnect();
                                        phpConnect6.execute(fileName,param).get();
                                    }

                                    Intent intent = new Intent(getApplicationContext(),Post.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(getApplicationContext(), "글 등록 완료", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getApplicationContext(), "재등록 바랍니다."+result2, Toast.LENGTH_LONG).show();
                                    return;

                                }


                            }catch (Exception e){

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "바로 구매 가격은 시작 가격보다 커야 합니다.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "천원보다 작을수 없습니다.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"빈칸을 채워주세요",Toast.LENGTH_LONG).show();
                    return;
                }

            }
        });


        //툴바 생성
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        //셀렉트박스
        spinner = findViewById(R.id.term);
        arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.term, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        nowBuyPrice = findViewById(R.id.nowBuyPrice);
        startPrice = findViewById(R.id.startPrice);

        //가격에 쉼표 추가
        nowBuyPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(!s.toString().equals(result)){     // StackOverflow를 막기위해,
                        result = df.format(Long.parseLong(s.toString().replaceAll(",", "")));   // 에딧텍스트의 값을 변환하여, result에 저장.
                        nowBuyPrice.setText(result);    // 결과 텍스트 셋팅.
                        nowBuyPrice.setSelection(result.length());     // 커서를 제일 끝으로 보냄.
                    }
                }catch (Exception e){

                }

            }

        });
        //가격에 쉼표 추가
        startPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(!s.toString().equals(result1)){     // StackOverflow를 막기위해,
                        result1 = df.format(Long.parseLong(s.toString().replaceAll(",", "")));   // 에딧텍스트의 값을 변환하여, result에 저장.
                        startPrice.setText(result1);    // 결과 텍스트 셋팅.
                        startPrice.setSelection(result1.length());     // 커서를 제일 끝으로 보냄.
                    }
                }catch (Exception e){

                }

            }
        });

        //화면 수정일 경우
        Intent gIntent = getIntent();
        final String postId = gIntent.getStringExtra("postId");
        if(postId!=null){
            saveBtn.setVisibility(View.GONE);
            updateBtn.setVisibility(View.VISIBLE);
            PhpConnect phpConnect = new PhpConnect();
            String result;
            try{
                String fileName = "post_detail.php";
                String param = "postId="+postId;
                result = phpConnect.execute(fileName,param).get();
                JSONArray jarray = new JSONArray(result);
                for(int i=0; i < jarray.length(); i++){
                    JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                    pdTitle.setText(jObject.getString("auctionTitle"));
                    pdContents.setText(jObject.getString("auctionContents"));

                    startPrice.setText(jObject.getString("startPrice"));
                    //수정불가
                    startPrice.setFocusable(false);
                    startPrice.setClickable(false);
                    startPrice.setVisibility(View.GONE);
                    nowBuyPrice.setText(jObject.getString("nowBuy"));
                    //수정불가
                    nowBuyPrice.setFocusable(false);
                    nowBuyPrice.setClickable(false);
                    nowBuyPrice.setVisibility(View.GONE);

                    constraintLayout.setVisibility(View.GONE);
                    linearLayout5.setVisibility(View.GONE);
                }
            }catch (Exception e){

            }

            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ipostTitle = pdTitle.getText().toString();
                    ipostContents = pdContents.getText().toString();
                    if(!ipostTitle.equals("")||!ipostContents.equals("")){
                        PhpConnect phpConnect3 = new PhpConnect();
                        fileName = "post_update.php";
                        param = "postId="+postId+"&pdTitle="+ipostTitle+"&pdContents="+ipostContents;
                        try{
                            String result = phpConnect3.execute(fileName,param).get();
                            System.out.println(result);
                            System.out.println("6666666666666");
                            if(result.equals("success")){
                                Intent intent = new Intent(getApplicationContext(),PostDetail.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("postId",postId);
                                startActivity(intent);
                            }else{
                                Toast.makeText(getApplicationContext(),"저장실패",Toast.LENGTH_LONG).show();
                            }
                        }catch (Exception e){

                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });
        }




    }



    //이미지 반환
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICTURE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {

                //ClipData 또는 Uri를 가져온다
                Uri uri = data.getData();
                ClipData clipData = data.getClipData();

                //이미지 URI 를 이용하여 이미지뷰에 순서대로 세팅한다.
                if(clipData!=null)
                {

                    for(int i = 0; i < clipData.getItemCount(); i++){
                        Uri urione =  clipData.getItemAt(i).getUri();
                        imgVO = new ImgVO(urione);
                        imgList.add(imgVO);

                    }
                    adapter = new Adapter(imgList);
                    recyclerView.setAdapter(adapter);

                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());

                }
                else if(uri != null){
                    imgVO = new ImgVO(uri);
                    imgList.add(imgVO);

                    adapter = new Adapter(imgList);
                    recyclerView.setAdapter(adapter);

                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());


                }
            }
        }
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
                System.out.println("실패?11");

                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                System.out.println("성공입니다.");
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
                System.out.println("성공을하자");

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });

            }
            return serverResponseCode;
        } // End else block


    }

}
