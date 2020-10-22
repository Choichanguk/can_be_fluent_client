package com.example.canbefluent.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.R;
import com.example.canbefluent.items.msg_item;
import com.example.canbefluent.my_profile;
import com.example.canbefluent.pojoClass.getChatList;
import com.example.canbefluent.pojoClass.getMsgList;

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
                String url = "http://52.78.58.117/profile_img/" + profile_img;
                ChatMessageViewHolder holder1 = (ChatMessageViewHolder) holder;
                holder1.msg.setText(item.getMessage());
                holder1.time.setText(time_str);
                Glide.with(mContext)
                        .load(url)
                        .into(holder1.profile_img);
            }
        }
        // 이미지 메세지
        else if(item.getType().equals("img")){
            String url = "http://52.78.58.117/chat_img/" + item.getMessage();
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
                String url_profile = "http://52.78.58.117/profile_img/" + profile_img;
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
        TextView time;


        public ChatMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            profile_img = itemView.findViewById(R.id.profile_img);
            msg = itemView.findViewById(R.id.msg);
            time = itemView.findViewById(R.id.time);
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


}
