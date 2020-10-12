package com.example.canbefluent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.canbefluent.practice.permission_practice;

import java.util.ArrayList;
import java.util.List;

/**
 * 액티비티로부터 context를 넘겨받아 필요한 권한설정을 해주는 클래스
 * check_Storage_Permission(): 저장소 접근 권한 체크 후, 권한요청 하는 메서드
 * check_camera_Permission():  카메라 사용 권한 체크 후, 권한요청 하는 메서드
 */
public class permission_class {

    Context context;
    public permission_class(Context context){
        this.context = context;
    }

    /**
     * 저장소 접근 권한이 허용되었는지 체크 후, 허용이 안되었으면 권한 신청을 하는 메소드
     */
    public boolean check_Storage_Permission(int request_code){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, request_code);
            return false;
        }
        else{
            return true;
        }


    }

    /**
     * 카메라 권한이 허용되었는지 체크 후, 허용이 안되었으면 권한 신청을 하는 메소드
     */
    public boolean check_camera_Permission(int request_code){

        if(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, request_code);

            return false;
        }
        else{
            return true;
        }
    }



}
