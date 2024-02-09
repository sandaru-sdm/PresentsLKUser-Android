package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sdm.presentslk.model.Products;
import com.sdm.presentslk.model.ReadWriteUserDetails;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SingleProductActivity extends AppCompatActivity {
    private static final String TAG = "SingleProductActivity";
    private String productId, userId, productName, productDescription, productCategory, productQty, productPrice;
    private TextView textProductName, textProductDescription, textProductCategory, textProductQty, textProductPrice;
    private ImageView productImage;
    private Button btnBuyItNow, btnAddToCart, btnAddToWishlist;
    private ProgressBar progressBar, progressBarImage;
    private SwipeRefreshLayout swipeContainer;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage storage;
    private Uri imagePathUri;
    private Products product;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        productId = getIntent().getStringExtra("productId");

        if (productId != null) {
            setupViews();
            setupFirebase();
            progressBar.setVisibility(View.VISIBLE);
            showProductDetails(productId);
            swipeToRefresh();
            setupBottomNavigation();
            navigateBack();

        } else {
            Toast.makeText(this, "Something Wrong! Please Try Again Later.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SingleProductActivity.this, HomeViewActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void navigateBack() {
        toolbar = findViewById(R.id.toolBar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                bottomNavigation(item.getItemId());
                return true;
            }
        });
    }
    private void bottomNavigation(int itemId) {
        if (itemId == R.id.home) {
            startActivity(new Intent(SingleProductActivity.this, HomeViewActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.cart) {
            startActivity(new Intent(SingleProductActivity.this, CartActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.wishlist) {
            startActivity(new Intent(SingleProductActivity.this, WishlistActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.orders) {
            startActivity(new Intent(SingleProductActivity.this, OrdersActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemId == R.id.userprofile) {
            startActivity(new Intent(SingleProductActivity.this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
        }
    }
    private void showProductDetails(String productId) {
        DatabaseReference productReference = FirebaseDatabase.getInstance().getReference("Products");
        productReference.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                product = snapshot.getValue(Products.class);
                if(product != null){
                    productName = product.getProductName();
                    productDescription = product.getProductDescription();
                    productCategory = product.getProductCategory();
                    productQty = product.getProductQty();
                    productPrice = product.getProductPrice();

                    storage.getReference("ProductImages/" + product.getImageUrl())
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imagePathUri = uri;
                                    if (imagePathUri != null) {
                                        Log.i(TAG, imagePathUri.toString());
                                        updateUI();
                                    } else {
                                        Log.e(TAG, "Image URI is null");
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                    Toast.makeText(SingleProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });

                } else {
                    Toast.makeText(SingleProductActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SingleProductActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void updateUI() {
        progressBarImage.setVisibility(View.VISIBLE);

        textProductName.setText("Product Name : " + productName);
        textProductDescription.setText("Product Description : " + productDescription);
        textProductCategory.setText("Product Category : " + productCategory);
        textProductQty.setText("Product Qty : " + productQty);
        textProductPrice.setText("Price : Rs. " + productPrice + "/=");

        Picasso.get()
                .load(imagePathUri)
                .resize(360, 360)
                .centerCrop()
                .into(productImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBarImage.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        progressBarImage.setVisibility(View.GONE);
                        Log.e(TAG, e.getMessage());
                    }
                });

        setButtonListeners();
    }
    private void setButtonListeners() {
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart(userId, productId, product);
            }
        });

        btnAddToWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWishlistStatus(userId, productId);
            }
        });

        btnBuyItNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleProductActivity.this, CheckoutActivity.class);
                intent.putExtra("productId", productId);
                startActivity(intent);
            }
        });
    }
    private void addToCart(String userId, String productId, Products product) {
        DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(userId).child("cart").child(productId);

        userCartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Product already exists in the cart, update the quantity
                    int existingQuantity = dataSnapshot.child("qty").getValue(Integer.class);
                    int newQuantity = existingQuantity + 1;
                    userCartRef.child("qty").setValue(newQuantity);
                } else {
                    // Product doesn't exist in the cart, add it with quantity 1
                    userCartRef.child("qty").setValue(1);
                    userCartRef.child("productData").setValue(product); // Add the entire product data
                }
                Toast.makeText(SingleProductActivity.this, "Product added to cart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SingleProductActivity.this, "Error adding product to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkWishlistStatus(String userId, String productId) {
        DatabaseReference wishlistRef = FirebaseDatabase.getInstance().getReference()
                .child("Registered Users").child(userId).child("wishlist").child(productId);

        wishlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    removeFromWishlist(userId, productId);
                } else {
                    addToWishlist(userId, productId, product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SingleProductActivity.this, "Error checking wishlist status", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addToWishlist(String userId, String productId, Products product) {
        DatabaseReference wishlistRef = FirebaseDatabase.getInstance().getReference()
                .child("Registered Users").child(userId).child("wishlist").child(productId);

        wishlistRef.setValue(product, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(SingleProductActivity.this, "Product added to wishlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SingleProductActivity.this, "Error adding product to wishlist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void removeFromWishlist(String userId, String productId) {
        DatabaseReference wishlistRef = FirebaseDatabase.getInstance().getReference()
                .child("Registered Users").child(userId).child("wishlist").child(productId);

        wishlistRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(SingleProductActivity.this, "Product removed from wishlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SingleProductActivity.this, "Error removing product from wishlist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setupViews() {
        textProductName = findViewById(R.id.textViewProductName_singleProduct);
        textProductDescription = findViewById(R.id.textViewProductDescription_singleProduct);
        textProductCategory = findViewById(R.id.textViewProductCategory_singleProduct);
        textProductQty = findViewById(R.id.textViewProductQty_singleProduct);
        textProductPrice = findViewById(R.id.textViewProductPrice_singleProduct);

        productImage = findViewById(R.id.imageViewProduct_singleProduct);

        btnAddToCart = findViewById(R.id.buttonAddToCart_singleProduct);
        btnAddToWishlist = findViewById(R.id.buttonAddToWishList_singleProduct);
        btnBuyItNow = findViewById(R.id.buttonBuyItNow_singleProduct);

        progressBar = findViewById(R.id. progressBar);

        swipeContainer = findViewById(R.id.swipeContainer);

        storage = FirebaseStorage.getInstance();

        progressBarImage = findViewById(R.id.progressBarImage);
    }
    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser != null){
            userId = firebaseUser.getUid();
        } else {
            Toast.makeText(SingleProductActivity.this, "Something Went Wrong! User's details are not available at the moment.", Toast.LENGTH_LONG).show();
        }
    }
    private void swipeToRefresh() {
        swipeContainer.setOnRefreshListener(() -> {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
            swipeContainer.setRefreshing(false);
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }
}