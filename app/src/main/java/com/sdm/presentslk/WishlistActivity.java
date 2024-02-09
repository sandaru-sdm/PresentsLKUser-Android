package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sdm.presentslk.adapter.ItemAdapter;
import com.sdm.presentslk.adapter.WishlistItemAdapter;
import com.sdm.presentslk.model.Products;
import com.sdm.presentslk.model.ReadWriteUserDetails;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WishlistActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "WishlistViewActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView sideBarUserName, sideBarUserEmail;
    private ImageView sideBarUserProfilePic;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private ArrayList<Products> products;
    private String fullName, email, userId;
    private BottomNavigationView bottomNavigationView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        setupViews();
        setupFirebase();
        setupNavigation();
        progressBar.setVisibility(View.VISIBLE);
        setupWishlistItems();

    }

    private void setupWishlistItems() {
        if (userId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users").child(userId).child("wishlist");
        } else {
            Log.e(TAG, "User ID is null");
        }

        products = new ArrayList<>();

        RecyclerView itemView = findViewById(R.id.recyclerWishlist);

        Log.d(TAG, "Number of items: " + products.size());

        WishlistItemAdapter wishlistItemAdapter1 = new WishlistItemAdapter(products, WishlistActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemView.setLayoutManager(linearLayoutManager);
        itemView.setAdapter(wishlistItemAdapter1);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                products.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Products product = snapshot.getValue(Products.class);
                    products.add(product);
                }
                wishlistItemAdapter1.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void setupNavigation() {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        // Initialize and assign variable
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Set home selector
        bottomNavigationView.setSelectedItemId(R.id.wishlist);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item selection here
                bottomNavigation(item.getItemId());
                return true;
            }
        });
    }
    private void setupViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);

        View headerView = navigationView.getHeaderView(0);
        sideBarUserName = headerView.findViewById(R.id.sideBarUserName);
        sideBarUserEmail = headerView.findViewById(R.id.sideBarUserEmail);
        sideBarUserProfilePic = headerView.findViewById(R.id.sideBarUserProfilePic);

        progressBar = findViewById(R.id.progressBar_wishlist);
    }
    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(WishlistActivity.this, "Something Went Wrong! User's details are not available at the moment.", Toast.LENGTH_LONG).show();
        } else {
            userId = firebaseUser.getUid();
            checkIfEmailVerified(firebaseUser);
            showUserProfile(firebaseUser);
            Log.i("USER", firebaseUser.toString());
        }
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

                    if(fullName != null){
                        sideBarUserName.setText(fullName);
                    } else {
                        sideBarUserName.setText("");
                    }

                    if(email != null){
                        sideBarUserEmail.setText(email);
                    } else {
                        sideBarUserEmail.setText("");
                    }

                    //Set User DP (After user has Uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();

                    Log.i("USER", fullName);
                    Log.i("USER", email);

                    if(uri != null){
                        //ImageViewer setImageURi() should not be used with regular URis. So we are using Picasso
                        Picasso.get().load(uri).into(sideBarUserProfilePic);
                    } else {
                        //ImageViewer setImageURi() should not be used with regular URis. So we are using Picasso
                        Picasso.get().load(R.drawable.no_profile_pic).into(sideBarUserProfilePic);
                    }

                } else {
                    Toast.makeText(WishlistActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WishlistActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void bottomNavigation(int itemId) {
        if (itemId == R.id.home) {
            startActivity(new Intent(WishlistActivity.this, HomeViewActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.cart) {
            startActivity(new Intent(WishlistActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.wishlist) {

        } else if (itemId == R.id.orders) {
            startActivity(new Intent(WishlistActivity.this, OrdersActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.userprofile) {
            startActivity(new Intent(WishlistActivity.this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item selection here
        if (item.getItemId() == R.id.sideNavHome) {
            startActivity(new Intent(WishlistActivity.this, HomeViewActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.home);
        } else if (item.getItemId() == R.id.sideNavCart) {
            startActivity(new Intent(WishlistActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.cart);
        } else if (item.getItemId() == R.id.sideNavWishlist) {

        } else if (item.getItemId() == R.id.sideNavOrders) {
            startActivity(new Intent(WishlistActivity.this, OrdersActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.orders);
        }  else if (item.getItemId() == R.id.sideNavProfile) {
            startActivity(new Intent(WishlistActivity.this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.userprofile);
        } else if (item.getItemId() == R.id.sideNavAboutUs) {
            startActivity(new Intent(WishlistActivity.this, AboutUsActivity.class));
            overridePendingTransition(0, 0);
        }
        // Close the drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }
    private void showAlertDialog() {
        //Setup Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(WishlistActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification next time.");

        //Open Email Apps if user click continue
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        //Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        //Show the AlertDialog
        alertDialog.show();
    }
}