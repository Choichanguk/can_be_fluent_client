package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView; // 바텀 네비게이션 뷰
    private FragmentManager fm;
    private FragmentTransaction ft;
    private frag_community frag_community;
    private frag_chats frag_chats;
    private frag_randomCall frag_randomCall;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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