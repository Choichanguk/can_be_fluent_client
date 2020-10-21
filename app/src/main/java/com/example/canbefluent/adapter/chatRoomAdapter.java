package com.example.canbefluent.adapter;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.R;
import com.example.canbefluent.pojoClass.getChatList;
import com.example.canbefluent.pojoClass.getMsgList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<getMsgList> mData = null ;
    private String user_index;
    private Context mContext;
    private String profile_img;

    public chatRoomAdapter(ArrayList<getMsgList> list, String user_index, Context context, String profile_img){
        mData = list;
        this.user_index = user_index;
        mContext = context;
        this.profile_img = profile_img;
    }

    @Override
    public int getItemViewType(int position) {
        getMsgList item = mData.get(position);
        if (item.getUser_index().equals(user_index)) {
            return 0;
        } else {
            return 1;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                Log.d("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.d("FFFF", "온크리트뷰홀더 : 0인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.my_chat_item, viewGroup, false);
                return new ChatMessageViewHolder2(view);
            case 1:
                Log.d("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.d("FFFF", "온크리트뷰홀더 : 1인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.opp_chat_item, viewGroup, false);
                return new ChatMessageViewHolder(view);
        }
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.opp_chat_item, viewGroup, false);
        return new ChatMessageViewHolder(view);
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        getMsgList item = mData.get(position);
        long time = Long.parseLong(item.getTime());

        String time_str = new SimpleDateFormat("HH:mm").format(time);

        if(item.getUser_index().equals(user_index)){
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
        else{
            String url = "http://52.78.58.117/profile_img/" + profile_img;
            ChatMessageViewHolder holder1 = (ChatMessageViewHolder) holder;
            holder1.msg.setText(item.getMessage());
            holder1.time.setText(time_str);
            Glide.with(mContext)
                    .load(url)
                    .into(holder1.profile_img);
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
}
