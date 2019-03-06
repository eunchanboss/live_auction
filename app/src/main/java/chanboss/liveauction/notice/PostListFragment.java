package chanboss.liveauction.notice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnect;
import chanboss.liveauction.main.Post;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//스와이프 리프레쉬 implements 선언
public class PostListFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FloatingActionButton fab;
    PostVO postVO;
    ArrayList<PostVO> postList;

    SwipeRefreshLayout swipeRefresh;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private PostAdapter postAdapter;

    private EditText searchBar;
    private Button searchBtn;



    private OnFragmentInteractionListener mListener;

    public PostListFragment() {
        // Required empty public constructor
    }




    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PostListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostListFragment newInstance() {
        PostListFragment fragment = new PostListFragment();
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
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        //검색 뷰연동
        searchBar = view.findViewById(R.id.searchBar);
        searchBtn = view.findViewById(R.id.searchBtn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchBar.getText().toString()!=""){
                    onListSelect("","searchWord="+searchBar.getText().toString());
                }else{

                }
            }
        });

        //리사이클러뷰 뷰연동
        //리사이클러뷰 생성
        recyclerView = view.findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(view.getContext());
        postList = new ArrayList<>();
        postList.clear();

        //당겨서 새로고침 뷰 연동
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                searchBar.setText("");
                onListSelect("","");

                swipeRefresh.setRefreshing(false);


            }
        });

        //플로팅 액션 버튼(글등록 버튼)
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),PostMerge.class);
                startActivity(intent);
            }
        });
        if(searchBar.getText().toString()!=""){
            onListSelect("","searchWord="+searchBar.getText().toString());
        }else{
            onListSelect("","");

        }
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //이미지 리사이클러뷰 생성

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

    //리스트 출력 및 검색 메소드
    public void onListSelect(String fileName, String param){
        postList.clear();
        PhpConnect phpConnect = new PhpConnect();
        String result;
        try {
            result = phpConnect.execute("post_select.php",param).get();
            JSONArray jarray = new JSONArray(result);
            for(int i=0; i < jarray.length(); i++){
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                System.out.println(jObject.getString("userId"));
                String auctionTitle= jObject.getString("auctionTitle");
                String userId = jObject.getString("userId");
                String userName=null;
                String userImg=null;
                String auctionContents = jObject.getString("auctionContents");
                String auctionContentsId = jObject.getString("auctionContentsId");
                String auctionContentsImg = jObject.getString("auctionContentsImg");
                String lastTime = jObject.getString("lastTime");

                String startPrice = jObject.getString("startPrice");
                String nowBuy = jObject.getString("nowBuy");
                String pdImg = jObject.getString("pdImg");
                String lastPrice = jObject.getString("lastPrice");
                postVO = new PostVO(pdImg,"",auctionTitle,userId,userName,userImg,auctionContents,auctionContentsId,auctionContentsImg,lastTime,startPrice,nowBuy,lastPrice);
                postList.add(postVO);
            }
            postAdapter = new PostAdapter(postList);
            recyclerView.setAdapter(postAdapter);

        }
        catch (Exception e){
            System.out.println(e);
        }
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


}
