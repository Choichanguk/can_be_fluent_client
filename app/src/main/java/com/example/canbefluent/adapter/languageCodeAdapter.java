package com.example.canbefluent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canbefluent.R;
import com.example.canbefluent.items.language_code_item;
import com.example.canbefluent.items.language_item;

import java.util.ArrayList;

public class languageCodeAdapter  extends  RecyclerView.Adapter<languageCodeAdapter.ViewHolder>{

    private ArrayList<language_code_item> mData = null ;
    private String mCode = "";

    // 리스너 객체 참조를 저장하는 변수
    private languageCodeAdapter.OnItemClickListener mListener = null ;

    public languageCodeAdapter(ArrayList<language_code_item> list, String code){
        mData = list;
        mCode = code;
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(languageCodeAdapter.OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public languageCodeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.language_code_item, parent, false) ;
        languageCodeAdapter.ViewHolder vh = new languageCodeAdapter.ViewHolder(view) ;
        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull languageCodeAdapter.ViewHolder holder, int position) {
        language_code_item item = mData.get(position) ;

        holder.lang_name.setText(item.getLang_name());
        holder.lang_ko_name.setText("(" + item.getLang_ko_name() + ")");

        if(mCode.equals(item.getLang_code())){
            holder.checkBox.setChecked(true);
        }
        else {
            holder.checkBox.setChecked(false);
        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView lang_name, lang_ko_name;
        CheckBox checkBox;


        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            lang_name = itemView.findViewById(R.id.lang_name);
            lang_ko_name = itemView.findViewById(R.id.lang_ko_name);
            checkBox = itemView.findViewById(R.id.checkBox);

            checkBox.setOnClickListener(new View.OnClickListener() {
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
