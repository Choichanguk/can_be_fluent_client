package com.example.canbefluent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.canbefluent.adapter.languageListAdapter;
import com.example.canbefluent.items.language_item;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.PostResult;
import com.example.canbefluent.pojoClass.getCountryNameResult;
import com.example.canbefluent.pojoClass.getRegisterUserResult;
import com.example.canbefluent.pojoClass.imgUploadResult;
import com.example.canbefluent.retrofit.RetrofitClient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register_setLanguage extends AppCompatActivity {
    Button btn_finish, btn_previous;
    ImageButton btn_set_native, btn_set_learn;
    ArrayList<String> country_list = new ArrayList<>(); // 국가이름을 담는 리스트

    RecyclerView recycler2, recycler3;
    languageListAdapter adapter2, adapter3;
    ArrayList<language_item> native_list = new ArrayList<>();
    ArrayList<language_item> learn_list = new ArrayList<>();

    private static final int GET_NATIVE = 4;
    private static final int GET_LEARN = 5;
    private static final String TAG = "Register_setLanguage";

    user_item user_item = new user_item();    //유저 데이터가 담기는 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_set_language);

        Intent intent = getIntent();
        user_item = (user_item) intent.getSerializableExtra("user item");

        /**
         * 국가 이름을 제공하는 api로부터 국가 이름을 가져온 후 country_list에 담아준다.
         */
        RetrofitClient retrofitClient = new RetrofitClient();
        Call<ArrayList<getCountryNameResult>> call = retrofitClient.service2.getCountryName("name;");

        //Enqueue로 비동기 통신 실행.
        call.enqueue(new Callback<ArrayList<getCountryNameResult>>() {

            //통신 완료 후 이벤트 처리 위한 Callback 리스너 onResponse, onFailure 등록
            @Override
            public void onResponse(Call<ArrayList<getCountryNameResult>> call, Response<ArrayList<getCountryNameResult>> response) {
                if(response.isSuccessful()){

                    //정상적으로 통신 성공
                    ArrayList<getCountryNameResult> result = response.body();

                    //api로부터 가져온 국가 리스트를 country_list에 담는다.
                    if(result != null){
                        for(int i=0; i<result.size(); i++){
                            country_list.add(result.get(i).toString());
                        }
                    }
                }
                else{
                    //통신이 실패할 경우
                    Log.e("main", "onResponse 실패");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<getCountryNameResult>> call, Throwable t) {
                //통신실패 (인터넷 끊김, 예외 발생 등 시스템적인 이유)
                Log.e("main", "onFailure" + t.getMessage());
            }
        });

        btn_set_native = findViewById(R.id.btn_set_native);   // 구사 가능 언어를 추가하는 버튼
        btn_set_learn = findViewById(R.id.btn_set_learn);   // 학습 언어를 추가하는 버튼

        btn_set_native.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(native_list.size() == 2){

                }
                else{
                    Intent intent = new Intent(getApplicationContext(), countryListActivity.class);
                    intent.putExtra("country list", country_list);
                    intent.putExtra("type", "native");
                    startActivityForResult(intent, GET_NATIVE);
                }
            }
        });

        btn_set_learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(getApplicationContext(), countryListActivity.class);
                    intent.putExtra("country list", country_list);
                    intent.putExtra("type", "practice");
                    startActivityForResult(intent, GET_LEARN);
            }
        });


        recycler2 = findViewById(R.id.recycler2);   //구사 언어를 담는 recycler
        recycler3 = findViewById(R.id.recycler3);   //학습 언어를 담는 recycler

        adapter2 = new languageListAdapter(native_list); // 구사 언어 recycler를 처리하는 어댑터

        // 아이템 클릭 리스너를 달아준다.
        // 아이템 클릭 시 해당 언어를 목록에서 제거할지 선택하는 dialog를 띄워주고, 확인을 누르면 목록에서 선택 아이템이 제거된다.
        adapter2.setOnItemClickListener(new languageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // type은 구사 언어인지 학습언어인지를 구분하는 역할
                // position은 선택 아이템의 index값.
                alertDialog("delete native", position);

            }
        });
        recycler2.setLayoutManager(new LinearLayoutManager(this));
        recycler2.setAdapter(adapter2);

        adapter3 = new languageListAdapter(learn_list);
        adapter3.setOnItemClickListener(new languageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                alertDialog("delete learn", position);
                adapter3.notifyDataSetChanged();
            }
        });
        recycler3.setLayoutManager(new LinearLayoutManager(this));
        recycler3.setAdapter(adapter3);

        btn_previous = findViewById(R.id.btn_previous); // 이전 화면으로 가는 버튼
        btn_finish = findViewById(R.id.btn_finish);     // 회원가입을 완료 버튼. 클릭 시 메인 화면으로 넘어간다.

        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /**
         * 회원가입 완료 버튼.
         *
         */
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(native_list.size() !=0 && learn_list.size() !=0){
                    for(int i=0; i<native_list.size(); i++){
                        if(i==0){
                            user_item.setNative_lang1(native_list.get(i).getLang_name());
                        }
                        else if (i==1){
                            user_item.setNative_lang2(native_list.get(i).getLang_name());
                        }
                    }

                    for(int i=0; i<learn_list.size(); i++){
                        if(i==0){
                            user_item.setPractice_lang1(learn_list.get(i).getLang_name());
                            user_item.setPractice_lang1_level(learn_list.get(i).getLevel());
                        }
                        else if (i==1){
                            user_item.setPractice_lang2(learn_list.get(i).getLang_name());
                            user_item.setPractice_lang2_level(learn_list.get(i).getLevel());
                        }
                    }

                    // user_info 객체에 담긴 프로필 이미지 path를 통해 file 객체를 만든다.
                    // Hash map에 file 객체를 담아 서버에 보내준다.
                    File file = new File(user_item.getProfile_img());
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part uploadedFile = MultipartBody.Part.createFormData("uploaded_file", file.getPath(), requestFile);

                    // 서버로 보낼 multipart 파라미터를 hash맵에 key, value 형태로 담아준다.
                    HashMap<String, RequestBody> params = new HashMap<>();
                    params.put("user_id", RequestBody.create(MediaType.parse("text/plain"), user_item.getUser_id()));
                    params.put("user_pw", RequestBody.create(MediaType.parse("text/plain"), user_item.getUser_pw()));
                    params.put("sex", RequestBody.create(MediaType.parse("text/plain"), user_item.getSex()));
                    params.put("year", RequestBody.create(MediaType.parse("text/plain"), user_item.getYear()));
                    params.put("month", RequestBody.create(MediaType.parse("text/plain"), user_item.getMonth()));
                    params.put("day", RequestBody.create(MediaType.parse("text/plain"), user_item.getDay()));
                    params.put("first_name", RequestBody.create(MediaType.parse("text/plain"), user_item.getFirst_name()));
                    params.put("last_name", RequestBody.create(MediaType.parse("text/plain"), user_item.getLast_name()));


                    params.put("native_lang1", RequestBody.create(MediaType.parse("text/plain"), user_item.getNative_lang1()));
                    params.put("practice_lang1", RequestBody.create(MediaType.parse("text/plain"), user_item.getPractice_lang1()));
                    params.put("practice_lang1_level", RequestBody.create(MediaType.parse("text/plain"), user_item.getPractice_lang1_level()));

                    if(user_item.getNative_lang2() !=null){
                        params.put("native_lang2", RequestBody.create(MediaType.parse("text/plain"), user_item.getNative_lang2()));
                    }
                    if(user_item.getPractice_lang2() !=null){
                        params.put("practice_lang2", RequestBody.create(MediaType.parse("text/plain"), user_item.getPractice_lang2()));
                        params.put("practice_lang2_level", RequestBody.create(MediaType.parse("text/plain"), user_item.getPractice_lang2_level()));
                    }

                    RetrofitClient retrofitClient = new RetrofitClient();
                    Call<getRegisterUserResult> call = retrofitClient.service.register_user(uploadedFile, params);
                    call.enqueue(new Callback<getRegisterUserResult>() {
                        @Override
                        public void onResponse(Call<getRegisterUserResult> call, Response<getRegisterUserResult> response) {
                            getRegisterUserResult result = response.body();
                            Log.e(TAG, "onResponse : " + result);


                            assert result != null;
                            if(result.toString().equals("success")){
                                alertDialog("success register", 0);
                            }
                            else{

                            }
                        }

                        @Override
                        public void onFailure(Call<getRegisterUserResult> call, Throwable t) {

                            Log.e(TAG, "onFailure result: " + t);
                        }
                    });

                    Log.e(TAG, "map: " + params);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            if(requestCode == GET_NATIVE){
                String country_name = data.getStringExtra("name");



                language_item item = new language_item();
                item.setLang_name(country_name);

                native_list.add(item);
                adapter2.notifyDataSetChanged();

                //제공언어의 수를 2개이하로 제한하고 제공언어를 이미 2개 선택했으면 언어 추가 버튼을 보이지 않게 한다.
                if(native_list.size() == 2){
                    btn_set_native.setVisibility(View.INVISIBLE);
                }

            }
            else if(requestCode == GET_LEARN){
                String country_name = data.getStringExtra("name");
                String level = data.getStringExtra("level");

                language_item item = new language_item();
                item.setLang_name(country_name);
                item.setLevel(level);

                learn_list.add(item);
                adapter3.notifyDataSetChanged();

                //학습언어의 수를 2개이하로 제한하고 학습언어를 이미 2개 선택했으면 언어 추가 버튼을 보이지 않게 한다.
                if(learn_list.size() == 2){
                    btn_set_learn.setVisibility(View.INVISIBLE);
                }
            }
        }
    }


    public void alertDialog(final String type, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(type.equals("success register")){
            builder.setTitle("").setMessage("회원가입에 성공했습니다!");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("user item", user_item);
                    startActivity(intent);
                }
            });
        }
        else{
            builder.setTitle("").setMessage("해당 언어를 목록에서 제거하시겠습니까?");


            builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    if(type == "delete native"){
                        native_list.remove(position);
                        adapter2.notifyDataSetChanged();

                        // 아이템을 제거하면 구사 언어 목록이 2미만이기 때문에 다시 언어를 추가할 수 있는 버튼을 보여준다.
                        btn_set_native.setVisibility(View.VISIBLE);
                    }
                    else if(type == "delete learn"){
                        learn_list.remove(position);
                        adapter3.notifyDataSetChanged();

                        // 아이템을 제거하면 제공 언어 목록이 2미만이기 때문에 다시 언어를 추가할 수 있는 버튼을 보여준다.
                        btn_set_learn.setVisibility(View.VISIBLE);
                    }
                }
            });

            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }


        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

}