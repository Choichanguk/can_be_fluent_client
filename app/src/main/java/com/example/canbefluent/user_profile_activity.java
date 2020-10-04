package com.example.canbefluent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.items.user_item;

import de.hdodenhof.circleimageview.CircleImageView;

public class user_profile_activity extends AppCompatActivity {
    private static final String TAG = "user_profile_activity";
    user_item user_item;
    CircleImageView profile_img;
    String url = "http://3.34.44.183/profile_img/";
    TextView name, native_lang1, native_lang2, practice_lang1, practice_lang2, practice_lang1_level, practice_lang2_level;
    LinearLayout linearLayout;
    ImageButton btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_activity);

        // 유저 리스트에서 선택한 유저의 정보를 담은 객체를 담아온다.
        Intent intent = getIntent();
        user_item = (user_item) intent.getSerializableExtra("user item");
        url = url + user_item.getProfile_img();

        //담아온 객체에 담겨있는 유저 정보를 각각의 위치에 세팅해준다.
        profile_img = findViewById(R.id.profile_img);   //프로필 이미지 세팅
        Glide.with(this)
                .load(url)
                .into(profile_img);

        name = findViewById(R.id.name);     // 유저 이름 세팅
        name.setText(user_item.getFirst_name());

        // 필수 언어 세팅
        native_lang1 = findViewById(R.id.native_lang1);
        native_lang1.setText(user_item.getNative_lang1());
        practice_lang1 = findViewById(R.id.practice_lang1);
        practice_lang1.setText(user_item.getPractice_lang1());
        practice_lang1_level = findViewById(R.id.practice_lang1_level);
        practice_lang1_level.setText(user_item.getPractice_lang1_level());

        linearLayout = findViewById(R.id.linearLayout5);

        // 추가 언어 세팅
        native_lang2 = findViewById(R.id.native_lang2);
        practice_lang2 = findViewById(R.id.practice_lang2);
        practice_lang2_level = findViewById(R.id.practice_lang2_level);


        if(user_item.getNative_lang2() != null){
            native_lang2.setText(user_item.getNative_lang2());
        }
        else{
            native_lang2.setVisibility(View.INVISIBLE);
        }

        if(user_item.getPractice_lang2() != null){
            practice_lang2.setText(user_item.getPractice_lang2());
            practice_lang2_level.setText(user_item.getPractice_lang2_level());
        }
        else{
            linearLayout.setVisibility(View.INVISIBLE);
        }

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}