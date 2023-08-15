package com.example.facenet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AddDataActivity extends AppCompatActivity {

    Database database = Database.getInstance();
    ImageView imageView;
    TextView result;
    Button back, finish;
    AutoCompleteTextView label_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        imageView = findViewById(R.id.imageView);
        result = findViewById(R.id.result);
        back = findViewById(R.id.button_back);
        finish = findViewById(R.id.button_finish);
        label_text = findViewById(R.id.label_text);

        List<String> hints = new ArrayList<>();
        for(String hint : database.getFeatVectors().keySet()) hints.add(hint);
        label_text.setAdapter(new ArrayAdapter<String>(AddDataActivity.this, android.R.layout.simple_list_item_1, hints));

        Intent intent = getIntent();
        byte[] bytes = intent.getExtras().getByteArray("image");
        String class_name = intent.getExtras().getString("class_name");
        float[] feat = intent.getExtras().getFloatArray("feat");

        imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        result.setText(class_name);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.putPerson(label_text.getText().toString(), feat);
                finish();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}