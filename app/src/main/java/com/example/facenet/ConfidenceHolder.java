package com.example.facenet;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ConfidenceHolder extends RecyclerView.ViewHolder {
    TextView class_name;
    TextView dist;

    public ConfidenceHolder(@NonNull View itemView) {
        super(itemView);
        class_name = itemView.findViewById(R.id.class_name);
        dist = itemView.findViewById(R.id.dist);
    }
}
