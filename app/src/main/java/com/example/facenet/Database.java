package com.example.facenet;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;

public class Database {
    private static Database database = null;
    public static class ImageHashMap extends HashMap<String, float[]> {}

    private static final String KEY_PREFIX = "image_classification.image_storage.";
    private static final String FEAT_VECTORS = KEY_PREFIX + "FEAT_VECTORS";
    public static SharedPreferences mPreferences;
    public static SharedPreferences.Editor mPreferencesEditor;
    private static Gson gson;
    ImageHashMap featVectors;


    public void init(Context context) {
        mPreferences = context.getSharedPreferences(KEY_PREFIX, Context.MODE_PRIVATE);
        mPreferencesEditor = mPreferences.edit();
        gson = new Gson();

        featVectors = gson.fromJson(mPreferences.getString(FEAT_VECTORS, gson.toJson(new ImageHashMap())), ImageHashMap.class);
        if(featVectors.size() == 0) {
            featVectors.put("Phúc", ((MainActivity) context).imageToFeatVector(BitmapFactory.decodeResource(context.getResources(), R.drawable.phuc_image)));
        }
    }

    public void saveData() {
        mPreferencesEditor.putString(FEAT_VECTORS, gson.toJson(featVectors));
        mPreferencesEditor.apply();
    }

    public ImageHashMap getFeatVectors() {
        return featVectors;
    }

    public void putPerson(String label, float[] feat) {
        featVectors.put(label, feat);
    }

    private Database() {}

    public static synchronized Database getInstance() {
        if (database == null)
            database = new Database();
        return database;
    }

}
