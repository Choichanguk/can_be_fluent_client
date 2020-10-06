package com.example.canbefluent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.items.user_item;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import de.hdodenhof.circleimageview.CircleImageView;

public class user_profile_activity extends AppCompatActivity {
    private static final String TAG = "user_profile_activity";
    user_item user_item;
    CircleImageView profile_img;
    String url = "http://3.34.44.183/profile_img/";
    TextView name, native_lang1, native_lang2, practice_lang1, practice_lang2, practice_lang1_level, practice_lang2_level, age;
    LinearLayout linearLayout;
    ImageButton btn_back;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_activity);

        // frag_community 에서 보낸 유저 객체를 받는다.
        Intent intent = getIntent();
        user_item = (user_item) intent.getSerializableExtra("user item");
        url = url + user_item.getProfile_img();

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
     * @return 만 나이
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