package com.example.canbefluent.login_process;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canbefluent.MainActivity;
import com.example.canbefluent.R;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;
import com.example.canbefluent.sharedPreference;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    Button btn_login;
    TextView btn_register;
    EditText user_id, user_pw;
    sharedPreference sharedPreference; // 유저의 id, 로그인 상태를 shared preference에 저장하는 클래스

    // 구글 로그인 연동을 위한 변수
    private FirebaseAuth mAuth = null;  // 파이어베이스 인증 객체
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;     // 구글 로그인 결과 코드
    private SignInButton signInButton;  // 구글 로그인 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user_id = findViewById(R.id.user_id);
        user_pw = findViewById(R.id.user_pw);



        btn_register = findViewById(R.id.btn_register);

        String mystring = "새 계정 만들기";
        SpannableString content = new SpannableString(mystring);
        content.setSpan(new UnderlineSpan(), 0, mystring.length(), 0);
        btn_register.setText(content);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), choice_register_option.class);
                startActivity(intent);
            }
        });

        btn_login = findViewById(R.id.btn_login);
        // 클릭 시 유저가 입력한 id, pw 값을 서버로 보낸다.
        // 서버는 로그인 가능 or 불가능인지 판별 후 결과 값을 클라이언트로 보낸다. (이 때 결과값이 로그인 가능이면 유저 정보도 함께 보낸다.)
        // 클라이언트는 결과값을 받아서 결과값이 로그인 가능이면 유저 정보를 POJO class에 담아 main activity에 넘긴다.
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = user_id.getText().toString();
                String pw = user_pw.getText().toString();
                Log.e(TAG, "id: " + id);
                Log.e(TAG, "pw: " + pw);

                if(id.equals("") || pw.equals("")){
                    alertDialog("write id");
                }
                else{
                    //유저가 입력한 id, pw 값을 서버로 보낸다.
                    RetrofitClient retrofitClient = new RetrofitClient();
                    Call<user_item[]> call = retrofitClient.service.login_process(id, pw);
                    call.enqueue(new Callback<user_item[]>() { // 서버로부터 결과 값을 받는 callback 함수
                        @Override
                        public void onResponse(Call<user_item[]> call, Response<user_item[]> response) {
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
                                // 결과 값이 success면
                                // 1. shared에 유저의 아이디와 로그인 상태를 저장한다.
                                // 2. user_item 객체를 main activity에 넘겨준다.

                                sharedPreference = new sharedPreference();
                                sharedPreference.saveUserId(getApplicationContext(), user_item.getUser_id());   //shared에 유저 아이디 저장
                                sharedPreference.saveUserPw(getApplicationContext(), user_item.getUser_pw());   //shared에 유저 비밀번호 저장
                                sharedPreference.saveLoginStatus(getApplicationContext(), true);    // shared에 로그인 상태 저장

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("user item", user_item);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                            else if(result.equals("fail")){ // 결과 값이 fail이면 로그인 실패 다이얼로그를 띄워준다.
                                alertDialog("login fail");
                            }
                        }

                        @Override
                        public void onFailure(Call<user_item[]> call, Throwable t) {
                            Log.e(TAG, "onFailure " + t.getMessage());
                        }
                    });
                }
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuth = FirebaseAuth.getInstance();

        // Set the dimensions of the sign-in button.
        signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "signInButton click");
                if(mAuth != null){
                    signOut();
                }
                signIn();
            }
        });

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Log.e(TAG, "singIn");

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * 구글 로그인 인증을 요청했을 때 결과 값을 되돌려 받는 곳
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            // The GoogleSignInAccount object contains information about the signed-in user, such as the user's name.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // 로그인 후 받아온 GoogleSignInAccount 객체를 처리하는 메소드
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.e(TAG, "handleSignInResult");
        try {
            // account에는 구글 로그인 정보를 담고 있다.
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
//            updateUI(account);
//            Log.e(TAG, "e mail: " + account.getEmail());
//            Log.e(TAG, "e mail: " + account.getFamilyName());
//            Log.e(TAG, "e mail: " + account.getGivenName());

            firebaseAuthWithGoogle(account);
//            account.getIdToken();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.e(TAG, "firebaseAuthWithGoogle");
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Snackbar.make(findViewById(R.id.layout_main), "Authentication Successed.", Snackbar.LENGTH_SHORT).show();
//                            Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                            final FirebaseUser user = mAuth.getCurrentUser();
                            String UID = user.getUid();

                            Log.e(TAG, "서버로 보내는 UID: " + UID);
                            // 서버로 UID를 보내 UID에 일치하는 유저 데이터를 가져온다.
                            // 일치하는 UID가 없으면 가입된 아이디 없다는 다이얼로그 띄우고 register 화면으로 이동시킨다.
                            RetrofitClient retrofitClient = new RetrofitClient();
                            Call<user_item[]> call = retrofitClient.service.login_process(UID);
                            call.enqueue(new Callback<user_item[]>() {
                                @Override
                                public void onResponse(Call<user_item[]> call, Response<user_item[]> response) {
                                    user_item[] user_item_arr = response.body();
                                    user_item user_item = new user_item();
                                    assert user_item_arr != null;
                                    for(user_item item : user_item_arr){
                                        user_item = item;
                                    }
                                    assert user_item != null;
                                    String result = user_item.getResult();
                                    if(result.equals("success")){
                                        // 결과 값이 success면
                                        // 1. shared에 유저의 아이디와 로그인 상태를 저장한다.
                                        // 2. user_item 객체를 main activity에 넘겨준다.

                                        sharedPreference = new sharedPreference();
                                        sharedPreference.saveUserId(getApplicationContext(), user_item.getUser_id());   //shared에 유저 UID 저장
//                                        sharedPreference.saveUserPw(getApplicationContext(), user_item.getUser_pw());   //shared에 유저 비밀번호 저장
                                        sharedPreference.saveLoginStatus(getApplicationContext(), true);    // shared에 로그인 상태 저장
                                        String user_id = sharedPreference.loadUserId(Login.this);
                                        Log.e(TAG, "user id: " + user_id);
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.putExtra("user item", user_item);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else if(result.equals("fail")){ // 결과 값이 fail이면 로그인 실패 다이얼로그를 띄워준다.
                                        alertDialog(acct);
//                                        alertDialog("google login fail");
                                    }
                                }

                                @Override
                                public void onFailure(Call<user_item[]> call, Throwable t) {

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
//                            Snackbar.make(findViewById(R.id.layout_main), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void alertDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(type.equals("write id")){
            builder.setTitle("").setMessage("아이디 or 비밀번호를 입력해주세요.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                }
            });
        }
        else if(type.equals("login fail")){
            builder.setTitle("").setMessage("아이디 or 비밀번호가 일치하지 않습니다.");

        }
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public void alertDialog(final GoogleSignInAccount acct){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("").setMessage("가입된 아이디가 존재하지 않습니다. 새로운 아이디를 등록하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                FirebaseUser user = mAuth.getCurrentUser();
                final String UID = user.getUid();
                final String first_name = acct.getGivenName();
                final String last_name = acct.getFamilyName();

                Intent intent = new Intent(Login.this, Register_setProfile.class);
                intent.putExtra("first_name", first_name);
                intent.putExtra("last_name", last_name);
                intent.putExtra("UID", UID);
                intent.putExtra("type", "google register");
                startActivity(intent);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }


    // 로그아웃 함수
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut();

    }

    // 회원탈퇴 함수
    private void revokeAccess() {
        mAuth.getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mGoogleSignInClient.revokeAccess();
                            Log.d(TAG, "User account deleted.");
                        }
                    }
                });
    }

}