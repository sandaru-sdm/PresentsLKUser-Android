package com.sdm.presentslk.adapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sdm.presentslk.SingleProductActivity;
import com.sdm.presentslk.WishlistActivity;
import com.sdm.presentslk.model.Products;
import com.sdm.presentslk.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
public class WishlistItemAdapter extends RecyclerView.Adapter<WishlistItemAdapter.ViewHolder>{
    public static final String TAG = "AdminItemAdapter";
    private ArrayList<Products> products;
    private FirebaseStorage storage;
    private Context context;
    private String category, productId;
    public WishlistItemAdapter(ArrayList<Products> products, Context context) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public WishlistItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_wishlist_products_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistItemAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Products pro = products.get(position);
        holder.textProductName.setText(pro.getProductName());
        holder.textProductPrice.setText("Price : Rs. "+pro.getProductPrice()+"/=");
        holder.textProductQty.setText("Qty "+pro.getProductQty());

        category = pro.getProductCategory();

        storage.getReference("ProductImages/"+pro.getImageUrl())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .resize(200,200)
                                .centerCrop()
                                .into(holder.imageButtonViewProduct);
                        Log.i(TAG, uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        holder.btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Products currentProduct = products.get(position);
                String userId = getCurrentUserId();
                String productId = currentProduct.getProductId();

                Log.d(TAG, "User ID: " + userId);
                Log.d(TAG, "Product ID: " + currentProduct.getProductId());

                addToCart(userId, productId, currentProduct);
            }
        });

        holder.btnRemoveFromWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Products currentProduct = products.get(position);
                String userId = getCurrentUserId();
                String productId = currentProduct.getProductId();

                Log.d(TAG, "User ID: " + userId);
                Log.d(TAG, "Product ID: " + currentProduct.getProductId());

                checkWishlistStatus(userId, productId);
            }
        });

        holder.imageButtonViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Products currentProduct = products.get(position);
                String userId = getCurrentUserId();
                String productId = currentProduct.getProductId();

                Intent intent = new Intent(context, SingleProductActivity.class);
                intent.putExtra("productId", productId);
                context.startActivity(intent);
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
                    // Product already exists in the wishlist, remove it
                    removeFromWishlist(userId, productId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToastOnUIThread("Error checking wishlist status");
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
                    showToastOnUIThread("Product removed from wishlist");
                } else {
                    showToastOnUIThread("Error removing product from wishlist");
                }
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
                removeFromWishlist(userId, productId);
                showToastOnUIThread("Product added to cart");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToastOnUIThread("Error adding product to cart");
            }
        });
    }


    private void showToastOnUIThread(String message) {
        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textProductName, textProductPrice, textProductQty;
        Button btnAddToCart, btnRemoveFromWishlist;
        ImageButton imageButtonViewProduct;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textViewProductName_wishlist);
            textProductPrice = itemView.findViewById(R.id.textViewProductPrice_wishlist);
            textProductQty = itemView.findViewById(R.id.textViewProductQty_wishlist);
            imageButtonViewProduct = itemView.findViewById(R.id.imageButtonProductImage_wishlist);
            btnAddToCart = itemView.findViewById(R.id.buttonAddToCart_wishlist);
            btnRemoveFromWishlist = itemView.findViewById(R.id.buttonRemoveFromWishlist);
        }
    }
    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return null;
        }
    }

}

