package com.example.canbefluent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class lang_level_popup extends AppCompatActivity {
    Button btn_ok;
    RadioGroup radioGroup;
    RadioButton Beginner, Intermediate, Advanced, Native;
    String Level;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lang_level_popup);

        radioGroup = findViewById(R.id.radioGroup);
        Beginner = findViewById(R.id.Beginner);
        Intermediate = findViewById(R.id.Intermediate);
        Advanced = findViewById(R.id.Advanced);
        Native = findViewById(R.id.Native);

        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);
        Beginner.setChecked(true);

        btn_ok = findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("level", Level);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if(i == R.id.Beginner) {
                Level = "Beginner";
            }
            else if(i == R.id.Intermediate){
                Level = "Intermediate";
            }
            else if(i == R.id.Advanced){
                Level = "Advanced";
            }
            else if(i == R.id.Native){
                Level = "Native";
            }
        }
    };
}