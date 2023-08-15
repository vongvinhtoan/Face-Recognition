package com.example.facenet;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConfidenceAdapter extends RecyclerView.Adapter<ConfidenceHolder> {
    Context context;
    List<ConfidenceItem> items;

    public ConfidenceAdapter(Context context, List<ConfidenceItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ConfidenceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.confidence_item_layout, parent, false);
        return new ConfidenceHolder(view);
    }

    private float[] pal(float t) {
        t = (float) ((3*t*t - 2*t*t*t) * Math.pow(t, 0.5));
        float[] a = {0.500F, 0.500F, 0.000F};
        float[] b = {0.138F, 0.268F, 0.000F};
        float[] c = {0.500F, 0.500F, 0.000F};
        float[] d = {0.000F, 0.500F, 0.000F};
        float[] res = {
                (float) (a[0] + b[0] * Math.cos(6.28318*(c[0]*t+d[0]))),
                (float) (a[1] + b[1] * Math.cos(6.28318*(c[1]*t+d[1]))),
                (float) (a[2] + b[2] * Math.cos(6.28318*(c[2]*t+d[2])))
        };
        return res;
    }

    @Override
    public void onBindViewHolder(@NonNull ConfidenceHolder holder, int position) {
        ConfidenceItem item = items.get(position);
        holder.class_name.setText(item.getClass_name());
        float progress = item.getDist();
        holder.indicator.setText(String.format("%.1f%%", progress * 100));

        float[] color = pal(progress);
        int[] intColor = {
                (int) (color[0]*255),
                (int) (color[1]*255),
                (int) (color[2]*255)
        };
        holder.similarity.getProgressDrawable().setColorFilter(
                Color.rgb(intColor[0], intColor[1], intColor[2]),
                PorterDuff.Mode.SRC_IN
        );
        holder.similarity.setProgress((int) (progress * 100));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setList(List<ConfidenceItem> confidences) {
        items = confidences;
    }

    public List<ConfidenceItem> getItems() {
        return items;
    }
}
