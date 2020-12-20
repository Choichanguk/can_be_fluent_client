package com.example.canbefluent.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.R;
import com.example.canbefluent.items.msg_item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<msg_item> mData = null ;
    private String user_index;
    private Context mContext;
    private String profile_img;

    public chatRoomAdapter(ArrayList<msg_item> list, String user_index, Context context, String profile_img){
        mData = list;
        this.user_index = user_index;
        mContext = context;
        this.profile_img = profile_img;
    }

    // 리스너 객체 참조를 저장하는 변수
    private chatRoomAdapter.OnItemClickListener mListener = null ;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(chatRoomAdapter.OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, View itemView, int position) ;
    }

    @Override
    public int getItemViewType(int position) {
        msg_item item = mData.get(position);
        if (item.getUser_index().equals(user_index) && item.getType().equals("msg")) {  // 내가 보낸 메세지 and 일반 메세지 타입
            return 0;
        }
        else if(!item.getUser_index().equals(user_index) && item.getType().equals("msg")){  // 상대가 보낸 메세지 and 일반 메세지 타입
            return 1;
        }
        else if(item.getUser_index().equals(user_index) && item.getType().equals("img")){   // 내가 보낸 메세지 and 이미지 메세지 타입
            return 2;
         }
        else if(!item.getUser_index().equals(user_index) && item.getType().equals("img")){  // 상대가 보낸 메세지 and 이미지 메세지 타입
            return 3;
        }
        else if(item.getUser_index().equals(user_index) && item.getType().equals("audio")){  // 내가 보낸 audio
            return 4;
        }
        else if(!item.getUser_index().equals(user_index) && item.getType().equals("audio")){  // 상대가 보낸 audio
            return 5;
        }
        else{
            return 0;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType) {
            // 내가 보낸 문자 메세지
            case 0:
                Log.e("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.e("FFFF", "온크리트뷰홀더 : 0인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.my_chat_item, viewGroup, false);
                return new ChatMessageViewHolder2(view);
            // 상대가 보낸 문자 메세지
            case 1:
                Log.e("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.e("FFFF", "온크리트뷰홀더 : 1인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.opp_chat_item, viewGroup, false);
                return new ChatMessageViewHolder(view);
            // 내가 보낸 이미지 메세지
            case 2:
                Log.e("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.e("FFFF", "온크리트뷰홀더 : 2인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.my_img_item, viewGroup, false);
                return new ChatMessageViewHolder4(view);
            // 상대가 보낸 이미지 메세지
            case 3:
                Log.e("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.e("FFFF", "온크리트뷰홀더 : 3인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.opp_img_item, viewGroup, false);
                return new ChatMessageViewHolder3(view);

            // 내가 보낸 음성 메세지
            case 4:
                Log.e("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.e("FFFF", "온크리트뷰홀더 : 4인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.my_audio_item, viewGroup, false);
                return new ChatMessageViewHolder6(view);
            // 상대가 보낸 음성 메세지
            case 5:
                Log.e("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.e("FFFF", "온크리트뷰홀더 : 5인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.opp_audio_item, viewGroup, false);
                return new ChatMessageViewHolder5(view);
        }
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.opp_chat_item, viewGroup, false);
        return new ChatMessageViewHolder(view);
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        msg_item item = mData.get(position);
        long time = item.getTime();

        String time_str = new SimpleDateFormat("HH:mm").format(time);

        // 문자 메세지
        if(item.getType().equals("msg")){
            if(item.getUser_index().equals(user_index)){    // 내가보낸 메세지
                ChatMessageViewHolder2 holder2 = (ChatMessageViewHolder2) holder;
                holder2.msg.setText(item.getMessage());
                holder2.time.setText(time_str);
                if(item.getStatus().equals("no read")){
                    holder2.status.setText("1");
                }
                else{
                    holder2.status.setText("");
                }
            }
            else{   // 상대방이 보낸 메세지
                String url = MyApplication.server_url + "/profile_img/" + profile_img;
                ChatMessageViewHolder holder1 = (ChatMessageViewHolder) holder;
                holder1.msg.setText(item.getMessage());
                holder1.time.setText(time_str);
                Glide.with(mContext)
                        .load(url)
                        .into(holder1.profile_img);

                if(item.getTranslated_message() != null){
                    holder1.translated_msg.setText(item.getTranslated_message());
                    holder1.translated_msg.setVisibility(View.VISIBLE);
                }else{
                    holder1.translated_msg.setVisibility(View.GONE);
                }
            }
        }
        // 이미지 메세지
        else if(item.getType().equals("img")){
            String url = MyApplication.server_url + "/chat_img/" + item.getMessage();
            Log.e("adpater", "url: " + url);
            GradientDrawable drawable= (GradientDrawable) mContext.getDrawable(R.drawable.img_round);
            if(item.getUser_index().equals(user_index)){    // 내가 보낸 이미지
                ChatMessageViewHolder4 holder4 = (ChatMessageViewHolder4) holder;
                holder4.img_chat.setBackground(drawable);
                holder4.time.setText(time_str);
                if(item.getStatus().equals("no read")){
                    holder4.status.setText("1");
                }
                else{
                    holder4.status.setText("");
                }
                Glide.with(mContext)
                        .load(url)
                        .into(holder4.img_chat);
            }
            else {  // 상대방이 보낸 이미지
                String url_profile = MyApplication.server_url + "/profile_img/" + profile_img;
                ChatMessageViewHolder3 holder3 = (ChatMessageViewHolder3) holder;
                holder3.img_chat.setBackground(drawable);
                holder3.time.setText(time_str);
                Glide.with(mContext)
                        .load(url)
                        .into(holder3.img_chat);
                Glide.with(mContext)
                        .load(url_profile)
                        .into(holder3.profile_img);
            }
        }
        // 음성 메시지
        else if(item.getType().equals("audio")){
            int play_time = item.getPlay_time();
            int sec = (play_time / 100) % 60;
            int min = (play_time / 100) / 60;
            @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d", min, sec);

            if(item.getUser_index().equals(user_index)){    // 내가 보낸 음성 메시지
                ChatMessageViewHolder6 holder6 = (ChatMessageViewHolder6) holder;

                holder6.play_time.setText(result);
                holder6.time.setText(time_str);
                if(item.getStatus().equals("no read")){
                    holder6.status.setText("1");
                }
                else{
                    holder6.status.setText("");
                }
            }
            else{   // 상대가 보낸 음성 메시지
                ChatMessageViewHolder5 holder5 = (ChatMessageViewHolder5) holder;
                String url_profile = MyApplication.server_url + "/profile_img/" + profile_img;
                holder5.play_time.setText(result);
                holder5.time.setText(time_str);
                Glide.with(mContext)
                        .load(url_profile)
                        .into(holder5.profile_img);

            }
        }

    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // 상대방 채팅 뷰들을 바인딩해주는 메서드
    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile_img;
        TextView msg;
        TextView translated_msg;
        TextView time;



        public ChatMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            profile_img = itemView.findViewById(R.id.profile_img);
            msg = itemView.findViewById(R.id.msg);
            time = itemView.findViewById(R.id.time);
            translated_msg = itemView.findViewById(R.id.translated_msg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListener != null) {
                            mListener.onItemClick(v, itemView, pos) ;
                        }
                    }
                }
            });
        }


    }

    // 내 채팅 뷰들을 바인딩 해주는 메서드.
    public class ChatMessageViewHolder2 extends RecyclerView.ViewHolder {

        TextView msg;
        TextView time;
        TextView status;

        public ChatMessageViewHolder2(@NonNull final View itemView) {
            super(itemView);
            msg = itemView.findViewById(R.id.msg);
            time = itemView.findViewById(R.id.time);
            status = itemView.findViewById(R.id.status);
        }
    }

    // 상대방 이미지 뷰들을 바인딩 해주는 메서드.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public class ChatMessageViewHolder3 extends RecyclerView.ViewHolder {


        TextView time;
        ImageView img_chat, profile_img;
        public ChatMessageViewHolder3(@NonNull final View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            img_chat = itemView.findViewById(R.id.img_chat);
            profile_img = itemView.findViewById(R.id.profile_img);
        }
    }

    // 내 이미지 메세지 뷰들을 바인딩 해주는 메서드.
    public class ChatMessageViewHolder4 extends RecyclerView.ViewHolder {

        ImageView img_chat;
        TextView time;
        TextView status;

        public ChatMessageViewHolder4(@NonNull final View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            status = itemView.findViewById(R.id.status);
            img_chat = itemView.findViewById(R.id.img_chat);
        }
    }

    // 상대방 audio 뷰들을 바인딩 해주는 메서드.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public class ChatMessageViewHolder5 extends RecyclerView.ViewHolder {


        TextView play_time, time;
        ImageButton audio_play, audio_pause;
        ImageView profile_img;
        public ChatMessageViewHolder5(@NonNull final View itemView) {
            super(itemView);
            play_time = itemView.findViewById(R.id.play_time);
            audio_play = itemView.findViewById(R.id.audio_play);
            audio_pause = itemView.findViewById(R.id.audio_pause);
            profile_img = itemView.findViewById(R.id.profile_img);
            time = itemView.findViewById(R.id.time);

            audio_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListener != null) {
                            mListener.onItemClick(v, itemView, pos) ;
                        }
                    }
                }
            });

            audio_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListener != null) {
                            mListener.onItemClick(v, itemView, pos) ;
                        }
                    }
                }
            });
        }
    }

    // 내 audio 뷰들을 바인딩 해주는 메서드.
    public class ChatMessageViewHolder6 extends RecyclerView.ViewHolder {

        TextView play_time, time, status;
        ImageButton audio_play, audio_pause;

        public ChatMessageViewHolder6(@NonNull final View itemView) {
            super(itemView);
            play_time = itemView.findViewById(R.id.play_time);
            audio_play = itemView.findViewById(R.id.audio_play);
            audio_pause = itemView.findViewById(R.id.audio_pause);
            time = itemView.findViewById(R.id.time);
            status = itemView.findViewById(R.id.status);

            audio_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListener != null) {
                            mListener.onItemClick(v, itemView, pos) ;
                        }
                    }
                }
            });

            audio_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListener != null) {
                            mListener.onItemClick(v, itemView, pos) ;
                        }
                    }
                }
            });
        }
    }


}
