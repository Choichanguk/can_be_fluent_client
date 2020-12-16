package com.example.canbefluent.retrofit;

import com.example.canbefluent.items.follow_item;
import com.example.canbefluent.items.img_item;
import com.example.canbefluent.items.msg_item;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.items.visitor_item;
import com.example.canbefluent.pojoClass.PostResult;
import com.example.canbefluent.pojoClass.getAudioFile;
import com.example.canbefluent.pojoClass.getChatList;
import com.example.canbefluent.pojoClass.getImgList;
import com.example.canbefluent.pojoClass.getLanguageNameResult;
import com.example.canbefluent.pojoClass.getMsgList;
import com.example.canbefluent.pojoClass.getRegisterUserResult;
import com.example.canbefluent.pojoClass.getResult;
import com.example.canbefluent.pojoClass.getRoomList;
import com.example.canbefluent.pojoClass.getStatus;
import com.example.canbefluent.pojoClass.imgUploadResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
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
    Call<ArrayList<user_item>> get_allUserInfo(@Query("user_index") String user_index);

    @GET("get_visitor_info.php/")  // 모든 방문자의 데이터를 받아오는 메서드
    Call<ArrayList<visitor_item>> get_visitor_Info(@Query("user_index") String user_index);

    @GET("update_login_time.php/")  // 로그인 시간을 업데이트 하는 메서드
    Call<getResult> update_login_time(@Query("user_index") String user_index);

    @GET("get_following.php/")  // following하는 유저 리스트를 가져오는 메서드
    Call<ArrayList<follow_item>> get_following(@Query("user_index") String user_index);

    @GET("get_follower.php/")  // 나를 follow하는 유저 리스트를 가져오는 메서드
    Call<ArrayList<follow_item>> get_follower(@Query("user_index") String user_index);

    @GET("follow_unfollow.php/")  // 서버로 팔로우 신청 or 팔로우 취소 요청하는 메서드
    Call<getResult> follow_unfollow(@Query("follower_index") String follower_index, @Query("followered_index") String followered_index, @Query("type") String type);

//    @GET("get_user_info.php/")  // 모든 유저의 데이터를 받아오는 메서드(로그인 후 보여질 유저 목록을 만들기 위해)
//    Call<ArrayList<user_item>> get_allUserInfo

    @GET("languages.php/")
    Call<ArrayList<getLanguageNameResult>> getLanguageName();

    // 일반 아이디로 로그인 할때
    @GET("login_process.php/")
    Call<user_item[]> login_process(@Query("user_id") String user_id, @Query("user_pw") String user_pw);

    // 구글 아이디로 로그인 할 때
    @GET("login_process.php/")
    Call<user_item[]> login_process(@Query("UID") String UID);

    // 서버로부터 선택한 채팅방 정보를 가져올 때
    @GET("chat_room.php/")
    Call<ArrayList<getRoomList>> get_room_info(@Query("sender_index") String sender_index, @Query("receiver_index") String receiver_index);

    // 서버로 프로필 방문 체크 확인할 때
    @GET("check_visitor.php/")
    Call<getResult> check_visit(@Query("visitor_index") String visitor_index, @Query("visited_index") String visited_index);


    // 서버로부터 채팅방 목록 가져올 때
    @GET("room_list.php/")
    Call<ArrayList<getRoomList>> get_room_list(@Query("user_index") String user_index);

    // 서버로부터 해당 인덱스의 채팅방 정보 가져올 때
    @GET("room_list.php/")
    Call<ArrayList<getRoomList>> get_room_info_from_noti(@Query("room_index") String room_index, @Query("user_index") String user_index);

    // 서버로부터 채팅방 목록 가져올 때
    @GET("msg_list.php/")
    Call<ArrayList<msg_item>> get_msg_list(@Query("room_index") String room_index, @Query("user_index") String user_index);


    @GET("update_address.php/")
    Call<ArrayList<user_item>> get_nearUserInfo(@Query("latitude") double latitude, @Query("longitude") double longitude, @Query("user_index") String user_index);

    @GET("get_images.php/")
    Call<ArrayList<img_item>> get_images(@Query("user_index") String user_index);

    @Multipart
    @POST("upload_img.php/")
    Call<imgUploadResult> uploadImage(@Part MultipartBody.Part File);

    @Multipart
    @POST("register_user.php/")
    Call<getRegisterUserResult> register_user(@Part MultipartBody.Part File, @PartMap HashMap<String, RequestBody> fields);

    @GET("update_token.php/")
    Call<getResult> update_token(@Query("user_id") String user_id, @Query("token") String token);

    @GET("update_intro.php/")
    Call<getResult> update_intro(@Query("user_id") String user_id, @Query("intro") String token);

    @GET("update_delete_profile_image.php/")
    Call<ArrayList<img_item>> update_delete_profile_image(@Query("user_index") String user_index, @Query("image_index") String image_index, @Query("image_name") String image_name, @Query("type") String type);


    @Multipart
    @POST("chat_img_upload.php/")
    Call<ArrayList<getImgList>> uploadMultiple(@Part("room_index") RequestBody room_index, @Part("user_index") RequestBody user_index, @Part("status") RequestBody status, @Part("size") RequestBody size, @Part("time") RequestBody time, @Part List<MultipartBody.Part> parts);

    @Multipart
    @POST("upload_img.php/")
    Call<ArrayList<getImgList>> uploadSingle(@Part("user_index") RequestBody user_index, @Part("size") RequestBody size, @Part("time") RequestBody time, @Part List<MultipartBody.Part> parts);

    @Multipart
    @POST("upload_img2.php/")
    Call<ArrayList<img_item>> uploadSingle2(@Part("user_index") RequestBody user_index, @Part("size") RequestBody size, @Part("time") RequestBody time, @Part List<MultipartBody.Part> parts);


    @Multipart
    @POST("audio_file_upload.php/")
    Call<getStatus> uploadAudio(@Part MultipartBody.Part File, @Part("room_index") RequestBody room_index, @Part("user_index") RequestBody user_index, @Part("status") RequestBody status, @Part("time") RequestBody time, @Part("play_time") RequestBody play_time);

    //영상통화 시 fcm 메시지를 보내는 메서드
    @POST("send")
    Call<String> sendRemoteMessage(
            @HeaderMap HashMap<String, String> headers,
            @Body String remoteBody
    );
}
