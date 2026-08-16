package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.data.AppPreferences;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * The app's main screen: everything currently in the user's pantry.
 *
 * <p>This is the first screen the user sees, and it is where the Read part of CRUD
 * happens — it queries the database and hands the results to a
 * {@link PantryAdapter} for display.</p>
 *
 * <h3>Why the data is loaded in onResume rather than onCreate</h3>
 *
 * <p>{@code onCreate} runs once, when the Activity is first built. {@code onResume}
 * runs every time the screen comes to the foreground — including when the user
 * returns from the Add or Edit screen, and when they reopen the app after leaving
 * it.</p>
 *
 * <p>Loading in onCreate would mean a newly added ingredient did not appear until the
 * app was restarted, because the list would still be holding the data it fetched when
 * the screen was first created. Loading in onResume guarantees the list always
 * reflects what is actually in the database.</p>
 */
public class PantryListActivity extends AppCompatActivity
        implements PantryAdapter.OnItemClickListener {

    private DatabaseHelper databaseHelper;
    private AppPreferences preferences;
    private PantryAdapter adapter;

    private RecyclerView recyclerPantry;
    private View layoutEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        // The theme has no built-in action bar, so the Material toolbar declared in
        // the layout is registered as this Activity's app bar. Doing it this way
        // gives full control over the toolbar's appearance and contents.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHelper = new DatabaseHelper(this);
        preferences = new AppPreferences(this);

        recyclerPantry = findViewById(R.id.recyclerPantry);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        // A LayoutManager decides how rows are arranged. LinearLayoutManager stacks
        // them vertically, which is the ordinary list appearance. Without one, the
        // RecyclerView has no idea where to position anything and shows nothing at
        // all — a common and confusing first mistake.
        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PantryAdapter(this);
        recyclerPantry.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> openAddScreen());
    }

    /** Puts the Clear all option into the toolbar overflow menu. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pantry_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_all) {
            confirmClearAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Asks before emptying the pantry.
     *
     * The item count is shown in the message so the user knows exactly how much
     * they are about to lose. There is no undo, so the confirmation matters.
     */
    private void confirmClearAll() {
        int count = databaseHelper.getPantryItemCount();

        // Nothing to clear, so say so rather than showing a pointless dialog.
        if (count == 0) {
            Toast.makeText(this, R.string.clear_all_already_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_all_title)
                .setMessage(getString(R.string.clear_all_message, count))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.action_clear_all, (dialog, which) -> {
                    databaseHelper.deleteAllPantryItems();
                    loadPantryItems();
                    Toast.makeText(this, R.string.clear_all_done, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /**
     * Opens the form with no id attached, which puts it in "add" mode.
     *
     * An Intent is the message used to start another screen. Here it just names
     * the Activity to open, with no extra data.
     */
    private void openAddScreen() {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        startActivity(intent);
    }

    /**
     * Refreshes the list every time this screen becomes visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavHelper.setup(this, R.id.nav_pantry);
        loadPantryItems();
    }

    /**
     * Reads every pantry item from the database and shows either the list or the
     * empty-state message.
     */
    private void loadPantryItems() {
        // Settings are re-read here, so returning from the Settings screen shows
        // the change straight away.
        adapter.setExpiryHighlight(
                preferences.isExpiryAlertsEnabled(),
                preferences.getExpiryAlertDays());

        List<PantryItem> items = databaseHelper.getAllPantryItems();
        adapter.setItems(items);

        // Exactly one of these two views is visible at any moment. The brief requires
        // meaningful feedback instead of a blank screen when there is nothing to show.
        boolean isEmpty = items.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerPantry.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Called by the adapter when a row is tapped. Opens the same form in "edit"
     * mode by attaching the tapped item's id to the Intent.
     *
     * Only the id is passed, not the whole item. The edit screen loads the current
     * values from the database itself, so it can never show stale data.
     */
    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    /**
     * Closes the database connection when the Activity is destroyed for good.
     *
     * <p>{@code isFinishing()} distinguishes the Activity genuinely closing from it
     * being destroyed and immediately recreated, which is what happens on a screen
     * rotation. Closing the database during a rotation would leave the recreated
     * screen with a closed connection.</p>
     */
    @Override
    protected void onDestroy() {
        if (isFinishing() && databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
