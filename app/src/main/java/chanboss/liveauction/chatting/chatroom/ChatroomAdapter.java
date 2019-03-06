package chanboss.liveauction.chatting.chatroom;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import chanboss.liveauction.R;
import chanboss.liveauction.chatting.chatin.ChatInList;
import chanboss.liveauction.chatting.chatin.ChatInRecylerHolder;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.notice.PostDetail;
import chanboss.liveauction.profile.UserVO;


public class ChatroomAdapter extends RecyclerView.Adapter<ChatroomRecylerHolder> {
    Context context;
    private ArrayList<ChatroomVO> chatroomList;
    private ArrayList<UserVO> userList;
    UserVO userVO;

    public ChatroomAdapter(ArrayList<ChatroomVO> itemList) {
        this.chatroomList = itemList;

    }

    @NonNull
    @Override
    public ChatroomRecylerHolder onCreateViewHolder(@NonNull ViewGroup parant, int i) {
        View view = LayoutInflater.from(parant.getContext()).inflate(R.layout.fragment_chatroom_item_list,parant,false);
        //뷰그룹에 접근
        context = parant.getContext();

        ChatroomRecylerHolder holder = new ChatroomRecylerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatroomRecylerHolder chatroomRecylerHolder, final int position) {
        chatroomRecylerHolder.lastMsgTV.setText(chatroomList.get(position).getLastMsg());
        chatroomRecylerHolder.lastMsgDateTV.setText(chatroomList.get(position).getLastMsgDate());
        chatroomRecylerHolder.lastMsgTV.setText(chatroomList.get(position).lastMsg);

        PhpConnect phpConnect2 = new PhpConnect();
        try{
            String result2 = phpConnect2.execute("profile_select.php","userId="+chatroomList.get(position).reciverId).get();
            JSONArray jarray2 = new JSONArray(result2);
            for(int j=0; j < jarray2.length(); j++) {
                JSONObject jObject2 = jarray2.getJSONObject(j);  // JSONObject 추출
                String userName = jObject2.getString("userName");
                String userImg = jObject2.getString("userImg");

                chatroomRecylerHolder.userNameTV.setText(userName);
                Glide
                        .with(context)
                        .load("http://ec2-13-209-50-167.ap-northeast-2.compute.amazonaws.com/user_img/"+userImg)
                        .apply(new RequestOptions().circleCrop())
                        .into(chatroomRecylerHolder.userImgIV);
            }
        }catch (Exception e){
            System.out.println(e);
        }

        chatroomRecylerHolder.chatroomEnterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ChatInList.class);
                intent.putExtra("chatroomId",chatroomList.get(position).chatroomId);
                intent.putExtra("userId",chatroomList.get(position).reciverId);
                context.startActivity(intent);
            }
        });

        //수정 필요
        //chatroomRecylerHolder.userNameTV.setText(userList.get(position).getUserName());
        //사진도 넣어요~

    }

    @Override
    public int getItemCount() {
        return chatroomList.size();
    }
}
