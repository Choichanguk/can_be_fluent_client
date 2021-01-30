package com.example.canbefluent;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canbefluent.adapter.callLogAdapter;
import com.example.canbefluent.items.callLog_item;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.utils.Constants;
import com.example.canbefluent.utils.MyApplication;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.socket.client.Socket.EVENT_CONNECT;

//import javax.swing.text.View;

public class frag_randomCall extends Fragment implements MyDialogFragment.MyDialogListener{
    private static final String TAG = "frag_randomCall";
    private View view;
    user_item user_item = MainActivity.user_item;

    TextView native_lang, practice_lang, btn_set_call, btn_call_log, no_log_view;

    String native_lang_code, practice_lang_code;

    ConstraintLayout find_call_view, call_log_view;
    LinearLayout set_option_view;
    RecyclerView call_log_recycle;
    callLogAdapter adapter;

    private Socket socket;

//    MainActivity instance = new MainActivity();
    MyApplication instance = new MyApplication();

    public static Intent serviceIntent;
    public static random_call_dialog e;

    public static randomCall_service ms; // 서비스 객체
    boolean isService = false; // 서비스 중인 확인용

    RetrofitClient retrofitClient = new RetrofitClient();

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            randomCall_service.MyBinder mb = (randomCall_service.MyBinder) service;

            ms = mb.getService();
            // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
        }
        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_random_call,container,false);

        native_lang_code = user_item.getPractice_lang1();
        practice_lang_code = user_item.getNative_lang1();

        native_lang = view.findViewById(R.id.native_lang);
        practice_lang = view.findViewById(R.id.practice_lang);

        Log.e(TAG, "모국어 코드: " + native_lang_code + "/ 연습언어 코드: " + practice_lang_code);

        Log.e(TAG, "모국어: " + MyApplication.lang_code_map.get(native_lang_code) + "/ 연습언어: " + MyApplication.lang_code_map.get(practice_lang_code));

        native_lang.setText(MyApplication.lang_code_map.get(practice_lang_code));     // 상대방의 모국어    (내 연습 언어)
        practice_lang.setText(MyApplication.lang_code_map.get(native_lang_code));     // 상대방의 연습 언어 (내 모국어)


        set_option_view = view.findViewById(R.id.set_option_view);  // 대화 상대 설정 뷰
        find_call_view = view.findViewById(R.id.find_call_view);    // 대화 상대 찾기 뷰
        call_log_view = view.findViewById(R.id.call_log_view);      // 통화 기록 보기 뷰

        no_log_view = view.findViewById(R.id.no_log_view);

        ImageButton btn_edit_native = view.findViewById(R.id.btn_edit_native);
        ImageButton btn_edit_practice = view.findViewById(R.id.btn_edit_practice);

        btn_set_call = view.findViewById(R.id.btn_set_call);
        btn_call_log = view.findViewById(R.id.btn_call_log);

        call_log_recycle = view.findViewById(R.id.call_log_recycle);


        /**
         * 랜덤통화 설정 버튼
         */
        btn_set_call.setOnClickListener(v -> {
            if(MainActivity.isSearching){
                find_call_view.setVisibility(View.VISIBLE);
                set_option_view.setVisibility(View.GONE);
            }
            else{
                find_call_view.setVisibility(View.GONE);
                set_option_view.setVisibility(View.VISIBLE);
            }
            call_log_view.setVisibility(View.GONE);

            btn_set_call.setTextColor(Color.parseColor("#36A2A6"));
            btn_call_log.setTextColor(Color.parseColor("#000000"));
        });

        /**
         * 랜덤통화 기록 보기 버튼
         */
        btn_call_log.setOnClickListener(v -> {
            find_call_view.setVisibility(View.GONE);
            set_option_view.setVisibility(View.GONE);
            call_log_view.setVisibility(View.VISIBLE);

            btn_set_call.setTextColor(Color.parseColor("#000000"));
            btn_call_log.setTextColor(Color.parseColor("#36A2A6"));
        });

        /**
         * 통화 상대방 모국어 수정 버튼
         */
        btn_edit_native.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(practice_lang_code, "native");
            }
        });

        /**
         * 통화 상대방 연습언어 수정 버튼
         */
        btn_edit_practice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(native_lang_code, "practice");
            }
        });

        /**
         * 매칭 시작 버튼
         */
        LinearLayout btn_start_call = view.findViewById(R.id.btn_start);
        btn_start_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                find_call_view.setVisibility(View.VISIBLE);
                set_option_view.setVisibility(View.GONE);
                serviceIntent =  new Intent(getActivity(), randomCall_service.class);
                serviceIntent.putExtra("native",native_lang_code);
                serviceIntent.putExtra("practice", practice_lang_code);

                getActivity().bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

                getActivity().startService(serviceIntent);

                MainActivity.search_floating_view.setVisibility(View.VISIBLE);
                MainActivity.isSearching = true;

            }
        });


        /**
         * 매칭 취소 버튼
         */
        LinearLayout btn_cancel_call = view.findViewById(R.id.btn_cancel_call);
        btn_cancel_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find_call_view.setVisibility(View.GONE);
                set_option_view.setVisibility(View.VISIBLE);

                getActivity().unbindService(conn);
                getActivity().stopService(serviceIntent);

                MainActivity.search_floating_view.setVisibility(View.GONE);
                MainActivity.isSearching = false;

            }
        });

        /**
         * 랜덤 통화 기록 불러오는 메서드
         */
//        get_call_log();
        return view;
    }


    private void openDialog(String lang_code, String type) {

        DialogFragment myDialogFragment = new MyDialogFragment(lang_code, type);

        myDialogFragment.setTargetFragment(this, 0);

        myDialogFragment.show(getFragmentManager(), "Search Filter");

    }



    @Override
    public void myCallback(ArrayList list) {
        if(list.get(0).equals("native")){
            practice_lang_code = (String) list.get(1);
            native_lang.setText(MyApplication.lang_code_map.get((String) list.get(1)));
            Log.e(TAG, "native lang code: " + native_lang_code);
        }
        else{

            native_lang_code = (String) list.get(1);
            practice_lang.setText(MyApplication.lang_code_map.get((String) list.get(1)));
            Log.e(TAG, "practice lang code: " + practice_lang_code);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            sendMessage("bye");
            socket.disconnect();
        }
    }

    private void sendMessage(Object message) {
        Log.e(TAG, "sendMessage");
        socket.emit("message", message);
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if(type.equals("find candidate")){
                user_item item = (user_item) intent.getSerializableExtra("user item");
//                Log.e(TAG, "test name: " + name);

                for (Fragment fragment: getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment.isVisible()) {
                        Log.e(TAG, "for문 실행");
                        if(fragment instanceof frag_randomCall){

                            e = new random_call_dialog(item);
                            e.show(getFragmentManager(), random_call_dialog.TAG_DIALOG);
                        }
                    }
                }
            }
            else if(type.equals("cancel")){

                for (Fragment fragment: getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment.isVisible()) {
                        Log.e(TAG, "for문 실행");
                        if(fragment instanceof frag_randomCall){

                            e.dismissDialog();
                            Toast.makeText(getActivity(), "상대방이 매칭을 취소했습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                }

            }
            else if(type.equals("time out")){
                for (Fragment fragment: getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment.isVisible()) {
                        Log.e(TAG, "for문 실행");
                        if(fragment instanceof frag_randomCall){

                            MainActivity.search_floating_view.setVisibility(View.GONE);
                            MainActivity.isSearching = false;


                            show();
//                            Toast.makeText(getActivity(), "매칭 타임아웃", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                find_call_view.setVisibility(View.GONE);
                call_log_view.setVisibility(View.GONE);
                set_option_view.setVisibility(View.VISIBLE);
            }



        }
    };


    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter("random call")
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                invitationResponseReceiver
        );
    }

    private void get_call_log(){
        retrofitClient.service.get_call_log(MainActivity.user_item.getUser_index())
                .enqueue(new Callback<ArrayList<callLog_item>>() {
                    @Override
                    public void onResponse(Call<ArrayList<callLog_item>> call, Response<ArrayList<callLog_item>> response) {
                        Log.e(TAG, "onResponse msg: " + response.body().toString());

                        if(response.isSuccessful()){
                            ArrayList<callLog_item> list = response.body();

                            if(list.size() != 0){
                                no_log_view.setVisibility(View.GONE);

                                call_log_recycle.setLayoutManager(new LinearLayoutManager(getContext()));
                                adapter = new callLogAdapter(list, getActivity());
                                adapter.setOnItemClickListener(new callLogAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View v, int position) {
                                        String user_index = list.get(position).getUser_index();
                                        Log.e(TAG, "선택 유저 idx: " + user_index);
                                        user_item user_item = new user_item();

                                        for (int i = 0; i < MyApplication.user_list.size(); i++){
                                            user_item item = MyApplication.user_list.get(i);
                                            if(user_index.equals(item.getUser_index())){
                                                user_item = item;
                                                break;
                                            }
                                        }

                                        Intent intent = new Intent(getActivity(), user_profile_activity.class);
                                        intent.putExtra("user item", user_item);

                                        startActivity(intent);
                                    }
                                });
                                call_log_recycle.setAdapter(adapter);
                            }
                            else{
                                no_log_view.setVisibility(View.VISIBLE);
                            }

                        }



                    }

                    @Override
                    public void onFailure(Call<ArrayList<callLog_item>> call, Throwable t) {

                    }
                });
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        get_call_log();
        if(!MainActivity.isSearching){
            find_call_view.setVisibility(View.GONE);
            call_log_view.setVisibility(View.GONE);
            set_option_view.setVisibility(View.VISIBLE);
            MainActivity.search_floating_view.setVisibility(View.GONE);
        }
    }

    public void show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("매칭 타임아웃");
        builder.setMessage("조건에 맞는 대화상대가 없습니다. 잠시 후 다시 매칭을 시도해주세요.");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }
}
