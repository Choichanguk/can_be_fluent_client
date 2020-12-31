package com.example.canbefluent.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.R;
import com.example.canbefluent.items.callLog_item;
import com.example.canbefluent.utils.MyApplication;

import java.util.ArrayList;


public class callLogAdapter extends RecyclerView.Adapter<callLogAdapter.ViewHolder> {

    private ArrayList<callLog_item> mData = null ;
    private Context mContext;
    String url = MyApplication.server_url + "/profile_img/";

    // 리스너 객체 참조를 저장하는 변수
    private callLogAdapter.OnItemClickListener mListener = null ;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(callLogAdapter.OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public callLogAdapter(ArrayList<callLog_item> list, Context context) {
        mData = list ;
        mContext = context;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, call_time, call_date;
        ImageView profile;


        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조.
            name = itemView.findViewById(R.id.user_name);
            call_time = itemView.findViewById(R.id.call_time);
            call_date = itemView.findViewById(R.id.call_date);
            profile = itemView.findViewById(R.id.profile_img);

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
    // LayoutInflater - XML에 정의된 Resource(자원) 들을 View의 형태로 반환.
    @Override
    public callLogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;    // context에서 LayoutInflater 객체를 얻는다.

        View view = inflater.inflate(R.layout.call_log_item, parent, false) ;	// 리사이클러뷰에 들어갈 아이템뷰의 레이아웃을 inflate.
        callLogAdapter.ViewHolder vh = new callLogAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(callLogAdapter.ViewHolder holder, int position) {
        callLog_item item = mData.get(position);
        int call_time = item.getCall_time();
        int call_min = call_time/60;
        holder.name.setText(item.getFirst_name());
        holder.call_time.setText(call_min+"분");
        holder.call_date.setText(item.getCall_date());

        Glide.with(mContext)
                .load(url + item.getProfile_img())
                .into(holder.profile);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }
}

