package com.sdm.presentslk;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

public class AboutUsActivity extends AppCompatActivity {

    private TextView textMobile, textAddress, textEmail;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        textMobile = findViewById(R.id.textViewMobileNumber);
        textAddress = findViewById(R.id.textViewAddress_aboutUs);
        textEmail = findViewById(R.id.textViewEmail_aboutUs);
        toolbar = findViewById(R.id.toolBar);

        navigateBack();

        textMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog("Make a Call", "Do you want to make a call?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phoneNumber = textMobile.getText().toString().trim();
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                        startActivity(dialIntent);
                    }
                });
            }
        });

        textEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog("Send Email", "Do you want to send an email?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String emailAddress = textEmail.getText().toString().trim();

                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:" + emailAddress));

                        startActivity(emailIntent);
                    }
                });
            }
        });

        textAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog("Get Location", "Do you want to get the location?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String address = textAddress.getText().toString().trim();

                        Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
                        mapIntent.setPackage("com.google.android.apps.maps");

                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        }
                    }
                });
            }
        });
    }

    private void showConfirmationDialog(String title, String message, DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", positiveClickListener)
                .setNegativeButton("No", null)
                .show();
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
}