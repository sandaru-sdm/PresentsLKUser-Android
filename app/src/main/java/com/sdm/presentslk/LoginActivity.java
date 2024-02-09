package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText editTextLoginEmail, editTextLoginPassword;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        progressBar = findViewById(R.id.progressBar);

        firebaseAuth = FirebaseAuth.getInstance();

        //Reset Password
        TextView textViewLinkResetPassword = findViewById(R.id.textViewForgotPasswordLink);
        textViewLinkResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        //Register
        TextView textViewLinkRegister = findViewById(R.id.textViewRegisterLink);
        textViewLinkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterTypeActivity.class));
            }
        });

        //Show hide Password using Eye icon
        ImageView imageViewShowHidePwd = findViewById(R.id.imageView_show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextLoginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is visible then hide it
                    editTextLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        //Login User
        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPwd = editTextLoginPassword.getText().toString();

                if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(LoginActivity.this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Email is Required.");
                    editTextLoginEmail.requestFocus();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(LoginActivity.this, "Please Re-enter Your Email.", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Email is not Valid.");
                    editTextLoginEmail.requestFocus();
                } else if(TextUtils.isEmpty(textPwd)){
                    Toast.makeText(LoginActivity.this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
                    editTextLoginPassword.setError("Password is Required.");
                    editTextLoginPassword.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail, textPwd);
                }

            }
        });
    }
    private void loginUser(String email, String pwd) {
        firebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    // Check if email is verified or not
                    if (firebaseUser.isEmailVerified()) {
                        // Get user type and go to the appropriate activity
                        String userId = firebaseUser.getUid();
                        checkUserTypeAndNavigate(userId);
                    } else {
                        // If user is an admin, send verification email
                        String userId = firebaseUser.getUid();
                        checkUserAndSendVerificationEmail(userId);
                    }

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        editTextLoginEmail.setError("User does not exist or no longer valid. Please register again.");
                        editTextLoginEmail.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextLoginEmail.setError("Invalid Email or Password.");
                        editTextLoginEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void checkUserAndSendVerificationEmail(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userType = dataSnapshot.child("userType").getValue(String.class);

                    if ("user".equals(userType)) {
                        // User is an admin, send verification email
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        firebaseUser.sendEmailVerification();
                        firebaseAuth.signOut(); // Sign out user
                        showAlertDialog();
                    } else {
                        editTextLoginEmail.setError("Admins cannot log in from this app");
                        editTextLoginEmail.requestFocus();
                        editTextLoginEmail.setText("");
                        editTextLoginPassword.setText("");
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_LONG).show();
                    firebaseAuth.signOut(); // Sign out the user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error reading user data", databaseError.toException());
                firebaseAuth.signOut(); // Sign out the user
            }
        });
    }
    private void checkUserTypeAndNavigate(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming you have a field named "userType" in your database
                    String userType = dataSnapshot.child("userType").getValue(String.class);

                    if ("admin".equals(userType)) {
                        // Do not allow login for admins in the user app
                        editTextLoginEmail.setError("Users Data Not Available");
                        editTextLoginEmail.requestFocus();
                        editTextLoginEmail.setText("");
                        editTextLoginPassword.setText("");
                        Toast.makeText(LoginActivity.this, "Data Not Available", Toast.LENGTH_LONG).show();
                        firebaseAuth.signOut(); // Sign out the user
                    } else {
                        // Continue with normal user login
                        // For example, navigate to the user's dashboard
                        startActivity(new Intent(LoginActivity.this, HomeViewActivity.class));
                        finish(); // Close Login Activity
                    }
                } else {
                    // Handle the case where the user data does not exist
                    Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_LONG).show();
                    firebaseAuth.signOut(); // Sign out the user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur during the database operation
                Log.e(TAG, "Error reading user data", databaseError.toException());
                firebaseAuth.signOut(); // Sign out the user
            }
        });
    }
    private void showAlertDialog() {
        //Setup Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification.");

        //Open Email Apps if user click continue
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);     //To email app in new window and not within our app
                startActivity(intent);
            }
        });

        //Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        //Show the AlertDialog
        alertDialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(LoginActivity.this, "You Can Login Now!", Toast.LENGTH_SHORT).show();
    }
}