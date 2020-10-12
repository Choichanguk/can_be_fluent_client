package com.example.canbefluent.practice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.canbefluent.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class google_logout_exam extends AppCompatActivity {
    private static final String TAG = "google_logout_exam";
    private FirebaseAuth mAuth ;
    Button logout, delete;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_logout_exam);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        logout = findViewById(R.id.log_out);
        delete = findViewById(R.id.delete);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revokeAccess();
            }
        });

        Log.e(TAG, "UID: " + mAuth.getUid());
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