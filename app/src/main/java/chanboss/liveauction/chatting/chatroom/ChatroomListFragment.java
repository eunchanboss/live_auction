package chanboss.liveauction.chatting.chatroom;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import chanboss.liveauction.R;
import chanboss.liveauction.chatting.chatin.ChatInVO;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.database.SessionManager;
import chanboss.liveauction.notice.PostAdapter;
import chanboss.liveauction.notice.PostVO;
import chanboss.liveauction.profile.UserVO;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatroomListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatroomListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatroomListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ChatroomVO chatroomVO;
    ArrayList<ChatroomVO> chatroomList;
    ArrayList<UserVO> userList;


    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    //어댑터 생성 요망
    ChatroomAdapter chatroomAdapter;

    //세션 생성
    SessionManager sessionManager;

    //상대방 id
    String reciverId;

    //상대방 유저 정보
    UserVO userVO;



    private OnFragmentInteractionListener mListener;

    public ChatroomListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @paaram2 Parameter 2.
     * @return A new instance of fragment ChatroomListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatroomListFragment newInstance() {
        ChatroomListFragment fragment = new ChatroomListFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chatroom_list, container, false);

        //리사이클러뷰 뷰연동
        //리사이클러뷰 생성
        recyclerView = view.findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(view.getContext());
        chatroomList = new ArrayList<>();
        chatroomList.clear();
        onListSelect();

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    //리스트 출력 및 검색 메소드
    public void onListSelect(){
        chatroomList.clear();
        PhpConnect phpConnect = new PhpConnect();

        sessionManager = new SessionManager(getContext());

        String result;
        String result2;

        try {
            result = phpConnect.execute("chat_list_user.php","userId="+sessionManager.getUserId()).get();
            JSONArray jarray = new JSONArray(result);
            for(int i=0; i < jarray.length(); i++){
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                String chatroomId= jObject.getString("chatroomId");
                String user1 = jObject.getString("user1");
                String user2 = jObject.getString("user2");
                if(user1.equals(sessionManager.getUserId())){
                    reciverId = user2;
                }else{
                    reciverId = user1;
                }
                System.out.println(chatroomId);
                System.out.println("11111111111111111111");

                PhpConnect phpConnect2 = new PhpConnect();
                result2 = phpConnect2.execute("chatroom_list_select.php","chatroomId="+chatroomId).get();
                JSONArray jarray2 = new JSONArray(result2);
                for(int j=0; j < jarray2.length(); j++) {
                    JSONObject jObject2 = jarray2.getJSONObject(j);  // JSONObject 추출
                    chatroomId = jObject2.getString("chatroomId");
                    String lastMsg = jObject2.getString("lastMsg");
                    String lastMsgDate = jObject2.getString("lastDate");
                    chatroomVO = new ChatroomVO(chatroomId,lastMsg,lastMsgDate,reciverId);
                    chatroomList.add(chatroomVO);

                }



            }


            chatroomAdapter = new ChatroomAdapter(chatroomList);
            recyclerView.setAdapter(chatroomAdapter);

        }
        catch (Exception e){
            System.out.println(e);
        }
    }

}
