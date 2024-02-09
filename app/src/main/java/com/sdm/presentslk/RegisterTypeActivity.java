package com.sdm.presentslk;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegisterTypeActivity extends AppCompatActivity {
    public static final String TAG = "SignInType";
    private Button buttonRegister, buttonRegisterGoogle, buttonRegisterOnetap;
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_type);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        buttonRegister = findViewById(R.id.buttonSignUp);
        buttonRegisterGoogle = findViewById(R.id.buttonSignUpWithGoogle);
        buttonRegisterOnetap = findViewById(R.id.buttonSignUpWithOneTap);

        firebaseAuth = FirebaseAuth.getInstance();
        signInClient = Identity.getSignInClient(getApplicationContext());

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterTypeActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        buttonRegisterGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetSignInIntentRequest signInIntentRequest = GetSignInIntentRequest.builder().setServerClientId(getString(R.string.web_client_id)).build();

                Task<PendingIntent> signInIntent = signInClient.getSignInIntent(signInIntentRequest);
                signInIntent.addOnSuccessListener(new OnSuccessListener<PendingIntent>() {
                    @Override
                    public void onSuccess(PendingIntent pendingIntent) {
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent).build();
                        signInLauncher.launch(intentSenderRequest);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });

        buttonRegisterOnetap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeginSignInRequest oneTapRequest = BeginSignInRequest.builder()
                        .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.web_client_id))
                                .setFilterByAuthorizedAccounts(true)
                                .build())
                        .build();

                Task<BeginSignInResult> beginSignInResultTask = signInClient.beginSignIn(oneTapRequest);
                beginSignInResultTask.addOnSuccessListener(new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(beginSignInResult.getPendingIntent()).build();
                        signInLauncher.launch(intentSenderRequest);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });
    }

    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        Task<AuthResult> authResultTask = firebaseAuth.signInWithCredential(credential);
        authResultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUi(user);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void handleSignInResult(Intent intent){
        try {
            SignInCredential signInCredential = signInClient.getSignInCredentialFromIntent(intent);
            String idToken = signInCredential.getGoogleIdToken();
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e){
            Log.e(TAG, e.getMessage());
        }

    }

    private void updateUi(FirebaseUser user){
        if(user != null){
            Intent intent = new Intent(RegisterTypeActivity.this, HomeViewActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            handleSignInResult(o.getData());
                        }
                    }
            );
}