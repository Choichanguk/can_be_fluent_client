package com.example.canbefluent.retrofit;

import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.PostResult;
import com.example.canbefluent.pojoClass.getCountryNameResult;
import com.example.canbefluent.pojoClass.getRegisterUserResult;
import com.example.canbefluent.pojoClass.imgUploadResult;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface RetrofitInterface {
    //@GET > HTTP Method GET/POST/PUT/DELETE/HEAD 중 무슨 작업인지 표시
    //post > 전체 URI에서 URL을 제외한 End Point(URI) / 등록한 baseUrl의 끝에 연결되는 End Point
    //{post} > @Path()의 괄호 안에 들어오는 값이 {}안으로 들어감

    @GET("index.php/")  // 모든 유저의 id값만 받아오는 메서드(id 중복체크를 위해)

    //Call<PostResult> > Call은 응답이 왔을 때  Callback으로 불려질 타입 / PostResult > 요청 GET에 대한 응답데이터를 받아서 DTO 객체화할 클래스 타입 지정
    //getPosts > 메소드 명. 자유롭게 설정 가능, 통신에 영향x
    //@Path("post") String post > 매개변수. 매개변수 post가 @Path("post")를 보고 @GET 내부 {post}에 대입
    Call<ArrayList<PostResult>> getPosts();

    @GET("get_user_info.php/")  // 모든 유저의 데이터를 받아오는 메서드(로그인 후 보여질 유저 목록을 만들기 위해)
    Call<ArrayList<user_item>> get_allUserInfo();

    @GET("rest/v2/all/")
    Call<ArrayList<getCountryNameResult>> getCountryName(@Query("name") String type);

    @GET("login_process.php/")
    Call<user_item[]> login_process(@Query("user_id") String user_id, @Query("user_pw") String user_pw);

    @GET("update_address.php/")
    Call<ArrayList<user_item>> get_nearUserInfo(@Query("latitude") double latitude, @Query("longitude") double longitude);

//    @Multipart
//    @POST("upload_img.php/")
//    Call<imgUploadResult> uploadImage(@Part MultipartBody.Part File);

    @Multipart
    @POST("register_user.php/")
    Call<getRegisterUserResult> register_user(@Part MultipartBody.Part File, @PartMap HashMap<String, RequestBody> fields);
//    @Path("post") String post



}
