package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.user_info.my_profile;
import com.example.canbefluent.utils.MyApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

import static io.socket.client.Socket.EVENT_CONNECT;

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
    String url = MyApplication.server_url + "/profile_img/";  // 서버로부터 프로필 이미지를 가져오는 url 뒤에 파일 이름을 붙여서 사용.
    String url_file = "";

    Button btn_exam;
    public static ConstraintLayout search_floating_view;
    public static boolean isSearching = false;

    public static Socket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 랜덤통화 상대 찾기 플로팅 뷰
        search_floating_view = findViewById(R.id.search_floating_view);
        search_floating_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title.setText(R.string.random_call);
                if(frag_community != null) getSupportFragmentManager().beginTransaction().hide(frag_community).commit();
                if(frag_chats != null) getSupportFragmentManager().beginTransaction().hide(frag_chats).commit();
                if(frag_randomCall != null) getSupportFragmentManager().beginTransaction().show(frag_randomCall).commit();
            }
        });

        ProgressBar progressBar = findViewById(R.id.indeterminateBar);

//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()) {
//                            Log.e(TAG, "getInstanceId failed", task.getException());
//
//                            return;
//                        }
//                        // Get new Instance ID token
//                        String token = task.getResult().getToken();
//                        Log.e(TAG, "token: " + token);
//                    }
//                });

        Intent intent = getIntent();
        user_item = (user_item) intent.getSerializableExtra("user item");

        url_file = url + user_item.getProfile_img(); //url 유저 프로필 이미지에 해당하는 파일 이름을 붙여준다.

        Log.e(TAG, "url: " + url);

        // user_item에 담긴 데이터를 세팅해준다.
        profile_img = findViewById(R.id.profile_img);

        Glide.with(this)
                .load(url_file)
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
                        Log.e(TAG, "token2: " + token);
                    }
                });



        bottomNavigationView = findViewById(R.id.bottomNavi);
        title = findViewById(R.id.view_title);

        //프래그먼트 생성
        frag_community = new frag_community();

        //제일 처음 띄워줄 뷰를 세팅해줍니다. commit();까지 해줘야 합니다.
        getSupportFragmentManager().beginTransaction().replace(R.id.Main_Frame,frag_community).commitAllowingStateLoss();


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    //menu_bottom.xml에서 지정해줬던 아이디 값을 받아와서 각 아이디값마다 다른 이벤트를 발생시킵니다.
                    case R.id.community: {
                        title.setText(R.string.community);
                        if(frag_community == null) {

                            frag_community = new frag_community();
                            getSupportFragmentManager().beginTransaction().add(R.id.Main_Frame, frag_community).commit();
                        }

                        if(frag_community != null) getSupportFragmentManager().beginTransaction().show(frag_community).commit();
                        if(frag_chats != null) getSupportFragmentManager().beginTransaction().hide(frag_chats).commit();
                        if(frag_randomCall != null) getSupportFragmentManager().beginTransaction().hide(frag_randomCall).commit();
                        return true;
                    }
                    case R.id.chats: {
                        title.setText(R.string.chat);
                        if(frag_chats == null) {
                            frag_chats = new frag_chats();
                            getSupportFragmentManager().beginTransaction().add(R.id.Main_Frame, frag_chats).commit();
                        }

                        if(frag_community != null) getSupportFragmentManager().beginTransaction().hide(frag_community).commit();
                        if(frag_chats != null) getSupportFragmentManager().beginTransaction().show(frag_chats).commit();
                        if(frag_randomCall != null) getSupportFragmentManager().beginTransaction().hide(frag_randomCall).commit();

                        return true;

                    }
                    case R.id.random_call: {
                        title.setText(R.string.random_call);
                        if(frag_randomCall == null) {
                            frag_randomCall = new frag_randomCall();
                            getSupportFragmentManager().beginTransaction().add(R.id.Main_Frame, frag_randomCall).commit();
                        }

                        if(frag_community != null) getSupportFragmentManager().beginTransaction().hide(frag_community).commit();
                        if(frag_chats != null) getSupportFragmentManager().beginTransaction().hide(frag_chats).commit();
                        if(frag_randomCall != null) getSupportFragmentManager().beginTransaction().show(frag_randomCall).commit();
                        return true;
                    }
                    default:
                        return false;

                }
            }
        });




    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
    }


    @Override
    protected void onResume() {
        super.onResume();

        Glide.with(this)
                .load(url + user_item.getProfile_img())
                .into(profile_img);
    }

    public void connect(String native_lang_code, String practice_lang_code){
        try {
            socket = IO.socket("http://canbefluent.xyz:8888/");
            socket.on(EVENT_CONNECT, args -> {
                Log.e(TAG, "connect 이벤트");
                // 소켓 연결 후 유저의 정보를 서버로 넘겨준다.
                socket.emit("set info", MainActivity.user_item.getUser_index(), native_lang_code, practice_lang_code);
                socket.emit("create or join", "foo");
            }).on("show dialog", args -> {
                Log.e(TAG, "show dialog 이벤트 발생");
            });
            socket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        if (socket != null) {
            sendMessage("bye");
            socket.disconnect();
            socket = null;

        }
    }

    private void sendMessage(Object message) {
        Log.e(TAG, "sendMessage");
        socket.emit("message", message);
    }

    public void visible_floating_view(String status){
        if(status.equals("visible")){
            search_floating_view.setVisibility(View.VISIBLE);
        }
        else if(status.equals("gone")){
            search_floating_view.setVisibility(View.GONE);
        }

    }

    /**
     * 매칭 타임아웃인 경우 보여주는 alert 다이얼로그
     */
    public void show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("매칭 타임아웃");
        builder.setMessage("조건에 맞는 대화상대가 없습니다. 잠시 후 다시 매칭을 시도해주세요.");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }


}