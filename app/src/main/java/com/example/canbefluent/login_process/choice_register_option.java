package com.example.canbefluent.login_process;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canbefluent.MainActivity;
import com.example.canbefluent.R;
import com.example.canbefluent.items.user_item;
import com.example.canbefluent.retrofit.RetrofitClient;
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

public class choice_register_option extends AppCompatActivity {
    SignInButton btn_register_google;   // 구글 로그인 버튼
    Button btn_register_direct;
    private static final String TAG = "choice_register_option";

    private FirebaseAuth mAuth = null;  // 파이어베이스 인증 객체
    private GoogleSignInClient mGoogleSignInClient = null;
    private static final int RC_SIGN_IN = 9001;     // 구글 로그인 결과 코드
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_register_option);

        btn_register_direct = findViewById(R.id.btn_register_direct);
        btn_register_google = findViewById(R.id.btn_register_google);

        // 구글 로그인 버튼의 text를 바꿔준다.
        setGooglePlusButtonText(btn_register_google, "구글 계정으로 가입");

        btn_register_direct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(choice_register_option.this, Register_setId.class);
                startActivity(intent);
            }
        });

        btn_register_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuth = FirebaseAuth.getInstance();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void signIn() {
        Log.e(TAG, "singIn");
        if (mGoogleSignInClient != null){
            mGoogleSignInClient.signOut();
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // 구글 로그인 버튼을 누른후 결과를 받는 메서드
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

    // 구글 로그인 후 firebase에 인증을 받는 메서드
    // 순서 설명
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.e(TAG, "firebaseAuthWithGoogle");
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            final String UID = user.getUid();
                            final String first_name = acct.getGivenName();
                            final String last_name = acct.getFamilyName();
                            user.getUid();

                            // 구글 계정으로 가입이 되어있는지 확인한는 절차
                            RetrofitClient retrofitClient = new RetrofitClient();
                            Call<user_item[]> call = retrofitClient.service.login_process(user.getUid());
                            call.enqueue(new Callback<user_item[]>() {
                                @Override
                                public void onResponse(Call<user_item[]> call, Response<user_item[]> response) {
                                    user_item[] user_item_arr = response.body();

                                    assert user_item_arr != null;
                                    user_item user_item = user_item_arr[0];

                                    String result = user_item.getResult();
                                    if(result.equals("success")){
                                        // 이미 구글 계정으로 가입한 id가 존재하는 경우
                                        // 이미 아이디가 존재한다는 다이얼로그를 띄워준다.
                                        alertDialog(user_item);
                                    }
                                    else if(result.equals("fail")){
                                        // 구글 계정으로 가입한 id가 없는 경우
                                        // 회원가입 진행
                                        Intent intent = new Intent(choice_register_option.this, Register_setProfile.class);
                                        intent.putExtra("first_name", first_name);
                                        intent.putExtra("last_name", last_name);
                                        intent.putExtra("UID", UID);
                                        intent.putExtra("type", "google register");
                                        startActivity(intent);
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


    /**
     * 구글 로그인 버튼 텍스트 변경 메서드
     * @param signInButton
     * @param buttonText
     */
    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Search all the views inside SignInButton for TextView
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            // if the view is instance of TextView then change the text SignInButton
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }


    public void alertDialog(final user_item user_item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("").setMessage("이미 가입한 아이디가 존재합니다. 로그인 하시겠습니까?");

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("user item", user_item);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}