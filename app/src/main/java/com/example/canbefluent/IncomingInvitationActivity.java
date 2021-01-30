package com.example.canbefluent;

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
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.canbefluent.app_rtc_sample.source.EglRenderer;
import com.example.canbefluent.databinding.ActivityIncomingInvitationBinding;
import com.example.canbefluent.ml_kit.FaceContourGraphic;
import com.example.canbefluent.practice.OutgoingInvitationActivity2;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.tutorial.SimpleSdpObserver;
import com.example.canbefluent.utils.Constants;
import com.example.canbefluent.utils.MyApplication;
//import com.facebook.soloader.SoLoader;

//import org.jitsi.meet.sdk.JitsiMeetActivity;
//import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
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
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
//import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
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

import com.example.canbefluent.app_rtc_sample.source.SurfaceViewRenderer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

public class IncomingInvitationActivity extends AppCompatActivity {
    private static final String TAG = "IncomingActivity";
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
    MediaConstraints videoConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;

    private PeerConnection peerConnection;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrackFromCamera;

    private VideoRenderer local_renderer;
    private VideoRenderer remote_renderer;

    MediaStream mediaStream;

//    private SurfaceViewRenderer surfaceView_local;
//    private SurfaceViewRenderer surfaceView_remote;

    RetrofitClient retrofitClient;
//    LinearLayout userInfo_view;

    ActivityIncomingInvitationBinding binding;

    /**
     * 마스크 기능 관련 변수 & 메서드
     */
    EglRenderer.FrameListener frameListener_remote;
    EglRenderer.FrameListener frameListener_local;
    boolean isFrameListen = false;
    int bitmap_count = 0;

    FaceContourGraphic faceGraphic_mask;
    FaceContourGraphic faceGraphic_beard;

    int local_view_width;
    int local_view_height;
    int remote_view_width;
    int remote_view_height;

    String remote_mask_type;
    String local_mask_type;

    FaceDetector detector;

    FaceDetectorOptions realTimeOpts;

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    /**
     * 레이아웃 크기를 구해주
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        remote_view_width = binding.frameLayout.getWidth();
        remote_view_height = binding.frameLayout.getHeight();
        local_view_width = binding.frame2.getWidth();
        local_view_height = binding.frame2.getHeight();
        Log.e(TAG, "리모트뷰 크기: ("+remote_view_width+", " + remote_view_height+")" );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_incoming_invitation);
        binding.maskOptionView.setVisibility(View.GONE);





        /**
         * 영상 통화 시 스피커 설정 (스피커 on, 에코 캔슬링)
         */
        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        setSpeakerphoneOn(true);
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);


        /**
         * 유저 정보 세팅
         */
        String profile_url = MyApplication.server_url + "/profile_img/" + getIntent().getStringExtra("user profile");
        String userName = getIntent().getStringExtra("user name");
        Log.e("IncomingInvitation", "profile_url: " + profile_url);
        Glide.with(this)
                .load(profile_url)
                .into(binding.profileImg);

        binding.userName.setText(userName);

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

        initializeSurfaceViews(); // surfaceView 초기화

        /**
         * 탐지된 얼굴 위치에 마스크를 그려주는 클래스의 객체생성
         */
        faceGraphic_mask = new FaceContourGraphic(binding.graphicOverlay, IncomingInvitationActivity.this, "sunglasses");
        faceGraphic_beard = new FaceContourGraphic(binding.graphicOverlay, IncomingInvitationActivity.this, "beard");

        /**
         * surfaceViewRenderer 프레임을 가져올 수 있는 리스너
         */
        frameListener_remote = new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap var1) {

                if(bitmap_count == 0){
                    faceGraphic_mask.setScale(var1.getWidth(), var1.getHeight());
                    faceGraphic_beard.setScale(var1.getWidth(), var1.getHeight());
                }

                if(bitmap_count % 4 == 0){
                    Log.e(TAG, "비트맵 스케일 1.0 (" + var1.getWidth() + ", " + var1.getHeight() + ")");
//                    Bitmap resized_bitmap = Bitmap.createScaledBitmap(var1, (int) (var1.getWidth() * 0.25), (int) (var1.getHeight() * 0.25), false);
                    InputImage image = InputImage.fromBitmap(var1, 0);
                    processImage(image, detector);
                }

                bitmap_count++;
            }
        };

        // video rendering 시작
        start();

        /**
         * 통화 요청 수락 버튼
         */
//        ImageView imageAcceptInvitation = findViewById(R.id.imageAcceptInvitation);

        binding.imageAcceptInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));

                binding.linearLayout8.setVisibility(View.GONE);
                binding.imageAcceptInvitation.setVisibility(View.GONE);
                binding.maskOptionView.setVisibility(View.VISIBLE);
                binding.btnMute.setVisibility(View.VISIBLE);
                connectToSignallingServer();
                startStreamingVideo();

                videoTrackFromCamera.removeRenderer(local_renderer);
                videoTrackFromCamera.addRenderer(new VideoRenderer(binding.surfaceView2));
            }
        });

        /**
         * 통화 요청 거절 버튼
        */
        ImageView imageRejectInvitation = findViewById(R.id.imageRejectInvitation);
        imageRejectInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_REJECTED,
                        getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));
            }
        });

        /**
         * 안경 씌우기 버튼
         */
        binding.sunglasses.setOnClickListener(v -> {
            faceGraphic_mask.setViewScale(local_view_width, local_view_height);

            local_mask_type = "sunglasses";
            if(!isFrameListen){
                binding.surfaceView.addFrameListener(frameListener_remote, 0.5f);
                isFrameListen = true;
            }

        });

        /**
         * 턱수염 씌우기 버튼
         */
        binding.beard.setOnClickListener(v -> {
            faceGraphic_beard.setViewScale(local_view_width, local_view_height);
            local_mask_type = "beard";
            if(!isFrameListen){
                binding.surfaceView.addFrameListener(frameListener_remote, 0.5f);
                isFrameListen = true;
            }
        });

        /**
         * 마스크 씌우기 취소 버튼
         */
        binding.cancelMask.setOnClickListener(v -> {
            if(isFrameListen){
                Log.e(TAG, "프레임 리스너 제거");
                binding.surfaceView.removeFrameListener(frameListener_local);
                detector.close();
                isFrameListen = false;
                binding.graphicOverlay.clear();
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
        binding.graphicOverlay.clear();
//        for (int i = 0; i < faces.size(); ++i) {
        Face face = faces.get(0);

        if(remote_mask_type.equals("sunglasses"))
        {
            binding.graphicOverlay.add(faceGraphic_mask);
            faceGraphic_mask.updateFace(face);
        }
        else if(remote_mask_type.equals("beard")){
            binding.graphicOverlay.add(faceGraphic_beard);
            faceGraphic_beard.updateFace(face);
        }


//        }
    }

    private void sendInvitationResponse(String type, String receiverToken){
        try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {

                        try{


                        }catch (Exception e){
                            Toast.makeText(IncomingInvitationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                    else {
                        Toast.makeText(IncomingInvitationActivity.this, "Invitation rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else{
                    Log.e("sendRemoteMessage", "onResponse fail");
                    Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("sendRemoteMessage", "onFailure");
                Toast.makeText(IncomingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)){
                    Toast.makeText(context, "Invitation cancelled", Toast.LENGTH_SHORT).show();
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
     * web rtc 관련 메서드
     */
    @AfterPermissionGranted(RC_CALL)
    private void start() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
//            connectToSignallingServer();
//
//            initializeSurfaceViews();
//
//            initializePeerConnectionFactory();
//
//            createVideoTrackFromCameraAndShowIt();
//
//            initializePeerConnections();
//
//            startStreamingVideo();


//            initializeSurfaceViews();

            initializePeerConnectionFactory();

            createVideoTrackFromCameraAndShowIt();

            initializePeerConnections();


        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
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
                            Log.d(TAG, "connectToSignallingServer: received an offer " + isInitiator + " " + isStarted);
                            if (!isInitiator && !isStarted) {
                                maybeStart();
                            }
                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("sdp")));
                            doAnswer();
                        } else if (message.getString("type").equals("answer") && isStarted) {
                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("sdp")));
                        } else if (message.getString("type").equals("candidate") && isStarted) {
                            Log.d(TAG, "connectToSignallingServer: receiving candidates");
                            IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
                            peerConnection.addIceCandidate(candidate);
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
                    String message = (String) args[0];
                    Log.e(TAG, "mask 메시지: " + message);
                    if(message.equals("sunglasses")){
                        if(detector == null){
                            detector = FaceDetection.getClient(realTimeOpts);
                        }
                        faceGraphic_mask.setViewScale(remote_view_width, remote_view_height);
                        remote_mask_type = "sunglasses";
                        if(!isFrameListen){
                            binding.surfaceView.addFrameListener(frameListener_remote, 1f);
                            isFrameListen = true;
                        }
                    }
                    else if(message.equals("beard")){
                        if(detector == null){
                            detector = FaceDetection.getClient(realTimeOpts);
                        }
                        faceGraphic_beard.setViewScale(remote_view_width, remote_view_height);
                        remote_mask_type = "beard";
                        if(!isFrameListen){
                            binding.surfaceView.addFrameListener(frameListener_remote, 1f);
                            isFrameListen = true;
                        }
                    }

                    else if(message.equals("cancel")){
                        Log.e(TAG, "프레임 리스너 제거");
                        binding.surfaceView.removeFrameListener(frameListener_remote);
                        if(detector != null){
                            detector.close();
                            detector = null;
                        }
                        isFrameListen = false;
                        binding.graphicOverlay.clear();
                    }

                }
            });
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void doAnswer() {
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new MediaConstraints());
    }

    private void maybeStart() {
        Log.d(TAG, "maybeStart: " + isStarted + " " + isChannelReady);
        if (!isStarted && isChannelReady) {
            isStarted = true;
            if (isInitiator) {
                doCall();
            }
        }
    }

    private void doCall() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "onCreateSuccess: ");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    private void sendMessage(Object message) {
        socket.emit("message", message);
    }

    private void initializeSurfaceViews() {
        rootEglBase = EglBase.create();

        //binding.frameLayout.setPosition(0, 0, 100, 100);
        binding.surfaceView.init(rootEglBase.getEglBaseContext(), null);
        binding.surfaceView.setEnableHardwareScaler(true);
        binding.surfaceView.setMirror(true);
        binding.surfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);


        binding.surfaceView2.init(rootEglBase.getEglBaseContext(), null);
        binding.surfaceView2.setEnableHardwareScaler(true);
        binding.surfaceView2.setMirror(true);
        binding.surfaceView2.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        binding.surfaceView2.setZOrderMediaOverlay(true);
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    private void createVideoTrackFromCameraAndShowIt() {
        audioConstraints = new MediaConstraints();
        videoConstraints = new MediaConstraints();

        VideoCapturer videoCapturer = createVideoCapturer();
        VideoSource videoSource = factory.createVideoSource(videoCapturer);
        videoSource.adaptOutputFormat(1000, 2000, 15);

        // 높이, 넓이, fps
        videoCapturer.startCapture(local_view_width, local_view_height, 5);

        local_renderer = new VideoRenderer(binding.surfaceView);

        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);

        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrackFromCamera.setEnabled(true);
        videoTrackFromCamera.addRenderer(local_renderer);
    }

    private void initializePeerConnections() {
        peerConnection = createPeerConnection(factory);
    }

    private void startStreamingVideo() {
        mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(localAudioTrack);
        mediaStream.addTrack(videoTrackFromCamera);
        peerConnection.addStream(mediaStream);

        sendMessage("got user media");
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

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
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

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
//                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);

                Log.e(TAG, "onAddStream remote video track: " + remoteVideoTrack);
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
        if(mediaStream != null){
            peerConnection.removeStream(mediaStream);
            peerConnection.close();
        }

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