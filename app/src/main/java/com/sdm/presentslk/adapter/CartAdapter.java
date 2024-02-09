package com.sdm.presentslk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.sdm.presentslk.CartActivity;
import com.sdm.presentslk.R;
import com.sdm.presentslk.SingleProductActivity;
import com.sdm.presentslk.model.Products;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    public static final String TAG = "AdminItemAdapter";
    private ArrayList<Products> products;
    private FirebaseStorage storage;
    private Context context;
    private String category, productId, userId;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    public CartAdapter(ArrayList<Products> products, Context context) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_cart_product_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Products pro = products.get(position);
        holder.textProductName.setText(pro.getProductName());

        int price = Integer.parseInt(pro.getProductPrice());
        int qty = Integer.parseInt(pro.getProductQty());

        int totalPrice = price * qty;
        String formattedPrice = String.format("Price : Rs. %d/=", totalPrice);
        holder.textProductPrice.setText(formattedPrice);
        holder.textProductQty.setText("Qty " + pro.getProductQty());

        category = pro.getProductCategory();
        productId = pro.getProductId();

        storage.getReference("ProductImages/"+pro.getImageUrl())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .resize(200,200)
                                .centerCrop()
                                .into(holder.productImage);
                        Log.i(TAG, uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            userId = firebaseUser.getUid();
            checkCartCollection(userId, holder);
        }

        //Qty Addition
        holder.imgBtnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQty(position);
            }
        });

        //Qty Subtraction
        holder.imgBtnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reduceQty(position);
            }
        });

        // Remove From Cart Button
        holder.btnRemoveFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Products currentProduct = products.get(position);
                String productId = currentProduct.getProductId();
                removeCartItem(userId, productId, position);
            }
        });

        //Buy It Now Button
        holder.btnBuyItNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Products selectedProduct = products.get(position);
                Intent intent = new Intent(context, SingleProductActivity.class);
                intent.putExtra("productId", selectedProduct.getProductId());
                context.startActivity(intent);
            }
        });

        //Single Product View
        holder.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Products currentProduct = products.get(position);
                String productId = currentProduct.getProductId();

                Intent intent = new Intent(context, SingleProductActivity.class);
                intent.putExtra("productId", productId);
                context.startActivity(intent);
            }
        });
    }
    private void checkCartCollection(String userId, ViewHolder holder) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Registered Users")
                .child(userId)
                .child("cart");

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (holder.btnBuyAll != null) {
                        holder.btnBuyAll.setEnabled(true);
                    }
                } else {
                    if (holder.btnBuyAll != null) {
                        holder.btnBuyAll.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to check cart collection: " + databaseError.getMessage());
            }
        });
    }


    private void increaseQty(int position) {
        Products product = products.get(position);

        int currentQty = Integer.parseInt(product.getProductQty());

        if (currentQty >= 0) {
            currentQty++;
            product.setProductQty(String.valueOf(currentQty));
            notifyItemChanged(position);

            updateQuantityInDatabase(product.getProductId(), currentQty);
        } else {
            Toast.makeText(context, "You Can't make quantity less than 0", Toast.LENGTH_SHORT).show();
        }
    }

    private void reduceQty(int position) {
        Products product = products.get(position);

        int currentQty = Integer.parseInt(product.getProductQty());

        if (currentQty > 1) {
            currentQty--;
            product.setProductQty(String.valueOf(currentQty));
            notifyItemChanged(position);

            updateQuantityInDatabase(product.getProductId(), currentQty);
        } else {
            Toast.makeText(context, "Quantity cannot be less than 1", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCartItem(String userId, String productId, int position) {
        DatabaseReference cartItemRef = FirebaseDatabase.getInstance()
                .getReference("Registered Users")
                .child(userId)
                .child("cart")
                .child(productId);

        cartItemRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Item removed successfully from the cart");
                        Log.d(TAG, "Position to remove: " + position);
                        Log.d(TAG, "Products before removal: " + products.toString());

                        if (!products.isEmpty() && position >= 0 && position < products.size()) {
                            products.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Product Removed from Cart", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Invalid position or empty product list");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to remove item from the cart: " + e.getMessage());
                    }
                });
    }



    private void updateQuantityInDatabase(String productId, int quantity) {

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Registered Users")
                .child(userId)
                .child("cart")
                .child(productId)
                .child("qty");

        cartRef.setValue(quantity)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Quantity updated successfully in the database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to update quantity in the database: " + e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {

        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textProductName, textProductPrice, textProductQty;
        ImageButton productImage, imgBtnNegative, imgBtnPositive;
        Button btnBuyItNow, btnRemoveFromCart, btnBuyAll;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textViewProductName_cart);
            textProductPrice = itemView.findViewById(R.id.textViewProductPrice_cart);
            textProductQty = itemView.findViewById(R.id.textViewCartQty_cart);
            productImage = itemView.findViewById(R.id.imageButtonProductImage_cart);
            imgBtnNegative = itemView.findViewById(R.id.imageButtonNegative);
            imgBtnPositive = itemView.findViewById(R.id.imageButtonPositive);
            btnBuyItNow = itemView.findViewById(R.id.buttonBuyItNow_cart);
            btnRemoveFromCart = itemView.findViewById(R.id.buttonRemoveFromCart);
            btnBuyAll = itemView.findViewById(R.id.btnBuyAll);
        }

    }

}
