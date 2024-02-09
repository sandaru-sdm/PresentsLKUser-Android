package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText editTextCurrentPassword, editTextNewPassword, editTextConfirmNewPassword;
    private TextView textViewAuthenticated;
    private Button buttonChangePassword, buttonReAuthenticate;
    private ProgressBar progressBar;
    private String userCurrentPassword;
    private ImageView changePasswordCurrentImg, changePasswordNewImg, changePasswordNewConfirmImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        MaterialToolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Drawable toggleDrawable = toolbar.getNavigationIcon();

        if (toggleDrawable != null) {
            toggleDrawable.setTint(getResources().getColor(R.color.white));
        }

        editTextNewPassword = findViewById(R.id.editTextChangePasswordNew);
        editTextCurrentPassword = findViewById(R.id.editTextChangePasswordCurrent);
        editTextConfirmNewPassword = findViewById(R.id.editTextChangePasswordNewConfirm);
        textViewAuthenticated = findViewById(R.id.textViewChangePasswordAuthenticated);
        progressBar = findViewById(R.id.progressBar);
        buttonReAuthenticate = findViewById(R.id.buttonChangePasswordAuthenticate);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        //Disable editText for new password, confirm new password, and change password button
        editTextNewPassword.setEnabled(false);
        editTextConfirmNewPassword.setEnabled(false);
        buttonChangePassword.setEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(ChangePasswordActivity.this, "Something Went Wrong! User's details not available right now.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }

        showHidePassword();
    }

    //Re Authenticate the User
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userCurrentPassword = editTextCurrentPassword.getText().toString();

                if(TextUtils.isEmpty(userCurrentPassword)){
                    Toast.makeText(ChangePasswordActivity.this, "Password Needed", Toast.LENGTH_SHORT).show();
                    editTextCurrentPassword.setError("Please Enter Your Current Password to Authenticate");
                    editTextCurrentPassword.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    //Re authenticate USer
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userCurrentPassword);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);

                                //Disable editText for current password and Enable for new password editText
                                editTextCurrentPassword.setEnabled(false);
                                editTextNewPassword.setEnabled(true);
                                editTextConfirmNewPassword.setEnabled(true);

                                //Enable change pwd buttons. Disable authenticate button
                                buttonReAuthenticate.setEnabled(false);
                                buttonChangePassword.setEnabled(true);

                                //Set TextView to show user is authenticated
                                textViewAuthenticated.setText("You are Authenticated/ Verified. You can Change your Password Now!");
                                textViewAuthenticated.setTextColor(ContextCompat.getColor(ChangePasswordActivity.this, R.color.green));
                                Toast.makeText(ChangePasswordActivity.this, "Password has been Verified. Change Password Now!", Toast.LENGTH_SHORT).show();

                                //Update color of the change password button
                                buttonChangePassword.setBackgroundTintList(ContextCompat.getColorStateList(ChangePasswordActivity.this, R.color.dark_green));

                                buttonChangePassword.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePassword(firebaseUser);
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            editTextCurrentPassword.setText("");
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePassword(FirebaseUser firebaseUser) {
        String userNewPassword = editTextNewPassword.getText().toString();
        String userConfirmNewPassword = editTextConfirmNewPassword.getText().toString();

        if(TextUtils.isEmpty(userNewPassword)){
            Toast.makeText(ChangePasswordActivity.this, "New Password is Needed", Toast.LENGTH_SHORT).show();
            editTextNewPassword.setError("Please Enter New Password");
            editTextNewPassword.requestFocus();
        } else if (editTextNewPassword.length() < 6) {
            Toast.makeText(ChangePasswordActivity.this, "Password Should contains at least 6 digits", Toast.LENGTH_LONG).show();
            editTextNewPassword.setError("Password is Too Weak");
            editTextNewPassword.requestFocus();
        }else if (TextUtils.isEmpty(userConfirmNewPassword)) {
            Toast.makeText(ChangePasswordActivity.this, "Please Confirm Your New Password", Toast.LENGTH_SHORT).show();
            editTextConfirmNewPassword.setError("Please Re-Enter Your New Password");
            editTextConfirmNewPassword.requestFocus();
        } else if (!userNewPassword.matches(userConfirmNewPassword)) {
            Toast.makeText(ChangePasswordActivity.this, "Password did not match", Toast.LENGTH_SHORT).show();
            editTextConfirmNewPassword.setError("Please Re-Enter Same Passwords");
            editTextConfirmNewPassword.requestFocus();
        } else if (userCurrentPassword.matches(userNewPassword)){
            Toast.makeText(ChangePasswordActivity.this, "New Password cannot be same as old password", Toast.LENGTH_SHORT).show();
            editTextNewPassword.setError("Please Enter a New Password");
            editTextNewPassword.requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(userNewPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    //Show Hide Passwords
    private void showHidePassword() {
        changePasswordCurrentImg = findViewById(R.id.imageView_show_hide_curr_pwd);
        changePasswordNewImg = findViewById(R.id.imageView_show_hide_new_pwd);
        changePasswordNewConfirmImg = findViewById(R.id.imageView_show_hide_new_pwd_confirm);

        //Show hide Password using Eye icon
        changePasswordCurrentImg.setImageResource(R.drawable.ic_hide_pwd);
        changePasswordNewImg.setImageResource(R.drawable.ic_hide_pwd);
        changePasswordNewConfirmImg.setImageResource(R.drawable.ic_hide_pwd);

        changePasswordCurrentImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextCurrentPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is visible then hide it
                    editTextCurrentPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    changePasswordCurrentImg.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextCurrentPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    changePasswordCurrentImg.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        changePasswordNewImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextNewPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is visible then hide it
                    editTextNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    changePasswordNewImg.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    changePasswordNewImg.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        changePasswordNewConfirmImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextConfirmNewPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is visible then hide it
                    editTextConfirmNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    changePasswordNewConfirmImg.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextConfirmNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    changePasswordNewConfirmImg.setImageResource(R.drawable.ic_show_pwd);
                }
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
            NavUtils.navigateUpFromSameTask(ChangePasswordActivity.this);
        } else if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(ChangePasswordActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_change_password){
            Intent intent = new Intent(ChangePasswordActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }   else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(ChangePasswordActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }   else if (id == R.id.menu_logout){
            firebaseAuth.signOut();
            Toast.makeText(ChangePasswordActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();   //Close UserProfileActivity
        } else {
            Toast.makeText(ChangePasswordActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void bottomNavigation(int itemId) {
        if (itemId == R.id.home) {
            startActivity(new Intent(ChangePasswordActivity.this, HomeViewActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.cart) {
            startActivity(new Intent(ChangePasswordActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        }else if (itemId == R.id.wishlist) {
            startActivity(new Intent(ChangePasswordActivity.this, WishlistActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.userprofile) {

        }
    }
}