package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of pantry items inside a {@link RecyclerView}.
 *
 * <h3>What an adapter is for</h3>
 *
 * <p>A RecyclerView knows how to scroll and how to recycle views efficiently, but it
 * knows nothing about pantry items. An adapter is the translator between the two: it
 * answers "how many rows are there?" and "what should row number 7 look like?".</p>
 *
 * <h3>Why it is called a RecyclerView</h3>
 *
 * <p>If the pantry held 500 items, creating 500 row layouts would be slow and would
 * waste a great deal of memory, even though only about eight fit on screen at once.
 * Instead the RecyclerView creates just enough rows to fill the screen and then
 * <em>reuses</em> them: when a row scrolls off the top it is not destroyed, it is
 * handed back with new data and reappears at the bottom.</p>
 *
 * <p>That recycling is exactly why {@link #onBindViewHolder} must set <em>every</em>
 * field on every call. A recycled row still holds the previous item's text, so any
 * field left untouched would show data belonging to a completely different
 * ingredient.</p>
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /**
     * Lets the Activity react to a row being tapped, without the adapter needing to
     * know what should happen.
     *
     * <p>The adapter's job is displaying data. Deciding that a tap should open the
     * edit screen is the Activity's job. This interface keeps that separation: the
     * adapter reports "row tapped", and whoever is listening decides what it means.</p>
     */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemClickListener clickListener;

    public PantryAdapter(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * Replaces the entire list with fresh data and redraws.
     *
     * <p>Called whenever the pantry changes — after adding, editing or deleting an
     * item, and whenever the screen returns to the foreground.</p>
     *
     * <p>{@code notifyDataSetChanged()} tells the RecyclerView that everything may
     * have changed, so it rebuilds all visible rows. A larger app would use DiffUtil
     * to work out precisely which rows changed and animate only those, but for a
     * pantry of a few dozen items the simpler approach is fast enough and far easier
     * to read.</p>
     */
    @SuppressWarnings("NotifyDataSetChanged")
    public void setItems(@NonNull List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Called by the RecyclerView when it needs a new row.
     *
     * <p>This turns the XML in {@code item_pantry.xml} into real View objects, a
     * process called inflating. It happens only a handful of times no matter how long
     * the list is, because rows are then reused.</p>
     */
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(row);
    }

    /**
     * Called every time a row needs to show a particular item — including when a
     * recycled row is being reused for different data.
     *
     * @param holder   the row being filled in
     * @param position which item in the list this row should display
     */
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.textName.setText(item.getName());
        holder.textQuantity.setText(item.getDisplayQuantity());

        // Expiry is optional, so there are two possible states and both must be
        // handled. If this only set the text when an expiry existed, a recycled row
        // would keep showing the previous item's expiry date.
        if (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) {
            holder.textExpiry.setText(R.string.no_expiry);
        } else {
            holder.textExpiry.setText(
                    holder.itemView.getContext().getString(R.string.expires_on, item.getExpiryDate()));
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    /**
     * @return how many rows the list should have. The RecyclerView calls this to work
     *         out how far it can scroll.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Holds references to the views inside one row.
     *
     * <p>Looking a view up with {@code findViewById} is comparatively slow because it
     * searches through the layout tree. Doing that while scrolling, for every field of
     * every row, causes visible stuttering. A ViewHolder performs each lookup once
     * when the row is created and then keeps the references, so binding new data is
     * just a handful of setText calls.</p>
     */
    static class PantryViewHolder extends RecyclerView.ViewHolder {

        final TextView textName;
        final TextView textQuantity;
        final TextView textExpiry;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
        }
    }
}
