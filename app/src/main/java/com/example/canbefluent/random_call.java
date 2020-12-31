package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.canbefluent.databinding.ActivityRandomCallBinding;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.getResult;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.tutorial.SimpleSdpObserver;
import com.example.canbefluent.utils.Constants;
import com.example.canbefluent.utils.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.canbefluent.app_rtc_sample.web_rtc.PeerConnectionClient.AUDIO_TRACK_ID;
import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_DISCONNECT;
import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class random_call extends AppCompatActivity {
    private static final String TAG = "random_call";

    ActivityRandomCallBinding binding;

    String url = MyApplication.server_url + "/profile_img/";

    user_item user_item;

    RetrofitClient retrofitClient = new RetrofitClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_random_call);

        user_item = (user_item) getIntent().getSerializableExtra("user item");

        /**
         * 통화 상대방 정보 세팅
         */
        binding.name.setText(user_item.getFirst_name());

        Glide.with(this)
                .load(url + user_item.getProfile_img())
                .into(binding.profileImg);

        /**
         * 음소거 버튼
         */
        binding.btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnMute.setVisibility(View.GONE);
                binding.btnUnmute.setVisibility(View.VISIBLE);
                frag_randomCall.ms.mute();
            }
        });

        /**
         * 음소거 해제 버튼
         */
       binding.btnUnmute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnMute.setVisibility(View.VISIBLE);
                binding.btnUnmute.setVisibility(View.GONE);
                frag_randomCall.ms.unMute();
            }
        });

        /**
         * 통화 종료 버튼
         */
        binding.btnFinishCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 통화 기록 저장하는 코드 넣어야 함
                 */

                save_and_finish_call(); // 통화 기록 저장한 후 통화 종료시키는 메서드

            }
        });

        /**
         * 통화 시간 표시 코드
         */
        binding.chronometer.setBase(SystemClock.elapsedRealtime());
        binding.chronometer.start();

        // 통화시간 (초)
//        int time = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase())/1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if(type.equals("finish call")){
                MainActivity.isSearching = false;
                finish();
            }
        }
    };



    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(random_call.this).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter("random call")
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(random_call.this).unregisterReceiver(
                invitationResponseReceiver
        );
    }

    /**
     * 통화 기록을 저장하고 통화를 종료시키는 메소드
     */
    private void save_and_finish_call(){
        int call_time = (int) ((SystemClock.elapsedRealtime() - binding.chronometer.getBase())/1000);
        // 파리미터 - (내 idx, 상대 idx)
        Log.e(TAG, "내 idx: " + MainActivity.user_item.getUser_index() + "/ 상대 idx: " + user_item.getUser_index() + "/ time: " + call_time + "초");
        retrofitClient.service.save_call_log(MainActivity.user_item.getUser_index(), user_item.getUser_index(), call_time)
                .enqueue(new Callback<getResult>() {
                    @Override
                    public void onResponse(Call<getResult> call, Response<getResult> response) {
                        if(response.isSuccessful()){
                            Log.e(TAG, "isSuccessful msg: " + response.body().toString());
                            frag_randomCall.ms.finishCAll();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<getResult> call, Throwable t) {
                        Log.e(TAG, "onFailure msg: " + t.getMessage());
                    }
                });
    }

}