package com.gallery.marsplay.customcamaera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ZoomControls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CameraActivity extends AppCompatActivity {
private Camera mCamera;
private FrameLayout frameLayout;
private ImageView click_camera,flash;
private CameraSurface cameraSurface;
private TextView zoom_value;
private ImageView flash_button;
private int f=0,g=0;
private ZoomControls   zoomControls;
private boolean hasFlash,isFlashOn=false;
private android.hardware.Camera.Parameters params;


    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().setStatusBarColor(ContextCompat.getColor(this ,R.color.blck));
        frameLayout=(FrameLayout)findViewById(R.id.frameLayout);
        flash_button = (ImageView) findViewById(R.id.toggleButton);
        flash=(ImageView)findViewById(R.id.flash_view);
        zoom_value=(TextView) findViewById(R.id.textView);

// check flash support..........................

        hasFlash=getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if(!hasFlash){
            flash.setVisibility(View.INVISIBLE);
        }

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isFlashOn){

                    turnOffFlash();
                }
                else{

                    turnOnFlash();
                }

            }
        });

      flash_button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

                 zoom_value.setText("");
                 flash.setImageResource(R.drawable.flash_off);
                 isFlashOn=false;
                 SwithCamera(f);
                 f++;
          }
      });

      // Picture callBack...................................................

      final Camera.PictureCallback mPictureCallback=new Camera.PictureCallback() {

          @Override
          public void onPictureTaken(byte[] bytes, Camera camera) {
         //Bitmap    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);


              Intent i=new Intent(CameraActivity.this,ShowPicture.class);
              i.putExtra("raw",bytes);
              i.putExtra("mode",f);
              i.putExtra("from","cm");
              startActivity(i);

              MainActivity mn=new MainActivity();
              mn.finish();

              finish();


          }
      };

       final Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            public void onShutter() {
                AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mgr.playSoundEffect(AudioManager.ERROR);
            }
        };

      // Click on Camera Button......................................................

        click_camera=(ImageView) findViewById(R.id.cp);


        click_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCamera != null)
                {
                mCamera.takePicture(shutterCallback,null,mPictureCallback);
                    AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                   mgr.setMode(AudioManager.MODE_RINGTONE);

                }
               // Toast.makeText(CameraActivity.this, "click", Toast.LENGTH_SHORT).show();
            }
            
            
        });

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},1 );
            return;

        }
        else{
            initCamera();
        }

    }

    private File getOutputMedialFile() {

        return null;
    }


    private void turnOffFlash() {
        if (isFlashOn) {


            params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
            isFlashOn = false;

            // changing button/switch image
     flash.setImageResource(R.drawable.flash_off);

        }
    }
    private void turnOnFlash(){
        if (!isFlashOn) {

            params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            mCamera.setParameters(params);
            isFlashOn = true;
          flash.setImageResource(R.drawable.flash);

        }
    }


    private void SwithCamera(int F) {

        cameraSurface.surfaceDestroyed(cameraSurface.getHolder());
        cameraSurface.getHolder().removeCallback(cameraSurface);
        cameraSurface.destroyDrawingCache();
        frameLayout.removeView(cameraSurface);
         mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();

//checking front and back camera.........
        if (F%2 == 1) {

            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);


        }
        if (F%2 == 0) {

            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        cameraSurface = new CameraSurface(CameraActivity.this, mCamera,zoom_value);

        frameLayout.addView(cameraSurface);

    }

    public void initCamera(){

        try {

                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);


        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
        cameraSurface=new CameraSurface(this,mCamera,zoom_value);
        frameLayout.addView(cameraSurface);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED &&
                grantResults[1] ==PackageManager.PERMISSION_GRANTED &&
                grantResults[2] ==PackageManager.PERMISSION_GRANTED ) {
            initCamera();
        }
        else{
            Toast.makeText(this, "Allow Camera Permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.stopPreview();
        mCamera.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera!=null)
        mCamera.stopPreview();
    }

    @Override
    public void onBackPressed() {
        //
        // super.onBackPressed();
        Intent i=new Intent(this,MainActivity.class);
        startActivity(i);
        finish();

    }
}
