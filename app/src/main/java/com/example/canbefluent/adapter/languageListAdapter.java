package com.example.canbefluent.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canbefluent.R;
import com.example.canbefluent.items.language_item;

import java.util.ArrayList;

public class languageListAdapter extends  RecyclerView.Adapter<languageListAdapter.ViewHolder>{
    private ArrayList<language_item> mData = null ;

    // 리스너 객체 참조를 저장하는 변수
    private languageListAdapter.OnItemClickListener mListener = null ;

    public languageListAdapter(ArrayList<language_item> list){
        mData = list;
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(languageListAdapter.OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public languageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.language_item, parent, false) ;
        languageListAdapter.ViewHolder vh = new languageListAdapter.ViewHolder(view) ;
        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull languageListAdapter.ViewHolder holder, int position) {
        language_item item = mData.get(position) ;



        holder.lang_name.setText(item.getLang_name());
        if(item.getLevel() != null){
            holder.lang_level.setText(item.getLevel());
        }
        else{
            holder.lang_level.setVisibility(View.INVISIBLE);
        }
//        holder.country_name.setText(name);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
//        TextView country_name;
        TextView lang_name, lang_level;


        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
//            country_name = itemView.findViewById(R.id.country_name) ;
            lang_name = itemView.findViewById(R.id.language);
            lang_level = itemView.findViewById(R.id.level);

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
