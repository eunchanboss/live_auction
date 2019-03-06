package chanboss.liveauction.notice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;

import chanboss.liveauction.R;
import chanboss.liveauction.chatting.chatin.ChatInList;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.database.SessionManager;
import chanboss.liveauction.notice.detail.DetailAdapter;
import chanboss.liveauction.notice.img.ImgVO;

public class PostDetail extends AppCompatActivity {

    //판매 상품 관련 뷰
    TextView lastPriceTV;   //경매 진행 금액
    TextView timerTV;       //경매 남은시간 타이머
    TextView lastPriceUserTV; //현재까지 마지막 입찰자
    TextView pdTitleTV;     //경매 상품 제목
    TextView pdContentsTV;  //경매 상품 상세 설명
    TextView pdNowBuyTV;    //상품 바로 구입 금액
    Button nowBuyBtn;       //상품 바로 구입 버튼
    EditText priceInputET;  //입찰 금액 입력
    Button bidBtn;          //입찰 버튼
    LinearLayout bidLayout; //입찰 기능 레이아웃 자체
    Button buyMsgBtn;
    Button cellMsgBtn;

    //유저 정보 관련 뷰
    ImageView userImgIV;     //유저 이미지
    TextView userNameTV;    //유저 이름
    TextView userGradeTV;   //유저 등급
    Button followBtn;       //해당 유저 팔로우

    //정보 조회시 받아올 데이터 관련 변수
    String userId;              //유저 id
    String userName;            //유저 이름
    String userImg;             //유저 사진
    String userGrade;           //유저 등급
    String pdImg;               //상품 사진
    String auctionContentsId;   //상품 id
    String auctionTitle;        //상품 명
    String auctionContents;     //상품 설명
    String startPrice;          //시작 가격
    String nowPrice;            //입찰 중인 현재가격
    String nowBuy;              //즉시 구입가격
    String lastPrice;           //입찰중인 가격
    String nowPriceUser;        //마지막 입찰자
    String auctionStat;         //상품 종료 여부
    String lastTime;            //종료 일자
    String sessionId;           //현재 내 아이디

    // 세자리로 끊어서 쉼표 보여주고, 소숫점 셋째짜리까지 보여준다.
    DecimalFormat df = new DecimalFormat("###,###.####");
    String restDot;


    //리사이클러뷰
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    //출력될 리사이클러뷰에 들어갈 이미지
    ArrayList<ImgVO> imgList;
    ImgVO imgVO;
    DetailAdapter detailAdapter;

    //타이머
    int i;
    int h, m, s;
    String ss,ms,hs;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_detail);


        //뷰연동
        lastPriceTV = findViewById(R.id.lastPriceTV);   //경매 진행 금액
        timerTV= findViewById(R.id.timerTV);       //경매 남은시간 타이머
        lastPriceUserTV = findViewById(R.id.lastPriceUserTV);
        pdTitleTV= findViewById(R.id.pdTitleTV);     //경매 상품 제목
        pdContentsTV= findViewById(R.id.pdContentsTV);  //경매 상품 상세 설명
        pdNowBuyTV= findViewById(R.id.pdNowBuyTV);    //상품 바로 구입 금액
        nowBuyBtn= findViewById(R.id.nowBuyBtn);       //상품 바로 구입 버튼
        priceInputET= findViewById(R.id.priceInputET);  //입찰 금액 입력
        bidBtn= findViewById(R.id.bidBtn);          //입찰 버튼
        bidLayout = findViewById(R.id.bidLayout);
        buyMsgBtn = findViewById(R.id.buyMsgBtn);
        cellMsgBtn = findViewById(R.id.cellMsgBtn);

        //유저 정보 관련 뷰
        userImgIV = findViewById(R.id.userImgIV);     //유저 이미지
        userNameTV= findViewById(R.id.userNameTV);    //유저 이름
        userGradeTV= findViewById(R.id.userGradeTV);   //유저 등급
        followBtn= findViewById(R.id.followBtn);       //해당 유저 팔로우

        //해당상품 아이디 추출
        Intent gIntent = getIntent();
        //이건 포스트 아이디를 빼오는거다.
        final String postId = gIntent.getStringExtra("postId");
        //현재 내아이디 추출
        SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
        sessionId = pref.getString("sessionId", "");

        //TEST
        //서버에서 해당 상품 조회
        PhpConnect phpConnect = new PhpConnect();
        String result;
        try {
            String fileName = "post_detail.php";
            String param = "postId=" + postId;
            result = phpConnect.execute(fileName, param).get();
            JSONArray jarray = new JSONArray(result);
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                auctionTitle = jObject.getString("auctionTitle");
                userId = jObject.getString("userId");
                userName = null;
                userImg = null;
                auctionContents = jObject.getString("auctionContents");
                auctionContentsId = jObject.getString("auctionContentsId");
                lastTime = jObject.getString("lastTime");
                startPrice = jObject.getString("startPrice");
                nowBuy = jObject.getString("nowBuy");
                pdImg = jObject.getString("pdImg");
                nowPrice = jObject.getString("nowPrice");
                nowPriceUser = jObject.getString("nowPriceUser");
                auctionStat = jObject.getString("auctionStat");
                lastPrice = jObject.getString("lastPrice");

                if(userId.equals(sessionId)){
                    priceInputET.setVisibility(View.GONE);
                    bidBtn.setVisibility(View.GONE);
                }
            }
            //세션 user와 글쓴 user가 같을때 수정버튼 보이게(구현 할 것)
            /*
            구현해라~
            */

            //userId로 유저 정보 조회
            PhpConnect userSelect = new PhpConnect();
            String userSelectResult;
            String fileName1 = "profile_select.php";
            String param1 = "userId="+userId;

            try {
                userSelectResult = userSelect.execute(fileName1,param1).get();
                JSONArray userJarray = new JSONArray(userSelectResult);
                for (int i = 0; i < userJarray.length(); i++) {
                    JSONObject jObject = userJarray.getJSONObject(i);  // JSONObject 추출
                    userName = jObject.getString("userName");
                    userImg = jObject.getString("userImg");
                    userGrade = jObject.getString("userGrade");
                    int grade = Integer.parseInt(userGrade);

                    Glide.with(getApplicationContext()).load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/user_img/"+userImg).apply(new RequestOptions().circleCrop()).into(userImgIV);
                    userNameTV.setText(userName);
                    if(grade>50){
                        userGradeTV.setText("다이아");
                    }else if(grade>40) {
                        userGradeTV.setText("플레티넘");
                    }else if(grade>30){
                        userGradeTV.setText("골드");
                    }else if(grade>20){
                        userGradeTV.setText("실버");
                    }else{
                        userGradeTV.setText("브론즈");
                    }
                }
            }catch (Exception e){

            }
            //마지막 입찰자가 없을때는 마지막 입찰자 뷰를 비워준다.
            if(nowPriceUser.equals("")||nowPriceUser.equals(null)){
                lastPriceUserTV.setText("");
                //경매 진행 가격에 대해서는 시작가격을 넣어준다.
                startPrice = String.format("%,d", intChanger(startPrice));
                lastPriceTV.setText(startPrice);
            }else{
                lastPriceUserTV.setText(nowPriceUser);
                lastPrice = String.format("%,d", intChanger(lastPrice));
                lastPriceTV.setText(lastPrice);
            }
            //제목 내용 바로구입금액
            pdTitleTV.setText(auctionTitle);
            pdContentsTV.setText(auctionContents);
            nowBuy = String.format("%,d", intChanger(nowBuy));
            pdNowBuyTV.setText(nowBuy);
            //조회된 아이디가 세션 id와 겹칠 경우 입찰 버튼 제거

            //입찰가가 바로구매가를 넘겼을때 바로구매 버튼 제거
            if(intChanger(pdNowBuyTV.getText().toString().replaceAll(",",""))<intChanger(lastPriceTV.getText().toString().replaceAll(",",""))){
                nowBuyBtn.setVisibility(View.GONE);

            }
            //남은시간에 대한 타이머 구현
            int lastTimeInt = Integer.parseInt(lastTime);
            i = lastTimeInt;
            //남은 시간이 존재할 경우
            if(i>0) {
                new CountDownTimer(3600 * 60 * 12, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) { // 총 시간과 주기
                        i--;
                        if (i > 0) {
                            s = i % 3600 % 60;
                            m = i % 3600 / 60;
                            h = i / 3600;
                            ss = String.valueOf(s);
                            ms = String.valueOf(m);
                            hs = String.valueOf(h);
                            timerTV.setText(hs + "시간" + ms + "분" + ss + "초");
                        } else {

                        }
                    }

                    @Override
                    public void onFinish() {

                    }
                }.start();  // 타이머 시작
            }else{//경매가 종료된 시점
                //시간 종료
                timerTV.setText("종료");
                //바로구입 버튼 없애기
                nowBuyBtn.setVisibility(View.GONE);
                //입찰기능 없애기
                bidBtn.setVisibility(View.GONE);
                priceInputET.setVisibility(View.GONE);
                //구매자,판매자 간의 채팅 버튼 구현 하세요~
                //파라미터 정리 userId-> 판매자 sessionId -> 접속한 클라이언트 사용자 nowPriceUser -> 마지막 입찰자
                if(nowPriceUser.equals(sessionId)&&!userId.equals(sessionId)){
                    buyMsgBtn.setVisibility(View.VISIBLE);
                }else if(userId.equals(sessionId)){
                    cellMsgBtn.setVisibility(View.VISIBLE);
                }
            }
        }catch (Exception e){

        }

        //이미지 리사이클러뷰 생성
        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        imgList = new ArrayList<>();
        imgList.clear();

        PhpConnect phpConnect1 = new PhpConnect();
        String result1;
        try{
            String fileName1 = "post_img_select.php";
            String param1 = "postId="+postId;
            result1 = phpConnect1.execute(fileName1,param1).get();
            System.out.println(result1);
            JSONArray jarray = new JSONArray(result1);
            for(int i=0; i < jarray.length(); i++){
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                String postImg = jObject.getString("postImg");
                Uri uri = Uri.parse(postImg);
                imgVO = new ImgVO(uri);
                imgList.add(imgVO);
            }


            detailAdapter = new DetailAdapter(imgList);
            recyclerView.setAdapter(detailAdapter);

            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

        }catch (Exception e){

        }
        //가격에 쉼표 추가
        priceInputET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(!s.toString().equals(restDot)){     // StackOverflow를 막기위해,
                        restDot = df.format(Long.parseLong(s.toString().replaceAll(",", "")));   // 에딧텍스트의 값을 변환하여, result에 저장.
                        priceInputET.setText(restDot);    // 결과 텍스트 셋팅.
                        priceInputET.setSelection(restDot.length());     // 커서를 제일 끝으로 보냄.
                    }
                }catch (Exception e){

                }

            }
        });

        //입찰 버튼 이벤트(네티서버에 값을 던져줌)
        bidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //입력 금액이 없을때
                    if(priceInputET.getText().toString().equals("")){
                        Toast.makeText(getApplicationContext(),"금액을 입력하세요.",Toast.LENGTH_LONG).show();
                    }else{
                        if(intChanger(priceInputET.getText().toString().replaceAll(",",""))%100!=0){
                            Toast.makeText(getApplicationContext(),"100원 단위로 입력해주세요.",Toast.LENGTH_LONG).show();
                            return;
                        }else{

                        }
                        //서버로 전송하여 업데이트 하기
                        //먼저 시작가격에 추가된 값을 더한다.
                        String return_price = stringChanger(intChanger(lastPriceTV.getText().toString().replaceAll(",",""))+intChanger(priceInputET.getText().toString().replaceAll(",","")));

                        String return_msg = "post"+"//"+postId+"//"+return_price+"//"+sessionId+"//"+auctionStat;
                        try{
                            new SendPriceTask().execute(return_msg);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        //채팅버튼
        buyMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ChatInList.class);
                intent.putExtra("userId",userId);
                //채팅방 유무를 조회해서 없으면 만들어서 id전송

                PhpConnect phpConnect2 = new PhpConnect();
                String chatRoomIdResult;
                SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
                final String sessionId = pref.getString("sessionId", "");
                String fileName1 = "chat_room_insert.php";
                String param1 = "userId="+userId+"&userId2="+sessionId;

                try {
                    chatRoomIdResult = phpConnect2.execute(fileName1, param1).get();
                    intent.putExtra("chatroomId",chatRoomIdResult);
                    startActivity(intent);
                    finish();
                }catch (Exception e){

                }

            }
        });

        cellMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ChatInList.class);
                intent.putExtra("userId",lastPriceUserTV.getText().toString());
                //채팅방 유무를 조회해서 없으면 만들어서 id전송

                PhpConnect phpConnect2 = new PhpConnect();
                String chatRoomIdResult;
                SharedPreferences pref = getSharedPreferences("sessionId", MODE_PRIVATE);
                final String sessionId = pref.getString("sessionId", "");
                String fileName1 = "chat_room_insert.php";
                String param1 = "userId="+lastPriceUserTV.getText().toString()+"&userId2="+sessionId;


                try {
                    chatRoomIdResult = phpConnect2.execute(fileName1, param1).get();
                    intent.putExtra("chatroomId",chatRoomIdResult);

                    startActivity(intent);
                    finish();
                }catch (Exception e){

                }
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
    }

    //다른 화면으로 넘어갈시 오류와 낭비를 막기위해 화면이 안보이게 되면 TCP 서버와의 연결을 해제한다.
    @Override
    protected void onStop() {
        super.onStop();
        try {
            socketChannel.close();

        } catch (IOException ee) {
            ee.printStackTrace();
        }
    }


    //2. 네티 서버 연동해서 결과 받기
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
        //당사자 클라이언트측
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String priceClientSetData = stringChanger(intChanger(lastPriceTV.getText().toString().replaceAll(",",""))+intChanger(priceInputET.getText().toString().replaceAll(",","")));
                    lastPriceTV.setText(String.format("%,d", intChanger(priceClientSetData)));
                    lastPriceTV.setFocusableInTouchMode(true);
                    lastPriceTV.requestFocus();
                    SessionManager sessionManager = new SessionManager(getApplicationContext());
                    lastPriceUserTV.setText(sessionManager.getUserId());
                    priceInputET.setText("");

                    //입찰가격이 바로구입가 금액 초과시
                    if(intChanger(pdNowBuyTV.getText().toString().replaceAll(",",""))<intChanger(priceClientSetData.replaceAll(",",""))){
                        nowBuyBtn.setVisibility(View.GONE);
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
    //누군가 입찰 진행시 다른 사용자 부분
    private Runnable showUpdate = new Runnable() {

        public void run() {
            //필요없는 부분 일듯..
            //String receive = "Coming word : " + data;
            String receive = data;
            System.out.println("receivetest:"+ receive);
            if(receive.split("//")[0].equals("post")){
                //서버에서 데이터 받아서 화면에 접속한 모든 사용자에게 데이터를 뿌려준다.
                lastPriceUserTV.setText(receive.split("//")[2]);
                lastPriceTV.setText(String.format("%,d", intChanger(receive.split("//")[1])));
                lastPriceTV.requestFocus();
                lastPriceTV.setFocusableInTouchMode(true);
                //입찰가격이 바로구입가 금액 초과시
                if (intChanger(pdNowBuyTV.getText().toString().replaceAll(",", "")) < intChanger(receive.split("//")[1])) {
                    nowBuyBtn.setVisibility(View.GONE);
                }

            }
        }

    };

    //스트링을 인트로 변환해주는 메소드
    public int intChanger(String priceInput){
        int return_int = Integer.parseInt(priceInput);
        return return_int;
    }

    //인트를 스트링으로 변환
    public String stringChanger(int priceInput){
        String return_string = String.valueOf(priceInput);
        return return_string;
    }


}
