package com.example.canbefluent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class Register_setProfile extends AppCompatActivity {
    private static final String TAG = "Register_setProfile";
    private static final int GALLERY_ACCESS_REQUEST_CODE = 0;
    private static final int CAMERA_ACCESS_REQUEST_CODE = 1;
    private static final int DATE_PICKER_REQUEST_CODE = 2;

    ImageView profile_img;
    ImageButton btn_camera, btn_gallery;
    TextView date_to_birth;
    RadioGroup radioGroup;
    RadioButton man, woman;
    Button btn_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_set_profile);

        profile_img = findViewById(R.id.profile_img);
        btn_camera = findViewById(R.id.btn_camera);
        btn_gallery = findViewById(R.id.btn_gallery);

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GALLERY_ACCESS_REQUEST_CODE);
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_ACCESS_REQUEST_CODE);
            }
        });

        btn_next = findViewById(R.id.btn_next); // 언어 설정 화면으로 가는 버튼
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register_setLanguage.class);
                startActivity(intent);
            }
        });

        /**
         * 카메라 권한 permission 받는 코드
         */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "권한 설정 완료");
//            } else {
//                Log.d(TAG, "권한 설정 요청");
//                ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//            }
//        }

        date_to_birth = findViewById(R.id.date_to_birth); //생년월일 적는 textView. 클릭 시 datePicker 다이얼로그(액티비티로 만든)가 뜬다.
        date_to_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), datePickerActivity.class);
                startActivityForResult(intent, DATE_PICKER_REQUEST_CODE);
            }
        });

        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        man = findViewById(R.id.rbtn_man);
        woman = findViewById(R.id.rbtn_woman);

        man.setChecked(true);






    }
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if(i == R.id.rbtn_man) {
//                Toast.makeText(getApplicationContext(), "라디오 그룹 버튼1 눌렸습니다.", Toast.LENGTH_SHORT).show();
            }
            else if(i == R.id.rbtn_woman){
//                Toast.makeText(getApplicationContext(), "라디오 그룹 버튼2 눌렸습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_ACCESS_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri selectedImageUri = data.getData();
            profile_img.setImageURI(selectedImageUri);

        }
        else if(requestCode == CAMERA_ACCESS_REQUEST_CODE && resultCode == Activity.RESULT_OK && data.hasExtra("data")){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap != null) {
                profile_img.setImageBitmap(bitmap);
            }
        }
        else if(requestCode == DATE_PICKER_REQUEST_CODE){
            int year = data.getIntExtra("mYear", 0);
            int month = data.getIntExtra("mMonth", 0) + 1;
            int day = data.getIntExtra("mDay", 0);
            String dateToBirth = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);
            date_to_birth.setText(dateToBirth);
        }
    }
}