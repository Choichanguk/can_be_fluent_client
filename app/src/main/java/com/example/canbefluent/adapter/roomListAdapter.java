package com.example.canbefluent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.R;
import com.example.canbefluent.pojoClass.getRoomList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class roomListAdapter extends RecyclerView.Adapter<roomListAdapter.ViewHolder>{
    private ArrayList<getRoomList> mData = null ;
    private Context context;

    // 리스너 객체 참조를 저장하는 변수
    private roomListAdapter.OnItemClickListener mListener = null ;

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public roomListAdapter(ArrayList<getRoomList> list) {
        mData = list ;
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(roomListAdapter.OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }


    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView last_msg, time, first_name;
        CircleImageView profile_img;


        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            last_msg = itemView.findViewById(R.id.last_chat) ;
            time = itemView.findViewById(R.id.time) ;
            first_name = itemView.findViewById(R.id.user_name) ;
            profile_img = itemView.findViewById(R.id.profile_img) ;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListener != null) {
                            mListener.onItemClick(v, pos) ;
                        }
                    }
                }
            });

        }
    }



    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public roomListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.chat_room_item, parent, false) ;
        roomListAdapter.ViewHolder vh = new roomListAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(roomListAdapter.ViewHolder holder, int position) {
        getRoomList item = mData.get(position) ;
        String url = MyApplication.server_url + "/profile_img/" + item.getProfile_img();

        long time = Long.parseLong(item.getTime());

        String time_str = new SimpleDateFormat("HH:mm").format(time);

        holder.last_msg.setText(item.getLast_message());
        holder.first_name.setText(item.getFirst_name());
        holder.time.setText(time_str);
        Glide.with(context)
                .load(url)
                .into(holder.profile_img);

    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }
}
