package com.example.canbefluent.practice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canbefluent.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class gps_practice extends AppCompatActivity {
    TextView location, distance;
    Button btn, btn2;

    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // 2개의 퍼미션을 요청 할 것
    // ACCESS_FINE_LOCATION: Allows an app to access precise location.
    // ACCESS_COARSE_LOCATION: Allows an app to access approximate location.
    // 내 앱에는 ACCESS_COARSE_LOCATION만 있어도 될듯
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    /**
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.e("permission 수락 시", "onRequestPermissionsResult");
        if ( requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                // shouldShowRequestPermissionRationale: Gets whether you should show UI with rationale before requesting a permission.
                // 이전에 앱이 이 권한을 요청했고 사용자가 요청을 거부한 경우, 이 메서드는 true를 반환
                // 과거에 사용자가 권한 요청을 거절하고 권한 요청 시스템 대화상자에서 Don't ask again 옵션을 선택한 경우, 이 메서드는 false를 반환
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(gps_practice.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(gps_practice.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){
        Log.e("checkRunTimePermission", "in");
        // 런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        // 앱에 권한이 있는 경우  PackageManager.PERMISSION_GRANTED 를 반환하고, 권한이 없는 경우  PERMISSION_DENIED를 반환

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(gps_practice.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(gps_practice.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            Log.e("isExist", "true");
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            Log.e("isExist", "false");
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(gps_practice.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(gps_practice.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();

                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(gps_practice.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);



            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                // requestPermissions(): 권한요청 메서드
                ActivityCompat.requestPermissions(gps_practice.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    public String getCurrentAddress( double latitude, double longitude) {
        // geocoding: 주소나 지명을 좌표로 변환시키는 작업
        // Geocoder: geocoding or reverst geocoding을 다루는 클래스
        // A class for handling geocoding and reverse geocoding.
        // The Geocoder class requires a backend service that is not included in the core android framework.
        // The Geocoder query methods will return an empty list if there no backend service in the platform.
        // Locale: object that represents a specific geographical, political, or cultural region.
        // getDefault: getDefault 메소드는 JVM에 적용된 디폴트 세팅 값(Locale)을 출력

        // 다른 언어로 정보를 받고 싶다면 Locale 객체를 직접 생성하면 됨
        // Locale(String language, String country)
        // Construct a locale from language and country.
        // Locale locale = Locale.KOREA;
        Locale locale = Locale.ENGLISH;
        Geocoder geocoder = new Geocoder(this, locale);

        List<Address> addresses;

        try {
            // addresses는 Locale.getDefault()로 localized 된다.
            // getFromLocation: 지리적 위치를 주소로 변환
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    6);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        // getAddressLine: 주소 개체의 개별 행을 가져온다.
        for(int i=0; i<addresses.size(); i++){
            Log.e("address", String.valueOf(addresses.get(i)));
        }
        Address address = addresses.get(4);
        return address.getAddressLine(0).toString()+"\n";
    }

    // 여기부터는 GPS 활성화를 위한 메소드들
    // gps를 설정할 지 물어보는 다이얼로그가 띄워지고, 수락버튼을 누르면 gps 서비스 실행
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(gps_practice.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // 이 코드 좀 더 공부
                // 아마 사용자가 수락하면 provider가 setting 될 듯
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    // 기기에서 GPS_PROVIDER와 NETWORK_PROVIDER를 제공하는지 확인 후 둘 중 하나라도 제공한다면 true,
    // 둘 다 제공되지 않으면 false 값을 리턴한다.
    public boolean checkLocationServicesStatus() {

        // LocationManager를 통해 폰에서 제공하는 모든 location관련 provider를 가져올 수 있다.
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //
        List<String> providerList = locationManager.getAllProviders();
        for (int i=0; i<providerList.size(); i++){
            Log.e("checkLocationServices", "provider: " + providerList.get(i));
        }

        // GPS_PROVIDER: GPS와 network를 활용해 위치를 확인. 정확 but 배터리 소모 심함
        // NETWORK_PROVIDER: network만 활용해 위치를 확인. 부정확 but 배터리 소모 적음
        // 어떤 provider로 위치를 측정할 지 정해서 우선순위를 줘야함
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_practice);

        location = findViewById(R.id.location);
        distance = findViewById(R.id.distance);

        btn = findViewById(R.id.button);
        btn2 = findViewById(R.id.button2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(gps_practice.this);

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);
                location.setText(address);

                Toast.makeText(gps_practice.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate_distance calculate_distance = new calculate_distance();
                double distance = calculate_distance.distanceInKilometerByHaversine(37.547889, 126.997128, 35.158874, 129.043846);
                Toast.makeText(gps_practice.this, "서울과 부산 간 거리: " + distance + "km", Toast.LENGTH_SHORT).show();
            }
        });

        // 기기에서 GPS_PROVIDER or NETWORK_PROVIDER를 제공 하는지 체크한다.
        // 둘 다 제공하지 않으면 gps를 설정할 지 물어보는 다이얼로그를 띄운다.
        // 둘 중 하나라도 제공하면
        if (!checkLocationServicesStatus()) {

            Log.e("provider check", "false");
            showDialogForLocationServiceSetting();
        }else {
            Log.e("provider check", "true");
            checkRunTimePermission();
        }


    }
}