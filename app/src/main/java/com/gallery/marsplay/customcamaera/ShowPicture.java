package com.gallery.marsplay.customcamaera;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

public class ShowPicture extends AppCompatActivity implements View.OnClickListener {
    byte[] bytes;
    ImageView mImageView;
    ImageView crop,rotateImage,upload;
    Bitmap bmpRotate;
    int mode=0,color=0;
    Bitmap bitmap;
    Uri imageUri=null,uri;
    ProgressDialog mProgress;
    String from;
    StorageReference mImageStorage;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);
        getWindow().setStatusBarColor(ContextCompat.getColor(this ,R.color.blck));
        mImageStorage = FirebaseStorage.getInstance().getReference();
        from=getIntent().getStringExtra("from");
        mode = getIntent().getIntExtra("mode", 0);
        mImageView = (ImageView) findViewById(R.id.im);

       // mImageView.setOnTouchListener(new CustomImageView());
        mProgress=new ProgressDialog(this);
        mProgress.setMessage("Uploading Image...");
        mProgress.setCanceledOnTouchOutside(false);


        if(from.equals("main")){
          uri=Uri.parse(getIntent().getStringExtra("uri"));
            CropImage.activity(uri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
            mImageView.setImageURI(uri);
        }

        if(from.equals("cm")){
            bytes=getIntent().getByteArrayExtra("raw");
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            uri=getImageUri(this,bitmap);
            // checking bitmap from  front or back......
            if (mode % 2 == 0) {
              bitmap=  rotate(bitmap, 180);
              uri=getImageUri(this,bitmap);

            }
                mImageView.setImageBitmap(bitmap);



        }

        crop = (ImageView) findViewById(R.id.crop);

        upload = (ImageView) findViewById(R.id.upload);
        crop.setOnClickListener(this);

        upload.setOnClickListener(this);




    }


// front camera bitmap rotatation 180 degree....

    private Bitmap rotate(Bitmap bitmap,int degree) {

        Matrix mat = new Matrix();
        mat.postRotate(degree);
      bmpRotate = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(),
                mat, true);


        return bmpRotate;

    }

    @Override
    public void onClick(View view) {


        if(view.getId()==R.id.upload){
            Uri upUri=null;

            if (imageUri != null) {
                upUri = imageUri;
                SaveInDatabase(upUri);

            } else {

                Toast.makeText(this, " First crop this Image,Then you will be able to upload it", Toast.LENGTH_SHORT).show();
            }
            }

        if(view.getId()==R.id.crop){
            Uri mURI;
            View popupContentView = LayoutInflater.from(ShowPicture.this).inflate(R.layout.aspect_ratio, null);
            // Popupwindow popupWindow = Popupwindow(context);
            final PopupWindow  popupWindow = new PopupWindow(popupContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setAnimationStyle(R.style.popup_window_animation_pop);
            popupWindow.setContentView(popupContentView);
            popupWindow.showAtLocation(crop, Gravity.BOTTOM, 0, 0);
            CardView one=popupContentView.findViewById(R.id.one);
            CardView two=popupContentView.findViewById(R.id.two);
            CardView three=popupContentView.findViewById(R.id.three);
            final Uri finalMUri = uri;
            one.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();
                    CropImage.activity(finalMUri)
                            .setAspectRatio(1, 1)
                            .setMinCropWindowSize(500, 500)
                            .start(ShowPicture.this);

                }
            });
            two.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();

                    CropImage.activity(finalMUri)
                            .setAspectRatio(3, 2)
                            .setMinCropWindowSize(500, 500)
                            .start(ShowPicture.this);

                }
            });
            three.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();
                    CropImage.activity(finalMUri)
                            .setAspectRatio(16, 9)
                            .setMinCropWindowSize(500, 500)
                            .start(ShowPicture.this);

                }
            });


            popupWindow.setContentView(popupContentView);

        }

    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "marsP", null);
        return Uri.parse(path);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                 imageUri = result.getUri();
                 uri=result.getUri();

                mImageView.setImageURI(null);
                mImageView.setImageURI(imageUri);

                }
              if(requestCode==RESULT_CANCELED)
                Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();

            }
            else{
            Toast.makeText(this, "No crop activity", Toast.LENGTH_SHORT).show();

        }
        }

      public void SaveInDatabase(Uri upUri){
            File thumb_filePath = new File(upUri.getPath());
            mProgress.show();

// creating thumnail.......................

            Bitmap thumb_bitmap = null;
            try {
                thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);
            } catch (IOException e) {
                e.printStackTrace();
                mProgress.hide();

            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] thumb_byte = baos.toByteArray();



            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
            Date date = new Date();
            String dd=formatter.format(date);

            //..... upploading image on server...........

            StorageReference filepath = mImageStorage.child("Image").child("IMG"+dd + ".jpg");
            final StorageReference thumb_filepath = mImageStorage.child("ThumbImage").child("thumbs").child("TMB" +dd+ ".jpg");

            //final byte[] finalThumb_byte = thumb_byte;
           // final StorageMetadata metadata = new StorageMetadata.Builder() .setContentType("image/jpg") .setContentLanguage("English") .setCustomMetadata("ajeet's special meta data", "JK nothing special here") .setCustomMetadata("location", "India") .build();

            filepath.putFile(upUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    final String download_url = task.getResult().getDownloadUrl().toString();
                    if(task.isSuccessful()){

                        UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                if(thumb_task.isSuccessful()){

                                    DatabaseReference mDatabase =FirebaseDatabase.getInstance().getReference().child("MarsPlay").push();

                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                                    Date date = new Date();
                                    HashMap<String, String> dataMap = new HashMap<>();
                                    dataMap.put("thumb_image", thumb_downloadUrl);
                                    dataMap.put("original_image", download_url);
                                    dataMap.put("date",formatter.format(date));


                                    //......Saving Image url in database........................

                                    mDatabase.setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mProgress.dismiss();
                                                Intent i=new Intent(ShowPicture.this,MainActivity.class);
                                                startActivity(i);
                                                finish();

                                                Toast.makeText(ShowPicture.this, "Image uploaded", Toast.LENGTH_SHORT).show();

                                            }
                                            else{
                                                mProgress.hide();
                                                Toast.makeText(ShowPicture.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });
                                }else{
                                    mProgress.hide();
                                    Toast.makeText(ShowPicture.this, thumb_task.getException().getMessage(), Toast.LENGTH_SHORT).show();


                                }
                            }
                        });
                    }
                    else{
                        mProgress.hide();
                        Toast.makeText(ShowPicture.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();


                    }
                }
            });

        }



    }



