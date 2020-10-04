package com.example.canbefluent.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canbefluent.R;

import java.util.ArrayList;

public class countryListAdapter extends RecyclerView.Adapter<countryListAdapter.ViewHolder> {
    private ArrayList<String> mData = null ;
    private ArrayList<String> mDataCopy = null ;
    private ArrayList<String> mDataClone = null ;

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null ;

    public countryListAdapter(ArrayList<String> list){
        mData = list;
        mDataCopy = list;
        mDataClone = list;
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public countryListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.country_item, parent, false) ;
        countryListAdapter.ViewHolder vh = new countryListAdapter.ViewHolder(view) ;
        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = mData.get(position) ;
        holder.country_name.setText(name);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView country_name;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            country_name = itemView.findViewById(R.id.country_name) ;

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

    /**
     * 검색 결과에 따라 필터링 해주는 메서드
     * @param text
     */
    public void filter(String text) {

        Log.e("adapter", text);
        if(text.isEmpty()){
            Log.e("empty", text);
            Log.e("size", String.valueOf(mDataCopy.size()));
            mData.clear();
            mData.addAll(mDataClone);
        } else{
            ArrayList<String> result = new ArrayList<>();
            text = text.toLowerCase();
            for(int i=0; i<mDataCopy.size(); i++){
                if(mDataCopy.get(i).toLowerCase().contains(text)){
                    result.add(mDataCopy.get(i));
                }
            }
            mData.clear();
            mData.addAll(result);
        }
        notifyDataSetChanged();
    }


}
