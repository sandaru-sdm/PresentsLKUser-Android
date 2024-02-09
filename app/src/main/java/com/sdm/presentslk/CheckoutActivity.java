package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sdm.presentslk.adapter.CheckoutAdapter;
import com.sdm.presentslk.adapter.CheckoutSingleAdapter;
import com.sdm.presentslk.model.Products;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutActivity";
    private MaterialToolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private ArrayList<Products> products;
    private String  userId, productId, cartData, price, totalPrice, qty;
    private BottomNavigationView bottomNavigationView;
    private Button btnBuyAll;
    private ProgressBar progressBar;
    private TextView textViewPrice, textViewTotalPrice, editTextQty;
    private RecyclerView recyclerView;
    private CheckoutSingleAdapter checkoutAdapter;
    private CheckoutAdapter checkoutAdapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        productId = getIntent().getStringExtra("productId");
        cartData = getIntent().getStringExtra("cartData");

        setupViews();
        navigateBack();
        setupFirebase();
//        setupNavigation();

        if(productId != null){
            progressBar.setVisibility(View.VISIBLE);
            setupSingleProducts();

            btnBuyAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyProduct();
                }
            });
        } else if (cartData != null){
            progressBar.setVisibility(View.VISIBLE);
            setupCartProducts();

            btnBuyAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyCartProduct();
                }
            });

        } else {
            Toast.makeText(CheckoutActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CheckoutActivity.this, HomeViewActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void buyProduct() {
        price = textViewPrice.getText().toString().trim();
        totalPrice = textViewTotalPrice.getText().toString().trim();

        int quantityForFirstItem = checkoutAdapter.getQuantity(0);

        DatabaseReference productQuantityRef = FirebaseDatabase.getInstance().getReference().child("Products").child(productId);

        productQuantityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String productQtyString = dataSnapshot.child("productQty").getValue(String.class);

                    try {
                        int productQty = Integer.parseInt(productQtyString);

                        if (quantityForFirstItem >= productQty) {
                            Toast.makeText(CheckoutActivity.this, "Insufficient product quantity. Please enter a lower quantity.", Toast.LENGTH_SHORT).show();
                        } else {
                            addItemsToOrder(quantityForFirstItem, productQty);

                            Intent intent = new Intent(CheckoutActivity.this, OrdersActivity.class);
                            startActivity(intent);
                            finish();

                            Toast.makeText(CheckoutActivity.this, "Purchase Successful!", Toast.LENGTH_SHORT).show();
                        }

                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Failed to parse product quantity to an integer");
                    }
                } else {
                    Log.e(TAG, "Product not found in the 'Products' collection");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }

    private void addItemsToOrder(int purchaseQuantity, int productQuantity) {
        String currentDateAndTime = getCurrentDateAndTime();
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(userId).child("Orders");
        DatabaseReference orderDateRef = ordersRef.child(currentDateAndTime);

        String currentDate = getCurrentDate();
        String currentTime = getCurrentTime();

        for (Products product : products) {
            String productId = product.getProductId();
            String productName = product.getProductName();
            String imageUrl = product.getImageUrl();

            DatabaseReference productRef = orderDateRef.child(productId);

            productRef.child("productName").setValue(productName);
            productRef.child("productID").setValue(productId);
            productRef.child("imageUrl").setValue(imageUrl);
            productRef.child("quantity").setValue(purchaseQuantity);
            productRef.child("price").setValue(price);
            productRef.child("totalPrice").setValue(totalPrice);
            productRef.child("status").setValue("Order Placed");
            productRef.child("date").setValue(currentDate);
            productRef.child("time").setValue(currentTime);

            DatabaseReference productQuantityRef = FirebaseDatabase.getInstance().getReference().child("Products").child(productId);
            updateProductQuantity(productQuantityRef, purchaseQuantity);

            Toast.makeText(CheckoutActivity.this, "Purchase Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CheckoutActivity.this, OrdersActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCurrentDateAndTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
    private void buyCartProduct() {
        ArrayList<Integer> allQuantities = checkoutAdapter2.getAllProductQuantities();

        price = textViewPrice.getText().toString().trim();
        totalPrice = textViewTotalPrice.getText().toString().trim();

        if (allQuantities.size() > 0) {
            String currentDateAndTime = getCurrentDateAndTime();
            DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(userId).child("cart");
            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Registered Users").child(userId).child("Orders");
            DatabaseReference orderDateRef = ordersRef.child(currentDateAndTime);

            String currentDate = getCurrentDate();
            String currentTime = getCurrentTime();

            for (int i = 0; i < products.size(); i++) {
                Products product = products.get(i);
                String productId = product.getProductId();
                String productName = product.getProductName();
                String imageUrl = product.getImageUrl();

                int quantityForCurrentProduct = allQuantities.get(i);

                DatabaseReference productQuantityRef = FirebaseDatabase.getInstance().getReference().child("Products").child(productId);

                productQuantityRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String productQtyString = dataSnapshot.child("productQty").getValue(String.class);

                            try {
                                int productQty = Integer.parseInt(productQtyString);

                                if (quantityForCurrentProduct >= productQty) {
                                    Toast.makeText(CheckoutActivity.this, "Insufficient product quantity for " + productName + ". Please enter a lower quantity.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                DatabaseReference productRef = orderDateRef.child(productId);

                                productRef.child("productName").setValue(productName);
                                productRef.child("productID").setValue(productId);
                                productRef.child("imageUrl").setValue(imageUrl);
                                productRef.child("quantity").setValue(quantityForCurrentProduct);
                                productRef.child("price").setValue(price);
                                productRef.child("totalPrice").setValue(totalPrice);
                                productRef.child("status").setValue("Order Placed");
                                productRef.child("date").setValue(currentDate);
                                productRef.child("time").setValue(currentTime);

                                DatabaseReference productQuantityRef = FirebaseDatabase.getInstance().getReference().child("Products").child(productId);
                                updateProductQuantity(productQuantityRef, quantityForCurrentProduct);

                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Failed to parse product quantity to an integer");
                            }
                        } else {
                            Log.e(TAG, "Product not found in the 'Products' collection");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, error.getMessage());
                    }
                });
            }

            userCartRef.removeValue();

            Toast.makeText(CheckoutActivity.this, "Purchase Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CheckoutActivity.this, OrdersActivity.class);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(CheckoutActivity.this, "No products in the cart!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProductQuantity(DatabaseReference productQuantityRef, int purchasedQuantity) {
        productQuantityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("productQty").exists()) {
                        String currentQuantityString = dataSnapshot.child("productQty").getValue(String.class);

                        try {
                            int currentQuantityInt = Integer.parseInt(currentQuantityString);

                            int updatedQuantity = currentQuantityInt - purchasedQuantity;

                            updatedQuantity = Math.max(updatedQuantity, 0);
                            String updatedQtyString = String.valueOf(updatedQuantity);

                            productQuantityRef.child("productQty").setValue(updatedQtyString);

                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Failed to parse current quantity to an integer");
                        }
                    } else {
                        productQuantityRef.child("productQty").setValue("initial_value_as_string");
                    }
                } else {
                    Log.e(TAG, "Product not found in the 'Products' collection");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }

    private void navigateBack() {
        toolbar = findViewById(R.id.toolBar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Back button clicked");
                onBackPressed();
            }
        });
    }

    private void setupSingleProducts() {
        if (productId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Products").child(productId);
        } else {
            Log.e(TAG, "Product ID is null");
        }

        products = new ArrayList<>();

        RecyclerView itemView = findViewById(R.id.recyclerCheckout);
        recyclerView = itemView;

        Log.d(TAG, "Number of items: " + products.size());

        RelativeLayout parentLayout = findViewById(R.id.RL_checkoutDetails);

        checkoutAdapter = new CheckoutSingleAdapter(products, CheckoutActivity.this, parentLayout, recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemView.setLayoutManager(linearLayoutManager);
        itemView.setAdapter(checkoutAdapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                products.clear();
                if (dataSnapshot.exists()) {
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    String productName = dataSnapshot.child("productName").getValue(String.class);
                    String productPrice = dataSnapshot.child("productPrice").getValue(String.class);

                    Products product = new Products(productId, productName, null, null, productPrice, null, imageUrl);

                    products.add(product);
                    checkoutAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Log.e(TAG, "Product not found");
                    progressBar.setVisibility(View.GONE);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setupCartProducts() {
        if (userId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users").child(userId).child("cart");
        } else {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "User ID is null");
            return;
        }

        products = new ArrayList<>();

        RecyclerView itemView = findViewById(R.id.recyclerCheckout);

        Log.d(TAG, "Number of items: " + products.size());

        RelativeLayout parentLayout = findViewById(R.id.RL_checkoutDetails);
        checkoutAdapter2 = new CheckoutAdapter(products, CheckoutActivity.this, parentLayout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemView.setLayoutManager(linearLayoutManager);
        itemView.setAdapter(checkoutAdapter2);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                products.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productId = snapshot.getKey();

                    int quantity = snapshot.child("qty").getValue(Integer.class);

                    DataSnapshot productDataSnapshot = snapshot.child("productData");
                    String imageUrl = productDataSnapshot.child("imageUrl").getValue(String.class);
                    String productName = productDataSnapshot.child("productName").getValue(String.class);
                    String productPrice = productDataSnapshot.child("productPrice").getValue(String.class);

                    Products product = new Products(productId, productName, null, String.valueOf(quantity), productPrice, null, imageUrl );

                    products.add(product);
                }


                checkoutAdapter2.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });

    }

//    private void setupNavigation() {
//        setSupportActionBar(toolbar);
//
//        // Initialize and assign variable
//        bottomNavigationView = findViewById(R.id.bottom_navigation);
//        // Set home selector
//        bottomNavigationView.setSelectedItemId(R.id.cart);
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                // Handle navigation item selection here
//                bottomNavigation(item.getItemId());
//                return true;
//            }
//        });
//    }
    private void setupViews() {
        toolbar = findViewById(R.id.toolBar);
        btnBuyAll = findViewById(R.id.btnBuyAll_checkout);
        progressBar = findViewById(R.id.progressBar_cart);
        textViewPrice = findViewById(R.id.textViewPrice2);
        textViewTotalPrice = findViewById(R.id.textViewTotal2);
    }
    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(CheckoutActivity.this, "Something Went Wrong! User's details are not available at the moment.", Toast.LENGTH_LONG).show();
        } else {
            userId = firebaseUser.getUid();
            Log.i("USER", firebaseUser.toString());
        }
    }

//    private void bottomNavigation(int itemId) {
//        if (itemId == R.id.home) {
//            startActivity(new Intent(CheckoutActivity.this, HomeViewActivity.class));
//            overridePendingTransition(0, 0);
//        } else if (itemId == R.id.cart) {
//
//        } else if (itemId == R.id.wishlist) {
//            startActivity(new Intent(CheckoutActivity.this, WishlistActivity.class));
//            overridePendingTransition(0, 0);
//        } else if (itemId == R.id.userprofile) {
//            startActivity(new Intent(CheckoutActivity.this, UserProfileActivity.class));
//            overridePendingTransition(0, 0);
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}