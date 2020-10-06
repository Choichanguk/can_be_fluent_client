package com.example.canbefluent.practice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.canbefluent.R;
import com.example.canbefluent.adapter.userListAdapter;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class practice_img_upload extends AppCompatActivity {
    RecyclerView recyclerView;
    userListAdapter userListAdapter;
    ArrayList<user_item> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_img_upload);

        RetrofitClient retrofitClient = new RetrofitClient();
        Call<ArrayList<user_item>> call = retrofitClient.service.get_allUserInfo();
        call.enqueue(new Callback<ArrayList<user_item>>() {
            @Override
            public void onResponse(Call<ArrayList<com.example.canbefluent.items.user_item>> call, Response<ArrayList<user_item>> response) {
                ArrayList<user_item> result = response.body();

                // 서버로부터 받아온 유저 리스트를 all_user_list에 담아준다.
                // 리사이클러뷰에 all_user_list를 보여준다.
                assert result != null;
                list.addAll(result);
                Log.e("practice", "user list size: " + list.size());

                //recycler
                recyclerView = findViewById(R.id.recyclerView);

                recyclerView.setLayoutManager(new LinearLayoutManager(practice_img_upload.this));

                // define an adapter
                userListAdapter = new userListAdapter(list);
                recyclerView.setAdapter(userListAdapter);
            }

            @Override
            public void onFailure(Call<ArrayList<com.example.canbefluent.items.user_item>> call, Throwable t) {

            }
        });

//        recyclerView = findViewById(R.id.recyclerView);

    }
}