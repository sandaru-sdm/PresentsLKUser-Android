package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sdm.presentslk.model.ReadWriteUserDetails;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {
    private EditText editTextUpdateName, editTextUpdateDoB, editTextUpdateMobile, editTextUpdateAddress;
    String textFullName, textDoB, textGender, textMobile, textAddress;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        MaterialToolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Drawable toggleDrawable = toolbar.getNavigationIcon();

        if (toggleDrawable != null) {
            toggleDrawable.setTint(getResources().getColor(R.color.white));
        }

        progressBar = findViewById(R.id.progressBar);
        editTextUpdateName = findViewById(R.id.editTextUpdateProfileName);
        editTextUpdateDoB = findViewById(R.id.editTextUpdateProfileDob);
        editTextUpdateMobile = findViewById(R.id.editTextUpdateProfileMobile);
        editTextUpdateAddress = findViewById(R.id.editTextUpdateProfileAddress);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //Show profile Data
        showProfile(firebaseUser);

        //Upload Profile Pic button
        TextView uploadProfilePic = findViewById(R.id.textViewUploadProfilePicture);
        uploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Update Profile
        Button buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });

        //Setting Up datePicker on EditText
        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                DatePickerDialog picker;

                //Date Picker Dialog
                picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });


    }

    private void updateProfile(FirebaseUser firebaseUser) {
        //Validate Mobile Number Using Regular Expressions
        String mobileRegex = "[0]{1}[7]{1}[01245678]{1}[0-9]{7}$";
        Matcher mobileMatcher ;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textMobile);

        if(TextUtils.isEmpty(textFullName)){
            Toast.makeText(UpdateProfileActivity.this, "Please Enter Your Full Name", Toast.LENGTH_LONG).show();
            editTextUpdateName.setError("Full Name is Required");
            editTextUpdateName.requestFocus();
        } else if (TextUtils.isEmpty(textDoB)) {
            Toast.makeText(UpdateProfileActivity.this, "Please Enter Your Date Of Birth", Toast.LENGTH_LONG).show();
            editTextUpdateDoB.setError("Date of Birth is Required");
            editTextUpdateDoB.requestFocus();
        } else if (TextUtils.isEmpty(textMobile)) {
            Toast.makeText(UpdateProfileActivity.this, "Please Enter Your Mobile Number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile Number is Required");
            editTextUpdateMobile.requestFocus();
        } else if (textMobile.length() != 10) {
            Toast.makeText(UpdateProfileActivity.this, "Please re-enter Your Mobile Number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile Number Should have 10 Digits");
            editTextUpdateMobile.requestFocus();
        } else if(!mobileMatcher.find()){
            Toast.makeText(UpdateProfileActivity.this, "Please re-enter Your Mobile Number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile Number is Not Valid");
            editTextUpdateMobile.requestFocus();
        } else if (TextUtils.isEmpty(textAddress)) {
            Toast.makeText(UpdateProfileActivity.this, "Please Enter Your Address", Toast.LENGTH_LONG).show();
            editTextUpdateAddress.setError("Address is Required");
            editTextUpdateAddress.requestFocus();
        } else {
            //Obtain the data entered by user
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();
            textAddress = editTextUpdateAddress.getText().toString();

            String userType = "user";

            String email = firebaseUser.getEmail().toString();

            //Enter User Data into Firebase Realtime Database. Setup dependencies
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(email, textDoB, textMobile, textAddress, userType);

            //Extract User reference from Database for "Registered Users
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        //Setting new display Name
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdate);

                        Toast.makeText(UpdateProfileActivity.this, "Update Successful!", Toast.LENGTH_LONG).show();

                        //Stop user from returning to UpdateProfileActivity on pressing back button and close entity
                        Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });

        }
    }

    //Fetch data from firebase and display
    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered =  firebaseUser.getUid();

        //Extracting user reference from database for "Registered User"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails != null){
                    textFullName = firebaseUser.getDisplayName();
                    textDoB = readUserDetails.doB;
                    textMobile = readUserDetails.mobile;
                    textAddress = readUserDetails.address;

                    if(textFullName != null){
                        editTextUpdateName.setText(textFullName);
                    } else {
                        editTextUpdateName.setText("");
                    }

                    if(textDoB != null){
                        editTextUpdateDoB.setText(textDoB);
                    } else {
                        editTextUpdateDoB.setText("");
                    }

                    if(textMobile != null){
                        editTextUpdateMobile.setText(textMobile);
                    } else {
                        editTextUpdateMobile.setText("");
                    }

                    if(textAddress != null){
                        editTextUpdateAddress.setText(textAddress);
                    } else {
                        editTextUpdateAddress.setText("");
                    }


                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //Create ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menue, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(UpdateProfileActivity.this);
        } else if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_change_password){
            Intent intent = new Intent(UpdateProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }   else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(UpdateProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }   else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();   //Close UserProfileActivity
        } else {
            Toast.makeText(UpdateProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void bottomNavigation(int itemId) {
        if (itemId == R.id.home) {
            startActivity(new Intent(UpdateProfileActivity.this, HomeViewActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.cart) {
            startActivity(new Intent(UpdateProfileActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        }else if (itemId == R.id.wishlist) {
            startActivity(new Intent(UpdateProfileActivity.this, WishlistActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.userprofile) {

        }
    }
}