package chanboss.liveauction.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;

import chanboss.liveauction.R;
import chanboss.liveauction.chatting.chatin.ChatInAdapter;
import chanboss.liveauction.chatting.chatin.ChatInList;
import chanboss.liveauction.chatting.chatin.ChatInVO;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.profile.UserVO;

public class ChatService extends Service {

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


    //알림 설정
    NotificationManager notificationManager;
    PendingIntent pendingIntent;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /*
    서비스에서 제일 먼저 생성됨
    */
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("서비스 시작");
        //서비스 실행시 tcp 연결
        //네티 서버에 클라이언트를 접속 시킨다.
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));

                } catch (Exception ioe) {
                    ioe.printStackTrace();

                }
                checkUpdate.start();
            }
        }).start();
    }

    /*
    서비스 호출시 사용
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    /*
    서비스 종료시 실행
    */

    @Override
    public void onDestroy() {
        System.out.println("서비스 종료");
        super.onDestroy();
    }

    //서버에서 데이터를 받아온다.
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

    private Runnable showUpdate = new Runnable() {
        public void run() {
            //여기서 data 변수는 서버측에서 보내는 메세지이다.
            String receive = data;
            String[] resultMsg = receive.split("//");


            createNotification(resultMsg[1],resultMsg[3],resultMsg[2]);
        }

    };


    private void createNotification(String chatroomId,String senderId,String msg) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("발신자 : "+senderId);
        builder.setContentText("내용 : "+msg);

        builder.setColor(Color.RED);
        // 사용자가 탭을 클릭하면 자동 제거
        Intent intent= new Intent(getApplicationContext(), ChatInList.class);
        intent.putExtra("userId", senderId);
        intent.putExtra("chatroomId",chatroomId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);


        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // id값은
        // 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(1, builder.build());
    }
}
