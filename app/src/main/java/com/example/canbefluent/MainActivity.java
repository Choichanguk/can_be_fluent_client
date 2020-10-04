package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.PostResult;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView; // 바텀 네비게이션 뷰
    private FragmentManager fm;
    private FragmentTransaction ft;
    private frag_community frag_community;
    private frag_chats frag_chats;
    private frag_randomCall frag_randomCall;
    TextView title;
    user_item user_item;
    ImageView profile_img;
    String url = "http://3.34.44.183/profile_img/";  // 서버로부터 프로필 이미지를 가져오는 url 뒤에 파일 이름을 붙여서 사용.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        user_item = (user_item) intent.getSerializableExtra("user item");

        url = url + user_item.getProfile_img(); //url 유저 프로필 이미지에 해당하는 파일 이름을 붙여준다.

        Log.e(TAG, "url: " + url);

        // user_item에 담긴 데이터를 세팅해준다.
        profile_img = findViewById(R.id.profile_img);

//        Picasso.get()
//                .load(url)
////                .rotate(90f) // 사진 파일을 회전해줍시다. Operator 끝났습니다.
//                .into(profile_img);

        Glide.with(this)
                .load(url)
                .into(profile_img);



        bottomNavigationView = findViewById(R.id.bottomNavi);
        title = findViewById(R.id.view_title);

        //프래그먼트 생성
        frag_community = new frag_community();
        frag_chats = new frag_chats();
        frag_randomCall = new frag_randomCall();

        //제일 처음 띄워줄 뷰를 세팅해줍니다. commit();까지 해줘야 합니다.
        getSupportFragmentManager().beginTransaction().replace(R.id.Main_Frame,frag_community).commitAllowingStateLoss();


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    //menu_bottom.xml에서 지정해줬던 아이디 값을 받아와서 각 아이디값마다 다른 이벤트를 발생시킵니다.
                    case R.id.community: {
                        title.setText("Community");
                        getSupportFragmentManager().beginTransaction().replace(R.id.Main_Frame, frag_community).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.chats: {
                        title.setText("Chats");
                        getSupportFragmentManager().beginTransaction().replace(R.id.Main_Frame, frag_chats).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.random_call: {
                        title.setText("random_call");
                        getSupportFragmentManager().beginTransaction().replace(R.id.Main_Frame, frag_randomCall).commitAllowingStateLoss();
                        return true;
                    }
                    default:
                        return false;

                }
            }
        });


    }
}