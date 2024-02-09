package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sdm.presentslk.model.ReadWriteUserDetails;
import com.sdm.presentslk.utill.Coordinates;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText editTextRegisterFullName, editTextRegisterEmail, editTextRegisterDob, editTextRegisterMobile, editTextRegisterAddress, editTextRegisterPassword, editTextRegisterConfirmPassword;
    private String latitude, longitude;
    private ProgressBar progressBar;
    private DatePickerDialog picker;
    private ImageView showHidePassword, showHidePasswordConfirm;
    private CheckBox checkBox;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        //Action bar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("Register");
        }

        Toast.makeText(RegisterActivity.this, "You can Register Now", Toast.LENGTH_LONG).show();

        progressBar = findViewById(R.id.progressBar);
        editTextRegisterFullName = findViewById(R.id.editTextRegisterFullName);
        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextRegisterDob = findViewById(R.id.editTextRegisterDob);
        editTextRegisterMobile = findViewById(R.id.editTextRegisterMobile);
        editTextRegisterAddress = findViewById(R.id.editTextRegisterAddress);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        editTextRegisterConfirmPassword = findViewById(R.id.editTextRegisterConfirmPassword);
        checkBox = findViewById(R.id.checkBoxTermsAndConditions);

        showHidePassword();

        //Setting Up datePicker on EditText
        editTextRegisterDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //Date Picker Dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextRegisterDob.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Obtain the entered data
                String textFullName = editTextRegisterFullName.getText().toString().trim();
                String textEmail = editTextRegisterEmail.getText().toString().trim();
                String textDob = editTextRegisterDob.getText().toString().trim();
                String textMobile = editTextRegisterMobile.getText().toString().trim();
                String textPwd = editTextRegisterPassword.getText().toString().trim();
                String textConfirmPwd = editTextRegisterConfirmPassword.getText().toString().trim();
                String textAddress = editTextRegisterAddress.getText().toString().trim();

                //Validate Mobile Number Using Regular Expressions
                String mobileRegex = "[0]{1}[7]{1}[01245678]{1}[0-9]{7}$";
                Matcher mobileMatcher ;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textMobile);

                if(TextUtils.isEmpty(textFullName)){
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Full Name", Toast.LENGTH_LONG).show();
                    editTextRegisterFullName.setError("Full Name is Required");
                    editTextRegisterFullName.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)){
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Email is Required");
                    editTextRegisterEmail.requestFocus();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(RegisterActivity.this, "Please re-enter Your Email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Valid Email is Required");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textDob)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Date Of Birth", Toast.LENGTH_LONG).show();
                    editTextRegisterDob.setError("Date of Birth is Required");
                    editTextRegisterDob.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Mobile Number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile Number is Required");
                    editTextRegisterMobile.requestFocus();
                } else if (textMobile.length() != 10) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter Your Mobile Number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile Number Should have 10 Digits");
                    editTextRegisterMobile.requestFocus();
                } else if(!mobileMatcher.find()){
                    Toast.makeText(RegisterActivity.this, "Please re-enter Your Mobile Number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile Number is Not Valid");
                    editTextRegisterMobile.requestFocus();
                } else if (TextUtils.isEmpty(textAddress)){
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Address", Toast.LENGTH_LONG).show();
                    editTextRegisterAddress.setError("Address is Required");
                    editTextRegisterAddress.requestFocus();
                }else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterPassword.setError("Password is Required");
                    editTextRegisterPassword.requestFocus();
                } else if (textPwd.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password Should contains at least 6 digits", Toast.LENGTH_LONG).show();
                    editTextRegisterPassword.setError("Password is Too Weak");
                    editTextRegisterPassword.requestFocus();
                } else if (TextUtils.isEmpty(textConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please Confirm Your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPassword.setError("Password Confirmation is Required");
                    editTextRegisterConfirmPassword.requestFocus();
                } else if (!textPwd.equals(textConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "Password and Confirm Password Should be Same", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPassword.setError("Password Confirmation is Required");
                    editTextRegisterConfirmPassword.requestFocus();
                    //Clear the entered passwords
                    editTextRegisterPassword.clearComposingText();
                    editTextRegisterConfirmPassword.clearComposingText();
                } else if (!checkBox.isChecked()) {
                    Toast.makeText(RegisterActivity.this, "Please Agree to Terms, Conditions and Privacy Policy", Toast.LENGTH_LONG).show();
                    checkBox.setError("Please Agree to Terms, Conditions and Privacy Policy");
                    checkBox.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    String textUserType = "user";

                    registerUser(textFullName, textEmail, textDob, textMobile, textAddress, textPwd, textUserType);
                }

            }
        });
    }

    private void showHidePassword() {
        showHidePassword = findViewById(R.id.imageView_show_hide_pwd);
        showHidePasswordConfirm = findViewById(R.id.imageView_show_hide_pwdConfirm);

        //Show hide Password using Eye icon
        showHidePassword.setImageResource(R.drawable.ic_hide_pwd);
        showHidePasswordConfirm.setImageResource(R.drawable.ic_hide_pwd);
        showHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextRegisterPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is visible then hide it
                    editTextRegisterPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    showHidePassword.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextRegisterPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showHidePassword.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        showHidePasswordConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextRegisterConfirmPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is visible then hide it
                    editTextRegisterConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    showHidePasswordConfirm.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextRegisterConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showHidePasswordConfirm.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });
    }

    private void registerUser(String textFullName, String textEmail, String textDob, String textMobile, String textAddress, String textPwd, String textUserType) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        //Create User Profile
        auth.createUserWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    //Update Display Name of the User
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    //Enter User Data into the Firebase Database
                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textEmail, textDob, textMobile, textAddress, textUserType);

                    //Extracting User reference from Database for "Registered Users"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                //Send Verification Email
                                firebaseUser.sendEmailVerification();

                                Toast.makeText(RegisterActivity.this, "User Registered Successfully. Please verify your email.", Toast.LENGTH_LONG).show();
                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                firebaseAuth.signOut();
                                //Open User Profile after successful registration
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

                                //To prevent User from returning back to register activity on pressing back button after registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();   //To close register activity

                            } else {

                                Toast.makeText(RegisterActivity.this, "User registration is failed. Please try Again.", Toast.LENGTH_LONG).show();

                            }
                            // Hide progress bar weather the user detail registration success or not
                            progressBar.setVisibility(View.GONE);


                        }
                    });

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e){
                        editTextRegisterPassword.setError("Your Password is too Weak. Kindly use a mix alphabets, numbers and special characters.");
                        editTextRegisterPassword.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        editTextRegisterPassword.setError("Your email is invalid or already in use.");
                        editTextRegisterPassword.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e){
                        editTextRegisterPassword.setError("User is already exists. Please use another email");
                        editTextRegisterPassword.requestFocus();
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(RegisterActivity.this, "You Can Register now!", Toast.LENGTH_SHORT).show();
    }
}


