package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canbefluent.adapter.chatRoomAdapter;
import com.example.canbefluent.items.msg_item;
import com.example.canbefluent.pojoClass.getImgList;
import com.example.canbefluent.pojoClass.getResult;
import com.example.canbefluent.pojoClass.getRoomList;
import com.example.canbefluent.pojoClass.getStatus;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.utils.FileUtils;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.utils.sharedPreference;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class message_room extends AppCompatActivity {
    private static final String TAG = "message_room";

    TextView name;
    String room_index="";      // 메세지 저장 시 필요한 room index
    String profile_img;     // 상대방의 프로필 이미지
    String first_name;      // 상대방의 first_name;
    String token;           // 상대방이 현재 방에 존재하지 않을 시, fcm 알림을 보내주기 위한 토큰
    String my_user_index, user_id;
    getRoomList room_obj;   // frag_chat으로부터 전달받는 room 객체

    String lang_code;

    DataInputStream is;
    DataOutputStream os;

    String msg="";  // 서버로부터 전송되는 msg
    EditText edit_msg;  //서버로 전송할 메세지를 작성하는 EditText
    ImageButton btn_send; //msg 보내는 버튼
    ImageButton btn_open_option;    // option버튼을 여는 버튼
    ImageButton btn_close_option;   // option 버튼을 닫는 버튼
    ImageButton btn_album, btn_camera, btn_record;
    ImageButton btn_close_record;
    ImageButton record, play, stop;
    Button btn_send_audio;
    RecyclerView img_list_recycler; // 유저가 선택한 이미지를 보여주는 뷰

    private ArrayList<Uri> imgList = new ArrayList<>();     // 선택한 이미지들의 Uri를 담는 리스트

    boolean isConnect = false; // 서버 접속여부를 판별하기 위한 변수 ( 만약 서버 접속이 끊긴다면 다른 방법으로 통신해야함)
    boolean isRunning=false;    // 어플 종료시 스레드 중지를 위해...
    Socket client_socket;     //클라이언트의 소켓
    ObjectInputStream ois;
    com.example.canbefluent.utils.sharedPreference sharedPreference = new sharedPreference();

    MainActivity mainActivity = new MainActivity();
//    user_item my_info = mainActivity.user_item;     // 내 아이디 정보를 담고 있는 객체


    LinearLayout msg_option_layout, chat_layout;     // 이미지, 카메라, 음성메세지를 전송할 수 있는 버튼이 있는 레이아웃, 채팅 담당 레이아웃
    ConstraintLayout record_layout;     // 녹음을 할 수 있는 레이아웃

//    ArrayList<getChatList> chatLists = new ArrayList<>();


    ArrayList<msg_item> msgLists = new ArrayList<>();
    RecyclerView chat_recycler;
    chatRoomAdapter chatRoomAdapter;

    RetrofitClient retrofitClient;
    Call<ArrayList<msg_item>> call;   // 메세지 정보를 불러ㅇ
    Call<ArrayList<getRoomList>> call2;
    Call<ArrayList<getImgList>> call3;

    TextView record_time;   // 녹음 시간을 나타내는 뷰


    /**
     * 녹음할 때 사용되는 변수
     */
    MediaRecorder recorder;  // 녹음을 하기위한 객체
    String filename;    // 녹음된 내용을 담는 임시 파일
    MediaPlayer player; // 녹음 내용을 재생시키는 객체
    int position = 0; // 다시 시작 기능을 위한 현재 재생 위치 확인 변수
    boolean isRecording = false;
    timeThread thread;
    public int i = 0;  // 녹음된 시간
    File file;

    int curPerson = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_room);

        msg_option_layout = findViewById(R.id.msg_option);
        record_layout = findViewById(R.id.record_layout);
        chat_layout = findViewById(R.id.chat_layout);
        my_user_index = sharedPreference.loadUserIndex(message_room.this);
        lang_code = sharedPreference.loadLangCode(message_room.this);
        Intent intent = getIntent();

        String type = intent.getStringExtra("type");
        if(type.equals("from room list")){  //메세지 버튼이나 방 목록을 통해 방에 들어왔을 경우
            room_obj = (getRoomList) intent.getSerializableExtra("room obj");
            set_room_info();
            /**
             * 클라이언트 소켓 생성 후 서버와 연결.
             */
            ConnectionThread thread = new ConnectionThread();
            thread.start();
        }
        else{   //채팅알림을 통해 방에 들어왔을 경우
            room_index = intent.getStringExtra("room_index");

            Log.e(TAG, "room_index: " + room_index);
            retrofitClient = new RetrofitClient();
            call2 = retrofitClient.service.get_room_info_from_noti(room_index, my_user_index);
            call2.enqueue(new Callback<ArrayList<getRoomList>>() {
                @Override
                public void onResponse(Call<ArrayList<getRoomList>> call, Response<ArrayList<getRoomList>> response) {
                    ArrayList<getRoomList> result = response.body();
                    Log.e(TAG, "room size: " + result.size());
                    room_obj = result.get(0);
                    set_room_info();

                    /**
                     * 클라이언트 소켓 생성 후 서버와 연결.
                     */
                    ConnectionThread thread = new ConnectionThread();
                    thread.start();
                }

                @Override
                public void onFailure(Call<ArrayList<getRoomList>> call, Throwable t) {

                }
            });
        }


        name = findViewById(R.id.name);
        name.setText(first_name);

//        user_id = my_info.getUser_id();
//        String my_first_name = my_info.getFirst_name();
        edit_msg = findViewById(R.id.edit_msg);
        btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                // 메세지, 이미지, 음성녹음 파일로 메세지 타입을 구분한다.
                // 입력한 문자열을 가져온다.


                if(imgList.size() > 0){     // 이미지 리스트에 이미지 uri가 담겨있다면 이미지 전송 로직을 실행
                    // 서버로 선택한 이미지들을 보내 db에 저장시킨다.
                    // 서버로부터 저장된 이미지 이름을 응답받는다.
                    // 응답받은 이미지 이름을 tcp 소켓을 통해 전달한다.

                    // create list of file parts (photo, video, ...)
                    List<MultipartBody.Part> parts = new ArrayList<>();

                    if (imgList != null) {
                        // create part for file (photo, video, ...)
                        for (int i = 0; i < imgList.size(); i++) {
                            parts.add(prepareFilePart("image"+i, imgList.get(i)));
                        }
                    }

                    final long time = System.currentTimeMillis(); // 메세지를 보낸 시간
                    // create a map of data to pass along
                    RequestBody room_Index = createPartFromString(room_index);
                    RequestBody user_index = createPartFromString(my_user_index);
                    RequestBody Time = createPartFromString(time+"");
                    RequestBody status; // 메세지 타입 - img

                    if(curPerson == 1){
                        status = createPartFromString("no read");
                    }
                    else{
                        status = createPartFromString("read");
                    }



                    RequestBody size = createPartFromString(""+parts.size());   // 보내는 이미지 개수

                    Log.e(TAG, "이미지 전송 room_Index: " + room_Index.toString());
                    Log.e(TAG, "이미지 전송 user_index: " + user_index.toString());
                    Log.e(TAG, "이미지 전송 status: " + status.toString());
                    Log.e(TAG, "이미지 전송 parts size: " + parts.size());

                    retrofitClient = new RetrofitClient();
                    call3 =retrofitClient.service.uploadMultiple(room_Index, user_index, status, size, Time, parts);
                    call3.enqueue(new Callback<ArrayList<getImgList>>() {
                        @Override
                        public void onResponse(Call<ArrayList<getImgList>> call, Response<ArrayList<getImgList>> response) {
                            ArrayList<getImgList> lists = response.body();
                            Log.e(TAG, "onResponse");
                            Log.e(TAG, "img list size: " + response.body().size());

                            // 서버로부터 저장된 이미지 이름을 받아오면 이미지 이름을 msg 객체로 담아 msg_list에 담는다.
                            // adapter에 notify 해준다.
                            assert lists != null;
                            ArrayList<String> uri_list = new ArrayList<>();
                            if(lists.size() > 0){
                                for (int i = 0; i < lists.size(); i++){
                                    uri_list.add(lists.get(i).getFile_name());

                                    msg_item item = new msg_item();
                                    item.setType("img");
                                    item.setMessage(lists.get(i).getFile_name());
                                    item.setTime(time);
                                    item.setUser_index(my_user_index);
                                    item.setRoom_index(room_index);
                                    if(curPerson == 1){
                                        item.setStatus("no read");
                                    }
                                    else{
                                        item.setStatus("read");
                                    }
                                    msgLists.add(item);
                                }

                                // 서버로 파일 이름이 담기 리스트를 담은 객체를 보낸다.
                                msg_item item1 = new msg_item();
                                item1.setType("img");
                                item1.setImg_list(uri_list);
                                item1.setTime(time);
                                item1.setUser_index(my_user_index);
                                item1.setRoom_index(room_index);

                                // 서버로 이미지 메세지 객체를 보낸다.
                                SendToServerThread thread=new SendToServerThread(client_socket, item1);
                                thread.start();

                                chatRoomAdapter = new chatRoomAdapter(msgLists, my_user_index, message_room.this, profile_img);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(message_room.this);
                                linearLayoutManager.setStackFromEnd(true);
                                chat_recycler.setLayoutManager(linearLayoutManager);
                                chat_recycler.setAdapter(chatRoomAdapter);
                            }

                            // 서버로부터 이미지 저장 확인 되면 imgList를 초기화하고, 이미지를 보여주는 가로 리사이클러뷰 창을 닫는다.
                            imgList = new ArrayList<>();
                            img_list_recycler.setVisibility(View.GONE);
                            edit_msg.setFocusableInTouchMode (true);
                            edit_msg.setFocusable(true);
                        }

                        @Override
                        public void onFailure(Call<ArrayList<getImgList>> call, Throwable t) {
                            Log.e(TAG, "onFailure: " + t.getMessage());
                        }
                    });
                }
                else{   // 일반 문자 메시지 전송

                    if(!edit_msg.getText().toString().equals("")){  // 빈 문자열일 경우 메세지 전송 버튼 클릭 시 아무일도 일어나지 않는다.
                        msg_item item = new msg_item();
                        item.setMessage(edit_msg.getText().toString());
                        item.setTime(System.currentTimeMillis());
                        item.setType("msg");
                        item.setRoom_index(room_index);
                        item.setUser_index(my_user_index);

                        // 송신 스레드 가동
                        // msg객체를 보낸다.
                        SendToServerThread thread=new SendToServerThread(client_socket, item);
                        thread.start();
                    }

                }
            }
        });


        btn_open_option = findViewById(R.id.btn_open_option);
        btn_open_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg_option_layout.setVisibility(View.VISIBLE);
                btn_close_option.setVisibility(View.VISIBLE);
                btn_open_option.setVisibility(View.GONE);
                chat_recycler.scrollToPosition(chat_recycler.getAdapter().getItemCount() - 1);
            }
        });

        btn_close_option = findViewById(R.id.btn_close_option);
        btn_close_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg_option_layout.setVisibility(View.GONE);
                btn_close_option.setVisibility(View.GONE);
                btn_open_option.setVisibility(View.VISIBLE);

                if(imgList.size() > 0){ // 선택한 이미지가 있는 상태에서 창 닫기 버튼을 클릭 시 list를 초기화 하고, 이미지를 보여주는 뷰를 보이지 않도록 한다.
                    imgList = new ArrayList<>();    // 이미지 list 초기화
                    img_list_recycler.setVisibility(View.GONE);
                    edit_msg.setFocusableInTouchMode (true);
                    edit_msg.setFocusable(true);
                }
            }
        });
        Log.e(TAG, "user index: " + my_user_index);


        btn_album = findViewById(R.id.btn_album);
        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooser();
            }
        });

        btn_record = findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record_layout.setVisibility(View.VISIBLE);
                chat_layout.setVisibility(View.GONE);
                msg_option_layout.setVisibility(View.GONE);
            }
        });
        btn_close_record = findViewById(R.id.btn_close_record);
        btn_close_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record_layout.setVisibility(View.GONE);
                chat_layout.setVisibility(View.VISIBLE);
                btn_open_option.setVisibility(View.VISIBLE);
                btn_close_option.setVisibility(View.GONE);

                btn_send_audio.setEnabled(false);
                play.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
                record.setVisibility(View.VISIBLE);

                Log.e(TAG, "record time: " + i);
                i=0; // 녹음 시간 초기화
                if(isRecording){
                    stopRecording();
                }
                record_time.setText("버튼을 눌러 녹음을 시작하세요.");
            }
        });

        record_time = findViewById(R.id.record_time);

        /**
         * 녹음 시작 버튼
         */
        record = findViewById(R.id.record);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sdcard = Environment.getExternalStorageDirectory();
                file = new File(sdcard, "recorded.mp4");
                filename = file.getAbsolutePath();
                Log.d("MainActivity", "저장할 파일 명 : " + filename);
                recordAudio();
                record.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);
            }
        });

        /**
         * 재생 버튼
         */
        play = findViewById(R.id.btn_play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
                play.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);
            }
        });

        /**
         * 재생 정지 버튼 or 녹음 중지 버튼
         */
        stop = findViewById(R.id.btn_stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording){
                    stopRecording();
                    stop.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                }
                else {
                    stopAudio();
                    stop.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                }

            }
        });

        /**
         * 녹음파일 서버로 업로드하는 버튼
         */
        btn_send_audio = findViewById(R.id.btn_send_audio);
        btn_send_audio.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                btn_send_audio.setEnabled(false);

                play.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
                record.setVisibility(View.VISIBLE);
                record_layout.setVisibility(View.GONE);
                chat_layout.setVisibility(View.VISIBLE);
                btn_open_option.setVisibility(View.VISIBLE);
                btn_close_option.setVisibility(View.GONE);

                Log.e(TAG, "record time: " + i);

                if(isRecording){
                    stopRecording();
                }
                record_time.setText("버튼을 눌러 녹음을 시작하세요.");

                // 서버로 audio 파일 전송하는 로직
                byte[] audio_byte = new byte[0];
                try {
                    audio_byte = Files.readAllBytes(file.toPath());
//                    String time = getPlayTime(filename);
                    Log.e(TAG, "byte 길이: " + audio_byte.length);
//                    Log.e(TAG, "재생시간: " + time);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "byte 생성 에러: " + e.getMessage());
                }

                if(audio_byte != null){
                    final long time = System.currentTimeMillis(); // 메세지를 보낸 시간
                    // create a map of data to pass along
                    RequestBody room_Index = createPartFromString(room_index);
                    RequestBody user_index = createPartFromString(my_user_index);
                    RequestBody Time = createPartFromString(time+"");
                    RequestBody play_time = createPartFromString(i+"");
                    RequestBody status;

                    final String Status;
                    if(curPerson == 1){
                        Status = "no read";
                        status = createPartFromString(Status);
                    }
                    else{
                        Status = "read";
                        status = createPartFromString(Status);
                    }

                    MultipartBody.Part part = toMultiPartFile("audio", audio_byte);
                    RetrofitClient retrofitClient = new RetrofitClient();
                    Call<getStatus> call = retrofitClient.service.uploadAudio(part, room_Index, user_index, status, Time, play_time);
                    call.enqueue(new Callback<getStatus>() {
                        @Override
                        public void onResponse(Call<getStatus> call, Response<getStatus> response) {
                            Log.e(TAG, "onResponse");   // error 로그

                            getStatus item = response.body();
                            Log.e(TAG, item.getMessage());  // error 로그
                            Log.e(TAG, item.getStatus());  // error 로그

                            if(item != null){
                                // tcp 소켓으로 상대방에게 오디오파일 이름을 전달해주는 로직
                                if(item.getStatus().equals("success")){     // 서버로부터 오디오파일 저장 완료 메시지를 받으면 msg 객체를 만들어서 tcp 서버로 전송한다.
                                    msg_item item1 = new msg_item();
                                    item1.setMessage(item.getMessage());
                                    item1.setStatus(Status);
                                    item1.setRoom_index(room_index);
                                    item1.setUser_index(my_user_index);
                                    item1.setType("audio");
                                    item1.setPlay_time(i);
                                    item1.setTime(time);

                                    // msg객체를 보낸다.
                                    SendToServerThread thread=new SendToServerThread(client_socket, item1);
                                    thread.start(); // 송신 스레드 가동
                                    i=0; // 녹음 시간 초기화
                                }


                            }
                            else{
                                Log.e(TAG, "response null");  // error 로그
                            }
                        }

                        @Override
                        public void onFailure(Call<getStatus> call, Throwable t) {
                            Log.e(TAG, "onFailure");   // error 로그
                            Log.e(TAG, t.getMessage());
                        }
                    });
                }




            }
        });
    }

    /**
     * 녹음하는 메서드
     */
    private void recordAudio() {
        recorder = new MediaRecorder();

        /* 그대로 저장하면 용량이 크다.
         * 프레임 : 한 순간의 음성이 들어오면, 음성을 바이트 단위로 전부 저장하는 것
         * 초당 15프레임 이라면 보통 8K(8000바이트) 정도가 한순간에 저장됨
         * 따라서 용량이 크므로, 압축할 필요가 있음 */
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 어디에서 음성 데이터를 받을 것인지
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 압축 형식 설정
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        recorder.setOutputFile(filename);

        thread = new timeThread();
        isRecording = true;
        thread.start();
        try {
            recorder.prepare();
            recorder.start();
            // 초시계 스레드 시작해야 함


//            Toast.makeText(this, "녹음 시작됨.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 녹음을 정지하는 메서드
     */
    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            isRecording = false;
            btn_send_audio.setEnabled(true);
//            Toast.makeText(this, "녹음 중지됨.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 녹음 파일을 재생시키는 메서드
     */
    private void playAudio() {
        try {
            closePlayer();

            player = new MediaPlayer();
            player.setDataSource(filename);
            player.prepare();
            player.start();

            playThread thread = new playThread(i);
            thread.start();

//            Toast.makeText(this, "재생 시작됨.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 녹음 파일을 중지시키는 메서드
     */
    private void stopAudio() {
        if (player != null && player.isPlaying()) {
            player.stop();

//            Toast.makeText(this, "중지됨.", Toast.LENGTH_SHORT).show();
        }
    }

    public void closePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }


    private void playAudioChat(String url) {
        try {
            closePlayer();

            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(url);
            player.prepare();
            player.start();
            double sec = player.getDuration()/1000.0;

            Log.e("창욱", "재생시간: " + sec);

//            Toast.makeText(this, "재생 시작됨.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseAudioChat() {
        if (player != null) {
            position = player.getCurrentPosition();
            player.pause();

//            Toast.makeText(this, "일시정지됨.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resumeAudioChat() {
        if (player != null && !player.isPlaying()) {
            player.seekTo(position);
            player.start();

//            Toast.makeText(this, "재시작됨.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 녹음 시간을 측정 할 스레드 클래스
     */
    public class timeThread extends Thread{
        @Override
        public void run() {
            i=0;
            while(isRecording){
                i++;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 화면에 초를 출력하기 위한 스레드
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int sec = (i / 100) % 60;
                        int min = (i / 100) / 60;
                        @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d", min, sec);
                        record_time.setText(result);
                    }
                });
            }
        }
    }

    /**
     * 플레이 시간을 측정 할 스레드 클래스
     */
    public class playThread extends Thread{
        int time;
        playThread(int time){
            this.time = time;
        }
        int j=0;
        @Override
        public void run() {

            while(j<time){
                j++;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 화면에 초를 출력하기 위한 스레드
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int sec = (j / 100) % 60;
                        int min = (j / 100) / 60;
                        @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d", min, sec);
                        record_time.setText(result);
                    }
                });
            }

            handler.sendEmptyMessage(0);
        }
    }

    Handler handler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){   // Message id 가 0 이면
                play.setVisibility(View.VISIBLE);
                stop.setVisibility(View.GONE);
            }
            else if(msg.what == 1){
                int position = msg.arg1;
                View itemView = (View) msg.obj;
                TextView play_time = itemView.findViewById(R.id.play_time);
                itemView.findViewById(R.id.audio_play).setVisibility(View.VISIBLE);
                itemView.findViewById(R.id.audio_pause).setVisibility(View.GONE);
//                play_time.setText(msgLists.get(position).getPlay_time());
            }
        }
    };

    public static MultipartBody.Part toMultiPartFile(String name, byte[] byteArray) {
        RequestBody reqFile = RequestBody.create(MediaType.parse("audio/mp4"), byteArray);

        return MultipartBody.Part.createFormData(name,
                "tmp_name", // filename, this is optional
                reqFile);
    }




    /**
     * Convert String and File to Multipart for Retrofit Library
     * @param descriptionString
     * @return
     */
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(FileUtils.MIME_TYPE_TEXT), descriptionString);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {

        Log.e("prepareFilePart", "uri: " + fileUri.toString());
        // use the FileUtils to get the actual file by uri
        File file = FileUtils.getFile(this, fileUri);

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create (MediaType.parse(FileUtils.MIME_TYPE_IMAGE), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }



    @Override
    protected void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");

        try{
            client_socket.close();
            isRunning=false;
        }catch (Exception e){
            e.printStackTrace();
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    /**
     * 외부
     */
    @SuppressLint("IntentReset")
    private void showChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(Intent.createChooser(intent,"다중 선택은 '포토'를 선택하세요."), 1111);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1111){
            if(data == null){

            }
            else{
                if(data.getClipData() == null){
                    Log.e("1. single choice", String.valueOf(data.getData()));
                    Uri imageUri = data.getData();
                    imgList.add(imageUri);
                }
                else{
                    ClipData clipData = data.getClipData();
                    Log.e("clipData", String.valueOf(clipData.getItemCount()));

                    if(clipData.getItemCount() > 10){   // 선택한 사진이 11장 이상이라면
                        Toast.makeText(message_room.this, "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                    }
                    else{   // 선택한 사진이 1장 이상 10장 이하라면
                        for (int i = 0; i < clipData.getItemCount(); i++){

                            Uri imageUri = clipData.getItemAt(i).getUri();  // 선택한 사진들의 uri를 얻어낸다.
                            try {
                                imgList.add(imageUri);  //uri를 list에 담는다.

                            } catch (Exception e) {
                                Log.e(TAG, "File select error", e);
                            }
                        }
                    }
                }

                final ImgListAdapter mAdapter = new ImgListAdapter(message_room.this, imgList);   // uri가 담긴 리스트를 listview adapter에 넘겨준다.

                img_list_recycler = findViewById(R.id.img_recycler);

                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                img_list_recycler.setLayoutManager(layoutManager);
                img_list_recycler.setAdapter(mAdapter);

                img_list_recycler.setVisibility(View.VISIBLE);

                mAdapter.setOnItemClickListener(new ImgListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        imgList.remove(position);
                        mAdapter.notifyDataSetChanged();
                        // imgList의 사이즈가 0이면 이미지를 보여주는 리사이클러뷰를 안보이게 하고, edittext를 활성화시킨다.
                        if(imgList.size() == 0){
                            img_list_recycler.setVisibility(View.GONE);
                            edit_msg.setFocusableInTouchMode (true);
                            edit_msg.setFocusable(true);
                        }
                    }
                });

                // 이미지 전송 시 edittext를 비활성화 시킨다.
                edit_msg.setClickable(false);
                edit_msg.setFocusable(false);

                for (int i = 0; i < imgList.size(); i++){
                    Log.e(TAG, "uri: " + imgList.get(i).toString());
                }
            }
        }
    }

    /**
     * 서버 소켓과 클라이언트 소켓 연결을 처리하는 스레드
     */
    class ConnectionThread extends Thread {

        @Override
        public void run() {
            try {
                // 접속한다.
                final Socket socket = new Socket(MyApplication.socket_server_url, 3333);
                client_socket = socket;

                Log.e(TAG, "서버와 연결 성공");

                // 방 넘버, 상대 유저, 토큰 값을 서버로 전달
                // 미리 입력했던 닉네임을 서버로 전달한다.

                msg_item item = new msg_item();
                item.setFirst_name(first_name);
                item.setRoom_index(room_index);
                item.setUser_index(my_user_index);
                item.setToken(token);
                item.setType("user io");


                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                oos.writeObject(item);
                oos.flush();
                Log.e(TAG, "obj 전송 완료");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 접속 상태를 true로 셋팅한다.
                        isConnect=true;
                        // 메세지 수신을 위한 스레드 가동
                        isRunning=true;
                        MessageThread thread=new MessageThread(socket);
                        thread.start();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "ConnectionThread error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 서버로부터 메시지를 전달받는 스레드
     */
    class MessageThread extends Thread {
        Socket socket;
        DataInputStream dis;
        ObjectInputStream ois;
        public MessageThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try{

                ois = new ObjectInputStream(client_socket.getInputStream());
                Log.e(TAG, "MessageThread ois 생성");
                while (isRunning){

                    final msg_item item = (msg_item) ois.readObject();

                    Log.e(TAG, "item get from server");
                    Log.e(TAG, "type: " + item.getType());
                    Log.e(TAG, "msg: " + item.getMessage());

                    // 서버에서 보내는 메세지 타입이 msg 라면, chat msg
                    // 타입이 img 라면, 이미지
                    // 타입이 voice 라면, 음성 녹음 파일
                    // 타입이 user io 라면, 유저 in or 유저 out
                    if(item.getType().equals("msg")){
                        String msg = item.getMessage();
                        long time = item.getTime();
                        String user_index = item.getUser_index();

                        Log.e(TAG, "index: " + user_index);
                        Log.e(TAG, "msg: " + msg);
                        Log.e(TAG, "time: " + time);

                        if(my_user_index.equals(user_index)){

                            if(curPerson == 1){     // 현재 방에 나 혼자 있을 때, 읽음 처리 x
                                item.setStatus("no read");
                            }
                            else{    // 방에 상대방이 입장해 있을 때, 읽음 처리 o
                                item.setStatus("read");
                            }
                            msgLists.add(item);
                        }
                        else {

                            item.setProfile_img(profile_img);
                            msgLists.add(item);
                        }
                    }
                    else if(item.getType().equals("img")){

                        // 내가 보낸 이미지는 이미 처리됨
                        if(!my_user_index.equals(item.getUser_index())){
                            ArrayList<String> file_list = item.getImg_list();;

                            for (int i = 0; i < file_list.size(); i++){
                                msg_item img_item = new msg_item();
                                img_item.setType("img");
                                img_item.setMessage(file_list.get(i));
                                img_item.setTime(item.getTime());
                                img_item.setUser_index(item.getUser_index());
                                msgLists.add(img_item);
                                Log.e(TAG, "file name: " + file_list.get(i));
                            }
                        }

                    }
                    else if(item.getType().equals("audio")){
                        msgLists.add(item);
                    }
                    else if(item.getType().equals("user io")){  // 유저가 방에 들어오거나 나갈때 서버로부터 받는 메세지

                        if(item.getMessage().equals("1")){  // 방에 사람이 한명일 때
                            curPerson = 1;
                        }
                        else{   // 방에 사람이 2명 일때
                            curPerson = 2;

                            // 내가 보낸 메세지를 모두 읽음 처리 한다.
                            for (int i = 0; i < msgLists.size(); i++){
                                    if(msgLists.get(i).getUser_index().equals(my_user_index)){
                                        msgLists.get(i).setStatus("read");
                                    }
                            }
                        }
                    }

                    // 화면에 출력
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 리사이클러뷰 어댑터에 리스트에 데이터가 추가되었다고 알린다.
//                            chatRoomAdapter = new chatRoomAdapter(msgLists, my_user_index, message_room.this, profile_img);
//                            chat_recycler.setAdapter(chatRoomAdapter);

                            if(chat_recycler != null){
                                Log.e(TAG, "list size: " + msgLists.size());
                                chatRoomAdapter = new chatRoomAdapter(msgLists, my_user_index, message_room.this, profile_img);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(message_room.this);
                                linearLayoutManager.setStackFromEnd(true);
                                chat_recycler.setLayoutManager(linearLayoutManager);
                                chat_recycler.setAdapter(chatRoomAdapter);

                                chatRoomAdapter.setOnItemClickListener(new chatRoomAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View v, View itemView, int position) {
                                        if(v.getId() == R.id.audio_play){
                                            itemView.findViewById(R.id.audio_pause).setVisibility(View.VISIBLE);
                                            v.setVisibility(View.GONE);
                                            String url = "http://13.124.159.44/audio_file_upload/" + msgLists.get(position).getMessage();
                                            Log.e(TAG, "play url: " + url);
                                            playAudioChat(url);
                                            playAudioThread thread = new playAudioThread(msgLists.get(position).getPlay_time(), position, itemView);
                                            thread.start();
                                        }
                                        else if(v.getId() == R.id.audio_pause){
                                            itemView.findViewById(R.id.audio_play).setVisibility(View.VISIBLE);
                                            v.setVisibility(View.GONE);
                                            pauseAudioChat();
                                        }
                                        else {
//                            Toast.makeText(message_room.this, "아이템 뷰 클릭: " + position, Toast.LENGTH_SHORT).show();
                                            AlertDialog.Builder builder = new AlertDialog.Builder(message_room.this);
                                            LayoutInflater inflater = getLayoutInflater();
                                            View view = inflater.inflate(R.layout.dialog_translate, null);
                                            builder.setView(view);
                                            final LinearLayout translate = view.findViewById(R.id.translate);
//                                            final LinearLayout correct = view.findViewById(R.id.correct);
//                                            final LinearLayout comment = view.findViewById(R.id.comment);

                                            final AlertDialog dialog = builder.create();

                                            // 번역하기 클릭 리스너
                                            translate.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    google_translate translation = new google_translate(message_room.this);
                                                    translation.getTranslateService();
                                                    final String translated_msg = translation.translate(lang_code, msgLists.get(position).getMessage());
//                                    Toast.makeText(message_room.this, "번역된 메세지: " + translated_msg, Toast.LENGTH_SHORT).show();

                                                    retrofitClient.service.update_translated_msg(msgLists.get(position).getMessage_index(), translated_msg)
                                                            .enqueue(new Callback<getResult>() {
                                                                @Override
                                                                public void onResponse(Call<getResult> call, Response<getResult> response) {
                                                                    if(response.body().toString().equals("success")){
                                                                        // 번역된 메세지를 띄우는 뷰를 보여준다.
                                                                        TextView view = itemView.findViewById(R.id.translated_msg);
                                                                        view.setVisibility(View.VISIBLE);
                                                                        view.setText(translated_msg);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<getResult> call, Throwable t) {

                                                                }
                                                            });
                                                    dialog.dismiss();
                                                }
                                            });

                                            dialog.show();

                                        }
                                    }
                                });
                            }

                        }
                    });
                }

            }catch (Exception e){
                Log.e(TAG, "MessageThread error2: " + e.getMessage());
                e.printStackTrace();
            }
        }


    }

    /**
     * 오디오 메시지 플레이시 돌아가는 스레드 클래스
     */
    public class playAudioThread extends Thread{
        int time, position;
        View itemView;
        playAudioThread(int time, int position, View itemView){
            this.time = time;
            this.itemView = itemView;
            this.position = position;
        }
        int i=0;
        @Override
        public void run() {

            while(i<time){
                i++;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 화면에 초를 출력하기 위한 스레드
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int sec = (i / 100) % 60;
                        int min = (i / 100) / 60;
                        @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d", min, sec);
                        TextView playtime = itemView.findViewById(R.id.play_time);
                        playtime.setText(result);
                    }
                });
            }
            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.arg1 = position;
            msg.obj = itemView;
            handler.sendMessage(msg);
        }
    }

    /**
     * 서버에 데이터를 전달하는 스레드
     */
    class SendToServerThread extends Thread{
        Socket socket;
        msg_item item;
        ObjectOutputStream oos;


        public SendToServerThread(Socket socket, msg_item item){
            try{
                this.socket=socket;
                this.item = item;
            }catch (Exception e){
                Log.e(TAG, "SendToServerThread error1: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try{

                oos = new ObjectOutputStream(socket.getOutputStream());

                // 서버로 데이터를 보낸다.
                Log.e(TAG, "msg 전송 전");
                oos.writeObject(item);
                oos.flush();

                Log.e(TAG, "msg 전송 완료");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edit_msg.setText("");
                    }
                });
            }catch (Exception e){
                Log.e(TAG, "SendToServerThread error2: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    /**
     * 해당 방의 정보가 담긴 room 객체로부터 필요한 정보를 세팅해주는 메서드
     */
    public void set_room_info(){
        // room_obj으로부터 필요한 정보들을 세팅
        room_index = room_obj.getRoom_index();       // 메세지 저장 시 필요한 room index
        profile_img = room_obj.getProfile_img();     // 상대방의 프로필 이미지
        first_name = room_obj.getFirst_name();       // 상대방의 first_name;
        token = room_obj.getToken();                 // 상대방이 현재 방에 존재하지 않을 시, fcm 알림을 보내주기 위한 토큰

        Log.e(TAG, "room_index: " + room_index);
        Log.e(TAG, "profile_img: " + profile_img);
        Log.e(TAG, "first_name: " + first_name);
        Log.e(TAG, "token: " + token);

        /**
         * 전달받은 room index를 통해 서버로부터 해당 방에 존재하는 메세지 목록을 가져온다.
         * 상대방이 나에게 보낸 메세지를 모두 읽음 처리한다.
         */
        Log.e(TAG, "my index: " + my_user_index);
        retrofitClient = new RetrofitClient();
        call = retrofitClient.service.get_msg_list(room_index, my_user_index);
        call.enqueue(new Callback<ArrayList<msg_item>>() {
            @Override
            public void onResponse(Call<ArrayList<msg_item>> call, Response<ArrayList<msg_item>> response) {
                Log.e(TAG, "onResponse");
                ArrayList<msg_item> result = response.body();
                if(result != null){
                    msgLists.addAll(result);
                }

                /**
                 * 리사이클러뷰, 어댑터 등록
                 */
                chat_recycler = findViewById(R.id.msg_recycler);
                chatRoomAdapter = new chatRoomAdapter(msgLists, my_user_index, message_room.this, profile_img);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(message_room.this);
                linearLayoutManager.setStackFromEnd(true);
                chat_recycler.setLayoutManager(linearLayoutManager);
                chat_recycler.setAdapter(chatRoomAdapter);

                chatRoomAdapter.setOnItemClickListener(new chatRoomAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, final View itemView, final int position) {
                        if(v.getId() == R.id.audio_play){
                            itemView.findViewById(R.id.audio_pause).setVisibility(View.VISIBLE);
                            v.setVisibility(View.GONE);
                            String url = "http://13.124.159.44/audio_file_upload/" + msgLists.get(position).getMessage();
                            Log.e(TAG, "play url: " + url);
                            playAudioChat(url);
                            playAudioThread thread = new playAudioThread(msgLists.get(position).getPlay_time(), position, itemView);
                            thread.start();
                        }
                        else if(v.getId() == R.id.audio_pause){
                            itemView.findViewById(R.id.audio_play).setVisibility(View.VISIBLE);
                            v.setVisibility(View.GONE);
                            pauseAudioChat();
                        }
                        else {
//                            Toast.makeText(message_room.this, "아이템 뷰 클릭: " + position, Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(message_room.this);
                            LayoutInflater inflater = getLayoutInflater();
                            View view = inflater.inflate(R.layout.dialog_translate, null);
                            builder.setView(view);
                            final LinearLayout translate = view.findViewById(R.id.translate);
//                            final LinearLayout correct = view.findViewById(R.id.correct);
//                            final LinearLayout comment = view.findViewById(R.id.comment);

                            final AlertDialog dialog = builder.create();

                            // 번역하기 클릭 리스너
                            translate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    google_translate translation = new google_translate(message_room.this);
                                    translation.getTranslateService();
                                    final String translated_msg = translation.translate(lang_code, msgLists.get(position).getMessage());
//                                    Toast.makeText(message_room.this, "번역된 메세지: " + translated_msg, Toast.LENGTH_SHORT).show();

                                    retrofitClient.service.update_translated_msg(msgLists.get(position).getMessage_index(), translated_msg)
                                            .enqueue(new Callback<getResult>() {
                                                @Override
                                                public void onResponse(Call<getResult> call, Response<getResult> response) {
                                                        if(response.body().toString().equals("success")){
                                                            // 번역된 메세지를 띄우는 뷰를 보여준다.
                                                            TextView view = itemView.findViewById(R.id.translated_msg);
                                                            view.setVisibility(View.VISIBLE);
                                                            view.setText(translated_msg);
                                                        }
                                                }

                                                @Override
                                                public void onFailure(Call<getResult> call, Throwable t) {

                                                }
                                            });
                                    dialog.dismiss();
                                }
                            });

                            dialog.show();

                        }
                    }
                });

                Log.e(TAG, "msg list size: " + msgLists.size());
            }

            @Override
            public void onFailure(Call<ArrayList<msg_item>> call, Throwable t) {
                Log.e(TAG, "onFailure");
            }
        });
    }

    public void show_msg_option_dialog(){

    }
}