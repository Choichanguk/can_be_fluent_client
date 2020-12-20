package com.example.canbefluent.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class sharedPreference {
    private static final String PREFERENCES_NAME = "유저 로그인 정보";

    private static SharedPreferences getPreferences(Context context){
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }



    public static void saveUserId(Context context, String ID){
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", ID);
        editor.apply();
    }

    public static void saveUserPw(Context context, String PW){
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_pw", PW);
        editor.apply();
    }

    public static void saveLoginStatus(Context context, boolean islogin){
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_login", islogin);
        editor.apply();
    }

    public static void saveUserIndex(Context context, String index){
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_index", index);
        editor.apply();
    }

    public static void saveFCMToken(Context context, String token){
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public static void saveLang_code(Context context, String lang_code){
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lang_code", lang_code);
        editor.apply();
    }

    public static void saveLangCode(Context context, String lang_code){
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lang trans code", lang_code);
        editor.apply();
    }

    public static String loadUserId(Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        String user_id = sharedPreferences.getString("user_id", null);

        if(user_id == null){
            user_id = "";
        }
        return user_id;
    }

    public static String loadUserPw(Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        String user_pw = sharedPreferences.getString("user_pw", null);

        if(user_pw == null){
            user_pw = "";
        }
        return user_pw;
    }

    public static boolean loadLoginStatus(Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        Boolean is_login = sharedPreferences.getBoolean("is_login", false);
        return is_login;
    }

    public static String loadUserIndex(Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        String user_index = sharedPreferences.getString("user_index", null);

        if(user_index == null){
            user_index = "";
        }
        return user_index;
    }

    public static String loadFCMToken(Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        String token = sharedPreferences.getString("token", null);

        if(token == null){
            token = "";
        }
        return token;
    }

    public static String loadLang_code(Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        String lang_code = sharedPreferences.getString("lang_code", null);

        if(lang_code == null){
            lang_code = "ko";  // 기본 언어 코드 값
        }
        return lang_code;
    }

    public static String loadLangCode(Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        String lang_code = sharedPreferences.getString("lang trans code", null);

        if(lang_code == null){
            lang_code = "ko";
        }
        return lang_code;
    }
}
