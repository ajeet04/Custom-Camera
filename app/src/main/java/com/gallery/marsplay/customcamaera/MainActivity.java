package com.gallery.marsplay.customcamaera;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.PorterDuff;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
FloatingActionButton cameraButton;
private BottomSheetBehavior mBottomSheetBehaviour;
private View bottomSheet;
private int SELECT_IMAGE=100;
private PopupWindow popupWindow;
private RecyclerView imageList;
private LinearLayoutManager mLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(ContextCompat.getColor(this ,R.color.blck));
      //  drawable.setColorFilter(ContextCompat.getColor(this,android.R.color.background_light), PorterDuff.Mode.SRC_ATOP);
        imageList=(RecyclerView)findViewById(R.id.image_list);
        int orientation = getResources().getConfiguration().orientation;
        int grid=0;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            grid=3;
        } else {
            grid=2;
        }

                mLayoutManager=new GridLayoutManager(this, grid);
        imageList.setLayoutManager(mLayoutManager);
      //  mLayoutManager.setReverseLayout(true);
        imageList.setHasFixedSize(true);

       // Bottom sheet View.........
        bottomSheet=findViewById(R.id.bottom_sheet);
        mBottomSheetBehaviour=BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehaviour.setPeekHeight(0);
        // chooser button...................
        cameraButton = (FloatingActionButton) findViewById(R.id.camera_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                ImageView camera=bottomSheet.findViewById(R.id.camera);
                ImageView gallery=bottomSheet.findViewById(R.id.gallery);
                ImageView cc=bottomSheet.findViewById(R.id.cc);
                cc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        mBottomSheetBehaviour.setPeekHeight(0);

                    }
                });
                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent cameraIntent=new Intent(MainActivity.this,CameraActivity.class);
                         startActivity(cameraIntent);
                        mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        mBottomSheetBehaviour.setPeekHeight(0);
                        finish();
                    }
                });

                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);

                    }
                });


            }
        });



    }

    private void sigIn() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword("marsplay@gmail.com", "marsplay")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(MainActivity.this, "signin", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {


                    Intent i=new Intent(this,ShowPicture.class);
                    i.putExtra("uri",data.getData().toString());
                    i.putExtra("from","main");
                    i.putExtra("mode",1);
                     startActivity(i);
                     finish();

                    mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    mBottomSheetBehaviour.setPeekHeight(0);
                }
            } else if (resultCode == Activity.RESULT_CANCELED)  {
                Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference mDatabase=FirebaseDatabase.getInstance().getReference().child("MarsPlay");

        FirebaseRecyclerAdapter<Model,MyViewHolder> mAdatper=new FirebaseRecyclerAdapter<Model, MyViewHolder>(
          Model.class,
          R.layout.item_list,
          MyViewHolder.class,
          mDatabase
        ) {
            @Override
            protected void populateViewHolder(final MyViewHolder viewHolder, final Model model, int position) {

                viewHolder.setThumbImage(model.getThumb_image());
                viewHolder.setDate(model.getDate());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        PopUp(model.getOriginal_image(),viewHolder.mView);

                    }
                });

            }
        };
        imageList.setAdapter(mAdatper);
    }

    private void PopUp(String orginal_image,View view) {

        View popupContentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_view_layout, null);
       // Popupwindow popupWindow = Popupwindow(context);
        popupWindow = new PopupWindow(popupContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(R.style.popup_window_animation_pop);

        popupWindow.setContentView(popupContentView);

        ImageView img=popupContentView.findViewById(R.id.imageView);
        ImageButton cancel=popupContentView.findViewById(R.id.cancel);
        Picasso.get().load(orginal_image)
                .placeholder(R.drawable.blur)
                .centerCrop()
                .fit().into(img);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();

            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
       View  mView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setThumbImage(String thumb_image) {

            ImageView mImage=mView.findViewById(R.id.image);
            Picasso.get().load(thumb_image)
                    .placeholder(R.drawable.blur)
                    .into(mImage);


        }

        public void setDate(String date) {
            TextView tv=mView.findViewById(R.id.date);
            tv.setText(date);

        }
    }

    @Override
    public void onBackPressed() {

        if(popupWindow!=null){
            popupWindow.setAnimationStyle(R.anim.fade_in_animation);
            popupWindow.dismiss();
        }
        else{
            finish();
        }
    }
}
