package chanboss.liveauction.chatting.chatin;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.database.SessionManager;
import chanboss.liveauction.profile.UserVO;
import chanboss.liveauction.service.ChatService;
import videortc.CallActivity;
import videortc.ReceptionActivity;


public class ChatInList extends AppCompatActivity {

    //받아올 데이터 변수 생성

    //뷰생성
    EditText msgTxt;
    Button sendBtn;

    //다이얼로그 뷰- 사진전송 기능과 영상통화 메뉴 선택
    ImageView dialogBtn;

    //리사이클러뷰 관련 변수
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ChatInAdapter chatInAdapter;
    ChatInVO chatInVO;
    ArrayList<ChatInVO> chatInList;
    UserVO userVO;
    ArrayList<UserVO> userList;

    //채팅방내에서 유저정보 (이걸로 리스트를 조회하게된다.)
    //my id
    String userId;
    //상대방 id
    String targetId;
    //채팅방 id
    String chatroomId;

    //네티서버용 변수
    //네티 서버 사용시 기본적으로 필요한 변수 및 클래스 선언
    Handler handler;
    String data;
    SocketChannel socketChannel;
    //host, port
    //로컬
    //private static final String HOST = "192.168.137.1";
    //서버
    private static final String HOST = "ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com";
    private static final int PORT = 9000;

    static final int REQUEST_TAKE_PHOTO = 2;
    Uri photoUri = null;

    //파일 업로드
    int serverResponseCode = 0;
    String upLoadServerUri = null;
    String photoPath;
    //그 외 값
    Date today = new Date();

    //DB연동 대화내용 조회 관련 변수
    PhpConnect phpConnect;
    String chatResult;
    String fileName;
    String param;
    //webRTC 방 번호 변수
    String randomRoomID = "";
    //서비스 관련 변수
    private ChatService chatService;
    private boolean isBind;
    private static final String UPPER_ALPHA_DIGITS = "ACEFGHJKLMNPQRUVWXY123456789";
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
        setContentView(R.layout.fragment_chatin_list);
        //뷰연동
        msgTxt = findViewById(R.id.msgTxt);
        sendBtn = findViewById(R.id.sendBtn);
        dialogBtn = findViewById(R.id.dialogBtn);

        //이미지 전송
        upLoadServerUri = "http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/chat_img_upload.php";//서버컴퓨터의 ip주소

        //내 id와 상대방 id 가져오기
        //내 id
        userId = new SessionManager(ChatInList.this).getUserId();
        //상대방 id
        Intent gIntent = getIntent();
        targetId = gIntent.getStringExtra("userId");
        //채팅방 id
        chatroomId = gIntent.getStringExtra("chatroomId");



        //리사이클러뷰 뷰연동
        //리사이클러뷰 생성
        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatInList = new ArrayList<>();
        chatInList.clear();

        userList = new ArrayList<>();
        userList.clear();

        //데이터 조회해서 뿌릴 곳 (파라미터 chatroomId)
        phpConnect = new PhpConnect();
        fileName = "chat_list.php";
        param = "chatroomId="+chatroomId;
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        try {
            chatResult = phpConnect.execute(fileName,param).get();
            JSONArray jarray = new JSONArray(chatResult);
            for(int i=0; i < jarray.length(); i++){
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                //채팅정보 조회결과
                String msg = jObject.getString("msg");
                String msgDate = jObject.getString("msgDate");
                String msgId = jObject.getString("msgId");
                String senderId = jObject.getString("senderId");
                String reciverId = jObject.getString("reciverId");
                String imgMsg = jObject.getString("imgMsg");
                String checkYn = jObject.getString("checkYn");

                //채팅결과넣어서 만들기
                chatInVO = new ChatInVO(chatroomId,msg,senderId,reciverId,msgDate,imgMsg);
                chatInVO.setCheckYn(checkYn);
                chatInList.add(chatInVO);
                recyclerView.smoothScrollToPosition (jarray.length()-1);
                if(!sessionManager.getUserId().equals(senderId)){
                    PhpConnect phpConnect_userSelect = new PhpConnect();
                    fileName = "chat_user_select.php";
                    param = "userId="+senderId;
                    try {
                        String userResult = phpConnect_userSelect.execute(fileName, param).get();
                        JSONArray jarrayUser = new JSONArray(userResult);
                        for (int j = 0; j < jarray.length(); j++) {
                            JSONObject jObjectUser = jarrayUser.getJSONObject(j);  // JSONObject 추출
                            //채팅정보 조회결과
                            String userName = jObjectUser.getString("userName");
                            String userImg = jObjectUser.getString("userImg");
                            userVO = new UserVO();
                            userVO.setUserName(userName);
                            userVO.setUserImg(userImg);
                            userList.add(userVO);
                        }
                    }catch (Exception e){
                        System.out.println(e);
                    }
                }

            }
        }catch (Exception e){
            System.out.println(e);
        }

        //데이터 조회해서 뿌릴 곳@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        chatInAdapter=new ChatInAdapter(chatInList,userList);
        recyclerView.setAdapter(chatInAdapter);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //다이얼 로그 띄우기
        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });
        //1.네티 서버 소켓연결
        //체크 포인트1 . post_detail 화면에서 소켓 온
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));

                } catch (Exception ioe) {
                    Log.d("asd", ioe.getMessage() + "a");
                    ioe.printStackTrace();

                }
                checkUpdate.start();
            }
        }).start();
        //전송 버튼 이벤트
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(msgTxt.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"메세지를 입력해주세요.",Toast.LENGTH_LONG).show();
                    return;
                }
                //서버 전달용 변수 만들기(type + msg + senderId + reciverId + chatroomId)
                String msg = "chat"+"//"+msgTxt.getText().toString()+"//"+userId+"//"+targetId+"//"+chatroomId;
                new SendPriceTask().execute(msg);


            }
        });



    }

    @Override
    protected void onStop() {
        Intent intent = new Intent(getApplicationContext(),ChatService.class);
        startService(intent);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    //서버전송 클래스
    //개별 클래스 만들기
    private class SendPriceTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); // 서버로
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        //내 클라이언트
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //전송 버튼 결과 코딩
                        if(randomRoomID.equals("")){
                            SimpleDateFormat time = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
                            if(msgTxt.getText().toString().equals("")){
                                chatInVO = new ChatInVO(chatroomId,photoPath.split("/")[photoPath.split("/").length-1],userId,targetId,time.format(today),"Y");
                            }else{
                                chatInVO = new ChatInVO(chatroomId,msgTxt.getText().toString(),userId,targetId,time.format(today),"N");
                            }

                            chatInList.add(chatInVO);
                            chatInAdapter=new ChatInAdapter(chatInList,userList);
                            recyclerView.setAdapter(chatInAdapter);
                            recyclerView.scrollToPosition(chatInAdapter.getItemCount()-1);
                            //linearLayoutManager.setStackFromEnd(true);
                            recyclerView.setLayoutManager(linearLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());

                            msgTxt.setText("");
                        }else{
                            //rtc.no2
                            System.out.println("종료1");
                            finish();
                        }

                }
            });
        }
    }

    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); //데이터받기
                Log.d("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) {
                    throw new IOException();
                }

                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                data = charset.decode(byteBuffer).toString();
                Log.d("receive", "msg :" + data);
                handler.post(showUpdate);
            } catch (IOException e) {
                Log.d("getMsg", e.getMessage() + "");
                try {
                    socketChannel.close();
                    break;
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    //tcp 서버에서 상대방 디바이스로 보냈을 때
    private Runnable showUpdate = new Runnable() {

        public void run() {
            //여기서 data 변수는 서버측에서 보내는 메세지이다.
            String receive = data;
            String type = receive.split("//")[0];
            if(type.equals("videoChat")){
                if(receive.split("//")[2].equals(new SessionManager(getApplicationContext()).getUserId())){
                    Intent intent = new Intent(ChatInList.this, ReceptionActivity.class);
                    //senderId ->
                    intent.putExtra("senderId",receive.split("//")[1]);
                    intent.putExtra("reciverId",receive.split("//")[2]);
                    intent.putExtra("randomRoomId",receive.split("//")[3]);
                    intent.putExtra("chatroomId",chatroomId);
                    startActivity(intent);
                    finish();
                }
            }else if(type.equals("chat")||type.equals("chatImg")){
                chatInVO = new ChatInVO(chatroomId,receive.split("//")[2],receive.split("//")[3],receive.split("//")[4],receive.split("//")[5],receive.split("//")[6]);
                chatInList.add(chatInVO);

                phpConnect = new PhpConnect();
                fileName = "chat_user_select.php";
                //아래 파라미터는 보낸사람?
                param = "userId="+receive.split("//")[3];
                try {
                    String userResult = phpConnect.execute(fileName, param).get();
                    JSONArray jarray = new JSONArray(userResult);
                    String userName=null;
                    String userImg=null;
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                        //채팅정보 조회결과
                        userName = jObject.getString("userName");
                        userImg = jObject.getString("userImg");
                        userVO = new UserVO();
                        userVO.setUserName(userName);
                        userVO.setUserImg(userImg);
                        userList.add(userVO);
                    }
                }catch (Exception e){
                    System.out.println(e);
                }
                PhpConnect phpConnect2 = new PhpConnect();
                String chatRoomIdResult;
                String fileName1 = "chat_room_insert.php";
                String param1 = "userId="+receive.split("//")[3]+"&userId2="+receive.split("//")[4];

                try {
                    chatRoomIdResult = phpConnect2.execute(fileName1, param1).get();
                    if(chatRoomIdResult.equals(chatroomId)){
                        chatInAdapter=new ChatInAdapter(chatInList,userList);
                        recyclerView.setAdapter(chatInAdapter);
                        recyclerView.scrollToPosition(chatInAdapter.getItemCount()-1);

                        //linearLayoutManager.setStackFromEnd(true);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                    }
                }catch (Exception e){

                }
            }

        }

    };






    void show()
    {
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("사진전송");
        ListItems.add("영상통화");
        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("메뉴");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                String selectedText = items[pos].toString();
                if(selectedText.equals("사진전송")){
                    doTakeAlbumAction();
                }else if(selectedText.equals("영상통화")){
                    //rtc.no1
                    randomRoomID = randomString(7, UPPER_ALPHA_DIGITS);
                    String msg = "videoChat"+"//"+userId+"//"+targetId+"//"+randomRoomID;
                    new SendPriceTask().execute(msg);
                    Intent intent = new Intent(ChatInList.this, CallActivity.class);
                    intent.putExtra("randomRoomId",randomRoomID);
                    intent.putExtra("chatroomId",chatroomId);
                    startActivity(intent);

                }
            }
        });
        builder.show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                photoUri = data.getData();
                photoPath = getPath(photoUri);

                PhpConnect phpConnect = new PhpConnect();
                SharedPreferences pref1 = getSharedPreferences("sessionId", MODE_PRIVATE);
                String sessionId1 = pref1.getString("sessionId", "");
                try {

                    String[] array = photoPath.split("/");
                    String path = array[array.length - 1];



                    //서버 전달용 변수 만들기(type + msg + senderId + reciverId + chatroomId)
                    String msg = "chatImg"+"//"+path+"//"+userId+"//"+targetId+"//"+chatroomId;
                    new SendPriceTask().execute(msg);
                } catch (Exception e) {

                }
                //File file = new File(photoUri.getPath());

                new Thread(new Runnable() {
                    public void run() {
                        uploadFile(photoPath);
                    }
                }).start();
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
                conn.setRequestProperty("userId",new SessionManager(getApplicationContext()).getUserId());
                conn.setRequestProperty("chatroomId",chatroomId);
                conn.setRequestProperty("reciverId",targetId);


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
                System.out.println(e);
                runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });

            }
            return serverResponseCode;
        } // End else block
    }

    @Override
    public void onBackPressed() {
        //Intent intent = new Intent(getApplicationContext(),Main)
        super.onBackPressed();
    }

    private String randomString(int length, String characterSet) {
        StringBuilder sb = new StringBuilder(); //consider using StringBuffer if needed
        for (int i = 0; i < length; i++) {
            int randomInt = new SecureRandom().nextInt(characterSet.length());
            sb.append(characterSet.substring(randomInt, randomInt + 1));
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
