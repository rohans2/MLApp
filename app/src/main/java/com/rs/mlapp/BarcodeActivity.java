package com.rs.mlapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.rs.mlapp.helpers.MyHelper;

import java.util.List;

import static com.rs.mlapp.BaseActivity.RC_SELECT_PICTURE;
import static com.rs.mlapp.BaseActivity.RC_STORAGE_PERMS1;
import static com.rs.mlapp.BaseActivity.RC_STORAGE_PERMS2;
import static com.rs.mlapp.BaseActivity.RC_TAKE_PICTURE;

public class BarcodeActivity extends BaseActivity {

    private ImageView mImageView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        mImageView = findViewById(R.id.image_view);
        mTextView = findViewById(R.id.text_view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_STORAGE_PERMS1:
                case RC_STORAGE_PERMS2:
                    checkStoragePermission(requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath(this, dataUri);
                    if (path == null) {
                        bitmap = MyHelper.resizeImage(imageFile, this, dataUri, mImageView);
                    } else {
                        bitmap = MyHelper.resizeImage(imageFile, path, mImageView);
                    }
                    if (bitmap != null) {
                        mTextView.setText(null);
                        mImageView.setImageBitmap(bitmap);
                        barcodeDetector(bitmap);
                    }
                    break;
                case RC_TAKE_PICTURE:
                    bitmap = MyHelper.resizeImage(imageFile, imageFile.getPath(), mImageView);
                    if (bitmap != null) {
                        mTextView.setText(null);
                        mImageView.setImageBitmap(bitmap);
                        barcodeDetector(bitmap);
                    }
                    break;
            }
        }
    }

    private void barcodeDetector(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                mTextView.setText(getInfoFromBarcode(firebaseVisionBarcodes));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mTextView.setText(R.string.error_detect);
            }
        });
    }

    private String getInfoFromBarcode(List<FirebaseVisionBarcode> barcodes) {
        StringBuilder result = new StringBuilder();
        for (FirebaseVisionBarcode barcode : barcodes) {
            result.append(barcode.getRawValue() + "\n");


        }
        if ("".equals(result.toString())) {
            return getString(R.string.error_detect);
        } else {
            return result.toString();
        }
    }
}