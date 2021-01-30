package com.example.canbefluent.utils;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.example.canbefluent.R;
import com.example.canbefluent.items.language_code_item;
import com.example.canbefluent.items.user_item;

import java.util.ArrayList;
import java.util.HashMap;

public class MyApplication extends android.app.Application{
    /**
     * 앱이 사용할 전역변수 설정
     */
    public static String server_url = "http://13.124.159.44";
    public static String socket_server_url = "13.124.159.44";
    public static HashMap<String,String> lang_code_map = new HashMap<String,String>(){};
    public static ArrayList<language_code_item> list = new ArrayList<>();
    public static ArrayList<user_item> user_list = new ArrayList<>();


    /**
     * Called when the application is starting,
     * before any activity, service, or receiver objects (excluding content providers) have been created
     */
    @Override
    public void onCreate() {
        super.onCreate();
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
