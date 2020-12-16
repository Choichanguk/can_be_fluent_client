package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.example.canbefluent.items.user_item;
import com.example.canbefluent.login_process.Login;
import com.example.canbefluent.pojoClass.getResult;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.user_info.set_app_language;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class splashActivity extends AppCompatActivity {
    private static final String TAG = "splashActivity";
    sharedPreference sharedPreference; // 유저의 id, 로그인 상태를 shared preference에 저장하는 클래스
    boolean isLogin = false;
    String user_id, user_pw;

    // 쉐어드에 저장되어있는 로그인 상태를 가져온다.
    // 로그인 상태가 true값이면 쉐어드로부터 id, pw 도 불러온다.
    // 불러온 id, pw 값을 서버로 보낸 후 id에 맞는 유저의 데이터를 불러온다.
    // 유저의 데이터를 mainActivity로 보낸다.
    // 로그인 상태가 false값이면 로그인 화면으로 넘어간다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPreference = new sharedPreference();
        isLogin = sharedPreference.loadLoginStatus(getApplicationContext());
        Log.e(TAG, "isLogin: " + isLogin);

        set_language_code();


        if(isLogin){


            // 서버로부터 유저의 정보를 가져온다
            Log.e(TAG, "if true");
            user_id = sharedPreference.loadUserId(getApplicationContext());
            user_pw = sharedPreference.loadUserPw(getApplicationContext());
            final RetrofitClient retrofitClient = new RetrofitClient();
            Call<user_item[]> call = retrofitClient.service.login_process(user_id, user_pw);
            call.enqueue(new Callback<user_item[]>() { // 서버로부터 결과 값을 받는 callback 함수
                @Override
                public void onResponse(Call<user_item[]> call, Response<user_item[]> response) {
                    Log.e(TAG, "onResponse");
                    user_item[] user_item_arr = response.body();
                    user_item user_item = new user_item();
//                            Log.e(TAG, "user_item: " + result);

                    assert user_item_arr != null;
                    for(user_item item : user_item_arr){
                        user_item = item;
                    }
                    assert user_item != null;
                    String result = user_item.getResult();
                    sharedPreference.saveUserIndex(splashActivity.this, user_item.getUser_index());

                    /**
                     * 파이어베이스 토큰을 얻는 로직
                     */
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if(task.isSuccessful() && task.getResult() != null){
                                String inviterToken = task.getResult().getToken();
                                Log.e("get Token", "inviterToken: " + inviterToken);
                                sharedPreference.saveFCMToken(splashActivity.this, inviterToken);
                            }
                        }
                    });

                    if(result.equals("success")){
                        Log.e(TAG, "onResponse success");
                        // 결과 값이 success면
                        // 1. shared에 유저의 아이디와 로그인 상태를 저장한다.
                        // 2. user_item 객체를 main activity에 넘겨준다.

                        // 서버로 로그인 시간을 업데이트 시킨다.
                        final com.example.canbefluent.items.user_item finalUser_item = user_item;
                        retrofitClient.service.update_login_time(user_item.getUser_index())
                                .enqueue(new Callback<getResult>() {
                                    @Override
                                    public void onResponse(Call<getResult> call, Response<getResult> response) {
                                        getResult item = response.body();
                                        Log.e(TAG, "onResponse message: " + item.toString());
                                        if(item.toString().equals("success")){
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.putExtra("user item", finalUser_item);
                                            startActivity(intent);
                                            finish();
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<getResult> call, Throwable t) {
                                        Log.e(TAG, "onFailure message: " + t.getMessage());
                                    }
                                });
                    }
                    else if(result.equals("fail")){ // 결과 값이 fail이면 로그인 실패 다이얼로그를 띄워준다.
//                        alertDialog("login fail");
                    }
                }

                @Override
                public void onFailure(Call<user_item[]> call, Throwable t) {
                    Log.e(TAG, "onFailure " + t.getMessage());
                }
            });
        }
        else{
            Log.e(TAG, "if false");
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        /**
         * 언어 설정
         */


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

    /**
     * 앱에 설정된 언어코드에 해당되는 언어로 모든 택스트를 바꿔주는 메서드
     */
    public void set_language_code(){
//        sharedPreference.saveLang_code(splashActivity.this, "ko");
        String language_code = sharedPreference.loadLang_code(splashActivity.this);
        Log.e(TAG, "언어코드: " + language_code);
        Locale locale = new Locale(language_code);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }


}