package com.example.canbefluent.login_process;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import com.example.canbefluent.ImageResizeUtils;
import com.example.canbefluent.R;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.permission_class;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Register_setProfile extends AppCompatActivity {
    private static final String TAG = "Register_setProfile";
    private static final int GALLERY_ACCESS_REQUEST_CODE = 0;
    private static final int CAMERA_ACCESS_REQUEST_CODE = 1;
    private static final int DATE_PICKER_REQUEST_CODE = 2;

    private final int MY_PERMISSION_STORAGE = 1111;    // 저장소 권한요청 코드
    private final int MY_PERMISSION_CAMERA = 2222;     // 카메라 권한요청 코드
    File tempFile;  // 받아온 이미지를 저장하는 임시 파일
    private Boolean isCamera = false;

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
    String type;
    String UID;

    // 필요한 권한 요청을 하는 클래스
    permission_class permission_class;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_set_profile);

        edit_first_name = findViewById(R.id.edit_first_name);   // 이름 적는 칸
        edit_last_name = findViewById(R.id.edit_last_name);     // 성 적은 칸

        permission_class = new permission_class(Register_setProfile.this);  //권한요청 클래스 객체 생성, 초기화

        Intent intent = getIntent();
        type = intent.getStringExtra("type");     //회원가입 유형에 따라 다른 데이터를 받는다.
        if(type.equals("direct register")){
            // 직접 id 만들기로 진행했을 시 받게되는 데이터
            // 이전 액티비티에서 유저가 입력한 id, pw를 담은 user_item 객체를 전달받는다.
            // 이 객체에 회원 정보를 담고, 가입 완료시 객체를 서버로 보낸다.
            user_item = (user_item) intent.getSerializableExtra("user item");
        }
        else if(type.equals("google register")){
            // 구글 아이디로 회원가입 시 받게되는 데이터
            first_name = intent.getStringExtra("first_name");
            last_name = intent.getStringExtra("last_name");
            UID = intent.getStringExtra("UID");

            user_item = new user_item();

            // 구글 아이디를 통해 받아온 데이터를 미리 세팅해준다.
            edit_first_name.setText(first_name);
            edit_last_name.setText(last_name);
            user_item.setUID(UID);
        }

        profile_img = findViewById(R.id.profile_img);           // 프로필 이미지 담는 뷰
        btn_camera = findViewById(R.id.btn_camera);             // 카메라로 이동하는 버튼
        btn_gallery = findViewById(R.id.btn_gallery);           // 갤러리로 이동하는 버튼


        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 갤러리로 이동 전에 권한이 허용되었는지 체크한다.
                boolean isChecked = permission_class.check_Storage_Permission(MY_PERMISSION_STORAGE);

                // 이미 권한을 허용했다면 갤러리 실행
                if(isChecked){
                    goToAlbum();   //갤러리 실행 메서드
                }

//                goToAlbum();
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//                startActivityForResult(intent, GALLERY_ACCESS_REQUEST_CODE);
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카메라로 이동 전에 권한이 허용되었는지 체크한다.
                boolean isChecked = permission_class.check_camera_Permission(MY_PERMISSION_CAMERA);

                // 이미 권한을 허용했다면 카메라 실행
                if(isChecked){
                    takePhoto();   //카메라 실행 메서드
                }
//                takePhoto();
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_ACCESS_REQUEST_CODE);
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
                if(tempFile!=null && !first_name.equals("") && !last_name.equals("") && year_str!=null && month_str!=null && day_str!=null){
//                    Log.e(TAG, "path: " + path);
                    Log.e(TAG, "first: " + first_name);
                    Log.e(TAG, "last: " + last_name);
                    Log.e(TAG, "year: " + year_str);
                    Log.e(TAG, "month: " + month_str);
                    Log.e(TAG, "day: " + day_str);
                    Log.e(TAG, "sex: " + sex);
                    Log.e(TAG, "profile: " + tempFile.getName());

                    // 조건을 통과하면 각각의 데이터를 user_item 객체에 담아준다.
                    user_item.setProfile_img(tempFile.getName());
                    user_item.setTmpFile(tempFile);
                    user_item.setFirst_name(first_name);
                    user_item.setLast_name(last_name);
                    user_item.setYear(year_str);
                    user_item.setMonth(month_str);
                    user_item.setDay(day_str);
                    user_item.setSex(sex);

                    // 유저 데이터를 담은 user_id 객체를 인텐트에 담아준다.
                    intent.putExtra("user item", user_item);
                    intent.putExtra("type", type);
                    startActivity(intent);
                }
                else if(tempFile == null){
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
        if (resultCode != Activity.RESULT_OK) {

            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if(tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }
            return;
        }

        if (requestCode == GALLERY_ACCESS_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

//            Uri selectedImageUri = data.getData();
//            profile_img.setImageURI(selectedImageUri);
//            path = FileUtil.getPath(selectedImageUri, getApplicationContext());

            Uri photoUri = data.getData();

            cropImage(photoUri);

        }
//         && resultCode == Activity.RESULT_OK && data.hasExtra("data"
        else if(requestCode == CAMERA_ACCESS_REQUEST_CODE){

//            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//            if (bitmap != null) {
//                profile_img.setImageBitmap(bitmap);
//            }

            // 앨범에 있지만 카메라 에서는 data.getData()가 없음
            Uri photoUri = Uri.fromFile(tempFile);
            Log.d(TAG, "takePhoto photoUri : " + photoUri);

            cropImage(photoUri);


        }
        else if(requestCode == Crop.REQUEST_CROP){
            setImage();
        }
        else if(requestCode == DATE_PICKER_REQUEST_CODE){
            if(data != null){
                int year = data.getIntExtra("mYear", 0);
                int month = data.getIntExtra("mMonth", 0) + 1;
                int day = data.getIntExtra("mDay", 0);

                year_str = Integer.toString(year);
                month_str = String.format("%02d", month);
                day_str = String.format("%02d", day);

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

    /**
     * 갤러리로 이동시키는 메서드
     */
    private void goToAlbum() {
        Log.e(TAG, "goToAlbum");
        isCamera = false;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, GALLERY_ACCESS_REQUEST_CODE);
    }

    /**
     * 카메라에서 이미지를 가져오는 메서드
     */
    private void takePhoto() {
        Log.e(TAG, "takePhoto");
        isCamera = true;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            //
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

            Uri photoUri = FileProvider.getUriForFile(this, "com.example.canbefluent.provider", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, CAMERA_ACCESS_REQUEST_CODE);

        }
        else if (tempFile != null) {

            Uri photoUri = Uri.fromFile(tempFile);  // 카메라에서 찍은 사진이 저장될 주소
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, CAMERA_ACCESS_REQUEST_CODE);
        }
    }

    /**
     * 카메라에서 찍은 사진을 저장할 임시 파일을 만드는 메서드
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        Log.e(TAG, "createImageFile");
        // 이미지 파일 이름 ( blackJin_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "blackJin_" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( blackJin )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/blackJin/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 빈 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

    /**
     * 카메라 or 갤러리에서 가져온 이미지를 Crop 화면으로 보내주는 메서드
     * @param photoUri
     */
    private void cropImage(Uri photoUri) {
        Log.e(TAG, "cropImage");
        Log.d(TAG, "tempFile : " + tempFile);

        /**
         *  갤러리에서 선택한 경우에는 tempFile 이 없으므로 새로 생성해줍니다.
         */
        if(tempFile == null) {
            try {
                tempFile = createImageFile();
            } catch (IOException e) {
                Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
            }
        }

        //크롭 후 저장할 Uri
        Uri savingUri = Uri.fromFile(tempFile);

        Crop.of(photoUri, savingUri).asSquare().start(this);
    }

    private void setImage() {

        Log.e(TAG, "setImage");

        ImageResizeUtils.resizeFile(tempFile, tempFile, 1280, isCamera);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);

        profile_img.setImageBitmap(originalBm);

    }

    /**
     * 권한 요청 후 결과를 받는 메서드
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_STORAGE: // 저장소 접근 권한 요청으로 부터 온 결과 처리
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {  //권한이 거부되었을 때
                        Toast.makeText(Register_setProfile.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                //권한이 허용 되면 갤러리로 이동한다.
                goToAlbum();
                break;

            case MY_PERMISSION_CAMERA: // 카메라 사용 권한 요청으로 부터 온 결과 처리
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {  //권한이 거부되었을 때
                        Toast.makeText(Register_setProfile.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                //권한이 허용 되면 카메라로 이동한다.
                takePhoto();
                break;
        }
    }
}