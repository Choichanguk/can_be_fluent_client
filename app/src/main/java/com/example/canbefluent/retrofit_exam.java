package com.example.canbefluent;

import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class retrofit_exam {

//    public class RetrofitClient{
//        //Retrofit.Build를 통해 Retrofit 인스턴스 생성
//        Retrofit retrofit = new Retrofit.Builder()
//
//
//                .baseUrl("") //baseUrl 등록(반드시 '/'로 마무리 해야함)
//                .addConverterFactory(GsonConverterFactory.create()) // JSON을 변환해줄 Gson 변환기 등록
//                .build();
//
//        // RetrofitService.class 무슨 의미인지 모르겠음 / retrofit 인스턴스로 인터페이스 객체 구현
//        public RetrofitInterface service = retrofit.create(RetrofitInterface.class);
//    }

//    RetrofitClient retrofitClient = new RetrofitClient();


    //Interface 객체 구현. retrofit을 통한 객체 구현, 추상 메소드 중 사용할 메소드 Call 객체에 등록
//    public interface RetrofitInterface {
//        //@GET > HTTP Method GET/POST/PUT/DELETE/HEAD 중 무슨 작업인지 표시
//        //post > 전체 URI에서 URL을 제외한 End Point(URI) / 등록한 baseUrl의 끝에 연결되는 End Point
//        //{post} > @Path()의 괄호 안에 들어오는 값이 {}안으로 들어감
//        @GET("post/{post}")
//
//        //Call<PostResult> > Call은 응답이 왔을 때  Callback으로 불려질 타입 / PostResult > 요청 GET에 대한 응답데이터를 받아서 DTO 객체화할 클래스 타입 지정
//        //getPosts > 메소드 명. 자유롭게 설정 가능, 통신에 영향x
//        //@Path("post") String post > 매개변수. 매개변수 post가 @Path("post")를 보고 @GET 내부 {post}에 대입
//        Call<PostResult> getPosts(@Path("post") String post);
//    }

    // DTO 모델 - PostResult Class 선언


}
