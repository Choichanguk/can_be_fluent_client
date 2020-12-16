package com.example.canbefluent.user_info;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.canbefluent.R;
import com.example.canbefluent.adapter.languageCodeAdapter;
import com.example.canbefluent.items.language_code_item;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.sharedPreference;
import com.example.canbefluent.splashActivity;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class set_translate_language extends AppCompatActivity {
    private static final String TAG = "set_translate_language";
    ImageButton btn_back;
    RetrofitClient retrofitClient;
    RecyclerView lang_code_recycle;
    languageCodeAdapter adapter;

    sharedPreference sharedPreference = new sharedPreference();
    String lang_code; // 유저의 언어 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_translate_language);

        lang_code = sharedPreference.loadLangCode(set_translate_language.this);

        // 서버로 언어이름과 언어코드를 가져오는 통신
        retrofitClient = new RetrofitClient();
        retrofitClient.service.getLanguageNameCode()
                .enqueue(new Callback<ArrayList<language_code_item>>() {
                    @Override
                    public void onResponse(Call<ArrayList<language_code_item>> call, Response<ArrayList<language_code_item>> response) {
                        final ArrayList<language_code_item> list = response.body();

                        // 리사이클러뷰 만들어주는 코드
                        lang_code_recycle = findViewById(R.id.lang_code_recycle);
                        adapter = new languageCodeAdapter(list, lang_code);
                        adapter.setOnItemClickListener(new languageCodeAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position) {
                                String lang_code = list.get(position).getLang_code();
                                sharedPreference.saveLangCode(set_translate_language.this, lang_code);

                                Intent intent = new Intent(set_translate_language.this, edit_config_language.class);
                                finish();
                                startActivity(intent);
                            }
                        });
                        lang_code_recycle.setAdapter(adapter);
                        lang_code_recycle.setLayoutManager(new LinearLayoutManager(set_translate_language.this));

                    }

                    @Override
                    public void onFailure(Call<ArrayList<language_code_item>> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });

        /**
         * 뒤로가기 버튼
         */
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}