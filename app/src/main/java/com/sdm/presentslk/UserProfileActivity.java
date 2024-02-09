package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sdm.presentslk.model.ReadWriteUserDetails;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity {
    private TextView textViewWelcome, textViewFullName, textViewEmail, textViewDoB, textViewMobile, registeredDate, textViewAddress;
    private ProgressBar progressBar;
    private String fullName, email, doB, mobile, address;
    private ImageView imageView;
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        MaterialToolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Drawable toggleDrawable = toolbar.getNavigationIcon();

        if (toggleDrawable != null) {
            toggleDrawable.setTint(getResources().getColor(R.color.white));
        }

        swipeToRefresh();

        textViewWelcome = findViewById(R.id.textView_show_welcome);
        textViewFullName = findViewById(R.id.textViewShowFullName);
        textViewEmail = findViewById(R.id.textViewShowEmail);
        textViewDoB = findViewById(R.id.textViewShowDob);
        textViewMobile = findViewById(R.id.textViewShowMobile);
        textViewAddress = findViewById(R.id.textViewShowAddress);
        progressBar = findViewById(R.id.progressBarProfilePic);
        registeredDate = findViewById(R.id.textViewShowRegisteredDate);

        //Set OnClickListener on ImageView to Open UploadProfilePictureActivity
        imageView = findViewById(R.id.imageViewProfile);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, UploadProfilePicActivity.class);
            startActivity(intent);
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser == null){
            Toast.makeText(UserProfileActivity.this, "Something Went Wrong! User's details are not available at the moment.", Toast.LENGTH_LONG).show();
        } else {
            checkIfEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Set home selector
        bottomNavigationView.setSelectedItemId(R.id.userprofile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                bottomNavigation(item.getItemId());
                return true;
            }
        });

    }

    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        //Setup Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification next time.");

        //Open Email Apps if user click continue
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);     //To email app in new window and not within our app
            startActivity(intent);
        });

        //Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        //Show the AlertDialog
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //Extracting User Reference from Database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetail = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetail != null){
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    doB = readUserDetail.doB;
                    address = readUserDetail.address;
                    mobile = readUserDetail.mobile;

                    long creationTimestamp = firebaseUser.getMetadata().getCreationTimestamp();

                    Date creationDate = new Date(creationTimestamp);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    String formattedCreationDate = dateFormat.format(creationDate);

                    registeredDate.setText(dateFormat.format(creationDate).toString());

                    if(fullName != null){
                        textViewWelcome.setText("Welcome, " + fullName + "!");
                    } else {
                        textViewWelcome.setText("");
                        Toast.makeText(UserProfileActivity.this, "Please Update Your Profile and Add Missing Details", Toast.LENGTH_SHORT).show();
                    }

                    if(fullName != null){
                        textViewFullName.setText(fullName);
                    } else {
                        textViewFullName.setText("");
                    }

                    textViewEmail.setText(email);

                    if(doB != null){
                        textViewDoB.setText(doB);
                    } else {
                        textViewDoB.setText("");
                    }

                    if(address != null){
                        textViewAddress.setText(address);
                    } else {
                        textViewAddress.setText("");
                    }

                    if(mobile != null){
                        textViewMobile.setText(mobile);
                    } else {
                        textViewMobile.setText("");
                    }

                    //Set User DP (After user has Uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();

                    //ImageViewer setImageURi() should not be used with regular URis. So we are using Picasso
                    Picasso.get().load(uri).into(imageView);


                } else {
                    Toast.makeText(UserProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void swipeToRefresh() {
        //Look up for swipe container
        swipeContainer = findViewById(R.id.swipeContainer);

        //Setup Refresh Listener
        swipeContainer.setOnRefreshListener(() -> {
            //Code to refresh goes here.
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
            swipeContainer.setRefreshing(false);
        });

        //Configure refresh colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
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
            NavUtils.navigateUpFromSameTask(UserProfileActivity.this);
        } else if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UserProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_change_password){
            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }   else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }   else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UserProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();   //Close UserProfileActivity
        } else {
            Toast.makeText(UserProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void bottomNavigation(int itemId) {
        if (itemId == R.id.home) {
            startActivity(new Intent(UserProfileActivity.this, HomeViewActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.cart) {
            startActivity(new Intent(UserProfileActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        }else if (itemId == R.id.wishlist) {
            startActivity(new Intent(UserProfileActivity.this, WishlistActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.orders) {
            startActivity(new Intent(UserProfileActivity.this, OrdersActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.userprofile) {

        }
    }
}