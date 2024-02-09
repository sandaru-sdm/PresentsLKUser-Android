package com.sdm.presentslk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.sdm.presentslk.R;
import com.sdm.presentslk.SingleProductActivity;
import com.sdm.presentslk.model.Products;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {

    public static final String TAG = "CheckoutAdapter";
    private ArrayList<Products> products;
    private FirebaseStorage storage;
    private Context context;
    private String category, productId, userId;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private View view;
    private int totalPrice = 0;
    private RelativeLayout parentLayout;

    public CheckoutAdapter(ArrayList<Products> products, Context context, RelativeLayout parentLayout) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.totalPrice = 0;
        this.parentLayout = parentLayout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        view = inflater.inflate(R.layout.layout_checkout_cart_product_view, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Products pro = products.get(position);
        holder.textProductName.setText(pro.getProductName());

        if (pro.getProductPrice() != null && pro.getProductQty() != null) {
            try {
                int price = Integer.parseInt(pro.getProductPrice());
                int qty = Integer.parseInt(pro.getProductQty());

                int productTotalPrice = price * qty;
                totalPrice += productTotalPrice;

                String formattedPrice = String.format("Price : Rs. %d/=", productTotalPrice);
                holder.textProductPrice.setText(formattedPrice);
                holder.textProductQty.setText("Qty " + pro.getProductQty());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing price or quantity: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Product price or quantity is null");
        }

        updateTotalPriceTextView();

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
        }

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

        //Plus Button
        holder.imgBtnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQty(position);
            }
        });

        //Negative Button
        holder.imgBtnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reduceQty(position);
            }
        });

    }

    private void updateTotalPriceTextView() {
        TextView textViewPrice = parentLayout.findViewById(R.id.textViewPrice2);
        TextView textViewTotal2 = parentLayout.findViewById(R.id.textViewTotal2);
        if (textViewTotal2 != null) {
            textViewPrice.setText(String.format("Rs. %d.00/=", totalPrice));
            textViewTotal2.setText(String.format("Rs. %d.00/=", totalPrice + 2500));
        } else {
            Log.e(TAG, "TextViewTotal2 is null");
        }
    }

    private void calculateTotalPrice() {
        totalPrice = 0;

        for (Products product : products) {
            if (product.getProductPrice() != null && product.getProductQty() != null) {
                try {
                    int price = Integer.parseInt(product.getProductPrice());
                    int qty = Integer.parseInt(product.getProductQty());

                    int productTotalPrice = price * qty;
                    totalPrice += productTotalPrice;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing price or quantity: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Product price or quantity is null");
            }
        }

        updateTotalPriceTextView();
    }

    private void increaseQty(int position) {
        Products product = products.get(position);

        int currentQty = Integer.parseInt(product.getProductQty());

        if (currentQty >= 0) {
            currentQty++;
            product.setProductQty(String.valueOf(currentQty));

            updateQuantityInDatabase(product.getProductId(), currentQty);

            notifyItemChanged(position);
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
                        calculateTotalPrice();
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

        EditText editTextProductQty;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textViewProductName_cart);
            textProductPrice = itemView.findViewById(R.id.textViewProductPrice_cart);
            textProductQty = itemView.findViewById(R.id.textViewCartQty_cart);
            productImage = itemView.findViewById(R.id.imageButtonProductImage_cart);
            imgBtnNegative = itemView.findViewById(R.id.imageButtonNegative);
            imgBtnPositive = itemView.findViewById(R.id.imageButtonPositive);
            editTextProductQty = itemView.findViewById(R.id.editTextQty_checkout);
        }

    }

    public ArrayList<Integer> getAllProductQuantities() {
        ArrayList<Integer> quantities = new ArrayList<>();

        for (Products product : products) {
            if (product.getProductQty() != null) {
                try {
                    int qty = Integer.parseInt(product.getProductQty());
                    quantities.add(qty);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing quantity: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Product quantity is null");
            }
        }

        return quantities;
    }
}
