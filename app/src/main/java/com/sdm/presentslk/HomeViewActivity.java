package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.sdm.presentslk.model.Products;
import com.sdm.presentslk.model.ReadWriteUserDetails;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HomeViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "HomeViewActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView sideBarUserName, sideBarUserEmail;
    private EditText searchText;
    private ImageView sideBarUserProfilePic;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private ArrayList<Products> products;
    private String fullName, email, userID;
    private BottomNavigationView bottomNavigationView;
    private ImageButton imgBtnSearch;
    private ItemAdapter adminItemAdapter;
    private ArrayList<Products> originalProducts;
    private ProgressBar progressBar;
    private NotificationManager notificationManager;
    private String chanelId = "Order Status Update";
    private DatabaseReference databaseReferenceNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);

        // Inflate the header view and find the views within it
        View headerView = navigationView.getHeaderView(0);
        sideBarUserName = headerView.findViewById(R.id.sideBarUserName);
        sideBarUserEmail = headerView.findViewById(R.id.sideBarUserEmail);
        sideBarUserProfilePic = headerView.findViewById(R.id.sideBarUserProfilePic);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressBar = findViewById(R.id.progressBar_home);

        //Search
        imgBtnSearch = findViewById(R.id.imgBtnSearch);
        searchText = findViewById(R.id.textInputSearch);
        imgBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchText.getText().toString();
                searchProducts(searchQuery);
                searchText.clearFocus();
                hideKeyboard();
            }
        });

        searchText.clearFocus();

        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchText.clearFocus();
                }
            }
        });

        originalProducts = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);
        //Recycler View
        loadData();

        ImageButton refreshButton = findViewById(R.id.imgBtnRefresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                searchText.clearFocus();
                searchText.setText("");
                resetProducts();
                loadData();
            }
        });

        if(firebaseUser == null){
            Toast.makeText(HomeViewActivity.this, "Something Went Wrong! User's details are not available at the moment.", Toast.LENGTH_LONG).show();
        } else {
            checkIfEmailVerified(firebaseUser);
            showUserProfile(firebaseUser);
            Log.i("USER", firebaseUser.toString());
        }

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
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item selection here
                bottomNavigation(item.getItemId());
                return true;
            }
        });

        setNotifications();
    }

    private void setNotifications() {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(chanelId, "INFO", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            channel.setDescription("PresentsLk Order Status");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{0, 1000, 1000, 1000});
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        databaseReferenceNotification = FirebaseDatabase.getInstance().getReference("Registered Users").child(userID).child("Orders");

        databaseReferenceNotification.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Intent intent = new Intent(HomeViewActivity.this, OrdersActivity.class);

                PendingIntent pendingIntent = PendingIntent
                        .getActivity(HomeViewActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);


                Notification notification = new NotificationCompat.Builder(getApplicationContext(), chanelId)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_order1)
                        .setContentTitle("Order Status")
                        .setContentText("Your Order Status has been Changed!")
                        .setColor(Color.RED)
                        .setContentIntent(pendingIntent)
                        .build();

                notificationManager.notify(1, notification);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        } else {
            View view = getWindow().getDecorView();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void resetProducts() {
        Log.d(TAG, "Resetting products");
        products.clear();
        products.addAll(originalProducts);
        adminItemAdapter.notifyDataSetChanged();
    }
    private void loadData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Products");
        products = new ArrayList<>();

        RecyclerView itemView = findViewById(R.id.recyclerHomeProducts);

        Log.d(TAG, "Number of items: " + products.size());

        adminItemAdapter = new ItemAdapter(products, HomeViewActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemView.setLayoutManager(linearLayoutManager);
        itemView.setAdapter(adminItemAdapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                products.clear();
                originalProducts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Products product = snapshot.getValue(Products.class);
                    products.add(product);
                    originalProducts.add(product);
                }
                adminItemAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });

    }
    private void searchProducts(String searchText) {
        String searchQuery = searchText.toLowerCase();

        ArrayList<Products> filteredProducts = new ArrayList<>();
        for (Products product : originalProducts) {
            if (product.getProductName().toLowerCase().contains(searchQuery)) {
                filteredProducts.add(product);
            }
        }

        products.clear();
        products.addAll(filteredProducts);
        adminItemAdapter.notifyDataSetChanged();
    }
    private void showUserProfile(FirebaseUser firebaseUser) {
        userID = firebaseUser.getUid();

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
                    Toast.makeText(HomeViewActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeViewActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void bottomNavigation(int itemId) {
        if (itemId == R.id.home) {

        } else if (itemId == R.id.cart) {
            startActivity(new Intent(HomeViewActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.wishlist) {
            startActivity(new Intent(HomeViewActivity.this, WishlistActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.orders) {
            startActivity(new Intent(HomeViewActivity.this, OrdersActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.userprofile) {
            startActivity(new Intent(HomeViewActivity.this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item selection here
        if (item.getItemId() == R.id.sideNavHome) {
            // Handle the home menu item
        } else if (item.getItemId() == R.id.sideNavCart) {
            startActivity(new Intent(HomeViewActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.cart);
        } else if (item.getItemId() == R.id.sideNavWishlist) {
            startActivity(new Intent(HomeViewActivity.this, WishlistActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.wishlist);
        } else if (item.getItemId() == R.id.sideNavOrders) {
            startActivity(new Intent(HomeViewActivity.this, OrdersActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.orders);
        }  else if (item.getItemId() == R.id.sideNavProfile) {
            startActivity(new Intent(HomeViewActivity.this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
            bottomNavigationView.setSelectedItemId(R.id.userprofile);
        } else if (item.getItemId() == R.id.sideNavAboutUs) {
            startActivity(new Intent(HomeViewActivity.this, AboutUsActivity.class));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeViewActivity.this);
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
}
