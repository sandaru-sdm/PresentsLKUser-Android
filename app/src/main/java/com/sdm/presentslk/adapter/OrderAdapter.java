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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.sdm.presentslk.OrdersActivity;
import com.sdm.presentslk.R;
import com.sdm.presentslk.SingleProductActivity;
import com.sdm.presentslk.model.Orders;

import com.sdm.presentslk.model.Products;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>{

    public static final String TAG = "AdminItemAdapter";
    private ArrayList<Orders> orders;
    private FirebaseStorage storage;
    private Context context;

    private String productId, key;

    public OrderAdapter(ArrayList<Orders> orders, Context context) {
        this.orders = orders;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_orders_product_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Orders order = orders.get(position);
        holder.textProductName.setText(order.getProductName());
        holder.textProductPrice.setText(order.getPrice());
        holder.textProductTotal.setText("Total " + order.getTotalPrice());
        holder.textProductQty.setText("QTY : " + order.getQuantity());
        holder.textProductStatus.setText(order.getStatus());
        holder.textProductTime.setText("Time : "+order.getTime());
        holder.textProductDate.setText("Date : " +order.getDate());
        holder.textProductShipping.setText("Shipping : Rs. 2500.00/=");

        productId = order.getProductId();
        key = order.getKey();

        if (holder.imageButtonViewProduct != null) {
            storage.getReference("ProductImages/" + order.getImageUrl())
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get()
                                    .load(uri)
                                    .resize(200, 200)
                                    .centerCrop()
                                    .into(holder.imageButtonViewProduct);
                            Log.i(TAG, uri.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    });
        } else {
            Log.e(TAG, "imageButtonViewProduct is null");
        }

        holder.imageButtonViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, SingleProductActivity.class);
                intent.putExtra("productId", productId);
                context.startActivity(intent);
            }
        });

        holder.btnCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null){
                    String userId = firebaseUser.getUid();
                    removeOrder(productId, key, userId, position);
                } else {
                    Toast.makeText(context, "Something Went Wrong! User Details Not Available At the Moment.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void removeOrder(String productId, String key, String userId, int position) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Registered Users")
                .child(userId)
                .child("Orders")
                .child(key);

        orderRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (!orders.isEmpty() &&  position >= 0 && position < orders.size()) {
                            orders.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Order Cancelled Successfully!", Toast.LENGTH_SHORT).show();
                            reloadOrdersActivity();
                        } else {
                            Log.e(TAG, "Invalid position or empty product list");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to cancel order!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to cancel order: " + e.getMessage());
                    }
                });
    }

    private void reloadOrdersActivity() {
        Intent intent = new Intent(context, OrdersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        ((AppCompatActivity) context).finish();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textProductName, textProductPrice, textProductQty, textProductShipping, textProductTotal, textProductDate, textProductTime, textProductStatus;
        ImageButton imageButtonViewProduct;
        Button btnCancelOrder;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textViewProductName_orders);
            textProductPrice = itemView.findViewById(R.id.textViewProductPrice_orders);
            textProductQty = itemView.findViewById(R.id.textViewProductQty_orders);
            textProductShipping = itemView.findViewById(R.id.textViewProductShipping_orders);
            textProductTotal = itemView.findViewById(R.id.textViewProductTotal_orders);
            textProductDate = itemView.findViewById(R.id.textViewDate_orders);
            textProductTime = itemView.findViewById(R.id.textViewTime_orders);
            textProductStatus = itemView.findViewById(R.id.textViewStatus_orders);
            imageButtonViewProduct = itemView.findViewById(R.id.imageButtonProductImage_orders);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
        }
    }
}
