package com.example.canbefluent.user_info;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.canbefluent.MainActivity;
import com.example.canbefluent.R;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.utils.sharedPreference;

public class edit_config_language extends AppCompatActivity {
    com.example.canbefluent.items.user_item user_item;
    ImageView btn_config_app_lang, btn_config_practice_lang, btn_config_intro, btn_config_translate_lang;
    ImageButton btn_back;

    TextView app_lang, translate_lang, native_lang, practice_lang;
    com.example.canbefluent.utils.sharedPreference sharedPreference = new sharedPreference();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==111){
            if (resultCode==RESULT_OK) {
                    String lang_code = data.getStringExtra("result");
                    translate_lang.setText(MyApplication.lang_code_map.get(lang_code));

//                Toast.makeText(MainActivity.this, "result ok!", Toast.LENGTH_SHORT).show();
            }else{
//                Toast.makeText(MainActivity.this, "result cancle!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_config_language);
//        user_item = (user_item) getIntent().getSerializableExtra("user item");
        user_item = MainActivity.user_item;
        Log.e("onCreate", "user index: " + user_item.getUser_index());

        String app_lang_code = sharedPreference.loadLang_code(edit_config_language.this);
        String translate_lang_code = sharedPreference.loadLangCode(edit_config_language.this);

        app_lang = findViewById(R.id.app_lang);
        translate_lang = findViewById(R.id.translate_lang);
        native_lang = findViewById(R.id.native_lang);
        practice_lang = findViewById(R.id.practice_lang);

        app_lang.setText(MyApplication.lang_code_map.get(app_lang_code));
        translate_lang.setText(MyApplication.lang_code_map.get(translate_lang_code));
        native_lang.setText(MyApplication.lang_code_map.get(user_item.getNative_lang1()));
        practice_lang.setText(MyApplication.lang_code_map.get(user_item.getPractice_lang1()));

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
         * 번역 언어 설정 액티비티로 이동하는 버튼
         */
        btn_config_translate_lang = findViewById(R.id.btn_config_translate_lang);
        btn_config_translate_lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(edit_config_language.this, set_translate_language.class);
//                startActivity(intent);
                startActivityForResult(intent, 111);
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