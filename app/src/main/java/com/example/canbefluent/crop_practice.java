package com.example.canbefluent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.canbefluent.pojoClass.getRegisterUserResult;
import com.example.canbefluent.pojoClass.imgUploadResult;
import com.example.canbefluent.practice.permission_practice;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class crop_practice extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 15;
    private static final int PICK_FROM_CAMERA = 16;
    private static final String TAG = "crop_practice";
    private final int MY_PERMISSION_STORAGE = 1111;
    private final int MY_PERMISSION_CAMERA = 2222;
    File tempFile;  // 받아온 이미지를 저장하는 임시 파일
    ImageView imageView;
    Button btn_gallery, btn_camera ,btn;

    private Boolean isCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_practice);
        btn_gallery = findViewById(R.id.btn_gallery);
        final permission_class permission_class = new permission_class(crop_practice.this);


        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permission_class.check_Storage_Permission(MY_PERMISSION_STORAGE);

            }
        });

        btn_camera = findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permission_class.check_camera_Permission(MY_PERMISSION_CAMERA);
            }
        });

        btn = findViewById(R.id.button7);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempFile != null){
                    Log.e(TAG, "temp file: " + tempFile);

//                    File file = new File(user_item.getProfile_img());
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), tempFile);
                    MultipartBody.Part uploadedFile = MultipartBody.Part.createFormData("uploaded_file", tempFile.getPath(), requestFile);

                    RetrofitClient retrofitClient = new RetrofitClient();
                    Call<imgUploadResult> call = retrofitClient.service.uploadImage(uploadedFile);
                    call.enqueue(new Callback<imgUploadResult>() {
                        @Override
                        public void onResponse(Call<imgUploadResult> call, Response<imgUploadResult> response) {
                            imgUploadResult result = response.body();
                            Log.e(TAG, "onResponse result: " + result.getResult());
//                            Log.e(TAG, "onResponse value: " + result.getValue());
                            Log.e(TAG, "onResponse path: " + result.getPath());
                        }

                        @Override
                        public void onFailure(Call<imgUploadResult> call, Throwable t) {
                            Log.e(TAG, "onFailure path: " + t.getMessage());
                        }
                    });
                }
                else{
                    Log.e(TAG, "temp file is null");
                }

            }
        });
    }

    private void goToAlbum() {
        Log.e(TAG, "goToAlbum");
        isCamera = false;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");

        // 예외 처리
        // 앨범이나 카메라로 이동 후 사진 선택 or 저장을 하지 않고 뒤로가기를 한 경우
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

        if (requestCode == PICK_FROM_ALBUM) {

            Uri photoUri = data.getData();

            cropImage(photoUri);
        }


        else if(requestCode == PICK_FROM_CAMERA){

            // 앨범에 있지만 카메라 에서는 data.getData()가 없음
            Uri photoUri = Uri.fromFile(tempFile);
            Log.d(TAG, "takePhoto photoUri : " + photoUri);

            cropImage(photoUri);

//            setImage();     //갤러리에서 받아온 이미지를 넣는다.
        }

        else if(requestCode == Crop.REQUEST_CROP)  {

//            Uri uri = getImageContentUri(crop_practice.this, tempFile.getAbsolutePath());

            setImage();
        }
    }

    public static Uri getImageContentUri(Context context, String absPath) {
        Log.v(TAG, "getImageContentUri: " + absPath);
        Cursor cursor = context.getContentResolver()
                .query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI , new String[] {
                        MediaStore.Images.Media._ID
                }, MediaStore.Images.Media.DATA + "=? " , new String[] { absPath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , Integer.toString(id));
        }
        else if (!absPath.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, absPath);
            return context.getContentResolver().insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
        else {
            return null;
        }
    }


//    public static Uri getUriFromPath(ContentResolver cr, File file) {
//        String filePath = file.getPath();
//        Cursor c = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                null, "_data = '" + filePath + "'", null, null);
//        c.moveToNext();
//        int id = c.getInt(c.getColumnIndex("_id"));
//        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//        return uri;
//    }

    /**
     * 갤러리나 카메라에서 받아온 이미지를 넣는 이미지뷰에 세팅하는 메서드
     */
    private void setImage() {

        Log.e(TAG, "setImage");
        imageView = findViewById(R.id.imageView);

        ImageResizeUtils.resizeFile(tempFile, tempFile, 1280, isCamera);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);

        imageView.setImageBitmap(originalBm);

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
            startActivityForResult(intent, PICK_FROM_CAMERA);

        }
        else if (tempFile != null) {

            Uri photoUri = Uri.fromFile(tempFile);  // 카메라에서 찍은 사진이 저장될 주소
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_STORAGE:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(crop_practice.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                // 허용했다면 이 부분에서..
                goToAlbum();
                break;

            case MY_PERMISSION_CAMERA:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(crop_practice.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                takePhoto();
                break;
        }
    }
}