package com.example.canbefluent;

import com.google.gson.annotations.SerializedName;

public class PostResult {

    @SerializedName("user_id")
    private String userID;

    @SerializedName("user_pw")
    private String userPW;

    // @SerializedName으로 일치시켜 주지않을 경우엔 클래스 변수명이 일치해야함
//    private String title;

    // @SerializedName()로 변수명을 입치시켜주면 클래스 변수명이 달라도 알아서 매핑시켜줌
//    @SerializedName("name")
//    private String name;

    // toString()을 Override 해주지 않으면 객체 주소값을 출력함
    @Override
    public String toString() {
        return  userID;

    }
}
