package com.example.canbefluent.user_info;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.example.canbefluent.R;
import com.example.canbefluent.utils.sharedPreference;
import com.example.canbefluent.splashActivity;

public class set_app_language extends AppCompatActivity {
    ImageButton btn_back;
    String language_code;
    RadioButton english, korean, japanese;
    com.example.canbefluent.utils.sharedPreference sharedPreference = new sharedPreference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_app_language);

//        language_code = getIntent().getStringExtra("language code");
//        Log.e("onCreate", "language_code: " + language_code);

        language_code = sharedPreference.loadLang_code(set_app_language.this);
        Log.e("onCreate", "language_code: " + language_code);

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

        english = findViewById(R.id.btn_english);
        korean = findViewById(R.id.btn_korean);
        japanese = findViewById(R.id.btn_japanese);

        //shared에 저장된 language code에 따라 라디오 버튼 디폴트 체크
        switch(language_code){
            case  "en":
                english.setChecked(true);
            break;

            case  "ko":
                korean.setChecked(true);
            break;

            case  "ja":
                japanese.setChecked(true);
                break;
          default:
            break;
        }


        korean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                korean.setChecked(false);
                show_dialog("ko");
            }
        });

        japanese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                japanese.setChecked(false);
                show_dialog("ja");
            }
        });

        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                english.setChecked(false);
                show_dialog("en");
            }
        });
    }

    public void show_dialog(final String code){
        new AlertDialog.Builder(set_app_language.this) // TestActivity 부분에는 현재 Activity의 이름 입력.
            .setMessage(R.string.dialog_text)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which){

                    sharedPreference.saveLang_code(set_app_language.this, code);

                    Intent intent = new Intent(set_app_language.this, splashActivity.class);
                    ActivityCompat.finishAffinity(set_app_language.this);
                    startActivity(intent);

                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which){

                }
            })
            .show();
    }
}