package com.example.canbefluent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.canbefluent.adapter.userListAdapter;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag_community extends Fragment {
    private static final String TAG = "frag_community";
    private View view;
    TextView btn_all, btn_nearMe;
    LinearLayout all_view, nearMe_view;
    RecyclerView recycler_allUser, recycler_nearUser;
    userListAdapter allUser_adapter, nearUser_adapter;

    ArrayList<user_item> all_user_list = new ArrayList<>(); //모든 유저의 데이터를 담는 ArrayList
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        final View view =  inflater.inflate(R.layout.fragment_community, container, false);

        /**
         * 서버로부터 모든 유저의 정보를 불러온다.
         */
        RetrofitClient retrofitClient = new RetrofitClient();
        Call<ArrayList<user_item>> call = retrofitClient.service.get_allUserInfo();
        call.enqueue(new Callback<ArrayList<com.example.canbefluent.items.user_item>>() {
            @Override
            public void onResponse(Call<ArrayList<com.example.canbefluent.items.user_item>> call, Response<ArrayList<com.example.canbefluent.items.user_item>> response) {
                ArrayList<user_item> result = response.body();

                // 서버로부터 받아온 유저 리스트를 all_user_list에 담아준다.
                // 리사이클러뷰에 all_user_list를 보여준다.
                assert result != null;
                all_user_list.addAll(result);
                Log.e(TAG, "user list size: " + all_user_list.size());

                for (int i=0; i<all_user_list.size(); i++){
                    Log.e(TAG, "img: " + all_user_list.get(i).getProfile_img());

                }
                //recycler
                recycler_allUser = view.findViewById(R.id.recycler_allUser);

                recycler_allUser.setLayoutManager(new LinearLayoutManager(getActivity()));

                // define an adapter
                allUser_adapter = new userListAdapter(all_user_list);

                // 유저를 클릭하면 해당 유저의 정보를 객체에 담아 유저 프로필 액티비티로 이동시킨다.
                allUser_adapter.setOnItemClickListener(new userListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
//                        Log.e(TAG, "user id: " + all_user_list.get(position).getUser_id());

                        // 유저 리스트에서 선택한 유저의 정보를 담은 객체를 인텐트에 담는다.
                        user_item item = all_user_list.get(position);
                        Intent intent = new Intent(getActivity(), user_profile_activity.class);
                        intent.putExtra("user item", item);
                        startActivity(intent);
                    }
                });
                recycler_allUser.setAdapter(allUser_adapter);
            }

            @Override
            public void onFailure(Call<ArrayList<com.example.canbefluent.items.user_item>> call, Throwable t) {

            }
        });

        all_view = view.findViewById(R.id.all_view);
        nearMe_view = view.findViewById(R.id.nearMe_view);

        // 전체 유저 보기 버튼, 클릭 시 전체 유저에 대한 recyclerView가 나온다.
        btn_all = view.findViewById(R.id.btn_all);
        btn_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all_view.setVisibility(View.VISIBLE);
                nearMe_view.setVisibility(View.INVISIBLE);
            }
        });

        // 내 근처유저 보기 버튼, 클릭 시 내 근처유저에 대한 recyclerView가 나온다.
        btn_nearMe = view.findViewById(R.id.btn_nearMe);
        btn_nearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all_view.setVisibility(View.INVISIBLE);
                nearMe_view.setVisibility(View.VISIBLE);
            }
        });

        recycler_nearUser = view.findViewById(R.id.recycler_NearUser);





        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}
