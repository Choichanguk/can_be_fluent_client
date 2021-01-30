package com.example.canbefluent.practice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
//import com.example.canbefluent.MLkit.FaceContourGraphic;
import com.example.canbefluent.frag_randomCall;
import com.example.canbefluent.ml_kit.FaceContourGraphic;
import com.example.canbefluent.MainActivity;
import com.example.canbefluent.R;
import com.example.canbefluent.app_rtc_sample.source.EglRenderer;
import com.example.canbefluent.databinding.ActivityOutgoingInvitation2Binding;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.tutorial.SimpleSdpObserver;
import com.example.canbefluent.utils.Constants;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.utils.sharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

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
//import org.webrtc.EglRenderer;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
//import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.canbefluent.app_rtc_sample.web_rtc.PeerConnectionClient.AUDIO_TRACK_ID;
import static com.example.canbefluent.app_rtc_sample.web_rtc.PeerConnectionClient.VIDEO_TRACK_ID;
import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_DISCONNECT;
import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;


public class OutgoingInvitationActivity2 extends AppCompatActivity {
    private static final String TAG = "OutgoingActivity";
    AudioManager audioManager;

    /**
     * web rtc 관련 변수
     */
    private static final int RC_CALL = 111;

    private Socket socket;
    private boolean isInitiator;
    private boolean isChannelReady;
    private boolean isStarted;

    MediaConstraints audioConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;

    private PeerConnection peerConnection;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;
    private VideoRenderer local_renderer;
    private VideoRenderer remote_renderer;


    private boolean iceConnected = false;

    MediaStream localStream;


    private String inviterToken = null;
    RetrofitClient retrofitClient;
    com.example.canbefluent.utils.sharedPreference sharedPreference;
    //    Button button;
    String meetingRoom = null;
    LinearLayout userInfo_view;



//    int device_height;
//    int device_width;

    String mask_type;

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    ActivityOutgoingInvitation2Binding binding;


    /**
     * 마스크 기능 관련 변수
     */
    EglRenderer.FrameListener frameListener;    // frame이 생성될 때 생성된 frame을 비트맵으로 읽어오는 리스너
    boolean isFrameListen = false;
    int bitmap_count = 0;

    FaceContourGraphic faceGraphic_mask;        // 선글라스를 그려주기 위한 클래스의 객체
    FaceContourGraphic faceGraphic_beard;       // 수염을 그려주기 위한 클래스의 객체
//    FaceContourGraphic faceGraphic_iron;        // 아이언맨 마스크를 그려주기 위한 클래스의 객체

    FaceDetector detector = null;               // 얼굴 탐지 모델 객체
    FaceDetectorOptions realTimeOpts = null;    // 모델 설정 옵션 객체

    int local_view_width;   // 내 얼굴이 나오는 뷰의 넓이
    int local_view_height;  // 내 얼굴이 나오는 뷰의 높이

    /**
     * 레이아웃 크기를 구해주
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        local_view_width = binding.frameLayout2.getWidth();
        local_view_height = binding.frameLayout2.getHeight();
        Log.e(TAG, "로컬 뷰 크기: ("+local_view_width+", " + local_view_height+")" );
    }

       @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_outgoing_invitation2);
        binding.maskOptionView.setVisibility(View.GONE);

        sharedPreference = new sharedPreference();
        inviterToken = sharedPreference.loadFCMToken(OutgoingInvitationActivity2.this);

           /**
            * 영상통화 스피커 설정(스피커 on, 에코 캔슬링)
            */
        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        setSpeakerphoneOn(true);    // 스피커 on
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);  // 에코 캔슬링 on


        final String meetingType = getIntent().getStringExtra("type");

        /**
         * real time face detector 모델 옵션 객체 생성
         */
        realTimeOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setMinFaceSize(0.9f)
                        .build();

        /**
         * face detector 모델 생성
         */
        detector = FaceDetection.getClient(realTimeOpts);

           faceGraphic_mask = new FaceContourGraphic(binding.graphicOverlay2, OutgoingInvitationActivity2.this, "sunglasses");
           faceGraphic_beard = new FaceContourGraphic(binding.graphicOverlay2, OutgoingInvitationActivity2.this, "beard");


           initializeSurfaceViews();


        frameListener = new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap var1) {

                Log.e(TAG, "count 성공");

                // faceGraphic 객체에 받아온 비트맵의 크기를 set 시킨다.
                if(bitmap_count == 0){
                    faceGraphic_mask.setScale(var1.getWidth(), var1.getHeight());
                    faceGraphic_beard.setScale(var1.getWidth(), var1.getHeight());
//                    faceGraphic_iron.setScale(var1.getWidth(), var1.getHeight());
                }

                Log.e(TAG, "비트맵 스케일 1.0 (" + var1.getWidth() + ", " + var1.getHeight() + ")");
                InputImage image = InputImage.fromBitmap(var1, 0);
                processImage(image, detector);

                bitmap_count++;
            }
        };


        /**
         * user_item에 담긴 유저 정보를 세팅해준다. (상대방 user item)
         * 프로필 이미지
         * first name
         */
        final user_item user_item = (com.example.canbefluent.items.user_item) getIntent().getSerializableExtra("user item");
        Log.e(TAG, "상대방 토큰: " + user_item.getToken());
        if(user_item != null){

            String url = MyApplication.server_url + "/profile_img/" + user_item.getProfile_img();;
            Glide.with(this)
                    .load(url)
                    .into(binding.profileImg);

            binding.userName.setText(user_item.getFirst_name());

        }

        /**
         * 영상통화 취소 버튼
         * 누르면 이전 화면으로 돌아간다.
         */

        binding.imageStopInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user_item != null){
                    cancelInvitation(user_item.getToken());
                    finish();
                }
            }
        });

        if(meetingType != null && user_item != null){
            /**
             * 시그널링 서버 접속 코드 작성
             */
            start();

            initiateMeeting(meetingType, user_item.getToken());
        }

        /**
         * 안경 씌우기 버튼
         */
        binding.sunglasses.setOnClickListener(v -> {

            // faceGraphic 객체에 내 얼굴이 나오는 뷰의 크기를 set 시킨다.
            faceGraphic_mask.setViewScale(local_view_width, local_view_height);

            mask_type = "sunglasses";
            if(detector == null){
                detector = FaceDetection.getClient(realTimeOpts);
            }
            else{
                if(!isFrameListen){
                    binding.surfaceView2.addFrameListener(frameListener, 1);
                    isFrameListen = true;
                }
            }
            socket.emit("mask", "sunglasses");

        });

        /**
         * 턱수염 씌우기 버튼
         */
        binding.beard.setOnClickListener(v -> {

            faceGraphic_beard.setViewScale(local_view_width, local_view_height);

            mask_type = "beard";
            if(detector == null){
                detector = FaceDetection.getClient(realTimeOpts);
            }
            else{
                if(!isFrameListen){
                    binding.surfaceView2.addFrameListener(frameListener, 1);
                    isFrameListen = true;
                }
            }
            socket.emit("mask", "beard");
        });

           /**
            * 아이언맨 마스트 버튼
            */
//           binding.ironman.setOnClickListener(v -> {
//               faceGraphic_iron.setViewScale(local_view_width, local_view_height);
//
//               mask_type = "iron";
//               if(detector == null){
//                   detector = FaceDetection.getClient(realTimeOpts);
//               }
//               else{
//                   if(!isFrameListen){
//                       binding.surfaceView2.addFrameListener(frameListener, 1);
//                       isFrameListen = true;
//                   }
//               }
//               socket.emit("mask", "iron");
//           });

        /**
         * 마스크 씌우기 취소 버튼
         */
        binding.cancelMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(detector != null){
                    if(isFrameListen){
                        Log.e(TAG, "프레임 리스너 제거");
                        binding.surfaceView2.removeFrameListener(frameListener);
                        detector.close();
                        detector = null;
                        isFrameListen = false;
                        binding.graphicOverlay2.clear();

                    }
                }



                socket.emit("mask", "cancel");
            }
        });

           /**
            * 음소거 버튼
            */
           binding.btnMute.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   binding.btnMute.setVisibility(View.GONE);
                   binding.btnUnmute.setVisibility(View.VISIBLE);
                   localAudioTrack.setEnabled(false);
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
                   localAudioTrack.setEnabled(true);
               }
           });

    }

    private void initiateMeeting(String meetingType, String receiverToken){
        try{

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);
            data.put("user name", MainActivity.user_item.getFirst_name());
            data.put("user profile", MainActivity.user_item.getProfile_img());

            meetingRoom = "random num"; // 방 이름
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);


            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put("to", receiverToken);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        } catch (Exception e){
            Toast.makeText(OutgoingInvitationActivity2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, final String type) {
        retrofitClient = new RetrofitClient();
        retrofitClient.service3.sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    if(type.equals(Constants.REMOTE_MSG_INVITATION)){
                        Log.e("sendRemoteMessage", "onResponse success");
                        Toast.makeText(OutgoingInvitationActivity2.this, "Invitation sent successfully", Toast.LENGTH_SHORT).show();
                    }
                    else if(type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                        Toast.makeText(OutgoingInvitationActivity2.this, "Invitation cancelled", Toast.LENGTH_SHORT).show();
//                        finish();
                    }
                }
                else{
                    Log.e("sendRemoteMessage", "onResponse fail");
                    Toast.makeText(OutgoingInvitationActivity2.this, "fcm onResponse fail", Toast.LENGTH_SHORT).show();
//                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("sendRemoteMessage", "onFailure");
                Toast.makeText(OutgoingInvitationActivity2.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken){
        try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                    binding.linearLayout8.setVisibility(View.GONE);

                    binding.maskOptionView.setVisibility(View.VISIBLE);
                    binding.btnMute.setVisibility(View.VISIBLE);

                    localVideoTrack.removeRenderer(local_renderer);
                    localVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView2));

//                    userInfo_view.setVisibility(View.GONE);
//                    Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show();
                }
                else if(type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
//                    Toast.makeText(context, "Invitation rejected", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }

    /**
     * 이미지 detection 시작 메서드
     * @param image
     * @param detector
     */
    public void processImage(InputImage image, FaceDetector detector){

        detector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                // Task completed successfully
                                Log.e("processImage", "이미지 처리 성공");

                                processFaceContourDetectionResult(faces);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                Log.e("processImage", "이미지 처리 실패");
                            }
                        });
    }

    /**
     * 모델이 탐지한 결과를 처리해주는 메서드 (Image overlay)
     * @param faces
     */
    private void processFaceContourDetectionResult(List<Face> faces) {
        Log.e(TAG, "탐지한 얼굴개수: " + faces.size());
        // Task completed successfully
        if (faces.size() == 0) {
            Log.e(TAG, "No face found");
            return;
        }
        binding.graphicOverlay2.clear();
//        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.get(0);

            if(mask_type.equals("sunglasses"))
            {
                binding.graphicOverlay2.add(faceGraphic_mask);
                faceGraphic_mask.updateFace(face);
            }
            else if(mask_type.equals("beard")){
                binding.graphicOverlay2.add(faceGraphic_beard);
                faceGraphic_beard.updateFace(face);
            }
//            else if(mask_type.equals("iron")){
//                binding.graphicOverlay2.add(faceGraphic_iron);
//                faceGraphic_iron.updateFace(face);
//            }

//        }
    }


    /**
     * web rtc 관련 메서드
     */
    @AfterPermissionGranted(RC_CALL)
    private void start() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {


//            initializeSurfaceViews();
//
//            updateVideoView();

            initializePeerConnectionFactory();

            createVideoTrackFromCameraAndShowIt();

            initializePeerConnections();

            connectToSignallingServer();

            startStreamingVideo();



        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }

    private void initializeSurfaceViews() {
        rootEglBase = EglBase.create();
        //binding.frameLayout.setPosition(0, 0, 100, 100);
        binding.surfaceView.init(rootEglBase.getEglBaseContext(), null);

        binding.surfaceView.setEnableHardwareScaler(true);
        binding.surfaceView.setMirror(true);
//        binding.surfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

//        binding.frameLayout.setPosition(0, 0, 100, 100);
        binding.surfaceView2.init(rootEglBase.getEglBaseContext(), null);
        binding.surfaceView2.setEnableHardwareScaler(true);
        binding.surfaceView2.setMirror(true);
        binding.surfaceView2.setZOrderMediaOverlay(true);
//        binding.surfaceView2.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
    }


    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    private void createVideoTrackFromCameraAndShowIt() {
        audioConstraints = new MediaConstraints();

        VideoCapturer videoCapturer = createVideoCapturer();
        VideoSource videoSource = factory.createVideoSource(videoCapturer);


//        binding.surfaceView.updateSurfaceSize();
        videoSource.adaptOutputFormat(490, 875, 10);

        // 높이, 넓이, fps
        videoCapturer.startCapture(875, 490, 10);

        local_renderer = new VideoRenderer(binding.surfaceView);

        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);

        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(true);
        localVideoTrack.addRenderer(local_renderer);
    }

    /**
     * RTCPeerConnection 초기화
     * RTCPeerConnection - 암호화 및 대역폭 관리 및 오디오 또는 비디오 연결
     */
    private void initializePeerConnections() {
        peerConnection = createPeerConnection(factory);
    }

    /**
     * localStream을 RTCPeerConnection에 추가해준다.
     */
    private void startStreamingVideo() {
        localStream = factory.createLocalMediaStream("ARDAMS");
        localStream.addTrack(localAudioTrack);
        localStream.addTrack(localVideoTrack);

        /**
         * Adds a MediaStream as a local source of audio or video
         */
        peerConnection.addStream(localStream);

        sendMessage("got user media");
    }

    private void connectToSignallingServer() {
        try {
            socket = IO.socket("http://canbefluent.xyz:3000/");

            socket.on(EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "connectToSignallingServer: connect");
                    socket.emit("create or join", "foo");
                }
            }).on("ipaddr", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "connectToSignallingServer: ipaddr");
                }
            }).on("created", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "connectToSignallingServer: created");
                    isInitiator = true;
                }
            }).on("full", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "connectToSignallingServer: full");
                }
            }).on("join", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "connectToSignallingServer: join");
                    Log.d(TAG, "connectToSignallingServer: Another peer made a request to join room");
                    Log.d(TAG, "connectToSignallingServer: This peer is the initiator of room");
                    isChannelReady = true;
                }
            }).on("joined", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "connectToSignallingServer: joined");
                    isChannelReady = true;
                }
            }).on("log", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    for (Object arg : args) {
                        Log.d(TAG, "connectToSignallingServer: " + String.valueOf(arg));
                    }
                }
            }).on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "connectToSignallingServer: got a message");
                }
            }).on("message", args -> {
                try {
                    if (args[0] instanceof String) {
                        String message = (String) args[0];
                        if (message.equals("got user media")) {
                            maybeStart();
                        }
                    } else {
                        JSONObject message = (JSONObject) args[0];
                        Log.d(TAG, "connectToSignallingServer: got message " + message);
                        if (message.getString("type").equals("offer")) {
                            Log.e(TAG, "message type: offer");
                            Log.d(TAG, "connectToSignallingServer: received an offer " + isInitiator + " " + isStarted);
                            if (!isInitiator && !isStarted) {
                                maybeStart();
                            }
                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("sdp")));
                            Log.e(TAG, "offer를 setRemoteDescription에 set");
                            doAnswer();
                        } else if (message.getString("type").equals("answer") && isStarted) {
                            Log.e(TAG, "message type: answer");
                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("sdp")));
                            Log.e(TAG, "answer를 setRemoteDescription에 set");
                        } else if (message.getString("type").equals("candidate") && isStarted) {
                            Log.e(TAG, "message type: candidate");
                            Log.d(TAG, "connectToSignallingServer: receiving candidates");
                            IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));

                            /**
                             * RTCPeerConnection을 사용하는 웹이나 앱이 신규 ICE candidate를 signaling 채널을 통해 원격 유저로부터 수신하게되면,
                             * RTCPeerConnection.addIceCandidate()를 호출해서 브라우저의 ICE 에이전트에게 새로 수신한 candidate를 전달
                             */
                            peerConnection.addIceCandidate(candidate);
                            Log.e(TAG, "candidate를 peerConnection에 추가");

                            iceConnected = true;
                        }
                        /*else if (message === 'bye' && isStarted) {
                        handleRemoteHangup();
                    }*/
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).on(EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "connectToSignallingServer: disconnect");
                }
            }).on("mask", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "마스크 메세지: " + args);

                }
            });
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }



    private void maybeStart() {
        Log.d(TAG, "maybeStart: " + isStarted + " " + isChannelReady);
        if (!isStarted && isChannelReady) {
            isStarted = true;

            Log.e(TAG, "isInitiator: " + isInitiator);
            if (isInitiator) {
                doCall();
            }
        }
    }

    /**
     * Sending offer to peer
     * offer를 생성 후 LocalDescription에 set 후 시그널링 서버로 offer 전달
     */
    private void doCall() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.e(TAG, "offer 생성");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                Log.e(TAG, "offer를 LocalDescription에 set");
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    sendMessage(message);
                    Log.e(TAG, "offer를 시그널링 서버로 전달");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    /**
     * Sending answer to peer
     * answer을 생성 후 LocalDescription에 set 후 시그널링 서버로 answer 전달
     */
    private void doAnswer() {
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.e(TAG, "answer 생성");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                Log.e(TAG, "answer를 LocalDescription에 set");
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    sendMessage(message);
                    Log.e(TAG, "answer를 시그널링 서버로 전달");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new MediaConstraints());
    }

    private void sendMessage(Object message) {
        socket.emit("message", message);
    }





    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: ");
            }


            /**
             * connection 상태가 변화되었을 때(상대가 연결을 끊었을 때 등) 실행되는 이벤트
             * 연결 끊겼을때 실행되야 할 코드 짜주면 됨
             * @param iceConnectionState
             */
            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.e(TAG, "ice connection state: " + iceConnectionState);
                Log.d(TAG, "onIceConnectionChange: ");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: ");
                JSONObject message = new JSONObject();

                try {
                    message.put("type", "candidate");
                    message.put("label", iceCandidate.sdpMLineIndex);
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);

                    Log.d(TAG, "onIceCandidate: sending candidate " + message);
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            /**
             * The event is sent immediately after the call setRemoteDescription()
             * @param mediaStream
             */
            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());
                Log.e(TAG, "상대방의 stream을 받아옴");
                remoteVideoTrack = mediaStream.videoTracks.get(0);

//                remoteVideoTrack.dispose();
//                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
//                Log.e(TAG, "onAddStream remote video track: " + remoteVideoTrack);
//                Log.e(TAG, "onAddStream remote audio track: " + remoteAudioTrack);
//                remoteAudioTrack.setEnabled(true);
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView));

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;

    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }


        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    @Override
    protected void onDestroy() {
        if (socket != null) {
            sendMessage("bye");
            socket.disconnect();
        }
        peerConnection.removeStream(localStream);
        peerConnection.close();

        super.onDestroy();
    }

    /** Sets the speaker phone mode. */
    private void setSpeakerphoneOn(boolean on) {
        boolean wasOn = audioManager.isSpeakerphoneOn();
        if (wasOn == on) {
            return;
        }
        audioManager.setSpeakerphoneOn(on);
        Log.e(TAG, "setSpeakerphoneOn");
    }
}