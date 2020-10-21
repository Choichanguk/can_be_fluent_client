package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.PostResult;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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
    public static user_item user_item;  // 현재 접속한 유저의 정보를 담은 객체
    ImageView profile_img;
    String url = "http://52.78.58.117/profile_img/";  // 서버로부터 프로필 이미지를 가져오는 url 뒤에 파일 이름을 붙여서 사용.

    Button btn_exam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed", task.getException());

                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.e(TAG, "token: " + token);
                    }
                });


        /**
         * 브로드캐스트 리시버 등록
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter("blackJinData"));

//        /**
//         * client socket service 실행
//         */
//        Intent service_intent = new Intent(MainActivity.this, socket_service.class);
//        startService(service_intent);

        Intent intent = getIntent();
        user_item = (user_item) intent.getSerializableExtra("user item");

        url = url + user_item.getProfile_img(); //url 유저 프로필 이미지에 해당하는 파일 이름을 붙여준다.

        Log.e(TAG, "url: " + url);

        // user_item에 담긴 데이터를 세팅해준다.
        profile_img = findViewById(R.id.profile_img);

        Glide.with(this)
                .load(url)
                .into(profile_img);

        // 클릭 시 유저 정보를 인텐트에 담아 내 프로필 화면으로 보낸다.
        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, my_profile.class);
                intent.putExtra("user item", user_item);
                startActivity(intent);

            }
        });


        /**
         * fcm 기기 고유 토큰을 가져온다.
         * 가져온 토큰은 서버에 저장해야함 - MyFireBaseMessagingService.class의 sendRegistrationToServer메서드 에서
         */
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.e(TAG, "token: " + token);
                    }
                });



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

//
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //브로드캐스트 리시버 해제
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        // 클라리언트 소켓 서비스 종료 (실행중이라면)
        if(isRunningService()){
            Intent intent = new Intent(MainActivity.this, socket_service.class); // 이동할 컴포넌트
            stopService(intent); // 서비스 종료
        }
    }

    /**
     * 브로드캐스트 예제
     * 브로드캐스트 수신자
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // intent ..
            String data1 = intent.getStringExtra("data1");
            String data2 = intent.getStringExtra("data2");
            String data3 = intent.getStringExtra("data3");
            Log.e(TAG, "data1: " + data1 + " /data2: " + data2 + " /data3: " + data3);
            Log.e(TAG, "브로드캐스트 수신 완료");
        }
    };

    /**
     * 서비스가 실행중인지 알려주는 메서드
     * @return true or false
     */
    public Boolean isRunningService(){
        Log.e(TAG, "이름: " + socket_service.class.getName());
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (socket_service.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return  false;
    }


}