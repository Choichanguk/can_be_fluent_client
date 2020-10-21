package com.example.canbefluent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.getChatList;
import com.example.canbefluent.pojoClass.getRoomList;
import com.example.canbefluent.retrofit.RetrofitClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class user_profile_activity extends AppCompatActivity {
    private static final String TAG = "user_profile_activity";
    user_item user_item;
    CircleImageView profile_img;
    String url = "http://52.78.58.117/profile_img/";
    TextView name, native_lang1, native_lang2, practice_lang1, practice_lang2, practice_lang1_level, practice_lang2_level, age;
    LinearLayout linearLayout;
    ImageButton btn_back, btn_msg;

    RetrofitClient retrofitClient;
    Call<ArrayList<getRoomList>> call;
    sharedPreference sharedPreference = new sharedPreference();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_activity);

        // frag_community 에서 보낸 유저 객체를 받는다.
        Intent intent = getIntent();
        user_item = (user_item) intent.getSerializableExtra("user item");
        url = url + user_item.getProfile_img();

        String user_index = user_item.getUser_index();
        Log.e(TAG, "user index: " + user_index);

        //받은 객체에 담겨있는 유저 정보를 각각의 위치에 세팅해준다.
        //프로필 이미지 세팅
        profile_img = findViewById(R.id.profile_img);
        Glide.with(this)
                .load(url)
                .into(profile_img);

        // 유저 이름 세팅
        name = findViewById(R.id.name);
        name.setText(user_item.getFirst_name());

        // 유저 나이 세팅
        age = findViewById(R.id.age);
        String birthDate = user_item.getYear() + user_item.getMonth() + user_item.getDay();
        int age_int = getAge(birthDate);
        age.setText(" ," + age_int);

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



        // 메시지 방으로 들어가는 버튼
        btn_msg = findViewById(R.id.btn_msg);
        btn_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sender_index = sharedPreference.loadUserIndex(user_profile_activity.this);
                String receiver_index = user_item.getUser_index();

                Log.e(TAG, "sender_index: " + sender_index);
                Log.e(TAG, "receiver_index: " + receiver_index);

                /**
                 * 채팅방이 존재하는지 서버로부터 결과 받아와야함
                 * 만약 존재한다면 방 정보를 넘겨줌
                 * 존재하지 않는다면 그냥 입장
                 */
                retrofitClient = new RetrofitClient();
                call = retrofitClient.service.get_room_info(sender_index, receiver_index);
                call.enqueue(new Callback<ArrayList<getRoomList>>() {
                    @Override
                    public void onResponse(Call<ArrayList<getRoomList>> call, Response<ArrayList<getRoomList>> response) {
                        ArrayList<getRoomList> result = response.body();
                        Log.e(TAG, "onResponse");
                        Log.e(TAG, "result size: " + result.size());
                        Intent intent = new Intent(user_profile_activity.this, message_room.class);
                        intent.putExtra("room obj", result.get(0));
                        intent.putExtra("type", "from room list");
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(Call<ArrayList<getRoomList>> call, Throwable t) {
                        Log.e(TAG, "onFailure");
                    }
                });
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

    /**
     * 생년월일을 파라미터로 주면 만 나이를 리턴하는 메서드
     * @param birthDate 유저의 생년월일 ex) 19931213
     * @return 26(만 나이)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static int getAge(String birthDate) {
        LocalDate now = LocalDate.now();
        LocalDate parsedBirthDate = LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

        int Age = now.minusYears(parsedBirthDate.getYear()).getYear(); // (1)

        // (2)
        // 생일이 지났는지 여부를 판단하기 위해 (1)을 입력받은 생년월일의 연도에 더한다.
        // 연도가 같아짐으로 생년월일만 판단할 수 있다!
        if (parsedBirthDate.plusYears(Age).isAfter(now)) {
            Age = Age -1;
        }
        return Age;
    }

}