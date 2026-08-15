package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a list of recipes.
 *
 * The same adapter is reused for both lists on the suggestions screen. The only
 * difference between them is the small grey line under the recipe name, so that
 * text is passed in as a "subtitle" rather than being worked out here.
 *
 * That keeps the adapter simple: it displays whatever it is given and does not
 * need to know anything about matching rules.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    /** A recipe plus the line of text to show under its name. */
    public static class Row {
        public final Recipe recipe;
        public final String subtitle;

        public Row(Recipe recipe, String subtitle) {
            this.recipe = recipe;
            this.subtitle = subtitle;
        }
    }

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Row> rows = new ArrayList<>();
    private final OnRecipeClickListener clickListener;

    public RecipeAdapter(OnRecipeClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /** Swaps in a new set of rows and redraws the list. */
    @SuppressWarnings("NotifyDataSetChanged")
    public void setRows(@NonNull List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Row row = rows.get(position);

        holder.textName.setText(row.recipe.getName());
        holder.textSubtitle.setText(row.subtitle);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRecipeClick(row.recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        final TextView textName;
        final TextView textSubtitle;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textSubtitle = itemView.findViewById(R.id.textRecipeSubtitle);
        }
    }
}
