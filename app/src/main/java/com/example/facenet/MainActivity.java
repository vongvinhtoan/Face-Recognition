package com.example.facenet;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_TAKE_PIC = 1;
    private static final int REQUEST_CAMERA_FEATURE = 2;
    TextView result;
    RecyclerView confidence;
    ImageView imageView;
    Button takePicture, addPerson;
    EditText nameInput;
    int imageSize = 160;
    HashMap<String, float[]> featVectors;
    ConfidenceAdapter confidenceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        takePicture = findViewById(R.id.button_take_picture);
        addPerson = findViewById(R.id.button_add_person);
        featVectors = new HashMap<>();

        LinearLayoutManager layoutManager= new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        confidence.setLayoutManager(layoutManager);
        confidenceAdapter = new ConfidenceAdapter(this, new ArrayList<>());
        confidence.setAdapter(confidenceAdapter);

        initData();

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch camera if we have permission
                if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, REQUEST_TAKE_PIC);
                } else {
                    //Request camera permission if we don't have it.
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_FEATURE);
                }
            }
        });
    }

    private void initData() {
        featVectors.put("Nghiêm", imageToFeatVector(BitmapFactory.decodeResource(getResources(), R.drawable.nghiem_image)));
        featVectors.put("Phúc", imageToFeatVector(BitmapFactory.decodeResource(getResources(), R.drawable.phuc_image)));
        featVectors.put("Toàn", imageToFeatVector(BitmapFactory.decodeResource(getResources(), R.drawable.toan_image)));
        featVectors.put("Diệp Mai", imageToFeatVector(BitmapFactory.decodeResource(getResources(), R.drawable.mai_image)));
    }

    private Bitmap stripImage(Bitmap image) {
        int dimension = Math.min(image.getWidth(), image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
        return image;
    }

    private float[] imageToFeatVector(Bitmap image) {
        return new float[3];
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_TAKE_PIC) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                image = stripImage(image);
                imageView.setImageBitmap(image);

                ClassifiedItem item = classify(image);

                result.setText(item.getClassName());
                confidenceAdapter.setList(item.getConfidences());
                confidenceAdapter.notifyDataSetChanged();
            }
        }
    }

    private ClassifiedItem classify(Bitmap image) {
        float[] feat = imageToFeatVector(image);
        List<ConfidenceItem> confidences = new ArrayList<>();
        for (String class_name : featVectors.keySet()) {
            confidences.add(new ConfidenceItem(class_name, L2Dist(feat, featVectors.get(class_name))));
        }
        confidences.sort(new Comparator<ConfidenceItem>() {
            @Override
            public int compare(ConfidenceItem o1, ConfidenceItem o2) {
                float delta = o2.getDist() - o1.getDist();
                if(delta > 0) return 1;
                if(delta < 0) return -1;
                return 0;
            }
        });
        Log.i(TAG, "classify: " + String.valueOf(confidences.size()));
        return new ClassifiedItem(confidences.get(0).getClass_name(), confidences);
    }

    private float L2Dist(float[] a, float[] b) {
        float res = 0;
        float a_nor = 0;
        for(int i =0; i <a.length;i++) a_nor += a[i]*a[i];
        a_nor = (float)Math.pow(a_nor, 0.5);
        float b_nor = 0;
        for(int i =0; i <b.length;i++) b_nor += b[i]*b[i];
        b_nor = (float)Math.pow(b_nor, 0.5);
        for (int i=0; i<a.length; i++) res += Math.pow(a[i]/a_nor - b[i]/b_nor, 2);
        res = (float) Math.pow(res, 0.5);
        return res;
    }
}