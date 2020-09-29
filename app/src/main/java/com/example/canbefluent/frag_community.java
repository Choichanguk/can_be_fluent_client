package com.example.canbefluent;

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

import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag_community extends Fragment {
    private View view;
    TextView btn_all, btn_nearMe;
    LinearLayout all_view, nearMe_view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_community, container, false);

        all_view = rootView.findViewById(R.id.all_view);
        nearMe_view = rootView.findViewById(R.id.nearMe_view);

        // 전체 유저 보기 버튼, 클릭 시 전체 유저에 대한 recyclerView가 나온다.
        btn_all = rootView.findViewById(R.id.btn_all);
        btn_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all_view.setVisibility(View.VISIBLE);
                nearMe_view.setVisibility(View.INVISIBLE);
            }
        });

        // 내 근처유저 보기 버튼, 클릭 시 내 근처유저에 대한 recyclerView가 나온다.
        btn_nearMe = rootView.findViewById(R.id.btn_nearMe);
        btn_nearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all_view.setVisibility(View.INVISIBLE);
                nearMe_view.setVisibility(View.VISIBLE);
            }
        });

        RetrofitClient retrofitClient = new RetrofitClient();
        Call<PostResult> call = retrofitClient.service.getPosts();

        //Enqueue로 비동기 통신 실행.
        call.enqueue(new Callback<PostResult>() {
            //통신 완료 후 이벤트 처리 위한 Callback 리스너 onResponse, onFailure 등록
            @Override
            public void onResponse(Call<PostResult> call, Response<PostResult> response) {
                if(response.isSuccessful()){
                    //정상적으로 통신 성공
                    PostResult result = response.body();
                    Log.e("main", "onResponse: 성공, 결과\n" + result.toString());
                }
                else{
                    //통신이 실패할 경우
                    Log.e("main", "onResponse 실패");
                }
            }

            @Override
            public void onFailure(Call<PostResult> call, Throwable t) {
                //통신실패 (인터넷 끊김, 예외 발생 등 시스템적인 이유)
                Log.e("main", "onFailure" + t.getMessage());
            }
        });

        return rootView;
    }



}
