package com.example.canbefluent.login_process;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.canbefluent.MainActivity;
import com.example.canbefluent.R;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.sharedPreference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    Button btn_register, btn_login;
    EditText user_id, user_pw;
    com.example.canbefluent.sharedPreference sharedPreference; // 유저의 id, 로그인 상태를 shared preference에 저장하는 클래스
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user_id = findViewById(R.id.user_id);
        user_pw = findViewById(R.id.user_pw);

        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register_setId.class);
                startActivity(intent);

            }
        });

        btn_login = findViewById(R.id.btn_login);
        // 클릭 시 유저가 입력한 id, pw 값을 서버로 보낸다.
        // 서버는 로그인 가능 or 불가능인지 판별 후 결과 값을 클라이언트로 보낸다. (이 때 결과값이 로그인 가능이면 유저 정보도 함께 보낸다.)
        // 클라이언트는 결과값을 받아서 결과값이 로그인 가능이면 유저 정보를 POJO class에 담아 main activity에 넘긴다.
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = user_id.getText().toString();
                String pw = user_pw.getText().toString();
                Log.e(TAG, "id: " + id);
                Log.e(TAG, "pw: " + pw);

                if(id.equals("") || pw.equals("")){
                    alertDialog("write id");
                }
                else{
                    //유저가 입력한 id, pw 값을 서버로 보낸다.
                    RetrofitClient retrofitClient = new RetrofitClient();
                    Call<user_item[]> call = retrofitClient.service.login_process(id, pw);
                    call.enqueue(new Callback<user_item[]>() { // 서버로부터 결과 값을 받는 callback 함수
                        @Override
                        public void onResponse(Call<user_item[]> call, Response<user_item[]> response) {
                            user_item[] user_item_arr = response.body();
                            user_item user_item = new user_item();
//                            Log.e(TAG, "user_item: " + result);

                            assert user_item_arr != null;
                            for(user_item item : user_item_arr){
                                user_item = item;
                            }
                            assert user_item != null;
                            String result = user_item.getResult();
                            if(result.equals("success")){
                                // 결과 값이 success면
                                // 1. shared에 유저의 아이디와 로그인 상태를 저장한다.
                                // 2. user_item 객체를 main activity에 넘겨준다.

                                sharedPreference = new sharedPreference();
                                sharedPreference.saveUserId(getApplicationContext(), user_item.getUser_id());   //shared에 유저 아이디 저장
                                sharedPreference.saveUserPw(getApplicationContext(), user_item.getUser_pw());   //shared에 유저 비밀번호 저장
                                sharedPreference.saveLoginStatus(getApplicationContext(), true);    // shared에 로그인 상태 저장

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("user item", user_item);
                                startActivity(intent);
                                finish();
                            }
                            else if(result.equals("fail")){ // 결과 값이 fail이면 로그인 실패 다이얼로그를 띄워준다.
                                alertDialog("login fail");
                            }
                        }

                        @Override
                        public void onFailure(Call<user_item[]> call, Throwable t) {
                            Log.e(TAG, "onFailure " + t.getMessage());
                        }
                    });
                }
            }
        });
    }

    public void alertDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(type.equals("write id")){
            builder.setTitle("").setMessage("아이디 or 비밀번호를 입력해주세요.");
        }
        else if(type.equals("login fail")){
            builder.setTitle("").setMessage("아이디 or 비밀번호가 일치하지 않습니다.");
        }

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();


    }
}