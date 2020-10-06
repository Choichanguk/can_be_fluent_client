package com.example.canbefluent;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.canbefluent.adapter.userListAdapter;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.practice.GpsTracker;
import com.example.canbefluent.practice.gps_practice;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag_community extends Fragment {
    private static final String TAG = "frag_community";
    private View view;
    TextView btn_all, btn_nearMe, address_view;
    ImageButton btn_refresh;
    LinearLayout all_view, nearMe_view;
    RecyclerView recycler_allUser, recycler_nearUser;
    userListAdapter allUser_adapter, nearUser_adapter;
    ArrayList<user_item> all_user_list, near_user_list; //모든 유저의 데이터를 담는 ArrayList, 근처 유저의 데이터를 담는 ArrayList

    RetrofitClient retrofitClient;
    Call<ArrayList<user_item>> call_all_user, call_near_user;
    /**
     * gps 관련 변수
     */
    GpsTracker gpsTracker;  // 유저의 현재 위치의 위도와 경도를 구하는 클래스
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // 2개의 퍼미션을 요청 할 것
    // ACCESS_FINE_LOCATION: Allows an app to access precise location.
    // ACCESS_COARSE_LOCATION: Allows an app to access approximate location.
    // 내 앱에는 ACCESS_COARSE_LOCATION만 있어도 될듯
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        view =  inflater.inflate(R.layout.fragment_community, container, false);
        address_view = view.findViewById(R.id.address);
        all_user_list = new ArrayList<>();
        near_user_list = new ArrayList<>();
        Log.e(TAG, "onCreateView");

        /**
         * 서버로부터 모든 유저의 정보를 불러온다.
         */
        retrofitClient = new RetrofitClient();
        call_all_user = retrofitClient.service.get_allUserInfo();
        call_all_user.enqueue(new Callback<ArrayList<com.example.canbefluent.items.user_item>>() {
            @Override
            public void onResponse(Call<ArrayList<com.example.canbefluent.items.user_item>> call, Response<ArrayList<com.example.canbefluent.items.user_item>> response) {
                ArrayList<user_item> result = response.body();

                // 서버로부터 받아온 유저 리스트를 all_user_list에 담아준다.
                // 리사이클러뷰에 all_user_list를 보여준다.
                assert result != null;
                all_user_list.addAll(result);
//                Log.e(TAG, "user list size: " + all_user_list.size());

                for (int i=0; i<all_user_list.size(); i++){
//                    Log.e(TAG, "img: " + all_user_list.get(i).getProfile_img());

                }
                //recycler
                recycler_allUser = view.findViewById(R.id.recycler_allUser);

                recycler_allUser.setLayoutManager(new LinearLayoutManager(getActivity()));

                // define an adapter
                allUser_adapter = new userListAdapter(all_user_list);

                // 유저를 클릭하면 해당 유저의 정보를 객체에 담아 유저 프로필 액티비티로 이동시킨다.
                allUser_adapter.setOnItemClickListener(new userListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
//                        Log.e(TAG, "user id: " + all_user_list.get(position).getUser_id());

                        // 유저 리스트에서 선택한 유저의 정보를 담은 객체를 인텐트에 담는다.
                        user_item item = all_user_list.get(position);
                        Intent intent = new Intent(getActivity(), user_profile_activity.class);
                        intent.putExtra("user item", item);
                        startActivity(intent);
                    }
                });
                recycler_allUser.setAdapter(allUser_adapter);
            }

            @Override
            public void onFailure(Call<ArrayList<com.example.canbefluent.items.user_item>> call, Throwable t) {

            }
        });

        all_view = view.findViewById(R.id.all_view);
        nearMe_view = view.findViewById(R.id.nearMe_view);

        // 전체 유저 보기 버튼, 클릭 시 전체 유저에 대한 recyclerView가 나온다.
        btn_all = view.findViewById(R.id.btn_all);
        btn_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                all_view.setVisibility(View.VISIBLE);
                nearMe_view.setVisibility(View.INVISIBLE);

                btn_all.setTextColor(Color.parseColor("#36A2A6"));
                btn_nearMe.setTextColor(Color.parseColor("#747575"));
            }
        });

        // 내 근처유저 보기 버튼, 클릭 시 gps를 켤지 물어보는 다이얼로그가 나온다.
        // 수정버튼을 누르면 gps가 켜지고 현재 위치 정보를 얻게 된다.
        // 현재 위치 정보를 기준으로 근처 유저 목록이 recyclerView에 나온다.
        btn_nearMe = view.findViewById(R.id.btn_nearMe);
        btn_nearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 서버로부터 근처 유저 리스트를 받아왔으면 바로 리사이클러뷰를 띄운다.
                // 받아오지 못했으면 gps 켜져있는지 확인 후 서버로부터 근처 유저 리스트를 받아온다.
                if(near_user_list.size() == 0){
                    // 기기에서 GPS_PROVIDER or NETWORK_PROVIDER를 제공 하는지 체크한다.
                    // 둘 다 제공하지 않으면 gps를 설정할 지 물어보는 다이얼로그를 띄운다.
                    // 둘 중 하나라도 제공하면
                    if (!checkLocationServicesStatus()) {

//                    Log.e("provider check", "false");
                        showDialogForLocationServiceSetting();
                    }else {
//                    Log.e("provider check", "true");
                        checkRunTimePermission();
                    }
                }

                all_view.setVisibility(View.INVISIBLE);
                nearMe_view.setVisibility(View.VISIBLE);

                btn_all.setTextColor(Color.parseColor("#747575"));
                btn_nearMe.setTextColor(Color.parseColor("#F33AB5"));

            }
        });

        btn_refresh = view.findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkLocationServicesStatus()) {

                    showDialogForLocationServiceSetting();
                }else {

                    checkRunTimePermission();
                }
            }
        });
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onViewCreated");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.e(TAG, "onAttach");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "onDetach");
    }

    // 기기에서 GPS_PROVIDER와 NETWORK_PROVIDER를 제공하는지 확인 후 둘 중 하나라도 제공한다면 true,
    // 둘 다 제공되지 않으면 false 값을 리턴한다.
    public boolean checkLocationServicesStatus() {
        Log.e(TAG, "checkLocationServicesStatus 실행");
        // LocationManager를 통해 폰에서 제공하는 모든 location관련 provider를 가져올 수 있다.
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        //
        List<String> providerList = locationManager.getAllProviders();
        for (int i=0; i<providerList.size(); i++){
//            Log.e("checkLocationServices", "provider: " + providerList.get(i));
        }

        // GPS_PROVIDER: GPS와 network를 활용해 위치를 확인. 정확 but 배터리 소모 심함
        // NETWORK_PROVIDER: network만 활용해 위치를 확인. 부정확 but 배터리 소모 적음
        // 어떤 provider로 위치를 측정할 지 정해서 우선순위를 줘야함
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // 여기부터는 GPS 활성화를 위한 메소드들
    // gps를 설정할 지 물어보는 다이얼로그가 띄워지고, 수락버튼을 누르면 gps 서비스 실행
    private void showDialogForLocationServiceSetting() {
        Log.e(TAG, "showDialogForLocationServiceSetting 실행");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.e(TAG, "onActivityResult 실행");
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

    void checkRunTimePermission(){
        Log.e(TAG, "checkRunTimePermission 실행");
        // 런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        // 앱에 권한이 있는 경우  PackageManager.PERMISSION_GRANTED 를 반환하고, 권한이 없는 경우  PERMISSION_DENIED를 반환

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
            Log.e(TAG, "checkRunTimePermission 퍼미션 존재");
            gpsTracker = new GpsTracker(getActivity());

            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            String address = getCurrentAddress(latitude, longitude);
            address_view.setText(address);
            Toast.makeText(getActivity(), "위치가 업데이트 되었습니다.", Toast.LENGTH_SHORT).show();

            // gpsTracker로부터 얻은 위도, 경도를 서버로 보내서 저장
            call_near_user = retrofitClient.service.get_nearUserInfo(latitude, longitude);
            call_near_user.enqueue(new Callback<ArrayList<user_item>>() {
                @Override
                public void onResponse(Call<ArrayList<user_item>> call, Response<ArrayList<user_item>> response) {
                    near_user_list = response.body();
                    Log.e(TAG, "near user list size: " + near_user_list.size());

                    recycler_nearUser = view.findViewById(R.id.recycler_NearUser);
                    nearUser_adapter = new userListAdapter(near_user_list);
                    nearUser_adapter.setOnItemClickListener(new userListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int position) {

                            // 유저 리스트에서 선택한 유저의 정보를 담은 객체를 인텐트에 담아 user_profile_activity로 보낸다.
                            user_item item = near_user_list.get(position);
                            Intent intent = new Intent(getActivity(), user_profile_activity.class);
                            intent.putExtra("user item", item);
                            startActivity(intent);
                        }
                    });
                    recycler_nearUser.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recycler_nearUser.setAdapter(nearUser_adapter);

                }

                @Override
                public void onFailure(Call<ArrayList<user_item>> call, Throwable t) {

                }
            });


            Log.e(TAG, "위도: " + latitude + "/ 경도: " + longitude);
//            Toast.makeText(getActivity() ,"위도: " + latitude + "/ 경도: " + longitude, Toast.LENGTH_SHORT).show();


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
//            Log.e("isExist", "false");
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getActivity(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();

                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);



            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                // requestPermissions(): 권한요청 메서드
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    /**
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.e(TAG, "onRequestPermissionsResult 실행");
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

                Log.e(TAG, "onRequestPermissionsResult 퍼미션 존재");
                gpsTracker = new GpsTracker(getActivity());

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                Log.e(TAG, "위도: " + latitude + "/ 경도: " + longitude);
                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                // shouldShowRequestPermissionRationale: Gets whether you should show UI with rationale before requesting a permission.
                // 이전에 앱이 이 권한을 요청했고 사용자가 요청을 거부한 경우, 이 메서드는 true를 반환
                // 과거에 사용자가 권한 요청을 거절하고 권한 요청 시스템 대화상자에서 Don't ask again 옵션을 선택한 경우, 이 메서드는 false를 반환
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();


                }else {

                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
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
        Geocoder geocoder = new Geocoder(getActivity(), locale);

        List<Address> addresses;

        try {
            // addresses는 Locale.getDefault()로 localized 된다.
            // getFromLocation: 지리적 위치를 주소로 변환
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(getActivity(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getActivity(), "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getActivity(), "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        // getAddressLine: 주소 개체의 개별 행을 가져온다.
        for(int i=0; i<addresses.size(); i++){
//            Log.e("address", String.valueOf(addresses.get(i)));
        }
        Address address = addresses.get(4);
        Log.e(TAG, "address" + address);
        return address.getAddressLine(0).toString()+"\n";
    }






}
