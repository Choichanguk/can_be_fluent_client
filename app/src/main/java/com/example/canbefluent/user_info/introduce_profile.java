package com.example.canbefluent.user_info;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.utils.FileUtils;
import com.example.canbefluent.MainActivity;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.R;
import com.example.canbefluent.adapter.GrindAdapter;
import com.example.canbefluent.items.img_item;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.getResult;
import com.example.canbefluent.retrofit.RetrofitClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class introduce_profile extends AppCompatActivity {
    private static final String TAG = "introduce_profile";
    private static final int CAMERA_ACCESS_REQUEST_CODE = 1;
    private static final int GALLERY_ACCESS_REQUEST_CODE = 0;

    user_item user_item;
    TextView name, intro;
    ImageView btn_config_name, btn_config_intro, profile_img;
    ImageButton btn_back, btn_add_image;
    GridView upload_imgs;
    GrindAdapter adapter;  // grindView 어댑터

    private ArrayList<Uri> imgList = new ArrayList<>();

    File tempFile; // 카메라나 갤러리로부터 받아온 이미지를 저장하는 임시 파일

    String url = MyApplication.server_url + "/profile_img/";
    String url_file = "";
    RetrofitClient retrofitClient;
    Call<ArrayList<img_item>> call3;
    String my_user_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduce_profile);
        user_item = (user_item) getIntent().getSerializableExtra("user item");
        upload_imgs = findViewById(R.id.upload_imgs);


        retrofitClient = new RetrofitClient();
        retrofitClient.service.get_images(user_item.getUser_index())
                .enqueue(new Callback<ArrayList<img_item>>() {
                    @Override
                    public void onResponse(Call<ArrayList<img_item>> call, Response<ArrayList<img_item>> response) {
                        final ArrayList<img_item> list = response.body();

                        // 업로드한 이미지 목록 보여주는 gridView

                        adapter = new GrindAdapter(getApplicationContext());

                        for (int i = 0; i < list.size(); i++){
                            adapter.addItem(list.get(i));
                        }

                        upload_imgs.setAdapter(adapter);
                        upload_imgs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                                Toast.makeText(getApplicationContext(), position+"", Toast.LENGTH_SHORT).show();
                                showAlertDialog(user_item.getUser_index(), list.get(position));
                            }
                        });

                    }

                    @Override
                    public void onFailure(Call<ArrayList<img_item>> call, Throwable t) {

                    }
                });

        // 이름 세팅
        name = findViewById(R.id.textView15);
        name.setText(user_item.getFirst_name());

        // 소개글 세팅
        intro = findViewById(R.id.introduce);
        intro.setText(user_item.getIntro());

        // 프로필사진 세팅
        url_file = url + user_item.getProfile_img();
        profile_img = findViewById(R.id.profile_img);
        Glide.with(this)
                .load(url_file)
                .into(profile_img);


        // 이름 편집 버튼
        btn_config_name = findViewById(R.id.btn_config_name);
        btn_config_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 소개글 편집과 로직 같음
            }
        });

        // 소개글 편집 버튼
        btn_config_intro = findViewById(R.id.btn_config_intro);
        btn_config_intro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog("edit intro");
            }
        });


        // 이미지 추가 버튼
        btn_add_image = findViewById(R.id.btn_add_image);
        btn_add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog("add image");
            }
        });

        // 뒤로가기 버튼
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void showAlertDialog(String type)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(introduce_profile.this);
        LayoutInflater inflater = getLayoutInflater();
        if(type.equals("edit intro")){
            View view = inflater.inflate(R.layout.dialog_edit_intro, null);
            builder.setView(view);

            final EditText editText = (EditText)view.findViewById(R.id.introduce);
            editText.setText(intro.getText());
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    String value = editText.getText().toString();
                    intro.setText(value);

                    retrofitClient = new RetrofitClient();
                    retrofitClient.service.update_intro(user_item.getUser_id(), value)
                            .enqueue(new Callback<getResult>() {
                                @Override
                                public void onResponse(Call<getResult> call, Response<getResult> response) {
                                    Log.e("retrofit", "onResponse");
                                }

                                @Override
                                public void onFailure(Call<getResult> call, Throwable t) {
                                    Log.e("retrofit", "onFailure");
                                }
                            });
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();

                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if(type.equals("add image")){
            View view = inflater.inflate(R.layout.dialog_add_image, null);
            builder.setView(view);
            final LinearLayout camera = view.findViewById(R.id.camera);
            final LinearLayout gallery = view.findViewById(R.id.gallery);
            final AlertDialog dialog = builder.create();

            // 카메라로 이동하는 버튼
            camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(getApplicationContext(), "카메라버튼", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });

            // 갤러리로 이동하는 버튼
            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(getApplicationContext(), "갤러리버튼", Toast.LENGTH_SHORT).show();
                    showChooser();
                    dialog.dismiss();
                }
            });

            dialog.show();
        }

    }

    // 그리드뷰에 있는 프로필 이미지 아이템을 클릭했을 경우 나타나는 다이얼로그
    private void showAlertDialog(final String user_index, final img_item item){
        AlertDialog.Builder builder = new AlertDialog.Builder(introduce_profile.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_profile_option, null);
        builder.setView(view);
        final TextView set_first_img = view.findViewById(R.id.set_first_img);
        final TextView delete_img = view.findViewById(R.id.delete_img);
        final AlertDialog dialog = builder.create();

        // 대표 이미지 설정 버튼
        // 유저 idx, 해당 이미지 idx를 인자로 받는다.
        set_first_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    Toast.makeText(getApplicationContext(), "카메라버튼", Toast.LENGTH_SHORT).show();
                retrofitClient.service.update_delete_profile_image(user_index, item.getImage_index(), item.getImage_name(), "update")
                .enqueue(new Callback<ArrayList<img_item>>() {
                    @Override
                    public void onResponse(Call<ArrayList<img_item>> call, Response<ArrayList<img_item>> response) {
                        final ArrayList<img_item> list = response.body();
                        adapter = new GrindAdapter(getApplicationContext());

                        for (int i = 0; i < list.size(); i++){
                            adapter.addItem(list.get(i));
                        }

                        upload_imgs.setAdapter(adapter);
                        upload_imgs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                showAlertDialog(user_item.getUser_index(), list.get(position));
                            }
                        });

                        // 새로 갱신된 유저의 정보를 서버로부터 받아온다.
                        get_user_info();

                        // 업데이트 된 프로필 이미지를 적용시켜 준다.
                        Glide.with(introduce_profile.this)
                                .load(url + item.getImage_name())
                                .into(profile_img);
                    }

                    @Override
                    public void onFailure(Call<ArrayList<img_item>> call, Throwable t) {

                    }
                });
                dialog.dismiss();
            }
        });

        // 프로필 이미지 삭제 버튼
        // 유저 idx, 해당 이미지 idx를 인자로 받는다.
        delete_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 삭제하려는 이미지가 대표 사진이라면
                if(item.getIs_first().equals("T")){
                    retrofitClient.service.update_delete_profile_image(user_index, item.getImage_index(), item.getImage_name(), "delete first")
                            .enqueue(new Callback<ArrayList<img_item>>() {
                                @Override
                                public void onResponse(Call<ArrayList<img_item>> call, Response<ArrayList<img_item>> response) {
                                    final ArrayList<img_item> list = response.body();
                                    adapter = new GrindAdapter(getApplicationContext());

                                    for (int i = 0; i < list.size(); i++){
                                        adapter.addItem(list.get(i));
                                    }

                                    upload_imgs.setAdapter(adapter);
                                    upload_imgs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            showAlertDialog(user_item.getUser_index(), list.get(position));
                                        }
                                    });

                                    // 새로 갱신된 유저의 정보를 서버로부터 받아온다.
                                    get_user_info();

                                    // 업데이트 된 프로필 이미지를 적용시켜 준다.
                                    Glide.with(introduce_profile.this)
                                            .load(url + "basic.jpeg")
                                            .into(profile_img);
                                }

                                @Override
                                public void onFailure(Call<ArrayList<img_item>> call, Throwable t) {

                                }
                            });
                }
                else{
                    retrofitClient.service.update_delete_profile_image(user_index, item.getImage_index(), item.getImage_name(), "delete")
                            .enqueue(new Callback<ArrayList<img_item>>() {
                                @Override
                                public void onResponse(Call<ArrayList<img_item>> call, Response<ArrayList<img_item>> response) {
                                    final ArrayList<img_item> list = response.body();
                                    adapter = new GrindAdapter(getApplicationContext());

                                    for (int i = 0; i < list.size(); i++){
                                        adapter.addItem(list.get(i));
                                    }

                                    upload_imgs.setAdapter(adapter);
                                    upload_imgs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            showAlertDialog(user_item.getUser_index(), list.get(position));
                                        }
                                    });

                                    // 새로 갱신된 유저의 정보를 서버로부터 받아온다.
                                    get_user_info();


                                }

                                @Override
                                public void onFailure(Call<ArrayList<img_item>> call, Throwable t) {

                                }
                            });
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    };
    /**
     * 외부
     */
    @SuppressLint("IntentReset")
    private void showChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, 2222);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");

        if (requestCode == 2222 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri photoUri = data.getData();
            imgList.add(photoUri);

            image_upload();
            Log.e(TAG, "photoUri: " + photoUri);
        }
    }

    public void image_upload(){
        if(imgList.size() > 0){
            // 이미지 리스트에 이미지 uri가 담겨있다면 이미지 전송 로직을 실행
            // 서버로 선택한 이미지들을 보내 db에 저장시킨다.
            // 서버로부터 저장된 이미지 이름을 응답받는다.


            // create list of file parts (photo, video, ...)
            List<MultipartBody.Part> parts = new ArrayList<>();

            if (imgList != null) {
                // create part for file (photo, video, ...)
                for (int i = 0; i < imgList.size(); i++) {
                    parts.add(prepareFilePart("image"+i, imgList.get(i)));
                }
            }

            final long time = System.currentTimeMillis(); // 메세지를 보낸 시간
            // create a map of data to pass along
            RequestBody user_index = createPartFromString(user_item.getUser_index());
            RequestBody Time = createPartFromString(time+"");
            RequestBody status; // 메세지 타입 - img

            RequestBody size = createPartFromString(""+parts.size());   // 보내는 이미지 개수

            retrofitClient = new RetrofitClient();
            call3 =retrofitClient.service.uploadSingle2(user_index, size, Time, parts);
            call3.enqueue(new Callback<ArrayList<img_item>>() {
                @Override
                public void onResponse(Call<ArrayList<img_item>> call, Response<ArrayList<img_item>> response) {
                    final ArrayList<img_item> list = response.body();
                    adapter = new GrindAdapter(getApplicationContext());

                    for (int i = 0; i < list.size(); i++){
                        adapter.addItem(list.get(i));
                    }
                    Log.e(TAG, "리스트 사이즈: " + list.size());
                    upload_imgs.setAdapter(adapter);
                    upload_imgs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            showAlertDialog(user_item.getUser_index(), list.get(position));
                        }
                    });

                    // 새로 갱신된 유저의 정보를 서버로부터 받아온다.
                    get_user_info();

                    // 서버로부터 이미지 저장 확인 되면 imgList를 초기화한다.
                    imgList = new ArrayList<>();
                }

                @Override
                public void onFailure(Call<ArrayList<img_item>> call, Throwable t) {
//                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {

        Log.e("prepareFilePart", "uri: " + fileUri.toString());
        // use the FileUtils to get the actual file by uri
        File file = FileUtils.getFile(this, fileUri);

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create (MediaType.parse(FileUtils.MIME_TYPE_IMAGE), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    /**
     * Convert String and File to Multipart for Retrofit Library
     * @param descriptionString
     * @return
     */
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(FileUtils.MIME_TYPE_TEXT), descriptionString);
    }

    public void get_user_info(){
        Call<user_item[]> call = retrofitClient.service.login_process(user_item.getUser_id(), user_item.getUser_pw());
        call.enqueue(new Callback<user_item[]>() { // 서버로부터 결과 값을 받는 callback 함수
            @Override
            public void onResponse(Call<user_item[]> call, Response<user_item[]> response) {
                Log.e(TAG, "onResponse");
                user_item[] user_item_arr = response.body();
                user_item user_item = new user_item();
//                            Log.e(TAG, "user_item: " + result);

                assert user_item_arr != null;
                for(user_item item : user_item_arr){
                    user_item = item;
                }
                assert user_item != null;
                String result = user_item.getResult();


                if(result.equals("success")){
                    Log.e(TAG, "onResponse success");
                    // 결과 값이 success면 user_item 객체를 저장한다.
                    MainActivity.user_item = user_item;

                }
                else if(result.equals("fail")){ // 결과 값이 fail이면 로그인 실패 다이얼로그를 띄워준다.
//                        alertDialog("login fail");
                }
            }

            @Override
            public void onFailure(Call<user_item[]> call, Throwable t) {
                Log.e(TAG, "onFailure " + t.getMessage());
            }
        });
    }


}


