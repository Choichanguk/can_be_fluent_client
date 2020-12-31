package com.example.canbefluent;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.utils.MyApplication;

import java.util.Locale;

public class random_call_dialog extends DialogFragment {
    public static final String TAG_DIALOG = "random_call_dialog";

    user_item item;
//    String user_name, profile;
    ImageView profile_img;
    LinearLayout btn_cancel;
    TextView count, name;
    String url = MyApplication.server_url + "/profile_img/";

    CountDownTimer countDownTimer;

    public random_call_dialog(user_item item){
        this.item = item;
//        this.profile = profile;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_random_call, container);

        count = view.findViewById(R.id.count);

        name = view.findViewById(R.id.name);
        name.setText(item.getFirst_name());
        profile_img = view.findViewById(R.id.profile_img);

        Glide.with(this)
                .load(url + item.getProfile_img())
                .into(profile_img);

        /**
         * 연결 취소 버튼
         */
        btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                frag_randomCall.ms.cancel_match();
                dismiss();
            }
        });

        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                count.setText(String.format(Locale.getDefault(), "%d", millisUntilFinished / 1000L));
            }

            // 통화로 넘어가는 코드를 넣는다.
            public void onFinish() {
                MainActivity.isSearching = false;
                Log.e(TAG_DIALOG, "카운트다운 onFinish 발생");
                frag_randomCall.ms.start_call();
                Intent intent = new Intent(getActivity(), random_call.class);
                intent.putExtra("user item", item);
//                intent.putExtra("profile", profile);
                startActivity(intent);
                dismiss();
//                count.setText("Done.");
            }
        }.start();

        setCancelable(false);   // false일 때, 뷰 밖을 터치해도 다이얼로그 안꺼짐
        return view;
    }

    public void dismissDialog() {
        countDownTimer.cancel();
        this.dismiss();
    }
}
