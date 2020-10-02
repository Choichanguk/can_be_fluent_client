package com.example.canbefluent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register_setLanguage extends AppCompatActivity {
    ImageButton btn_set_native, btn_set_speak, btn_set_learn;
    ArrayList<String> country_list = new ArrayList<>(); // 국가이름을 담는 리스트
    private static final int GET_COUNTRY_NAME = 3;
    private static final String TAG = "Register_setLanguage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_set_language);

        btn_set_native = findViewById(R.id.btn_set_native); // 모어를 추가하는 버튼
        btn_set_speak = findViewById(R.id.btn_set_speak);   // 구사 가능 언어를 추가하는 버튼
        btn_set_learn = findViewById(R.id.btn_set_learn);   // 학습 언어를 추가하는 버튼



        btn_set_native.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), countryListActivity.class);
                intent.putExtra("country list", country_list);
                startActivityForResult(intent, GET_COUNTRY_NAME);
            }
        });

        /**
         * 국가 이름을 제공하는 api로부터 국가 이름을 가져온 후 country_list에 담아준다.
         */
        RetrofitClient retrofitClient = new RetrofitClient();
        Call<ArrayList<getCountryNameResult>> call = retrofitClient.service2.getCountryName("name;");

        //Enqueue로 비동기 통신 실행.
        call.enqueue(new Callback<ArrayList<getCountryNameResult>>() {

            //통신 완료 후 이벤트 처리 위한 Callback 리스너 onResponse, onFailure 등록
            @Override
            public void onResponse(Call<ArrayList<getCountryNameResult>> call, Response<ArrayList<getCountryNameResult>> response) {
                if(response.isSuccessful()){

                    //정상적으로 통신 성공
                    ArrayList<getCountryNameResult> result = response.body();

                    //api로부터 가져온 국가 리스트를 country_list에 담는다.
                    if(result != null){
                        for(int i=0; i<result.size(); i++){
                            country_list.add(result.get(i).toString());
                        }
                    }
                }
                else{
                    //통신이 실패할 경우
                    Log.e("main", "onResponse 실패");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<getCountryNameResult>> call, Throwable t) {
                //통신실패 (인터넷 끊김, 예외 발생 등 시스템적인 이유)
                Log.e("main", "onFailure" + t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_COUNTRY_NAME){
            String country_name = data.getStringExtra("name");
            Log.e(TAG, "picked name: " + country_name);
        }
    }


}