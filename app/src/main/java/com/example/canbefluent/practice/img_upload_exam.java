package com.example.canbefluent.practice;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.example.canbefluent.R;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

public class img_upload_exam extends AppCompatActivity {
    private static final String TAG = "img_upload_exam";
    ImageView imageView;
    ImageButton btn_camera, btn_gallery;
    Button btn_upload;
    private static final int GALLERY_ACCESS_REQUEST_CODE = 0;
    Uri img_uri;
    Bitmap img_bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_upload_exam);

        btn_camera = findViewById(R.id.btn_camera);
        btn_gallery = findViewById(R.id.btn_gallery);
        btn_upload = findViewById(R.id.btn_upload);
        imageView = findViewById(R.id.imageView);
        String url_str = "http://3.35.26.65/profile_img/Screenshot_20200925-205153_Gallery.jpg";
        try {
            URL url = new URL(url_str);
//            Log.e(TAG, "url: " + url);
            Picasso.get()
                .load(url_str)
                .into(imageView);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }



        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GALLERY_ACCESS_REQUEST_CODE);
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                RetrofitClient retrofitClient = new RetrofitClient();
//                String path = FileUtil.getPath(img_uri, getApplicationContext());
//                String path2 = getRealPathFromUri(img_uri);
//                Log.e(TAG, "path1: " + path);
//                Log.e(TAG, "img_uri: " + img_uri);
////                Log.e(TAG, "path1: " + path2);
//
//                File file = new File(path);
//
//                Log.e(TAG, "file get path: " + file.getPath());
//                Log.e(TAG, "file get name: " + file.getName());
//                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//                MultipartBody.Part uploadedFile = MultipartBody.Part.createFormData("uploaded_file", file.getPath(), requestFile);
//
//                Call<imgUploadResult> resultCall = retrofitClient.service.uploadImage(uploadedFile);
//
//                resultCall.enqueue(new Callback<imgUploadResult>() {
//                    @Override
//                    public void onResponse(Call<imgUploadResult> call, Response<imgUploadResult> response) {
//                        imgUploadResult result = response.body();
//                        Log.e(TAG, "onResponse result: " + result.getResult());
//                        Log.e(TAG, "onResponse value: " + result.getValue());
//                        Log.e(TAG, "onResponse path: " + result.getPath());
//                    }
//
//                    @Override
//                    public void onFailure(Call<imgUploadResult> call, Throwable t) {
//
//                        Log.e(TAG, "onFailure result: " + t);
//                    }
//                });
            }
        });





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_ACCESS_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            img_uri = selectedImageUri;

            imageView.setImageURI(img_uri);

            //이미지 경로 uri 확인해보기
//            new AlertDialog.Builder(this).setMessage(selectedImageUri.toString()+"\n"+imgPath).create().show();

        }
//        else if(requestCode == CAMERA_ACCESS_REQUEST_CODE && resultCode == Activity.RESULT_OK && data.hasExtra("data")){
//            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//            if (bitmap != null) {
//                profile_img.setImageBitmap(bitmap);
//            }
//        }
    }

    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
    String getRealPathFromUri(Uri uri){
        String[] proj= {MediaStore.Images.Media.DATA};
        CursorLoader loader= new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor= loader.loadInBackground();
        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result= cursor.getString(column_index);
        cursor.close();
        return  result;
    }

    /**
     * file path를 uri로 변환하는 메서드
     * @param filePath
     * @return
     */
    private Uri getUriFromPath(String filePath) {
        long photoId;
        Uri photoUri = MediaStore.Images.Media.getContentUri("external");
        String[] projection = {MediaStore.Images.ImageColumns._ID};
        Cursor cursor = getContentResolver().query(photoUri, projection, MediaStore.Images.ImageColumns.DATA + " LIKE ?", new String[] { filePath }, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(projection[0]);
        photoId = cursor.getLong(columnIndex);

        cursor.close();
        return Uri.parse(photoUri.toString() + "/" + photoId);
    }

    public static Uri getUriFromPath(ContentResolver cr, String path) {
        Uri fileUri = Uri.parse(path);
        String filePath = fileUri.getPath();
        Cursor c = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, "_data = '" + filePath + "'", null, null);
        c.moveToNext();
        int id = c.getInt(c.getColumnIndex("_id"));
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        return uri;
    }


}