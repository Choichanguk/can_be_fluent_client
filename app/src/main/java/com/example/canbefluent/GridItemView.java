package com.example.canbefluent;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

public class GridItemView extends LinearLayout {

    ImageView imageView;

    public GridItemView(Context context) {
        super(context);

        init(context);
    }

    public GridItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    // singer_item.xml을 inflation
    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.grind_item, this, true);

        imageView = findViewById(R.id.imageView);
    }

    public void setImage(String url) {
        Glide.with(this)
                .load(url)
                .into(imageView);
    }

    public void setBackground(){
//        imageView.setBackgroundResource(R.drawable.background_red);
        imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_red));
    }
}
