package chanboss.liveauction.streaming;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.notice.PostMerge;
import chanboss.liveauction.profile.UserVO;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StreamRoomFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StreamRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StreamRoomFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    SwipeRefreshLayout swipeRefresh;
    LiveAuctionVO liveAuctionVO;
    ArrayList<LiveAuctionVO> liveAuctionList;
    ArrayList<UserVO> userList;
    //리사이클러뷰
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    //어댑터 생성 요망
    LiveroomAdapter liveroomAdapter;

    //웹서버에 보낼 주소 및 파라미터
    String fileName;

    //스트리밍 방생성 버튼
    private FloatingActionButton fab;

    public StreamRoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment StreamRoomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StreamRoomFragment newInstance() {
        StreamRoomFragment fragment = new StreamRoomFragment();
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
        View view =inflater.inflate(R.layout.fragment_streamroom_list, container, false);
        //방생성 플로팅액션버튼 뷰연동
        fab = view.findViewById(R.id.fab);
        //이벤트
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),StreamRoomAdd.class);
                startActivity(intent);
            }
        });

        //리사이클러뷰 뷰연동
        //리사이클러뷰 생성
        recyclerView = view.findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(view.getContext());
        liveAuctionList = new ArrayList<>();
        //나머지 리사이클러뷰에 필요한 정보를 채우는 메소드!
        onListSelect();

        //당겨서 새로고침 뷰 연동
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                liveAuctionList.clear();
                onListSelect();
                swipeRefresh.setRefreshing(false);
            }
        });




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
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
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

    public void onListSelect(){
        //웹서버 클래스 접속
        PhpConnect phpConnect = new PhpConnect();
        //php 파일명 및 파라미터
        fileName = "liveroom_list.php";
        //받아온 결과값 result에 담기
        try {
            String result = phpConnect.execute(fileName,"").get();
            //result값은 json형태 이므로 LiveAuctionVO에 담을 수 있게 파싱
            JSONArray jarray = new JSONArray(result);
            for(int i=0; i < jarray.length(); i++){
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                //파싱해서 vo에 담기
                liveAuctionVO
                        = new LiveAuctionVO
                        (jObject.getString("liveId"),jObject.getString("liveName"),jObject.getString("userId"),
                                Integer.parseInt(jObject.getString("startPrice")),Integer.parseInt(jObject.getString("nowPrice")),jObject.getString("nowPriceUser"),
                                jObject.getString("userName"),jObject.getString("userImg"),jObject.getString("pdImg"),jObject.getString("liveStat"));
                //파싱한후 ArrayList<LiveAuctionVO>에 담기
                liveAuctionList.add(liveAuctionVO);
            }
        }catch (Exception e){

        }
        //어레이리스트를 어댑터에 주자!
        liveroomAdapter = new LiveroomAdapter(liveAuctionList);
        recyclerView.setAdapter(liveroomAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }
}
