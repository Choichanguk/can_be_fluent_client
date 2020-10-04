package com.example.canbefluent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canbefluent.items.user_item;

import java.io.File;
import java.io.InputStream;

import okhttp3.MultipartBody;

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
    Button btn_next, btn_previous;
    EditText edit_first_name, edit_last_name;

    user_item user_item;    //유저 데이터가 담기는 객체

    //유저 데이터들을 담는 변수
    String path;    //갤러리에서 가져온 이미지 path
    String first_name, last_name;
    String sex;
    String year_str, month_str, day_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_set_profile);

        // 이전 액티비티에서 유저가 입력한 id, pw를 담은 user_item 객체를 전달받는다.
        // 이 객체에 회원 정보를 담고, 가입 완료시 객체를 서버로 보낸다.
        Intent intent = getIntent();
        user_item = (user_item) intent.getSerializableExtra("user item");

        profile_img = findViewById(R.id.profile_img);           // 프로필 이미지 담는 뷰
        btn_camera = findViewById(R.id.btn_camera);             // 카메라로 이동하는 버튼
        btn_gallery = findViewById(R.id.btn_gallery);           // 갤러리로 이동하는 버튼
        edit_first_name = findViewById(R.id.edit_first_name);   // 이름 적는 칸
        edit_last_name = findViewById(R.id.edit_last_name);     // 성 적은 칸

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
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
        btn_previous = findViewById(R.id.btn_previous); // 이전 화면으로 가는 버튼

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register_setLanguage.class);
                first_name = edit_first_name.getText().toString();   //유저가 입력한 이름을 가져온다.
                last_name = edit_last_name.getText().toString();     //유저가 입력한 성을 가져온다.

                // 필수 데이터가 모두 입력되었는지 판별하는 조건문
                if(path!=null && !first_name.equals("") && !last_name.equals("") && year_str!=null && month_str!=null && day_str!=null){
                    Log.e(TAG, "path: " + path);
                    Log.e(TAG, "first: " + first_name);
                    Log.e(TAG, "last: " + last_name);
                    Log.e(TAG, "year: " + year_str);
                    Log.e(TAG, "month: " + month_str);
                    Log.e(TAG, "day: " + day_str);
                    Log.e(TAG, "sex: " + sex);

                    // 조건을 통과하면 각각의 데이터를 user_item 객체에 담아준다.
                    user_item.setProfile_img(path);
                    user_item.setFirst_name(first_name);
                    user_item.setLast_name(last_name);
                    user_item.setYear(year_str);
                    user_item.setMonth(month_str);
                    user_item.setDay(day_str);
                    user_item.setSex(sex);

                    // 유저 데이터를 담은 user_id 객체를 인텐트에 담아준다.
                    intent.putExtra("user item", user_item);
                    startActivity(intent);
                }
                else if(path == null){
                    alertDialog("set profile");
                }
                else if(first_name.equals("null") || last_name.equals("")){
                    alertDialog("set name");
                }
                else if(year_str == null){
                    alertDialog("set date");
                }
            }
        });

        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                sex = "man";
            }
            else if(i == R.id.rbtn_woman){
                sex = "woman";
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_ACCESS_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri selectedImageUri = data.getData();
            profile_img.setImageURI(selectedImageUri);
            path = FileUtil.getPath(selectedImageUri, getApplicationContext());

        }
        else if(requestCode == CAMERA_ACCESS_REQUEST_CODE && resultCode == Activity.RESULT_OK && data.hasExtra("data")){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap != null) {
                profile_img.setImageBitmap(bitmap);
            }
        }
        else if(requestCode == DATE_PICKER_REQUEST_CODE){
            if(data != null){
                int year = data.getIntExtra("mYear", 0);
                int month = data.getIntExtra("mMonth", 0) + 1;
                int day = data.getIntExtra("mDay", 0);

                year_str = Integer.toString(year);
                month_str = Integer.toString(month);
                day_str = Integer.toString(day);

                String dateToBirth = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);
                date_to_birth.setText(dateToBirth);
            }
        }
    }


    public void alertDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(type.equals("set profile")){
            builder.setTitle("").setMessage("프로필 이미지를 등록해주세요.");
        }
        else if(type.equals("set name")){
            builder.setTitle("").setMessage("이름을 입력해주세요.");
        }
        else if(type.equals("set date")){
            builder.setTitle("").setMessage("생년월일을 입력해주세요.");
        }


        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
//                Toast.makeText(getApplicationContext(), "확인", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }
}