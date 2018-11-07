package com.clidev.qdoi;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CameraKitUtility.CameraFunctionHandler {

    private Boolean isCameraMode;
    private Boolean canTakePicture;
    private Bitmap mBitmap;
    private Boolean isCamFacingFront;


    @BindView(R.id.cam_activity_cameraview) CameraView mCameraView;
    @BindView(R.id.cam_activity_cam_trigger) ImageView mCameraTrigger;
    @BindView(R.id.cam_activity_confirm_layout) ConstraintLayout mConfirmPhotoLayout;
    @BindView(R.id.cam_activity_confirm_image) ImageView mConfirmPhotoImageView;
    @BindView(R.id.cam_activity_upload_progress) ProgressBar mUploadProgress;
    @BindView(R.id.cam_activity_progress_text) TextView mUploadProgressTextView;
    //@BindView(R.id.cam_activity_upload_button) ImageView mUploadButton;
    //@BindView(R.id.cam_activity_cancel_button) ImageView mCancelButton;
    //@BindView(R.id.cam_activity_privacy_switch) Switch mPrivacySwitch;
    //@BindView(R.id.cam_activity_private_text) TextView mPrivateText;
    //@BindView(R.id.cam_activity_public_text) TextView mPublicText;
    @BindView(R.id.cam_activity_cam_layout) ConstraintLayout mCameraLayout;
    @BindView(R.id.cam_activity_flash_off) ImageView mFlashOff;
    @BindView(R.id.cam_activity_flash_on) ImageView mFlashOn;
    @BindView(R.id.cam_activity_cam_rotate) ImageView mCamRotate;
   // @BindView(R.id.cam_activity_rotate_left) ImageView mRotateLeft;
    //@BindView(R.id.cam_activity_rotate_right) ImageView mRotateRight;
    //@BindView(R.id.prediction) TextView mPrediction;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setTitle("Camera");

        FirebaseApp.initializeApp(this);



        // setting inital values
        isCameraMode = true;
        canTakePicture = false;
        isCamFacingFront = false;

        // setting on button clicked listener
        mCameraTrigger.setOnClickListener(this);
        mFlashOff.setOnClickListener(this);
        mFlashOn.setOnClickListener(this);
        mFlashOff.setOnClickListener(this);
        mCamRotate.setOnClickListener(this);
        //mRotateLeft.setOnClickListener(this);
        //mRotateRight.setOnClickListener(this);

        // set camera event listener
        mCameraView.addCameraKitListener(new CameraKitUtility(this));


    }



    ///////////////////////////////////////


    @Override
    protected void onResume() {
        super.onResume();

        // start camera
        if (isCameraMode) {
            mCameraView.start();
        }



    }


    @Override
    protected void onPause() {
        super.onPause();

        // stop camera
        mCameraView.stop();

    }





    // When buttons are clicked.
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cam_activity_cam_trigger:
                capturePhotoIfReady();
                break;


            case R.id.cam_activity_flash_off:
                // turn flash on
                mFlashOff.setVisibility(View.INVISIBLE);
                mFlashOn.setVisibility(View.VISIBLE);
                mCameraView.setMethod(CameraKit.Constants.METHOD_STANDARD);
                mCameraView.setFlash(CameraKit.Constants.FLASH_ON);
                Timber.d("flash off clicked");
                break;

            case R.id.cam_activity_flash_on:
                mFlashOn.setVisibility(View.INVISIBLE);
                mFlashOff.setVisibility(View.VISIBLE);
                mCameraView.setMethod(CameraKit.Constants.METHOD_STILL);
                mCameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                Timber.d("flash on clicked");
                break;

            case R.id.cam_activity_cam_rotate:
                if (isCamFacingFront == false) {
                    mCameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    isCamFacingFront = true;
                } else {
                    mCameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    isCamFacingFront = false;
                }
                break;



            default:
                break;
        }

    }




    private void capturePhotoIfReady() {
        if (canTakePicture == true) {
            showProgressBar();
            Timber.d("taking image");
            mCameraView.captureImage();
        } else {
            Toast.makeText(MainActivity.this, "Camera initating, please try again in a few seconds.", Toast.LENGTH_SHORT).show();
        }

    }

    private void hideUploadCancelSwitch() {
        //mUploadButton.setVisibility(View.INVISIBLE);
        //mCancelButton.setVisibility(View.INVISIBLE);
        //mPrivacySwitch.setVisibility(View.INVISIBLE);
        //mPublicText.setVisibility(View.INVISIBLE);
        //mPrivateText.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        mUploadProgress.setVisibility(View.VISIBLE);
        mUploadProgressTextView.setVisibility(View.VISIBLE);
    }



    private void backToCameraMode() {
        mCameraView.start();
        isCameraMode = true;

        // show cameras
        mCameraLayout.setVisibility(View.VISIBLE);

        // hide confirm image layout
        mConfirmPhotoLayout.setVisibility(View.INVISIBLE);
        hideProgressBar();


        // show upload or cancel buttons and privacy switch
        //mUploadButton.setVisibility(View.VISIBLE);
        //mCancelButton.setVisibility(View.VISIBLE);
        //mPrivacySwitch.setVisibility(View.VISIBLE);
        //mPublicText.setVisibility(View.VISIBLE);
        //mPrivateText.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        // hide progress bars
        mUploadProgressTextView.setVisibility(View.INVISIBLE);
        mUploadProgress.setVisibility(View.INVISIBLE);
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    /////////////////////////////////////////////////////////////////////



    // Camera event listener call back /////////////////////////////////////////////////////////////////////
    @Override
    public void onIsCameraReady(Boolean isReady) {
        canTakePicture = isReady;
    }


    @Override
    public void onImageCaptured(CameraKitImage cameraKitImage) {
        goToConfirmImageMode(cameraKitImage);

    }

    private void goToConfirmImageMode(CameraKitImage cameraKitImage) {
        mBitmap = CompressBitmapUtil.getResizedBitmap(cameraKitImage.getBitmap(), 500);

        // turn off camera
        mCameraView.stop();
        isCameraMode = false;

        // hide camera layout
        mCameraLayout.setVisibility(View.INVISIBLE);

        // show confirm image layout
        mConfirmPhotoLayout.setVisibility(View.VISIBLE);

        // display captured photo
        RequestOptions options = new RequestOptions();
        options.dontAnimate();

        Glide.with(MainActivity.this)
                .load(mBitmap)
                .apply(options)
                .into(mConfirmPhotoImageView);

        // run ML
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);

        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance()
                .getVisionLabelDetector();

        Task<List<FirebaseVisionLabel>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionLabel> labels) {
                                        hideProgressBar();
                                        String text = "";
                                       // Map<String, Float> prediction = new HashMap<>();
                                        for (FirebaseVisionLabel label: labels) {
                                            String name = label.getLabel();
                                            Float confidence = label.getConfidence();
                                            confidence = confidence * 100;
                                            String confidenceStr = String.format ("%.0f", confidence);

                                            if (text.isEmpty()) {
                                                text = name + " : " + confidenceStr + "%\n";
                                            } else {
                                                text = text + name + " : " + confidenceStr + "%\n";
                                            }

                                        }

                                        backToCameraMode();

                                        // show prediction with dialog box
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setTitle("Predictions are:")
                                                .setMessage(text)
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                })
                                                .show();



                                        /*
                                        Timber.d(text);
                                        mPrediction.setText(text);
                                        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
                                        */
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        hideProgressBar();
                                        backToCameraMode();
                                    }
                                });

    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    */


    /////////////////////////////////////////////////////////////////////////////////////////////////////////



}