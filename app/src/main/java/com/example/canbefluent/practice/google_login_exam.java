package com.example.canbefluent.practice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.canbefluent.R;
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

public class google_login_exam extends AppCompatActivity{

    TextView updateUI;

    private FirebaseAuth mAuth = null;  // 파이어베이스 인증 객체
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;     // 구글 로그인 결과 코드
    private SignInButton signInButton;  // 구글 로그인 버튼

    private static final String TAG = "google_login_exam";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login_exam);
        updateUI = findViewById(R.id.updateUI);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuth = FirebaseAuth.getInstance();

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "signInButton click");
                signIn();
            }
        });
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

//        //로그인 한 후 바로 다음 화면으로 이동하게 하는 로직
//        if (mAuth.getCurrentUser() != null) {
//            FirebaseUser account = mAuth.getCurrentUser();
//
//            Task<GetTokenResult> token = account.getIdToken(true);
//            Log.e(TAG, "meta data: " + account.getUid());
//            Intent intent = new Intent(getApplication(), google_logout_exam.class);
//            token.toString();
////            zzu@f16e492
//            startActivity(intent);
//            finish();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 이미 사인한 구글 id가 있는지 조회한 후, 없다면 account에 null 값 할당
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        updateUI(account);
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

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.e(TAG, "firebaseAuthWithGoogle");
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Snackbar.make(findViewById(R.id.layout_main), "Authentication Successed.", Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
//                            UserInfo userInfo = (UserInfo) user.getProviderData();

                            // Prompt the user to re-provide their sign-in credentials
//                            user.reauthenticate(credential)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            Log.d(TAG, "User re-authenticated.");
//                                        }
//                                    });

                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
//                            Snackbar.make(findViewById(R.id.layout_main), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void updateUI(FirebaseUser user){
        Log.e(TAG, "updateUI");
        if (user != null) {

            Intent intent = new Intent(this, google_logout_exam.class);
            startActivity(intent);
            finish();
        }
    }
}