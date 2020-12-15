package com.example.canbefluent.user_info;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canbefluent.MainActivity;
import com.example.canbefluent.R;
import com.example.canbefluent.adapter.userListAdapter;
import com.example.canbefluent.adapter.visitorAdapter;
import com.example.canbefluent.items.visitor_item;
import com.example.canbefluent.retrofit.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class show_visitor_acvitity extends AppCompatActivity {
    private static final String TAG = "show_visitor_acvitity";
    TextView visitor_num;

    RecyclerView visitor_recycle;
    visitorAdapter adapter;

    RetrofitClient retrofitClient = new RetrofitClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_visitor_acvitity);


        retrofitClient.service.get_visitor_Info(MainActivity.user_item.getUser_index())
                .enqueue(new Callback<ArrayList<visitor_item>>() {
                    @Override
                    public void onResponse(Call<ArrayList<visitor_item>> call, Response<ArrayList<visitor_item>> response) {
                        ArrayList<visitor_item> list = response.body();

                        if(list.size() == 0){
                            Log.e(TAG, "방문자 수 0");
                        }
                        else {

                            visitor_recycle.setLayoutManager(new LinearLayoutManager(show_visitor_acvitity.this));

                            adapter = new visitorAdapter(list, show_visitor_acvitity.this);
                            adapter.setOnItemClickListener(new userListAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View v, int position) {
                                    Toast.makeText(show_visitor_acvitity.this, "버튼 클릭: " + position, Toast.LENGTH_SHORT).show();
                                }
                            });
                            visitor_recycle.setAdapter(adapter);

                        }

                        visitor_num = findViewById(R.id.visitor_num);
                        visitor_num.setText(list.size() + "");

                    }

                    @Override
                    public void onFailure(Call<ArrayList<visitor_item>> call, Throwable t) {

                    }
                });
        visitor_recycle = findViewById(R.id.visitor_recycle);

    }
}