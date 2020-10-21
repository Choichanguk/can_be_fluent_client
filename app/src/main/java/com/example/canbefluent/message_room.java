package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canbefluent.adapter.chatRoomAdapter;
import com.example.canbefluent.items.msg_item;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.getImgList;
import com.example.canbefluent.pojoClass.getMsgList;
import com.example.canbefluent.pojoClass.getRoomList;
import com.example.canbefluent.retrofit.RetrofitClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
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



    DataInputStream is;
    DataOutputStream os;

    String msg="";  // 서버로부터 전송되는 msg
    EditText edit_msg;  //서버로 전송할 메세지를 작성하는 EditText
    ImageButton btn_send; //msg 보내는 버튼
    ImageButton btn_open_option;    // option버튼을 여는 버튼
    ImageButton btn_close_option;   // option 버튼을 닫는 버튼
    ImageButton btn_album, btn_camera, btn_record;
    RecyclerView img_list_recycler; // 유저가 선택한 이미지를 보여주는 뷰

    private ArrayList<Uri> imgList = new ArrayList<>();     // 선택한 이미지들의 Uri를 담는 리스트

    boolean isConnect = false; // 서버 접속여부를 판별하기 위한 변수 ( 만약 서버 접속이 끊긴다면 다른 방법으로 통신해야함)
    boolean isRunning=false;    // 어플 종료시 스레드 중지를 위해...
    Socket client_socket;     //클라이언트의 소켓
    ObjectInputStream ois;
    sharedPreference sharedPreference = new sharedPreference();

    MainActivity mainActivity = new MainActivity();
//    user_item my_info = mainActivity.user_item;     // 내 아이디 정보를 담고 있는 객체


    LinearLayout msg_option_layout;     // 이미지, 카메라, 음성메세지를 전송할 수 있는 버튼이 있는 레이아웃
//    ArrayList<getChatList> chatLists = new ArrayList<>();


    ArrayList<getMsgList> msgLists = new ArrayList<>();
    RecyclerView chat_recycler;
    chatRoomAdapter chatRoomAdapter;

    RetrofitClient retrofitClient;
    Call<ArrayList<getMsgList>> call;   // 메세지 정보를 불러ㅇ
    Call<ArrayList<getRoomList>> call2;
    Call<ArrayList<getImgList>> call3;


    int curPerson = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_room);

        msg_option_layout = findViewById(R.id.msg_option);

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

        my_user_index = sharedPreference.loadUserIndex(message_room.this);

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

                    // create a map of data to pass along
                    RequestBody room_Index = createPartFromString(room_index);
                    RequestBody user_index = createPartFromString(my_user_index);
                    RequestBody status;

                    if(curPerson == 1){
                        status = createPartFromString("no read");
                    }
                    else{
                        status = createPartFromString("read");
                    }
                    RequestBody size = createPartFromString(""+parts.size());

                    Log.e(TAG, "이미지 전송 room_Index: " + room_Index.toString());
                    Log.e(TAG, "이미지 전송 user_index: " + user_index.toString());
                    Log.e(TAG, "이미지 전송 status: " + status.toString());
                    Log.e(TAG, "이미지 전송 parts size: " + parts.size());

//                    retrofitClient = new RetrofitClient();
//                    call3 =retrofitClient.service.uploadMultiple(room_Index, user_index, status, size, parts);
//                    call3.enqueue(new Callback<ArrayList<getImgList>>() {
//                        @Override
//                        public void onResponse(Call<ArrayList<getImgList>> call, Response<ArrayList<getImgList>> response) {
//                            Log.e(TAG, "onResponse");
//                            Log.e(TAG, "img list size: " + response.body().size());
//                        }
//
//                        @Override
//                        public void onFailure(Call<ArrayList<getImgList>> call, Throwable t) {
//                            Log.e(TAG, "onResponse");
//                        }
//                    });


                }
                else{   // 일반 문자 메시지 전송
                    msg_item item = new msg_item();
                    item.setMsg(edit_msg.getText().toString());
                    item.setTime(System.currentTimeMillis());
                    item.setType("msg");

                    // 송신 스레드 가동
                    // msg객체를 보낸다.
                    SendToServerThread thread=new SendToServerThread(client_socket, item);
                    thread.start();
                }
//                String msg = edit_msg.getText().toString();
//                long curTime = System.currentTimeMillis();
            }
        });


        /**
         * 리사이클러뷰, 어댑터 등록
         */
        chat_recycler = findViewById(R.id.msg_recycler);
        chatRoomAdapter = new chatRoomAdapter(msgLists, my_user_index, message_room.this, profile_img);
        chat_recycler.setLayoutManager(new LinearLayoutManager(message_room.this));
        chat_recycler.setAdapter(chatRoomAdapter);


        btn_open_option = findViewById(R.id.btn_open_option);
        btn_open_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg_option_layout.setVisibility(View.VISIBLE);
                btn_close_option.setVisibility(View.VISIBLE);
                btn_open_option.setVisibility(View.GONE);
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
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
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


//                    listView.setAdapter(mAdapter);
//                    Toast.makeText(MainActivity.this, "다중 선택이 불가능한 기기입니다.", Toast.LENGTH_LONG).show();
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
                final Socket socket = new Socket("59.187.215.69", 3333);
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
            try {

//                this.socket = socket;
//                InputStream is =
//                dis = new DataInputStream(is);



            } catch (Exception e) {
                Log.e(TAG, "MessageThread error1: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try{

                ois = new ObjectInputStream(client_socket.getInputStream());
                Log.e(TAG, "MessageThread ois 생성");
                while (isRunning){

                    msg_item item = (msg_item) ois.readObject();

                    Log.e(TAG, "item get from server");
                    Log.e(TAG, "type: " + item.getType());
                    Log.e(TAG, "msg: " + item.getMsg());
                    // 서버에서 보내는 메세지 타입이 msg라면 chat msg
                    // 타입이 user in 라면, 새 유저 입장
                    // 타입이 user out 라면, 유저 나감
                    if(item.getType().equals("msg")){
                        String msg = item.getMsg();
                        String time = item.getTime()+"";
                        String user_index = item.getUser_index();

                        Log.e(TAG, "index: " + user_index);
                        Log.e(TAG, "msg: " + msg);
                        Log.e(TAG, "time: " + time);
                        Log.e(TAG, "time: " + time);

                        if(my_user_index.equals(user_index)){

                            getMsgList item2 = new getMsgList();
                            item2.setMessage(msg);
                            item2.setTime(time);
                            item2.setUser_index(user_index);

                            if(curPerson == 1){     // 현재 방에 나 혼자 있을 때, 읽음 처리 x
                                item2.setStatus("no read");
                            }
                            else if(curPerson == 2){    // 방에 상대방이 입장해 있을 때, 읽음 처리 o
                                item2.setStatus("read");
                            }

                            msgLists.add(item2);
                        }
                        else {
                            getMsgList item2 = new getMsgList();
                            item2.setMessage(msg);
                            item2.setTime(time);
                            item2.setUser_index(user_index);
                            item2.setProfile_img(profile_img);
                            msgLists.add(item2);
                        }
                    }
                    else if(item.getType().equals("user io")){  // 유저가 방에 들어오거나 나갈때 서버로부터 받는 메세지

                        if(item.getMsg().equals("1")){  // 방에 사람이 한명일 때
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
                            chatRoomAdapter.notifyDataSetChanged();
                            Log.e(TAG, "list size: " + msgLists.size());
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
//                this.msg=msg;
//                this.time=time;

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

                if(item.getType().equals("msg")){   // msg type이 msg일 때(문자 채팅)
                    oos.writeObject(item);
                    oos.flush();
                }
                else if(item.getType().equals("img")){  // msg type이 img일 때(이미지 파일)

                }
                else if(item.getType().equals("voice")){    // msg type이 voice 일 때(음성 녹음 파일)

                }
//                msg_item item = new msg_item();
//                item.setType("msg");
//                item.setMsg(msg);
//                item.setTime(time);


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
        retrofitClient = new RetrofitClient();
        call = retrofitClient.service.get_msg_list(room_index, my_user_index);
        call.enqueue(new Callback<ArrayList<getMsgList>>() {
            @Override
            public void onResponse(Call<ArrayList<getMsgList>> call, Response<ArrayList<getMsgList>> response) {
                Log.e(TAG, "onResponse");
                ArrayList<getMsgList> result = response.body();
                if(result != null){
                    msgLists.addAll(result);
                }

                Log.e(TAG, "msg list size: " + msgLists.size());
                chatRoomAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<ArrayList<getMsgList>> call, Throwable t) {
                Log.e(TAG, "onFailure");
            }
        });
    }



}