package com.example.canbefluent.retrofit;

import com.example.canbefluent.MyApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private static OkHttpClient.Builder httpClientBuilder;

    private static OkHttpClient provideOkHttpClient(HttpLoggingInterceptor interceptor){
        httpClientBuilder =new OkHttpClient.Builder();

        httpClientBuilder.addInterceptor(interceptor);
        httpClientBuilder.connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30,TimeUnit.SECONDS)
                .writeTimeout(30,TimeUnit.SECONDS)
                .build();
        return httpClientBuilder.build();
    }

    private static HttpLoggingInterceptor provideLoggingInterceptor(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    //Retrofit.Build를 통해 Retrofit 인스턴스 생성
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MyApplication.server_url) //baseUrl 등록(반드시 '/'로 마무리 해야함)
            .addConverterFactory(GsonConverterFactory.create(gson)) // JSON을 변환해줄 Gson 변환기 등록
            .client(provideOkHttpClient(provideLoggingInterceptor()))
            .build();

    Retrofit retrofit2 = new Retrofit.Builder()
            .baseUrl("https://restcountries.eu/") //baseUrl 등록(반드시 '/'로 마무리 해야함)
            .addConverterFactory(GsonConverterFactory.create(gson)) // JSON을 변환해줄 Gson 변환기 등록
            .build();

    Retrofit retrofit3 = new Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/fcm/") //baseUrl 등록(반드시 '/'로 마무리 해야함)
            .addConverterFactory(ScalarsConverterFactory.create()) // JSON을 변환해줄 Gson 변환기 등록
            .build();

    // RetrofitService.class 무슨 의미인지 모르겠음 / retrofit 인스턴스로 인터페이스 객체 구현
    public RetrofitInterface service = retrofit.create(RetrofitInterface.class);
    public RetrofitInterface service2 = retrofit2.create(RetrofitInterface.class);
    public RetrofitInterface service3 = retrofit3.create(RetrofitInterface.class);
}
