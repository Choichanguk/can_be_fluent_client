package com.example.canbefluent.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canbefluent.R;
import com.example.canbefluent.items.language_code_item;

import java.util.ArrayList;

public class countryListAdapter extends RecyclerView.Adapter<countryListAdapter.ViewHolder> implements Filterable {
    private static final String TAG = "countryListAdapter";

    ArrayList<language_code_item> unFilteredlist;
    ArrayList<language_code_item> filteredList;
//    private ArrayList<String> mDataClone = null ;

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null ;

    public countryListAdapter(ArrayList<language_code_item> list){
        Log.e(TAG, "countryListAdapter");
        this.filteredList = list;
        this.unFilteredlist = list;
//        mDataClone = list;
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int positionm, language_code_item item) ;
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
        language_code_item item = filteredList.get(position) ;
        holder.country_name.setText(item.getLang_name() + "(" + item.getLang_ko_name() + ")");
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return filteredList.size();
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
                            mListener.onItemClick(v, pos, filteredList.get(pos)) ;
                        }
                    }
                }
            });
        }
    }


//    public void filter(String text) {
//        Log.e(TAG, "filter");
//
////        Log.e("adapter", text);
//        if(text.isEmpty()){
////            Log.e("empty", text);
//            Log.e("size", String.valueOf(mDataCopy.size()));
//            mData.clear();
//            mData.addAll(mDataCopy);
//        } else{
//            ArrayList<String> result = new ArrayList<>();
//            text = text.toLowerCase();
//            for(int i=0; i<mDataCopy.size(); i++){
//                if(mDataCopy.get(i).toLowerCase().contains(text)){
//                    result.add(mDataCopy.get(i));
//                }
//            }
//            mData.clear();
//            mData.addAll(result);
//        }
//        notifyDataSetChanged();
//    }

    /**
     * 검색 결과에 따라 필터링 해주는 메서드
     //     * @param text
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredList = unFilteredlist;
                } else {
                    ArrayList<language_code_item> filteringList = new ArrayList<>();
                    for (language_code_item item : unFilteredlist) {
                        if (item.getLang_name().toLowerCase().contains(charString.toLowerCase()) || item.getLang_ko_name().toLowerCase().contains(charString.toLowerCase())) {
                            filteringList.add(item);
                        }
                    }
                    filteredList = filteringList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<language_code_item>) results.values;
                notifyDataSetChanged();
            }
        };
    }

}
