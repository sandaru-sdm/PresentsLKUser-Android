package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {
    private final static String TAG = "DeleteProfileActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPassword;
    private TextView textViewAuthenticate;
    private ProgressBar progressBar;
    private String userPassword;
    private Button buttonReAuthenticate, buttonDeleteUser;
    private ImageView passwordImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        //Set Actionbar Visible
        MaterialToolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Drawable toggleDrawable = toolbar.getNavigationIcon();

        if (toggleDrawable != null) {
            toggleDrawable.setTint(getResources().getColor(R.color.white));
        }

        //Show or hide Password
        showHidePassword();

        progressBar = findViewById(R.id.progressBar);
        editTextUserPassword = findViewById(R.id.editTextDeleteUserPassword);
        textViewAuthenticate = findViewById(R.id.textView_delete_user_authenticated);
        buttonDeleteUser = findViewById(R.id.buttonDeleteUser);
        buttonReAuthenticate = findViewById(R.id.button_delete_user_authenticate);

        //Disable Delete User button
        buttonDeleteUser.setEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this, "Something Went Wrong! User Details are not available at the Moment.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }

    //Re Authenticate the User
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPassword = editTextUserPassword.getText().toString();

                if(TextUtils.isEmpty(userPassword)){
                    Toast.makeText(DeleteProfileActivity.this, "Password Needed", Toast.LENGTH_SHORT).show();
                    editTextUserPassword.setError("Please Enter Your Current Password to Authenticate");
                    editTextUserPassword.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    //Re authenticate USer
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPassword);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);

                                //Disable editText for password.
                                editTextUserPassword.setEnabled(false);

                                //Enable Delete User button and Disable Re authenticate Button
                                buttonReAuthenticate.setEnabled(false);
                                buttonDeleteUser.setEnabled(true);

                                //Set TextView to show user is authenticated
                                textViewAuthenticate.setText("You are Authenticated/ Verified. You can delete your account Now!");
                                textViewAuthenticate.setTextColor(ContextCompat.getColor(DeleteProfileActivity.this, R.color.green));
                                Toast.makeText(DeleteProfileActivity.this, "Password has been Verified. You can Delete Your Profile Now! Be careful, this action is irreversible.", Toast.LENGTH_SHORT).show();

                                //Update color of the change password button
                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfileActivity.this, R.color.dark_green));

                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        showAlertDialog();

                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            editTextUserPassword.setText("");
                            editTextUserPassword.setError("Please Check Your Password Again.");
                            editTextUserPassword.requestFocus();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        //Setup Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete User and Related Data?");
        builder.setMessage("Do you really want to delete your profile and related data? This action is irreversible.");

        //Open Email Apps if user click continue
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserData(firebaseUser);
            }
        });

        //Return to user profile activity if User press cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        //Change the button color of the Continue
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        //Show the AlertDialog
        alertDialog.show();
    }

    private void deleteUser() {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    firebaseAuth.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "User has been deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e){
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void deleteUserData(FirebaseUser firebaseUser) {
        //Delete display pic if available
        if(firebaseUser.getPhotoUrl() != null){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "OnSuccess : Photo Deleted" );
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Delete data from db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess : User Data Deleted");
                //Delete User
                deleteUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Show Hide Passwords
    private void showHidePassword() {
        passwordImg = findViewById(R.id.imageView_show_hide_pwd);

        //Show hide Password using Eye icon
        passwordImg.setImageResource(R.drawable.ic_hide_pwd);


        passwordImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextUserPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is visible then hide it
                    editTextUserPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    passwordImg.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextUserPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordImg.setImageResource(R.drawable.ic_show_pwd);
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
            NavUtils.navigateUpFromSameTask(DeleteProfileActivity.this);
        } else if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_change_password){
            Intent intent = new Intent(DeleteProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }   else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(DeleteProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }   else if (id == R.id.menu_logout){
            firebaseAuth.signOut();
            Toast.makeText(DeleteProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();   //Close UserProfileActivity
        } else {
            Toast.makeText(DeleteProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void bottomNavigation(int itemId) {
        if (itemId == R.id.home) {
            startActivity(new Intent(DeleteProfileActivity.this, HomeViewActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.cart) {
            startActivity(new Intent(DeleteProfileActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        }else if (itemId == R.id.wishlist) {
            startActivity(new Intent(DeleteProfileActivity.this, WishlistActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.userprofile) {

        }
    }
}