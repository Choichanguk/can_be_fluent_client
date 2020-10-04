package com.example.canbefluent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.canbefluent.items.user_item;
import com.example.canbefluent.pojoClass.PostResult;
import com.example.canbefluent.retrofit.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register_setId extends AppCompatActivity {
    private static final String TAG = "Register_setId";
    Button btn_next, btn_previous, btn_checkId;
    EditText id, pw, re_pw;
    TextView id_check_txt, pw_check_txt;
    user_item user_item = new user_item();  // 회원가입 정보를 담을 객체 생성
    boolean isCheckDuple = false;   // 아이디 중복 체크 했는지 판별하는 boolean
    boolean isSamePW = false;   // 비밀번호가 똑같은지 체크하는 boolean

    ArrayList<String> id_array = new ArrayList<>(); // 서버로부터 받아온 user id list를 담는 list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_set_id);

        btn_next = findViewById(R.id.btn_next); // 프로필 설정으로 이동하는 버튼
        btn_checkId = findViewById(R.id.btn_checkId);   // 아이디 중복확인 버튼
        btn_previous = findViewById(R.id.btn_previous);

        id_check_txt = findViewById(R.id.id_check_txt); // id 중복인지 알려주는 textview
        pw_check_txt = findViewById(R.id.pw_check_txt); // pw와 re_pw가 일치하는지 알려주는 textview

        id = findViewById(R.id.id);
        pw = findViewById(R.id.pw);
        re_pw = findViewById(R.id.re_pw);

        id.addTextChangedListener(textWatcher2);
        re_pw.addTextChangedListener(textWatcher);

        // 프로필 설정 액티비티로 넘어가는 버튼(모든 입력 제대로 받았는지 확인해야함)
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCheckDuple && isSamePW){
                    user_item.setUser_id(id.getText().toString());
                    user_item.setUser_pw(pw.getText().toString());
                    Intent intent = new Intent(getApplicationContext(), Register_setProfile.class);
                    intent.putExtra("user item", user_item);
                    startActivity(intent);
                }
                else if(!isCheckDuple){
                    alertDialog("check duple");
                }
                else if(!isSamePW){
                    alertDialog("check password");
                }

            }
        });

        // 아이디 중복확인 버튼
        // 클릭 시 서버로부터 user id list를 가져와서 유저가 입력한 id와 비교 후 id 중복확인 문구를 띄워준다.
        btn_checkId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String input_id = id.getText().toString();

                if(input_id.equals("")){
                    alertDialog("write id");
                }
                else{

                    RetrofitClient retrofitClient = new RetrofitClient();
                    Call<ArrayList<PostResult>> call = retrofitClient.service.getPosts();

                    //Enqueue로 비동기 통신 실행.
                    call.enqueue(new Callback<ArrayList<PostResult>>() {

                        //통신 완료 후 이벤트 처리 위한 Callback 리스너 onResponse, onFailure 등록
                        @Override
                        public void onResponse(Call<ArrayList<PostResult>> call, Response<ArrayList<PostResult>> response) {
                            if(response.isSuccessful()){
                                //정상적으로 통신 성공
                                ArrayList<PostResult> result = response.body();
//                                Log.e("main", "onResponse: 성공, 결과\n" + result);
                                if(result != null){
                                    for(int i=0; i<result.size(); i++){
                                        id_array.add(result.get(i).toString());
                                    }
                                }
                                else{
                                    id_check_txt.setText("사용 가능한 아이디입니다.");
                                    id_check_txt.setTextColor(Color.BLUE);
                                    isCheckDuple = true;
                                }

                                if(id_array.contains(input_id)){
                                    id_check_txt.setText("이미 사용중인 아이디입니다.");
                                    id_check_txt.setTextColor(Color.RED);
                                    isCheckDuple = false;
                                }
                                else{
                                    alertDialog("can use");
                                    id_check_txt.setText("사용 가능한 아이디입니다.");
                                    id_check_txt.setTextColor(Color.BLUE);
                                    isCheckDuple = true;
                                }

                            }
                            else{
                                //통신이 실패할 경우
                                Log.e("main", "onResponse 실패");
                            }
                        }

                        @Override
                        public void onFailure(Call<ArrayList<PostResult>> call, Throwable t) {
                            //통신실패 (인터넷 끊김, 예외 발생 등 시스템적인 이유)
                            Log.e("main", "onFailure" + t.getMessage());
                        }
                    });
                }
            }
        });

        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void alertDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(type.equals("write id")){
            builder.setTitle("").setMessage("아이디를 입력해주세요.");
        }
        else if(type.equals("check duple")){
            builder.setTitle("").setMessage("아이디 중복확인이 필요합니다.");
        }
        else if(type.equals("check password")){
            builder.setTitle("").setMessage("비밀번호가 일치하지 않습니다.");
        }
        else if(type.equals("can use")){
            builder.setTitle("").setMessage("사용 가능한 아이디입니다.");
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
     * editText의 입력변화 감지 리스너
     * re_pw editText의 입력값이 pw editText의 입력값과 일치하는지 실시간 감지
     */
    private final TextWatcher textWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // 입력하기 전에 조치
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 입력란에 변화가 있을 시 조치
            String input_pw = pw.getText().toString();
            if(!input_pw.equals(s.toString())){
                pw_check_txt.setText("비밀번호가 일치하지 않습니다.");
                pw_check_txt.setTextColor(Color.RED);
                isSamePW = false;
            }
            else{
                pw_check_txt.setText("비밀번호가 일치합니다.");
                pw_check_txt.setTextColor(Color.BLUE);
                isSamePW = true;
            }
        }

        public void afterTextChanged(Editable s) {
            // 입력이 끝났을 때 조치
        }
    };

    private final TextWatcher textWatcher2 = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // 입력하기 전에 조치
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 입력란에 변화가 있을 시 조치
            isCheckDuple = false;
        }

        public void afterTextChanged(Editable s) {
            // 입력이 끝났을 때 조치
        }
    };
}