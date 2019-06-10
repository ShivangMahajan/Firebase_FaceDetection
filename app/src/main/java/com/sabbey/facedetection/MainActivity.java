package com.sabbey.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button camera, ok;
    FirebaseVisionFaceDetectorOptions options;
    final int CAMERA = 3;
    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;
    static final String RESULT_TEXT = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.camera_button);
        ok = findViewById(R.id.okButton);

        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build();

        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        onCamera();

    }

    public void onCamera(){

        camera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA && resultCode == RESULT_OK)
        {
            Bundle bundle = data.getExtras();
            Bitmap photo = (Bitmap) bundle.get("data");
            image = FirebaseVisionImage.fromBitmap(photo);

            detector.detectInImage(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<FirebaseVisionFace>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionFace> faces) {
                                    if (faces.size() == 0)
                                    {
                                        Toast.makeText(MainActivity.this, "No face detected", Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                    else
                                    {
                                        String result = "";
                                        int i = 1;
                                        for (FirebaseVisionFace face : faces){
                                             result = result.concat(i + ".\n")
                                                        .concat("Smile:" + face.getSmilingProbability() * 100 + " %\n")
                                                        .concat("Left eye open:" + face.getLeftEyeOpenProbability() * 100 + " %\n")
                                                        .concat("Right eye open:" + face.getRightEyeOpenProbability() * 100 + " %\n");

                                             i++;
                                        }

                                        Bundle bundle = new Bundle();
                                        bundle.putString(RESULT_TEXT, result);
                                        DialogFragment dialogFragment = new ResultFragment();
                                        dialogFragment.setCancelable(false);
                                        dialogFragment.setArguments(bundle);
                                        dialogFragment.show(getSupportFragmentManager(), "Result");


                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });


        }
    }

}
