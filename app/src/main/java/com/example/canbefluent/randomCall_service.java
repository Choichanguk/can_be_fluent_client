package com.example.canbefluent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.tutorial.SimpleSdpObserver;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import io.socket.client.IO;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.canbefluent.app_rtc_sample.web_rtc.PeerConnectionClient.AUDIO_TRACK_ID;
import static io.socket.client.Socket.EVENT_CONNECT;
import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class randomCall_service extends Service {
    private static final String TAG = "randomCall_service";
    public static Socket socket;


    /**
     * webRTC 관련 변수
     */
    private boolean isInitiator;
    private boolean isChannelReady;
    private boolean isStarted;
    private static final int RC_CALL = 112;
    MediaConstraints audioConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;
    private PeerConnection peerConnection;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    MediaStream mediaStream;
    AudioManager audioManager;



    RetrofitClient retrofitClient = new RetrofitClient();
    String native_lang_code, practice_lang_code;
    String candidate_index;

    CountDownTimer countDownTimer = null;
    count_down_thread countDownThread = null;
    public randomCall_service() {
    }

    IBinder mBinder = new MyBinder();

    class MyBinder extends Binder {
        randomCall_service getService() { // 서비스 객체를 리턴
            return randomCall_service.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    int getRan() { // 임의 랜덤값을 리턴하는 메서드
        return new Random().nextInt();
    }

    @Override
    public void onCreate() {
        Log.d("StartService","onCreate()");
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("StartService","onStartCommand()");

        native_lang_code = intent.getStringExtra("native");
        practice_lang_code = intent.getStringExtra("practice");
        Log.e("service", "native: " + intent.getStringExtra("native"));
        Log.e("service", "practice: " + intent.getStringExtra("practice"));

        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        setSpeakerphoneOn(true);
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);

        connect(native_lang_code, practice_lang_code);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d("StartService","onDestroy()");
        super.onDestroy();
        if (socket != null) {
            disconnect();
            socket.disconnect();
            socket = null;
        }
    }


    private void connect(String native_lang_code, String practice_lang_code){
        // 매칭 카운트다운 시작
        if(countDownThread == null){
            countDownThread = new count_down_thread();
            countDownThread.start();
        };

        try {
            socket = IO.socket("http://canbefluent.xyz:8888/");
            socket.on(EVENT_CONNECT, args -> {
                Log.e(TAG, "connect 이벤트");

                // 소켓 연결 후 유저의 정보를 서버로 넘겨준다.
                socket.emit("set info", MainActivity.user_item.getUser_index(), native_lang_code, practice_lang_code);
//                socket.emit("create or join", "foo");
            }).on("find candidate", args -> {
                Log.e(TAG, "find candidate");

                // 매칭 카운트다운 중지
                if(countDownThread != null){
                    Log.e(TAG, "스레드 interrupt");
                    countDownThread.interrupt();
                    countDownThread = null;
                }

                candidate_index = (String) args[0];
                get_candidate_info(candidate_index);

                Log.e(TAG, "candidate index: " + candidate_index);

            }).on("cancel match", args -> {
                Log.e(TAG, "cancel match 이벤트 받음");

                // 매칭 카운트다운 시작
                if(countDownThread == null){
                    countDownThread = new count_down_thread();
                    countDownThread.start();
                };

                // 매칭 성공 후 상대방이 통화 연결 거부했을 때
                Intent intent = new Intent("random call");
                intent.putExtra("type", "cancel");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }).on("created", args -> {  // initiator가 방 생성 시 발생되는 이벤트
                Log.e(TAG, "created 이벤트");

                isInitiator = true;
                start();
                sendMessage("got user media");
            }).on("joined", args -> {   // 방에 누가 들어오면 발생되는 이벤트 방생성자도 이벤트 발생함
                Log.e(TAG, "joined 이벤트");

                isChannelReady = true;
            }).on("join", args -> {   // 방에 참여 시 발생되는 이벤트 (방 생성자는 이벤트 발생 x)
                Log.e(TAG, "join 이벤트");

                isChannelReady = true;
                start();
                sendMessage("got user media");
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
                            Log.e(TAG, "connectToSignallingServer: received an offer " + isInitiator + " " + isStarted);
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
            }).on("finish call", args -> {
                Log.e(TAG, "finish call 이벤트 발생");
                if(mediaStream != null){
                    peerConnection.removeStream(mediaStream);
                    peerConnection.close();

                    socket.emit("bye");
                    initialize_variable();
                    Intent intent = new Intent("random call");
                    intent.putExtra("type", "finish call");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            });
            socket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 소켓 연결을 종료시키는 메서드
     * 앱을 종료하거나 매칭 검색 중단 시 소켓 연결 종료
     */
    private void disconnect(){
        if(countDownThread != null){
            Log.e(TAG, "스레드 interrupt");
            countDownThread.interrupt();
            countDownThread = null;
        };

        Log.e(TAG, "disconnect");
        socket.emit("bye");


    }

    /**
     * 매칭 상대 찾은 후 통화 연결을 하기 위한 메소드
     */
    public void start_call(){
        socket.emit("create or join");
    }

    // 매칭 상대가 구해졌을 때 통화 연결 취소하는 경우
    void cancel_match(){
        socket.emit("cancel match", candidate_index);

        // 매칭 카운트다운 시작
        if(countDownThread == null){
            countDownThread = new count_down_thread();
            countDownThread.start();
        };
    }

    /**
     * 매칭 상대방의 정보를 서버로부터 가져오는 메서드
     * @param candidate_index
     */
    private void get_candidate_info(String candidate_index){

                retrofitClient.service.get_random_user_info(candidate_index)
                .enqueue(new Callback<user_item>() {
                    @Override
                    public void onResponse(Call<user_item> call, Response<user_item> response) {
                        Log.e(TAG, "onResponse");
                        user_item item = response.body();

                        // 받아온 후보 정보를 브로드캐스트로 넘겨준다.
                        Intent intent = new Intent("random call");
                        intent.putExtra("type", "find candidate");
                        intent.putExtra("user item", item);
//                        intent.putExtra("profile", item.getProfile_img());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                    }

                    @Override
                    public void onFailure(Call<user_item> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }


    private void doAnswer() {

        Log.e(TAG, "doAnswer");
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
        Log.e(TAG, "maybeStart");
        Log.d(TAG, "maybeStart: " + isStarted + " " + isChannelReady);
        if (!isStarted && isChannelReady) {
            isStarted = true;
            if (isInitiator) {
                doCall();
            }
        }
    }

    private void doCall() {
        Log.e(TAG, "doCall");
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
        Log.e(TAG, "sendMessage");
        socket.emit("message", message);
    }

    private void initializeSurfaceViews() {
        Log.e(TAG, "initializeSurfaceViews");
        rootEglBase = EglBase.create();
    }

    private void initializePeerConnectionFactory() {
        Log.e(TAG, "initializePeerConnectionFactory");
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    private void createVideoTrackFromCameraAndShowIt() {
        audioConstraints = new MediaConstraints();
        Log.e(TAG, "createVideoTrackFromCameraAndShowIt");


        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
    }

    private void initializePeerConnections() {
        Log.e(TAG, "initializePeerConnections");
        peerConnection = createPeerConnection(factory);
    }

    private void startStreamingVideo() {
        Log.e(TAG, "startStreamingVideo");
        mediaStream = factory.createLocalMediaStream("ARDAMS");
//        mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(localAudioTrack);
        peerConnection.addStream(mediaStream);

//        sendMessage("got user media");
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        Log.e(TAG, "createPeerConnection");
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
                Log.e(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.e(TAG, "onAddStream");

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.e(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.e(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.e(TAG, "onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    private void start(){
        initializeSurfaceViews();

        initializePeerConnectionFactory();

        createVideoTrackFromCameraAndShowIt();

        initializePeerConnections();

        startStreamingVideo();

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


    public void mute(){
        Log.e(TAG, "음소거");
        localAudioTrack.setEnabled(false);
    }

    public void unMute(){
        Log.e(TAG, "음소거 해제");
        localAudioTrack.setEnabled(true);
    }

    /**
     * 통화 종료 이벤트
     */
    public void finishCAll(){
        socket.emit("finish call");

        // webRTC 관련 변수를 초기화 한다.
        if(mediaStream != null){
            peerConnection.removeStream(mediaStream);
            peerConnection.close();

            initialize_variable();
        }
    }

    private void initialize_variable(){
        isInitiator = false;
        isChannelReady = false;
        isStarted = false;
        audioConstraints = null;
        audioSource = null;
        localAudioTrack = null;
        peerConnection = null;
        rootEglBase = null;
        factory = null;
        mediaStream = null;
    }

//    private void start_count_down(){
//        countDownTimer = new CountDownTimer(10000, 1000) {
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            // 매칭 타임 아웃. 매칭을 취소 시킨다.
//            public void onFinish() {
//                Intent intent = new Intent("random call");
//                intent.putExtra("type", "time out");
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                socket.emit("bye");
//                countDownTimer = null;
//            }
//        }.start();
//    }



    private class count_down_thread extends Thread{

        @Override
        public void run() {
            super.run();
            Log.e(TAG, "스레드 시작");
            int count = 0;
            while(count < 60){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, "스레드 중지");
                    return;
                }
                count ++;
            }

            if(count == 60){
                Intent intent = new Intent("random call");
                intent.putExtra("type", "time out");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                socket.emit("bye");
                countDownThread = null;
            }


        }
    }

//    private void change_pitch(){
//        SoundPool sp ;
////        sp = new SoundPool(1, audioManager.getMicrophones(), 1)
////        audioManager
//    }

}