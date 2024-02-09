package com.sdm.presentslk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UploadProfilePicActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageView imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_pic);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));
        }

        // For AppCompat
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("Upload Profile Picture");
        }

        Button buttonUploadPicChoose = findViewById(R.id.upload_pic_choose_button);
        Button buttonUploadPic = findViewById(R.id.upload_pic_button);
        progressBar = findViewById(R.id.progressBar);
        imageViewUploadPic = findViewById(R.id.imageView_profile_dp);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();

        //Set User's current DP in ImageView (If upload already). We will use Picasso since ImageViewer setImage not support to regular URIs.
        Picasso.get().load(uri).into(imageViewUploadPic);

        //To Upload pic, open the file manager and choose picture, set picture to the image view
        buttonUploadPicChoose.setOnClickListener(v -> openFileChooser());

        //Upload Image
        buttonUploadPic.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            uploadPic();
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);
        }

    }

    private void uploadPic(){
        if(uriImage != null){
            //Save the image with uid of the currently logged in user
            StorageReference fileReference = storageReference.child(Objects.requireNonNull(authProfile.getCurrentUser()).getUid() + "/displaypic." + getFileExtension(uriImage));

            //Upload image to storage
            fileReference.putFile(uriImage).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    firebaseUser = authProfile.getCurrentUser();

                    //Finally set the display image of the user after upload
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
                    firebaseUser.updateProfile(profileUpdates);
                });

                progressBar.setVisibility(View.GONE);
                Toast.makeText(UploadProfilePicActivity.this, "Upload Successful!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(UploadProfilePicActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();

            }).addOnFailureListener(e -> Toast.makeText(UploadProfilePicActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePicActivity.this, "No File Selected!", Toast.LENGTH_LONG).show();
        }
    }

    //Obtain File Extension of the image
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //Create ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menue, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UploadProfilePicActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_change_password){
            Intent intent = new Intent(UploadProfilePicActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(UploadProfilePicActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }  else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UploadProfilePicActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UploadProfilePicActivity.this, MainActivity.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();   //Close UserProfileActivity
        } else {
            Toast.makeText(UploadProfilePicActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }


}