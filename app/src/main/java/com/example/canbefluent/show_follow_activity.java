package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canbefluent.adapter.followerAdapter;
import com.example.canbefluent.adapter.followingAdapter;
import com.example.canbefluent.items.follow_item;
import com.example.canbefluent.pojoClass.getResult;
import com.example.canbefluent.retrofit.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class show_follow_activity extends AppCompatActivity {
    private static final String TAG = "show_follow_activity";

    LinearLayout btn_following, btn_follower;
    RecyclerView following_recycle, follower_recycle;
    followingAdapter followingAdapter;
    followerAdapter followerAdapter;
    ImageButton btn_back;

    TextView following_text, follower_text, following_num, follower_num;

    RetrofitClient retrofitClient = new RetrofitClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_follow_activity);

        btn_following = findViewById(R.id.btn_following);
        btn_follower = findViewById(R.id.btn_follower);

        following_recycle = findViewById(R.id.following_recycle);
        follower_recycle = findViewById(R.id.follower_recycle);

        following_text = findViewById(R.id.following_text);
        follower_text = findViewById(R.id.follower_text);
        following_num = findViewById(R.id.following_num);
        follower_num = findViewById(R.id.follower_num);


        /**
         * 팔로우 기능
         */
        // 1. 서버로부터 내가 follow하는 유저의 리스트를 가져온다.
        retrofitClient.service.get_following(MainActivity.user_item.getUser_index())
                .enqueue(new Callback<ArrayList<follow_item>>() {
                    @Override
                    public void onResponse(Call<ArrayList<follow_item>> call, Response<ArrayList<follow_item>> response) {
                        ArrayList<follow_item> list = response.body();
                        Log.e(TAG, "following 사이즈: " + list.size());


                        following_recycle = findViewById(R.id.following_recycle);
                        following_recycle.setLayoutManager(new LinearLayoutManager(show_follow_activity.this));

                        followingAdapter = new followingAdapter(list, show_follow_activity.this);
                        following_recycle.setAdapter(followingAdapter);
                    }

                    @Override
                    public void onFailure(Call<ArrayList<follow_item>> call, Throwable t) {
                        Log.e(TAG, "onFailure 메시지: " + t.getMessage());
                    }
                });


        // 2. 서버로부터 나를 follow하는 유저의 리스트를 가져온다.
        retrofitClient.service.get_follower(MainActivity.user_item.getUser_index())
                .enqueue(new Callback<ArrayList<follow_item>>() {
                    @Override
                    public void onResponse(Call<ArrayList<follow_item>> call, Response<ArrayList<follow_item>> response) {
                        final ArrayList<follow_item> list = response.body();
                        Log.e(TAG, "followed 사이즈: " + list.size());

                        follower_recycle = findViewById(R.id.follower_recycle);
                        follower_recycle.setLayoutManager(new LinearLayoutManager(show_follow_activity.this));

                        followerAdapter = new followerAdapter(list, show_follow_activity.this);
                        followerAdapter.setOnItemClickListener(new followerAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, final View itemView, final int position) {
                                switch(v.getId()){
                                    // 팔로우 신청 버튼
                                  case  R.id.btn_follow:

                                        retrofitClient.service.follow_unfollow(MainActivity.user_item.getUser_index(), list.get(position).getUser_index(), "follow")
                                                .enqueue(new Callback<getResult>() {
                                                    @Override
                                                    public void onResponse(Call<getResult> call, Response<getResult> response) {
                                                        getResult result = response.body();
                                                        if(result.toString().equals("success")){
                                                            // 팔로우 버튼 변경 -> 언팔로우 버튼
                                                            itemView.findViewById(R.id.btn_unfollow).setVisibility(View.VISIBLE);
                                                            itemView.findViewById(R.id.btn_follow).setVisibility(View.GONE);

                                                            // FCM 메시지 전송
                                                            initiateFollowFCM(list.get(position).getToken());
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<getResult> call, Throwable t) {
                                                        Log.e(TAG, "onFailure message: " + t.getMessage());
                                                    }
                                                });
                                     break;

                                     // 팔로우 취소 버튼
                                  case  R.id.btn_unfollow:

                                      retrofitClient.service.follow_unfollow(MainActivity.user_item.getUser_index(), list.get(position).getUser_index(), "unfollow")
                                              .enqueue(new Callback<getResult>() {
                                                  @Override
                                                  public void onResponse(Call<getResult> call, Response<getResult> response) {
                                                      getResult result = response.body();
                                                      if(result.toString().equals("success")){
                                                          // 언팔로우 버튼 변경 -> 팔로우 버튼
                                                          itemView.findViewById(R.id.btn_unfollow).setVisibility(View.GONE);
                                                          itemView.findViewById(R.id.btn_follow).setVisibility(View.VISIBLE);
                                                      }
                                                  }

                                                  @Override
                                                  public void onFailure(Call<getResult> call, Throwable t) {
                                                      Log.e(TAG, "onFailure message: " + t.getMessage());
                                                  }
                                              });
                                      break;
                                  default:
                                    break;
                                }
                            }
                        });
                        follower_recycle.setAdapter(followerAdapter);
                    }

                    @Override
                    public void onFailure(Call<ArrayList<follow_item>> call, Throwable t) {
                        Log.e(TAG, "onFailure 메시지: " + t.getMessage());
                    }
                });



        /**
         * 팔로잉 리스트 버튼
         */
        btn_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                follower_recycle.setVisibility(View.GONE);
                following_recycle.setVisibility(View.VISIBLE);

                follower_text.setTextColor(Color.parseColor("#000000"));
                follower_num.setTextColor(Color.parseColor("#000000"));
                following_text.setTextColor(Color.parseColor("#36A2A6"));
                following_num.setTextColor(Color.parseColor("#36A2A6"));
            }
        });

        /**
         * 팔로워 리스트 버튼
         */
        btn_follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                follower_recycle.setVisibility(View.VISIBLE);
                following_recycle.setVisibility(View.GONE);

                follower_text.setTextColor(Color.parseColor("#36A2A6"));
                follower_num.setTextColor(Color.parseColor("#36A2A6"));
                following_text.setTextColor(Color.parseColor("#000000"));
                following_num.setTextColor(Color.parseColor("#000000"));
            }
        });

        // 뒤로가기 버튼
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    /**
     * FCM 메시지 보내기 위한 파라미터 만드는 메서드
     */
    private void initiateFollowFCM(String receiverToken){
        try{

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, "follow");
//            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
//            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);
            data.put("user name", MainActivity.user_item.getFirst_name());
            data.put("user profile", MainActivity.user_item.getProfile_img());

//            meetingRoom = "random num";
//            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);


            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put("to", receiverToken);

            Log.e("FCM", "data: " + data);
            Log.e("FCM", "body: " + body);

            sendRemoteMessage(body.toString());

        }catch (Exception e){
            Toast.makeText(show_follow_activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * FCM 메시지 보내는 메서드
     * @param remoteMessageBody
     */
    private void sendRemoteMessage(String remoteMessageBody) {
        retrofitClient = new RetrofitClient();
        retrofitClient.service3.sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    Log.e("FCM", "onResponse success");
                }
                else{
                    Log.e("FCM", "onResponse fail");
                    Toast.makeText(show_follow_activity.this, response.body(), Toast.LENGTH_SHORT).show();
//                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("FCM", "onFailure");
                Toast.makeText(show_follow_activity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//                finish();
            }
        });
    }
}