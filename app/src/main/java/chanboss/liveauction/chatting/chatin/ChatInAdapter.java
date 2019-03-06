package chanboss.liveauction.chatting.chatin;

import android.content.Context;
import android.content.SharedPreferences;
import android.se.omapi.Session;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.database.SessionManager;
import chanboss.liveauction.profile.UserVO;

public class ChatInAdapter extends RecyclerView.Adapter<ChatInRecylerHolder> {

    Context context;
    //메인뷰로부터 정보를 받아올 리스트 생성
    private ArrayList<ChatInVO> chatInList;
    //받아온 정보로 부터 상대방 이름,프로필을 받아올 vo생성
    UserVO userVO;
    private ArrayList<UserVO> userList;
    //유저정보를 받기위한 웹서버 연동 객체 생성
    PhpConnect phpConnect;

    //받아온 리스트를 받아올 생성자

    //세션 받아오기
    SessionManager sessionManager;


    public ChatInAdapter(ArrayList itemList,ArrayList userList) {
        this.chatInList = itemList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ChatInRecylerHolder onCreateViewHolder(@NonNull ViewGroup parant, int i) {
        View view = LayoutInflater.from(parant.getContext()).inflate(R.layout.fragment_chatin_item_list,parant,false);
        //뷰그룹에 접근
        context = parant.getContext();

        ChatInRecylerHolder holder = new ChatInRecylerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatInRecylerHolder chatInRecylerHolder, int position) {

        //받아온 리스트를 뿌려줄때 메세지의 송신자가 내가 아니라면 왼쪽, 송신자가 나라면 오른쪽 레이아웃을 보이게 해준다.
        //비교는 세션과 해준다.
        sessionManager = new SessionManager(context);
        if(!chatInList.equals(null)){
            if(chatInList.get(position).getSenderId().equals(sessionManager.getUserId())){
                chatInRecylerHolder.senderLayout.setVisibility(View.VISIBLE);
                chatInRecylerHolder.reciverLayout.setVisibility(View.GONE);
                if(chatInList.get(position).imgMsg.equals("Y")){
                    //msg가 path값일땐 사진 출력
                    chatInRecylerHolder.senderImgMsgIV.setVisibility(View.VISIBLE);
                    Glide
                            .with(context)
                            .load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/chat_img/"+chatInList.get(position).getMsg())
                            .into(chatInRecylerHolder.senderImgMsgIV);

                    chatInRecylerHolder.senderMsgTV.setVisibility(View.GONE);
                    chatInRecylerHolder.senderDateTV.setVisibility(View.GONE);
                }else if(chatInList.get(position).imgMsg.equals("N")){
                    chatInRecylerHolder.senderImgMsgIV.setVisibility(View.GONE);
                    chatInRecylerHolder.senderMsgTV.setText(chatInList.get(position).getMsg());
                    chatInRecylerHolder.senderDateTV.setText(chatInList.get(position).getMsgDate());
                }
            }else{
                chatInRecylerHolder.reciverLayout.setVisibility(View.VISIBLE);
                chatInRecylerHolder.senderLayout.setVisibility(View.GONE);
                chatInRecylerHolder.reciverNmTV.setText(userList.get(0).getUserName());
                Glide
                        .with(context)
                        .load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/user_img/"+userList.get(0).getUserImg())
                        .apply(new RequestOptions().circleCrop())
                        .into(chatInRecylerHolder.reciverImgIV);
                if(chatInList.get(position).imgMsg.equals("Y")){
                    chatInRecylerHolder.reciverImgMsgIV.setVisibility(View.VISIBLE);
                    Glide
                            .with(context)
                            .load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/chat_img/"+chatInList.get(position).getMsg())
                            .into(chatInRecylerHolder.reciverImgMsgIV);

                    chatInRecylerHolder.reciverMsgTV.setVisibility(View.GONE);
                    chatInRecylerHolder.reciverDateTV.setVisibility(View.GONE);
                }else if(chatInList.get(position).imgMsg.equals("N")){
                    chatInRecylerHolder.reciverImgMsgIV.setVisibility(View.GONE);
                    chatInRecylerHolder.reciverMsgTV.setText(chatInList.get(position).getMsg());
                    chatInRecylerHolder.reciverDateTV.setText(chatInList.get(position).getMsgDate());
                }


            }
        }
    }

    @Override
    public int getItemCount() {
        return chatInList.size();
    }
}
