package com.example.canbefluent.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.R;
import com.example.canbefluent.items.visitor_item;

import java.util.ArrayList;

public class visitorAdapter extends RecyclerView.Adapter<visitorAdapter.ViewHolder>{

    private ArrayList<visitor_item> mData = null ;
    private Context mContext;

    // 리스너 객체 참조를 저장하는 변수
    private visitorAdapter.OnItemClickListener mListener = null ;


    // 생성자에서 데이터 리스트 객체를 전달받음.
    public visitorAdapter(ArrayList<visitor_item> list, Context context) {
        mData = list ;
        mContext = context;
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(visitorAdapter.OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, visitor_time;
        Button btn_follow;
        ImageView profile_image;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조.
            name = itemView.findViewById(R.id.user_name) ;
            visitor_time = itemView.findViewById(R.id.visit_time);
            btn_follow = itemView.findViewById(R.id.btn_follow);
            profile_image = itemView.findViewById(R.id.profile_img);

            btn_follow.setOnClickListener(new View.OnClickListener() {
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
    public visitorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;    // context에서 LayoutInflater 객체를 얻는다.

        View view = inflater.inflate(R.layout.visitor_item, parent, false) ;	// 리사이클러뷰에 들어갈 아이템뷰의 레이아웃을 inflate.
        visitorAdapter.ViewHolder vh = new visitorAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(visitorAdapter.ViewHolder holder, int position) {
        visitor_item item = mData.get(position) ;

        String url = MyApplication.server_url + "/profile_img/" + item.getProfile_image();

        holder.name.setText(item.getFirst_name());
        holder.visitor_time.setText(item.getVisit_time());
        Glide.with(mContext)
                .load(url)
                .into(holder.profile_image);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }

}
