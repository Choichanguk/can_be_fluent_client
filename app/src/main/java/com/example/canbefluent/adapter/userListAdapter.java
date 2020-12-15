package com.example.canbefluent.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.MyApplication;
import com.example.canbefluent.R;
import com.example.canbefluent.items.language_item;
import com.example.canbefluent.items.user_item;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class userListAdapter extends RecyclerView.Adapter<userListAdapter.ViewHolder>{

    private ArrayList<user_item> mData = null ;
    private Context context;

    // 리스너 객체 참조를 저장하는 변수
    private userListAdapter.OnItemClickListener mListener = null ;

    public userListAdapter(ArrayList<user_item> list){
        mData = list;
//        Log.e("adapter", "data size: " + mData.size());
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(userListAdapter.OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public userListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.user_list_item, parent, false) ;
        userListAdapter.ViewHolder vh = new userListAdapter.ViewHolder(view) ;
        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull userListAdapter.ViewHolder holder, int position) {
        user_item item = mData.get(position) ;
        String url = MyApplication.server_url + "/profile_img/" + item.getProfile_img();

        holder.user_name.setText(item.getFirst_name());
//        Log.e("adapter", "url: " + url);
//        Picasso.get()
//                .load(url)
//
////                .rotate(90f) // 사진 파일을 회전해줍시다. Operator 끝났습니다.
//                .into(holder.profile_img);

        Glide.with(context)
                .load(url)
                .into(holder.profile_img);

        if(item.getDistance() != 0.0d){
            int distance = Integer.parseInt(String.valueOf(Math.round(item.getDistance())));
            holder.distance.setText(distance + "km");
        }
        else{
            holder.distance.setText("");
        }

        holder.intro.setText(item.getIntro());
        holder.native_lang1.setText(item.getNative_lang1());
        holder.practice_lang1.setText(item.getPractice_lang1());
        holder.practice_lang1_level.setText(item.getPractice_lang1_level());
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile_img;




        TextView user_name, intro, distance, native_lang1, practice_lang1, practice_lang1_level;


        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
//            country_name = itemView.findViewById(R.id.country_name) ;
//            lang_name = itemView.findViewById(R.id.language);
//            lang_level = itemView.findViewById(R.id.level);
            profile_img = itemView.findViewById(R.id.profile_img);
            user_name = itemView.findViewById(R.id.user_name);
            intro = itemView.findViewById(R.id.intro);
            distance = itemView.findViewById(R.id.distance);
            native_lang1 = itemView.findViewById(R.id.lang_native1);
            practice_lang1 = itemView.findViewById(R.id.lang_practice1);
            practice_lang1_level = itemView.findViewById(R.id.lang_practice1_level);

            GradientDrawable drawable= (GradientDrawable) context.getDrawable(R.drawable.img_round);
            profile_img.setBackground(drawable);
            profile_img.setClipToOutline(true);

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
}
