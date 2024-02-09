package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class SplashViewActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_PresentsLK_FullScreen);
        setContentView(R.layout.activity_splash_view);

        progressBar = findViewById(R.id.splashProgressBar);
        firebaseAuth = FirebaseAuth.getInstance();

        ImageView imageView = findViewById(R.id.splashImage);
        Picasso.get().load(R.drawable.logo)
                .resize(1200, 1200)
                .into(imageView);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        }, 5000);

        // Add an authentication state listener
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in, start HomeViewActivity
                    Log.d("AUTH", "onCreate: " + firebaseAuth.getCurrentUser());
                    startNextActivity(HomeViewActivity.class);
                } else {
                    // User is signed out, start MainActivity
                    startNextActivity(MainActivity.class);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        };

        // Add the authentication state listener
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void startNextActivity(final Class<?> activityClass) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashViewActivity.this, activityClass));
                finish();
            }
        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the authentication state listener
        if (firebaseAuth != null && authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
