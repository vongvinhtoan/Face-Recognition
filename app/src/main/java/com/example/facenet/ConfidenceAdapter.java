package com.example.facenet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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

    @Override
    public void onBindViewHolder(@NonNull ConfidenceHolder holder, int position) {
        ConfidenceItem item = items.get(position);
        holder.class_name.setText(item.getClass_name());
        holder.dist.setText(String.valueOf(item.getDist()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setList(List<ConfidenceItem> confidences) {
        items = confidences;
    }
}
