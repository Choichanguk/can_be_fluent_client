package com.example.canbefluent.user_info;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.MainActivity;
import com.example.canbefluent.MyApplication;
import com.example.canbefluent.R;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.show_follow_activity;
import com.example.canbefluent.viewpager_img;

public class my_profile extends AppCompatActivity {
    ImageView profile_img;
    user_item user_item;
    ImageButton btn_back;
    ConstraintLayout config_intro, config_language, config_following, config_visitor;

    TextView name;
    String url = MyApplication.server_url + "/profile_img/";
    String url_file = "";
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
        url_file = url + user_item.getProfile_img();
        profile_img = findViewById(R.id.profile_img);
        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), viewpager_img.class);
                intent.putExtra("user item", user_item);
                startActivity(intent);
            }
        });

        Glide.with(this)
                .load(url_file)
                .into(profile_img);

        GradientDrawable drawable= (GradientDrawable) my_profile.this.getDrawable(R.drawable.img_round);
        profile_img.setBackground(drawable);
        profile_img.setClipToOutline(true);

        config_intro = findViewById(R.id.config_intro);
        config_intro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(my_profile.this, introduce_profile.class);
                intent.putExtra("user item", user_item);
                Log.e("tag", "user index: " + user_item.getUser_index());
                Log.e("tag", "user id: " + user_item.getUser_id());
                Log.e("tag", "user name: " + user_item.getFirst_name());
//                Log.e("tag", "user index: " + user_item.getUser_index());

                startActivity(intent);
            }
        });

        /**
         * 언어 설정 버튼
         */
        config_language = findViewById(R.id.config_language);
        config_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(my_profile.this, edit_config_language.class);
                intent.putExtra("user item", user_item);
                startActivity(intent);
            }
        });

        /**
         * 방문자 리스트 액티비티로 이동하는 버튼
         */
        config_visitor = findViewById(R.id.config_visitor);
        config_visitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(my_profile.this, show_visitor_acvitity.class);
                startActivity(intent);
            }
        });

        /**
         * 팔로워 리스트 액티비티로 이동하는 버튼
         */
        config_following = findViewById(R.id.config_following);
        config_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(my_profile.this, show_follow_activity.class);
                startActivity(intent);
            }
        });



        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        user_item user_item = MainActivity.user_item;
        Glide.with(this)
                .load(url + user_item.getProfile_img())
                .into(profile_img);
    }

}