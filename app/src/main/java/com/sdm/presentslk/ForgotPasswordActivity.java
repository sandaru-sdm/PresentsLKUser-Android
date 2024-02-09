package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {
    private final static String TAG = "ForgotPasswordActivity";
    private Button buttonPwdReset;
    private EditText editTextPwdResetEmail;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        editTextPwdResetEmail = findViewById(R.id.editTextPasswordResetEmail);
        buttonPwdReset = findViewById(R.id.buttonPasswordReset);
        progressBar = findViewById(R.id.progressBar);

        buttonPwdReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextPwdResetEmail.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(ForgotPasswordActivity.this, "Please Enter Your Registered Email.", Toast.LENGTH_LONG).show();
                    editTextPwdResetEmail.setError("Email is Required.");
                    editTextPwdResetEmail.requestFocus();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(ForgotPasswordActivity.this, "Please Enter Valid Email.", Toast.LENGTH_LONG).show();
                    editTextPwdResetEmail.setError("Email is Not Valid.");
                    editTextPwdResetEmail.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    resetPassword(email);
                }
            }
        });
    }

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, "Please Check Your Email for Password Rest Link", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);

                    //Clear stack to prevent user coming back to ForgotPasswordActivity on pressing back button.
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();   //Close UserProfileActivity
                } else {

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e){
                        editTextPwdResetEmail.setError("User Does  Not Exists or No Longer Available. Please Register Again.");
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(ForgotPasswordActivity.this, "You Can reset your Password now!", Toast.LENGTH_SHORT).show();
    }
}