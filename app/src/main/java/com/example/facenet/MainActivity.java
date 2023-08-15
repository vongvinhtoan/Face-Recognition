package com.example.facenet;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.facenet.ml.Facenet512;
import com.google.gson.Gson;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import android.util.Base64;
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
    ConfidenceAdapter confidenceAdapter;
    Database database;
    boolean took_picture;
    ClassifiedItem classifiedItem = null;
    private float acceptedPercent = .8f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        took_picture = false;

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        takePicture = findViewById(R.id.button_take_picture);
        addPerson = findViewById(R.id.button_add_person);

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

        addPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(took_picture == false) return;
                Intent intent = new Intent(MainActivity.this, AddDataActivity.class);
                Gson gson = new Gson();
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                intent.putExtra("image", bytes);
                intent.putExtra("class_name", result.getText().toString());
                intent.putExtra("feat", classifiedItem.feat);
                intent.putExtra("isStranger", classifiedItem.getConfidences().get(0).getDist() < acceptedPercent);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        database = Database.getInstance();
        database.init(this);
    }

    private Bitmap stripImage(Bitmap image, int imageSize) {
        int dimension = Math.min(image.getWidth(), image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
        if(imageSize > 0) image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
        return image;
    }

    public float[] imageToFeatVector(Bitmap image) {
        image = stripImage(image, imageSize);
        float[] res = null;
        try {
            Facenet512 model = Facenet512.newInstance(this);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int id = 0;
            for(int i=0; i<imageSize; i++) {
                for(int j=0; j<imageSize; j++) {
                    int val = intValues[id++];
                    float normalizer = 1.f / 255.f;
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * normalizer);
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * normalizer);
                    byteBuffer.putFloat(((val) & 0xFF) * normalizer);
                }
            }

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 160, 160, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Facenet512.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            res = outputFeature0.getFloatArray();
            float nor = 0;
            for(float x : res) nor += x*x;
            nor = (float) Math.pow(nor, 0.5f);
            for(int i=0; i<res.length; i++) res[i] /= nor;

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return res;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_TAKE_PIC) {
                took_picture = true;
                Bitmap image = (Bitmap) data.getExtras().get("data");
                image = stripImage(image, -1);
                imageView.setImageBitmap(image);

                classifiedItem = classify(image);

                float conf = classifiedItem.getConfidences().get(0).getDist();
                result.setTextColor(Color.parseColor("#C30000"));
                String class_name = classifiedItem.getClassName();
                if(conf < acceptedPercent) {
                    result.setTextColor(Color.parseColor("#000000"));
                    class_name = "Stranger";
                }

                result.setText(class_name);
                confidenceAdapter.setList(classifiedItem.getConfidences());
                confidenceAdapter.notifyDataSetChanged();
            }
        }
    }

    private ClassifiedItem classify(Bitmap image) {
        float[] feat = imageToFeatVector(image);
        List<ConfidenceItem> confidences = new ArrayList<>();
        Database.ImageHashMap featVectors = database.getFeatVectors();
        for (String class_name : featVectors.keySet()) {
            confidences.add(new ConfidenceItem(class_name, dot(feat, featVectors.get(class_name))));
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
        return new ClassifiedItem(confidences.get(0).getClass_name(), confidences, feat);
    }

    private float dot(float[] a, float[] b) {
        float res = 0;
        for (int i=0; i<a.length; i++) res += a[i] * b[i];
        return res;
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

    @Override
    protected void onStop() {
        database.saveData();
        Log.i(TAG, "onStop: called AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaa");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        database.saveData();
        Log.i(TAG, "onDestroy: called AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaa");
        super.onDestroy();
    }
}