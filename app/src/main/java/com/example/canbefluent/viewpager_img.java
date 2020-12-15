package com.example.canbefluent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.canbefluent.adapter.viewpager_adapter;
import com.example.canbefluent.items.img_item;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;
import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class viewpager_img extends AppCompatActivity {
    private static final String TAG = "viewpager_img";
    private ArrayList<img_item> imageList;
    private CircleIndicator3 mIndicator;
    private static final int DP = 24;

    com.example.canbefluent.items.user_item user_item;
    ViewPager2 viewPager2;
    RetrofitClient retrofitClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_img);

        user_item = (user_item) getIntent().getSerializableExtra("user item");

        Log.e(TAG, "user_index: " + user_item.getUser_index());
        retrofitClient = new RetrofitClient();
        retrofitClient.service.get_images(user_item.getUser_index())
                .enqueue(new Callback<ArrayList<img_item>>() {
                    @Override
                    public void onResponse(Call<ArrayList<img_item>> call, Response<ArrayList<img_item>> response) {
                        imageList = response.body();
//                        initializeData(imageList);
                        Log.e(TAG, "response: " + imageList.size());

                        init(imageList);
                    }

                    @Override
                    public void onFailure(Call<ArrayList<img_item>> call, Throwable t) {

                    }
                });
    }


    public void init(ArrayList<img_item> list){
        Log.e(TAG, "init");
        viewPager2 = findViewById(R.id.viewPager);
        Log.e(TAG, "init1");
        ViewPagerAdapter adapter = new ViewPagerAdapter(list, viewpager_img.this);
        Log.e(TAG, "init2");
        viewPager2.setAdapter(adapter);
        Log.e(TAG, "init3");

        mIndicator = (CircleIndicator3) findViewById(R.id.indicator);
        mIndicator.setViewPager(viewPager2);
        mIndicator.createIndicators(list.size(),0);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mIndicator.animatePageSelected(position);
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mIndicator.animatePageSelected(position);
            }
        });
    }


    public class ViewPagerAdapter extends RecyclerView.Adapter<ViewHolderPage> {
        private ArrayList<img_item> listData;
        private Context mContext = null;
        ViewPagerAdapter(ArrayList<img_item> data, Context context) {
            Log.e(TAG, "ViewPagerAdapter");
            this.listData = data;
            this.mContext = context;
        }
        @Override
        public ViewHolderPage onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.e(TAG, "onCreateViewHolder");
            Context context = parent.getContext();
            Log.e(TAG, "onCreateViewHolder1");
            View view = LayoutInflater.from(context).inflate(R.layout.image_view, parent,false);
            Log.e(TAG, "onCreateViewHolder2");
            return new ViewHolderPage(view);
        }
        @Override
        public void onBindViewHolder(ViewHolderPage holder, int position) {
            Log.e(TAG, "onBindViewHolder1");
            if(holder instanceof ViewHolderPage){
                Log.e(TAG, "onBindViewHolder2");
                ViewHolderPage viewHolder = (ViewHolderPage) holder;
                viewHolder.onBind(listData.get(position), mContext);
            }
        }
        @Override
        public int getItemCount() {
            return listData.size();
        }
    }
}