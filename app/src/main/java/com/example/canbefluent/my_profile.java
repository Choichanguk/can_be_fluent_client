package com.example.canbefluent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.items.user_item;

public class my_profile extends AppCompatActivity {
    ImageView profile_img;
    user_item user_item;
    ImageButton btn_back;

    TextView name;
    String url = "http://3.34.44.183/profile_img/";
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Intent intent = getIntent();
        user_item = (user_item) intent.getSerializableExtra("user item");

        //이름 세팅
        name = findViewById(R.id.name);
        name.setText(user_item.getFirst_name());

        //프로필 이미지 세팅
        url = url + user_item.getProfile_img();
        profile_img = findViewById(R.id.profile_img);

        Glide.with(this)
                .load(url)
                .into(profile_img);

        GradientDrawable drawable= (GradientDrawable) my_profile.this.getDrawable(R.drawable.img_round);
        profile_img.setBackground(drawable);
        profile_img.setClipToOutline(true);

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}