package com.example.canbefluent;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    //Retrofit.Build를 통해 Retrofit 인스턴스 생성
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://13.125.7.202/") //baseUrl 등록(반드시 '/'로 마무리 해야함)
            .addConverterFactory(GsonConverterFactory.create()) // JSON을 변환해줄 Gson 변환기 등록
            .build();

    // RetrofitService.class 무슨 의미인지 모르겠음 / retrofit 인스턴스로 인터페이스 객체 구현
    public RetrofitInterface service = retrofit.create(RetrofitInterface.class);
}
