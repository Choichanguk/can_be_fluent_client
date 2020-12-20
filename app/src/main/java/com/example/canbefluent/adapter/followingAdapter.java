package com.example.canbefluent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.R;
import com.example.canbefluent.items.follow_item;

import java.util.ArrayList;

public class followingAdapter extends RecyclerView.Adapter<followingAdapter.ViewHolder> {

    private ArrayList<follow_item> mData = null ;
    private Context mContext;

    // 리스너 객체 참조를 저장하는 변수
    private followingAdapter.OnItemClickListener mListener = null ;


    // 생성자에서 데이터 리스트 객체를 전달받음.
    public followingAdapter(ArrayList<follow_item> list, Context context) {
        mData = list ;
        mContext = context;
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(followingAdapter.OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, last_login;

        ImageView profile_image;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조.
            name = itemView.findViewById(R.id.user_name) ;
            last_login = itemView.findViewById(R.id.last_login);
            profile_image = itemView.findViewById(R.id.profile_img);

        }
    }


    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    // LayoutInflater - XML에 정의된 Resource(자원) 들을 View의 형태로 반환.
    @Override
    public followingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;    // context에서 LayoutInflater 객체를 얻는다.

        View view = inflater.inflate(R.layout.following_item, parent, false) ;	// 리사이클러뷰에 들어갈 아이템뷰의 레이아웃을 inflate.
        followingAdapter.ViewHolder vh = new followingAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(followingAdapter.ViewHolder holder, int position) {
        follow_item item = mData.get(position) ;

        String url = MyApplication.server_url + "/profile_img/" + item.getProfile_image();

        holder.name.setText(item.getFirst_name());
        holder.last_login.setText(" " + item.getLast_login());
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
