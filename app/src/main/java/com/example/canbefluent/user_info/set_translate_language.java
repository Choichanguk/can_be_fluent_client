package com.example.canbefluent.user_info;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.canbefluent.R;
import com.example.canbefluent.adapter.languageCodeAdapter;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.utils.sharedPreference;
import com.example.canbefluent.utils.MyApplication;

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

        // 리사이클러뷰 만들어주는 코드
        lang_code_recycle = findViewById(R.id.lang_code_recycle);
        adapter = new languageCodeAdapter(MyApplication.list, lang_code);
        adapter.setOnItemClickListener(new languageCodeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                String lang_code = MyApplication.list.get(position).getLang_code();
                sharedPreference.saveLangCode(set_translate_language.this, lang_code);

//                Intent intent = new Intent(set_translate_language.this, edit_config_language.class);
//                finish();
//                startActivity(intent);
                Intent intent = new Intent();
                intent.putExtra("result", lang_code);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        lang_code_recycle.setAdapter(adapter);
        lang_code_recycle.setLayoutManager(new LinearLayoutManager(set_translate_language.this));

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