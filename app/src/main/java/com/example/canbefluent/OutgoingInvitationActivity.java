package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.canbefluent.app_rtc_sample.source.EglRenderer;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.tutorial.SimpleSdpObserver;
import com.example.canbefluent.utils.Constants;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.utils.sharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
//import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.net.URISyntaxException;
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

public class OutgoingInvitationActivity extends AppCompatActivity {
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
    MediaConstraints videoConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;

    private PeerConnection peerConnection;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack localVideoTrack;

    private SurfaceViewRenderer surfaceView_local;
    private SurfaceViewRenderer surfaceView_remote;

    MediaStream localStream;



    private String inviterToken = null;
    RetrofitClient retrofitClient;
    com.example.canbefluent.utils.sharedPreference sharedPreference;
//    Button button;
    String meetingRoom = null;
    LinearLayout userInfo_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        sharedPreference = new sharedPreference();
        inviterToken = sharedPreference.loadFCMToken(OutgoingInvitationActivity.this);
//        Log.e("OutgoingInvitation", "token: " + inviterToken);

        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        setSpeakerphoneOn(true);
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);


        final String meetingType = getIntent().getStringExtra("type");

        CircleImageView profileImg = findViewById(R.id.profile_img);
        TextView userName = findViewById(R.id.user_name);

        surfaceView_local = findViewById(R.id.surface_view);
        surfaceView_remote = findViewById(R.id.surface_view2);
        userInfo_view = findViewById(R.id.linearLayout8);

        /**
         * real time face detector 모델 옵션 객체 생성
         */
        FaceDetectorOptions realTimeOpts =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();

        /**
         * face detector 모델 생성
         */
        FaceDetector detector = FaceDetection.getClient(realTimeOpts);

        /**
         * surfaceView_local로부터 bitmap을 가져온다.
         */
//        surfaceView_local.addFrameListener(bitmap -> {
//            Log.e("addFrameListener", String.valueOf(bitmap));
//
//            // 파라미터: bitmap, rotation degree
//            InputImage image = InputImage.fromBitmap(bitmap, 0);
//
//            processImage(image, detector);
////            processAndRecognize(bitmap);
//        }, 1.f);

        surfaceView_local.addFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap bitmap) {
                Log.e("addFrameListener", String.valueOf(bitmap));
            }
        }, 1.f);

        /**
         * user_item에 담긴 유저 정보를 세팅해준다. (상대방 user item)
         * 프로필 이미지
         * first name
         */
        final user_item user_item = (com.example.canbefluent.items.user_item) getIntent().getSerializableExtra("user item");
        if(user_item != null){

            String url = MyApplication.server_url + "/profile_img/" + user_item.getProfile_img();;
            Glide.with(this)
                    .load(url)
                    .into(profileImg);

            userName.setText(user_item.getFirst_name());
        }

        /**
         * 영상통화 취소 버튼
         * 누르면 이전 화면으로 돌아간다.
         */
        ImageView imageStopInvitation = findViewById(R.id.imageStopInvitation);
        imageStopInvitation.setOnClickListener(new View.OnClickListener() {
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
//            Log.e("상대방 토큰 확인", user_item.getToken());
        }

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

        }catch (Exception e){
            Toast.makeText(OutgoingInvitationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation sent successfully", Toast.LENGTH_SHORT).show();
                    }
                    else if(type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else{
                    Log.e("sendRemoteMessage", "onResponse fail");
                    Toast.makeText(OutgoingInvitationActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("sendRemoteMessage", "onFailure");
                Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
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
                    userInfo_view.setVisibility(View.GONE);
//                    Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show();
                }
                else if(type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    Toast.makeText(context, "Invitation rejected", Toast.LENGTH_SHORT).show();
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

    public void processImage(InputImage image, FaceDetector detector){
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        Log.e("processImage", "이미지 처리 성공");
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


            initializeSurfaceViews();

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
        surfaceView_local.init(rootEglBase.getEglBaseContext(), null);
        surfaceView_local.setEnableHardwareScaler(true);
        surfaceView_local.setMirror(true);

        surfaceView_remote.init(rootEglBase.getEglBaseContext(), null);
        surfaceView_remote.setEnableHardwareScaler(true);
        surfaceView_remote.setMirror(true);
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
        videoCapturer.startCapture(1280, 720, 30);

        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);

        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(true);
        localVideoTrack.addRenderer(new VideoRenderer(surfaceView_local));
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
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                
//                remoteVideoTrack.dispose();
//                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
//                Log.e(TAG, "onAddStream remote video track: " + remoteVideoTrack);
//                Log.e(TAG, "onAddStream remote audio track: " + remoteAudioTrack);
//                remoteAudioTrack.setEnabled(true);
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addRenderer(new VideoRenderer(surfaceView_remote));

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