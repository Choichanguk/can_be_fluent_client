package com.example.canbefluent;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitInterface {
    //@GET > HTTP Method GET/POST/PUT/DELETE/HEAD 중 무슨 작업인지 표시
    //post > 전체 URI에서 URL을 제외한 End Point(URI) / 등록한 baseUrl의 끝에 연결되는 End Point
    //{post} > @Path()의 괄호 안에 들어오는 값이 {}안으로 들어감
    @GET("index.php/")

    //Call<PostResult> > Call은 응답이 왔을 때  Callback으로 불려질 타입 / PostResult > 요청 GET에 대한 응답데이터를 받아서 DTO 객체화할 클래스 타입 지정
    //getPosts > 메소드 명. 자유롭게 설정 가능, 통신에 영향x
    //@Path("post") String post > 매개변수. 매개변수 post가 @Path("post")를 보고 @GET 내부 {post}에 대입
    Call<ArrayList<PostResult>> getPosts();

    @GET("rest/v2/all/")
    Call<ArrayList<getCountryNameResult>> getCountryName(@Query("name") String type);

//    @Path("post") String post



}
