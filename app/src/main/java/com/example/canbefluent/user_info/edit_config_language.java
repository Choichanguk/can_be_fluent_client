package com.example.canbefluent.user_info;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.canbefluent.R;
import com.example.canbefluent.items.user_item;

public class edit_config_language extends AppCompatActivity {
    com.example.canbefluent.items.user_item user_item;
    ImageView btn_config_app_lang, btn_config_practice_lang, btn_config_intro;
    ImageButton btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_config_language);
        user_item = (user_item) getIntent().getSerializableExtra("user item");
        Log.e("onCreate", "user index: " + user_item.getUser_index());

        /**
         * 앱 언어 설정 액티비티로 이동하는 버튼
         */
        btn_config_app_lang = findViewById(R.id.btn_config_app_lang);
        btn_config_app_lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(edit_config_language.this, set_app_language.class);
//                intent.putExtra("language code", "ko");
                startActivity(intent);
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