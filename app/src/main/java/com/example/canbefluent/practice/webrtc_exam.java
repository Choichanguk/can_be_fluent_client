package com.example.canbefluent.practice;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.canbefluent.R;
import com.example.canbefluent.tutorial.SimpleSdpObserver;

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
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.example.canbefluent.app_rtc_sample.web_rtc.PeerConnectionClient.AUDIO_TRACK_ID;
import static com.example.canbefluent.app_rtc_sample.web_rtc.PeerConnectionClient.VIDEO_TRACK_ID;
import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_DISCONNECT;
import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class webrtc_exam extends AppCompatActivity {

    private static final String TAG = "CompleteActivity";
    private static final int RC_CALL = 111;

    AudioManager audioManager;

    private Socket socket;
    private boolean isInitiator;
    private boolean isChannelReady;
    private boolean isStarted;

    MediaConstraints audioConstraints;
    MediaConstraints videoConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;


    private PeerConnection localPeer;
    private EglBase rootEglBase;
    private PeerConnectionFactory peerConnectionFactory;
    private VideoTrack localVideoTrack;

    private SurfaceViewRenderer surfaceView_local;
    private SurfaceViewRenderer surfaceView_remote;

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webrtc_exam);

        surfaceView_local = findViewById(R.id.surface_view);
        surfaceView_remote = findViewById(R.id.surface_view2);
        btn = findViewById(R.id.button3);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        setSpeakerphoneOn(true);
        start();
    }

    @Override
    protected void onDestroy() {
        if (socket != null) {
            sendMessage("bye");
            socket.disconnect();
        }
        super.onDestroy();
    }

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
                            localPeer.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("sdp")));
                            doAnswer();
                        } else if (message.getString("type").equals("answer") && isStarted) {
                            localPeer.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("sdp")));
                        } else if (message.getString("type").equals("candidate") && isStarted) {
                            Log.d(TAG, "connectToSignallingServer: receiving candidates");
                            IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
                            localPeer.addIceCandidate(candidate);
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

    private void doAnswer() {
        localPeer.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                localPeer.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
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

        /**
         * creating Offer
         */
        localPeer.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.e(TAG, "onCreateSuccess: ");
                localPeer.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
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
        surfaceView_local.init(rootEglBase.getEglBaseContext(), null);
        surfaceView_local.setEnableHardwareScaler(true);
        surfaceView_local.setMirror(true);

        surfaceView_remote.init(rootEglBase.getEglBaseContext(), null);
        surfaceView_remote.setEnableHardwareScaler(true);
        surfaceView_remote.setMirror(true);
    }

    private void initializePeerConnectionFactory() {

        /**
         * 파라미터 context, initAudio, initVideo, videoCodecHwAcceleration
         */
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        peerConnectionFactory = new PeerConnectionFactory(null);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = new PeerConnectionFactory(options);// 수정
//        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    private void createVideoTrackFromCameraAndShowIt() {
        MediaConstraints constraints = new MediaConstraints();
        audioConstraints = new MediaConstraints();
//        videoConstraints = new MediaConstraints();

        VideoCapturer videoCapturer = createVideoCapturer();

        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer);
        localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoCapturer.startCapture(1280, 720, 30);

        audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);

        localVideoTrack.setEnabled(true);
        localVideoTrack.addRenderer(new VideoRenderer(surfaceView_local));
    }

    /**
     *
     */
    private void initializePeerConnections() {
        localPeer = createPeerConnection(peerConnectionFactory);
    }

    /**
     * create Local Media Stream
     */
    private void startStreamingVideo() {
        MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(localAudioTrack);
        mediaStream.addTrack(localVideoTrack);
        localPeer.addStream(mediaStream);

        sendMessage("got user media");
    }

    /**
     * PeerConnection 생성 메서드 (video, audio track 생성 후)
     * @param factory
     * @return
     */
    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();

        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);

        //create sdpConstraints
        MediaConstraints sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true")); // 추가
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "true")); // 추가


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

        return factory.createPeerConnection(rtcConfig, sdpConstraints, pcObserver);
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