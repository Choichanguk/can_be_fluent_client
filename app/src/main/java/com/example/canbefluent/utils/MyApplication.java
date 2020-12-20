package com.example.canbefluent.utils;

import com.example.canbefluent.R;
import com.example.canbefluent.items.language_code_item;

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


    /**
     * Called when the application is starting,
     * before any activity, service, or receiver objects (excluding content providers) have been created
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

}
