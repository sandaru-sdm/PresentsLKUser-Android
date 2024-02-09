package com.sdm.presentslk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.storage.FirebaseStorage;
import com.sdm.presentslk.R;
import com.sdm.presentslk.SingleProductActivity;
import com.sdm.presentslk.model.Products;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CheckoutSingleAdapter extends RecyclerView.Adapter<CheckoutSingleAdapter.ViewHolder> {

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
    private RecyclerView recyclerView;
    private TextView price, total;
    private int qty;

    public CheckoutSingleAdapter(ArrayList<Products> products, Context context, RelativeLayout parentLayout, RecyclerView recyclerView) {
        this.products = products;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.parentLayout = parentLayout;
        this.recyclerView = recyclerView;

        setInitialPrices();
    }

    private void setInitialPrices() {
        totalPrice = 0;

        for (Products product : products) {
            double productPrice = Double.parseDouble(product.getProductPrice());
            totalPrice += productPrice;
        }

        double totalPriceWithShipping = totalPrice + 2500.00;

        String formattedPrice = String.format("Price : Rs. %d/=", (int) totalPrice);
        price = parentLayout.findViewById(R.id.textViewPrice2);
        price.setText(formattedPrice);


        String formattedTotalPrice = String.format("Total : Rs. %.2f/=", totalPriceWithShipping);
        total = parentLayout.findViewById(R.id.textViewTotal2);
        total.setText(formattedTotalPrice);
    }




    @NonNull
    @Override
    public CheckoutSingleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        view = inflater.inflate(R.layout.layout_checkout_single_product_view, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Products pro = products.get(position);
        holder.textProductName.setText(pro.getProductName());
        holder.editTextProductQty.setText("1");
        qty = Integer.parseInt(String.valueOf(holder.editTextProductQty.getText()));

        double productPrice = Double.parseDouble(pro.getProductPrice());
        String formattedProductPrice = String.format("Price : Rs. %.2f/=", productPrice);
        holder.textProductPrice.setText(formattedProductPrice);

        category = pro.getProductCategory();
        productId = pro.getProductId();

        storage.getReference("ProductImages/" + pro.getImageUrl())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .resize(200, 200)
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
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
        }

        // Single Product View
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

        holder.editTextProductQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "AfterTextChanged: " + editable.toString());

                if (editable.toString().isEmpty()) {
                    holder.textProductPrice.setText("Price : Rs. 0.00/=");
                    CheckoutSingleAdapter.this.price.setText("Total : Rs. 0.00/=");
                    return;
                }

                int quantity = Integer.parseInt(editable.toString());
                Log.d(TAG, "Quantity: " + quantity);

                if (quantity < 1) {
                    editable.clear();
                    editable.append("1");
                    return;
                }

                double productPrice = Double.parseDouble(pro.getProductPrice());
                double totalPrice = quantity * productPrice;
                Log.d(TAG, "Total Price: " + totalPrice);

                String formattedPrice = String.format("Rs. %.2f/=", totalPrice);
                holder.textProductPrice.setText(formattedPrice);

                double totalPriceWithShipping = totalPrice + 2500.00;
                String formattedTotalPrice = String.format("Rs. %.2f/=", totalPriceWithShipping);

                price.setText(formattedPrice);
                total.setText(formattedTotalPrice);
            }

        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public int getQuantity(int position) {
        if (position < products.size()) {
            View itemView = recyclerView.getChildAt(position);
            EditText editTextQty = itemView.findViewById(R.id.editTextQty_checkout);

            try {
                return Integer.parseInt(editTextQty.getText().toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textProductName, textProductPrice, textProductQty;
        ImageButton productImage;
        EditText editTextProductQty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textViewProductName_cart);
            textProductPrice = itemView.findViewById(R.id.textViewProductPrice_cart);
            textProductQty = itemView.findViewById(R.id.textViewCartQty_cart);
            productImage = itemView.findViewById(R.id.imageButtonProductImage_cart);
            editTextProductQty = itemView.findViewById(R.id.editTextQty_checkout);
        }
    }
}
