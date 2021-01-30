package com.example.canbefluent;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.canbefluent.adapter.userListAdapter;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.practice.GpsTracker;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.utils.sharedPreference;

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
    SwipeRefreshLayout refreshLayout; // 새로고침 레이아웃

    RetrofitClient retrofitClient;
    Call<ArrayList<user_item>> call_all_user, call_near_user;

    MainActivity instance = new MainActivity();

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

    String user_id;
    com.example.canbefluent.utils.sharedPreference sharedPreference;

//    frag_randomCall.e;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        view =  inflater.inflate(R.layout.fragment_community, container, false);

//        if(MainActivity.socket != null){
//            instance.visible_floating_view("visible");
//        }
//        else{
//            instance.visible_floating_view("gone");
//        }

        address_view = view.findViewById(R.id.address);
        all_user_list = new ArrayList<>();
        near_user_list = new ArrayList<>();
        Log.e(TAG, "onCreateView");



        sharedPreference = new sharedPreference();
        user_id = sharedPreference.loadUserId(getContext());    // 쉐어드에 저장된 유저의 id를 가져온다.
        Log.e(TAG, "user id: " + user_id);

        refreshLayout = view.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                set_allUSerList("refresh");

            }
        });

        /**
         * 서버로부터 모든 유저의 정보를 불러온다.
         */
        set_allUSerList("set first");

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
                btn_nearMe.setTextColor(Color.parseColor("#000000"));
            }
        });

        // 내 근처유저 보기 버튼, 클릭 시 gps를 켤지 물어보는 다이얼로그가 나온다.
        // 수정버튼을 누르면 gps가 켜지고 현재 위치 정보를 얻게 된다.
        // 현재 위치 정보를 기준으로 근처 유저 목록이 recyclerView에 나온다.
        btn_nearMe = view.findViewById(R.id.btn_nearMe);
        btn_nearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 내 기기 위치정보에 접근 권한이 허용되었는지 체크 후, 허용 되어있지 않다면 권한을 요청한다.
                // 그 후 gps가 켜져있지 않다면 gps를 사용할 지 다이얼로그를 띄워준다.
                // 1. 기기 위치 접근 권한 check
                // 2. 체크되어 있으면 gps 켜져있는지 check
                // gps 켜져 있으면 위도 경도 구하기 - checkRunTimePermission();
                // gps 안켜져 있으면 gps 사용 요청 다이얼로그 띄우기
                if(near_user_list.size() == 0){
                    if(check_LocationAccess_Permission(PERMISSIONS_REQUEST_CODE)){     //기기 위치 접근 권한 check

                        if (checkLocationServicesStatus()) {
                            //gps 켜져 있으면 위도 경도 구하기 - checkRunTimePermission();
//                    checkRunTimePermission();
                            all_view.setVisibility(View.INVISIBLE);
                            nearMe_view.setVisibility(View.VISIBLE);
                            btn_all.setTextColor(Color.parseColor("#000000"));
                            btn_nearMe.setTextColor(Color.parseColor("#F33AB5"));

                            Log.e(TAG, "onRequestPermissionsResult 퍼미션 존재");
                            gpsTracker = new GpsTracker(getActivity());

                            double latitude = gpsTracker.getLatitude();
                            double longitude = gpsTracker.getLongitude();

                            String address = getCurrentAddress(latitude, longitude);
                            address_view.setText(address);
                            Toast.makeText(getActivity(), "위치가 업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
                            get_nearUser_from_server(latitude, longitude);
                            Log.e(TAG, "위도: " + latitude + "/ 경도: " + longitude);


                        }else {
                            // gps 안켜져 있으면 gps 사용 요청 다이얼로그 띄우기
                            showDialogForLocationServiceSetting();

                        }

                    }
                }
                else{
                    all_view.setVisibility(View.INVISIBLE);
                    nearMe_view.setVisibility(View.VISIBLE);
                    btn_all.setTextColor(Color.parseColor("#000000"));
                    btn_nearMe.setTextColor(Color.parseColor("#F33AB5"));
                }
            }
        });

        btn_refresh = view.findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_LocationAccess_Permission(PERMISSIONS_REQUEST_CODE);
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
        builder.setTitle("GPS 비활성화");
        builder.setMessage("해당 기능을 사용하기 위해서는 GPS 서비스가 필요합니다.");
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

                //GPS가 활성화 되었는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        /////////////////////////////////////////////////
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");

                        all_view.setVisibility(View.INVISIBLE);
                        nearMe_view.setVisibility(View.VISIBLE);
                        btn_all.setTextColor(Color.parseColor("#747575"));
                        btn_nearMe.setTextColor(Color.parseColor("#F33AB5"));

                        Log.e(TAG, "onRequestPermissionsResult 퍼미션 존재");
                        gpsTracker = new GpsTracker(getActivity());

                        double latitude = gpsTracker.getLatitude();
                        double longitude = gpsTracker.getLongitude();
                        get_nearUser_from_server(latitude, longitude);
                        String address = getCurrentAddress(latitude, longitude);
                        address_view.setText(address);
                        Toast.makeText(getActivity(), "위치가 업데이트 되었습니다.", Toast.LENGTH_SHORT).show();

                        Log.e(TAG, "위도: " + latitude + "/ 경도: " + longitude);

                        return;
                    }
                }

                break;
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

            // 모든 퍼미션을 허용했다면
            if ( check_result ) {

                /**
                 *
                 */
                //체크되어 있으면 gps 켜져있는지 check
                if (checkLocationServicesStatus()) {
                    //gps 켜져 있으면 위도 경도 구하기 - checkRunTimePermission();
//                    checkRunTimePermission();
                    all_view.setVisibility(View.INVISIBLE);
                    nearMe_view.setVisibility(View.VISIBLE);
                    btn_all.setTextColor(Color.parseColor("#000000"));
                    btn_nearMe.setTextColor(Color.parseColor("#F33AB5"));

                    Log.e(TAG, "onRequestPermissionsResult 퍼미션 존재");
                    gpsTracker = new GpsTracker(getActivity());

                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();

                    String address = getCurrentAddress(latitude, longitude);
                    address_view.setText(address);
                    Toast.makeText(getActivity(), "위치가 업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
                    get_nearUser_from_server(latitude, longitude);
                    Log.e(TAG, "위도: " + latitude + "/ 경도: " + longitude);


                }else {
                    // gps 안켜져 있으면 gps 사용 요청 다이얼로그 띄우기
                    showDialogForLocationServiceSetting();
                }
            }
        }
    }



    public String getCurrentAddress( double latitude, double longitude) {
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


    /**
     * 내 위치 기기에 접근할 수 있도록 하는 권한이 허용되었는지 체크
     */
    public boolean check_LocationAccess_Permission(int request_code){
        Log.e("permission: ", "check_LocationAccess_Permission");
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("permission: ", "false");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, request_code);
            return false;
        }
        else{
            Log.e("permission: ", "true");
            return true;
        }
    }

    /**
     * 서버로 현재 위치의 위도, 경도를 보내고 서버에서 정렬해준 near user list를 받아오는 메서드
     * 서버에선 위도와 경도를 통해 유저 사이의 거리를 계산 후, 가까운 순서대로 정렬한 리스트를 보내준다.
     * @param latitude 위도
     * @param longitude 경도
     */
    public void get_nearUser_from_server(double latitude, double longitude){

        // gpsTracker로부터 얻은 위도, 경도를 서버로 보내서 저장
        call_near_user = retrofitClient.service.get_nearUserInfo(latitude, longitude, MainActivity.user_item.getUser_index());
        call_near_user.enqueue(new Callback<ArrayList<user_item>>() {
            @Override
            public void onResponse(Call<ArrayList<user_item>> call, Response<ArrayList<user_item>> response) {
                near_user_list = response.body();

                assert near_user_list != null;

                for (int i = 0; i<near_user_list.size(); i++){
                    if(user_id.equals(near_user_list.get(i).getUser_id())){
                        near_user_list.remove(i);
                        Log.e(TAG, "for문 들어옴");
                        break;
                    }

                }

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
    }

    public void set_allUSerList(final String type){
        retrofitClient = new RetrofitClient();
        call_all_user = retrofitClient.service.get_allUserInfo(MainActivity.user_item.getUser_index());
        call_all_user.enqueue(new Callback<ArrayList<com.example.canbefluent.items.user_item>>() {
            @Override
            public void onResponse(Call<ArrayList<com.example.canbefluent.items.user_item>> call, Response<ArrayList<com.example.canbefluent.items.user_item>> response) {
                ArrayList<user_item> result = response.body();
                MyApplication.user_list = result;
                Log.e(TAG, "onResponse");
                // 서버로부터 받아온 유저 리스트를 all_user_list에 담아준다.
                // 리사이클러뷰에 all_user_list를 보여준다.
                assert result != null;
                all_user_list.addAll(result);
                for(int i=0; i<all_user_list.size(); i++){
                    if(all_user_list.get(i).getUser_id().equals(user_id)){
                        all_user_list.remove(i);
                        break;
                    }
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
                        Log.e(TAG, "first: " + item.getFirst_name());
                        Log.e(TAG, "last: " + item.getLast_name());
                        Log.e(TAG, "sex: " + item.getSex());
                        Log.e(TAG, "profile: " + item.getProfile_img());
//                        Log.e(TAG, "year: " + item.getYear());
                        Log.e(TAG, "year: " + item.getYear());
                        Log.e(TAG, "month: " + item.getMonth());
                        Log.e(TAG, "day: " + item.getDay());

                        startActivity(intent);
                    }
                });
                recycler_allUser.setAdapter(allUser_adapter);

                if(type.equals("refresh")){
                    //새로고침 종료
                    refreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<com.example.canbefluent.items.user_item>> call, Throwable t) {

            }
        });
    }


    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if(type.equals("find candidate")){
                user_item item = (user_item) intent.getSerializableExtra("user item");

                for (Fragment fragment: getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment.isVisible()) {
                        Log.e(TAG, "for문 실행");
                        if(fragment instanceof frag_community){

//                            e = new random_call_dialog(name, profile);
                            frag_randomCall.e.show(getFragmentManager(), random_call_dialog.TAG_DIALOG);
                        }
                    }
                }
            }
            else if(type.equals("cancel")){

                for (Fragment fragment: getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment.isVisible()) {
                        Log.e(TAG, "for문 실행");
                        if(fragment instanceof frag_community){

                            frag_randomCall.e.dismissDialog();
                            Toast.makeText(getActivity(), "상대방이 매칭을 취소했습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
            else if(type.equals("time out")){
                for (Fragment fragment: getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment.isVisible()) {
                        Log.e(TAG, "for문 실행");
                        if(fragment instanceof frag_community){

                            MainActivity.search_floating_view.setVisibility(View.GONE);
                            MainActivity.isSearching = false;

                            show();
//                            Toast.makeText(getActivity(), "매칭 타임아웃", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter("random call")
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                invitationResponseReceiver
        );
    }


    private void openDialog(String lang_code, String type) {

        DialogFragment myDialogFragment = new MyDialogFragment(lang_code, type);

        myDialogFragment.setTargetFragment(this, 0);

        myDialogFragment.show(getFragmentManager(), "Search Filter");

    }

    /**
     * 매칭 타임아웃인 경우 보여주는 alert 다이얼로그
     */
    public void show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("매칭 타임아웃");
        builder.setMessage("조건에 맞는 대화상대가 없습니다. 잠시 후 다시 매칭을 시도해주세요.");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }
}
