package com.androidhive.ultrahdwallpapers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.androidhive.ultrahdwallpapers.Common.Common;

import com.androidhive.ultrahdwallpapers.Model.Category;
import com.androidhive.ultrahdwallpapers.Model.Wallpapers;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UploadWallpaperActivity extends AppCompatActivity {

    //Widgets
    private ImageView image_preview;
    private Button btn_upload;
    private MaterialSpinner spinner;
    private MaterialEditText txtWallpaper_name;

    //Spinner data
    private final Map<String, String> spinnerData = new HashMap<>();

    private StorageReference storageRef;

    //Filepath
    private Uri filepath;

    //Category ID set from Spinner
    private String categoryIdSelected = "";

    //Name of image file
    private String nameOfFile = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_wallpaper);

        //Firebase
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();

        //Interstitial Ad
        Common.setInterstitialAd(this, 1);


        //Widgets
        image_preview = findViewById(R.id.img_preview);
        Button btn_browse = findViewById(R.id.btn_browse);
        btn_upload = findViewById(R.id.btn_upload);
        txtWallpaper_name = findViewById(R.id.wallpaper_name);
        spinner = findViewById(R.id.spinner_category);



        loadCategorySpinner();


        //Browse Button
        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();
            }
        });


        //Upload Button
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (spinner.getSelectedIndex() == 0) {

                    Toast.makeText(UploadWallpaperActivity.this, "Please choose category", Toast.LENGTH_SHORT).show();


                } else if (txtWallpaper_name.getText().toString().isEmpty()){

                    Toast.makeText(UploadWallpaperActivity.this, "Please Type a name", Toast.LENGTH_SHORT).show();


                }else{
                        uploadWallpaper();

                }


            }
        });


    }


    //*******************************Browse Image From Gallery****************************

    private void chooseImage() {

        //Intent to go to Users Gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image: "), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null) {

            filepath = data.getData();


            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);

                image_preview.setImageBitmap(bitmap);

                btn_upload.setEnabled(true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //*******************************Load Category Spinner****************************

    private void loadCategorySpinner() {


        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_BACKGROUND)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {


                            Category category = postSnapshot.getValue(Category.class);

                            String key = postSnapshot.getKey();

                            spinnerData.put(key, category != null ? category.getName() : null);


                        }

                        //Because Material Spinner will not recieve hint so we need to give custom hint
                        //we have to put spinnerdata in arraylist then convert it to list
                        Object[] valueArray = spinnerData.values().toArray(); //The values are the category names (second argument of spinnderData)

                        //Create empty list
                        List<Object> valueList = new ArrayList<>();

                        valueList.add(0, "Category"); //We will add first item as hint "Category"
                        valueList.addAll(Arrays.asList(valueArray)); //Add all of the category names in array convert to list

                        //We can only pass list
                        spinner.setItems(valueList);

                        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

                                //When use chooses category we will get category(key)
                                Object[] keyArray = spinnerData.keySet().toArray();

                                List<Object> keyList = new ArrayList<>();
                                keyList.add(0, "categoryId");
                                keyList.addAll(Arrays.asList(keyArray));

                                categoryIdSelected = keyList.get(position).toString();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    //*******************************Upload Wllpaper to Firebase Storage****************************

    private void uploadWallpaper() {

        if (filepath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AlertDialogStyle);
            progressDialog.setTitle("Please Wait");
            progressDialog.show();

            nameOfFile = UUID.randomUUID().toString();

            final StorageReference imgFolder = storageRef.child("images/" + nameOfFile);

            imgFolder.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            progressDialog.dismiss();

                            imgFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    saveUriToCategory(categoryIdSelected, uri.toString());
                                    finish();
                                    Toast.makeText(UploadWallpaperActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();



                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(UploadWallpaperActivity.this, "Couldn't Upload" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Error On Uploading", ""+e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("uploading: " + (int) progress + "%");
                }
            });

        }
    }

    private void deleteImageFromStorage(String nameOfFile) {

        //Go to the images folder and delete file name "nameOfFile"
        storageRef.child("images/" + nameOfFile)
                .delete();
    }




    //*******************************Save image to Background in Firebase****************************

    //Capitalize 1st character of word
    private static String toUpperCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (String anArr : arr) {
            sb.append(Character.toUpperCase(anArr.charAt(0)))
                    .append(anArr.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private void saveUriToCategory(String categoryIdSelected, final String imageLink) {

        FirebaseDatabase.getInstance()
                .getReference(Common.WALLPAPER_LIST)
                .push() //Generate random Key
                .setValue(new Wallpapers(
                        categoryIdSelected,
                        imageLink,
                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                        nameOfFile,
                        toUpperCase(txtWallpaper_name.getText().toString()),
                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName(),
                        Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl()).toString()));

    }

    //*******************************Delete Image on back Button pressed****************************

    /**
     * This will delete image from firebase storage if user presses back button,
     * without clicking submit button
     */
    @Override
    public void onBackPressed() {
        deleteImageFromStorage(nameOfFile);
        super.onBackPressed();
    }


}
