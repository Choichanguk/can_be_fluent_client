package com.example.canbefluent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canbefluent.adapter.roomListAdapter;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.getRoomList;
import com.example.canbefluent.retrofit.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag_chats extends Fragment {
    private static final String TAG = "frag_chats";
    private View view;
    RetrofitClient retrofitClient;
    Call<ArrayList<getRoomList>> call;

    RecyclerView room_list_recycle;
    roomListAdapter adapter;

    String user_index = MainActivity.user_item.getUser_index();

    MainActivity instance = new MainActivity();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_chats,container,false);

//        if(MainActivity.socket != null){
//            instance.visible_floating_view("visible");
//        }
//        else{
//            instance.visible_floating_view("gone");
//        }

        /**
         * 서버로부터 모든 채팅방의 목록을 불러온다.
         */
        retrofitClient = new RetrofitClient();
        call = retrofitClient.service.get_room_list(user_index);
        call.enqueue(new Callback<ArrayList<getRoomList>>() {
            @Override
            public void onResponse(Call<ArrayList<getRoomList>> call, Response<ArrayList<getRoomList>> response) {
                ArrayList<getRoomList> result = response.body();
                Log.e(TAG, "room size: " + result.size());
                final ArrayList<getRoomList> roomLists = new ArrayList<>();

                // 가져온 룸 리스트를 만들어둔 룸 리스트에 담는다.
                for (int i = 0; i < result.size(); i++){
                    if(result.get(i).getLast_message() != null){
                        roomLists.add(result.get(i));
                    }
                }

                // 방 개수가 0개가 아닐때만 리사이클러뷰 등록한다.
                if(roomLists.size() != 0){
                    room_list_recycle = view.findViewById(R.id.room_recycle);
                    adapter = new roomListAdapter(roomLists);
                    room_list_recycle.setLayoutManager(new LinearLayoutManager(getActivity()));
                    room_list_recycle.setAdapter(adapter);
                    adapter.setOnItemClickListener(new roomListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int position) {
                            // 리사이클러뷰에 있는 방을 클릭 시 해당 방에 대한 정보를 담은 room 객체를 message_room 액티비티로 전해준다.
                            Toast.makeText(getActivity(), "room index: " + roomLists.get(position).getRoom_index(), Toast.LENGTH_LONG).show();
                            String room_index = roomLists.get(position).getRoom_index();
                            Intent intent = new Intent(getActivity(), message_room.class);
                            intent.putExtra("room obj", roomLists.get(position));
                            intent.putExtra("type", "from room list");
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ArrayList<getRoomList>> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });

        return view;
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if(type.equals("find candidate")){
                String name = intent.getStringExtra("name");
                String profile = intent.getStringExtra("profile");
                Log.e(TAG, "test name: " + name);

                for (Fragment fragment: getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment.isVisible()) {
                        Log.e(TAG, "for문 실행");
                        if(fragment instanceof frag_chats){

                            frag_randomCall.e.show(getFragmentManager(), random_call_dialog.TAG_DIALOG);
                        }
                    }
                }
            }
            else if(type.equals("cancel")){

                frag_randomCall.e.dismissDialog();
                Toast.makeText(getActivity(), "상대방이 매칭을 취소했습니다.", Toast.LENGTH_LONG).show();
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

    private void openDialog(String lang_code, String type) {

        DialogFragment myDialogFragment = new MyDialogFragment(lang_code, type);

        myDialogFragment.setTargetFragment(this, 0);

        myDialogFragment.show(getFragmentManager(), "Search Filter");

    }
}
