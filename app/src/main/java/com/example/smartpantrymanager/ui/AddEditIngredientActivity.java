package com.example.smartpantrymanager.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * One screen that handles both adding a new ingredient and editing an existing one.
 *
 * Which mode it is in depends on whether an item id was passed in the Intent.
 * No id means "add". An id means "edit that item".
 *
 * Using one screen for both avoids duplicating the same form and the same
 * validation code twice.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Key for the id passed in through the Intent. */
    public static final String EXTRA_ITEM_ID = "extra_item_id";

    /** Units the user can pick from. A fixed list keeps the data tidy. */
    private static final String[] UNITS = {
            "g", "kg", "ml", "l", "piece", "tsp", "tbsp", "cup"
    };

    private DatabaseHelper databaseHelper;

    // -1 means we are adding. Anything else means we are editing that item.
    private long itemId = PantryItem.NOT_SAVED;

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editName;
    private TextInputEditText editQuantity;
    private TextInputEditText editExpiry;
    private Spinner spinnerUnit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        databaseHelper = new DatabaseHelper(this);

        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);

        setupUnitSpinner();

        // Read the id out of the Intent that opened this screen.
        // getLongExtra returns the default (-1) when the key was never put in.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, PantryItem.NOT_SAVED);
        boolean isEditing = itemId != PantryItem.NOT_SAVED;

        setupToolbar(isEditing);

        if (isEditing) {
            loadExistingItem();
        }

        editExpiry.setOnClickListener(v -> showDatePicker());

        MaterialButton buttonClearDate = findViewById(R.id.buttonClearDate);
        buttonClearDate.setOnClickListener(v -> editExpiry.setText(""));

        MaterialButton buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> saveItem());
    }

    /** Fills the unit dropdown. ArrayAdapter turns a plain array into spinner rows. */
    private void setupUnitSpinner() {
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
    }

    /** Title and back arrow. The title tells the user which mode they are in. */
    private void setupToolbar(boolean isEditing) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(
                    isEditing ? R.string.title_edit_ingredient : R.string.title_add_ingredient);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /** Loads the item being edited and puts its values into the form. */
    private void loadExistingItem() {
        PantryItem item = databaseHelper.getPantryItem(itemId);

        // The item could be missing if it was deleted elsewhere. Fail safely
        // rather than crashing on a null.
        if (item == null) {
            Toast.makeText(this, R.string.deleted_item, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editName.setText(item.getName());
        editQuantity.setText(trimNumber(item.getQuantity()));
        editExpiry.setText(item.getExpiryDate() == null ? "" : item.getExpiryDate());

        // Select the saved unit in the spinner.
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equalsIgnoreCase(item.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }
    }

    /** Shows a calendar so the user does not have to type a date in the right format. */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // month is 0-based in the picker, so January is 0. Add 1 for display.
                    String date = String.format(Locale.UK, "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    editExpiry.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Food cannot expire in the past, so block earlier dates.
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    /**
     * Checks the form, then either inserts a new row or updates an existing one.
     */
    private void saveItem() {
        if (!isFormValid()) {
            return;
        }

        String name = textOf(editName);
        double quantity = parseQuantity(textOf(editQuantity));
        String unit = (String) spinnerUnit.getSelectedItem();

        String expiry = textOf(editExpiry);
        // Store null rather than an empty string, so "no expiry" is one clear value.
        String expiryOrNull = expiry.isEmpty() ? null : expiry;

        if (itemId == PantryItem.NOT_SAVED) {
            PantryItem newItem = new PantryItem(name, quantity, unit, expiryOrNull);
            databaseHelper.addPantryItem(newItem);
        } else {
            PantryItem existing = new PantryItem(itemId, name, quantity, unit, expiryOrNull);
            databaseHelper.updatePantryItem(existing);
        }

        Toast.makeText(this, R.string.saved_item, Toast.LENGTH_SHORT).show();

        // Closing this screen returns to the pantry list, whose onResume reloads
        // the data and shows the change.
        finish();
    }

    /**
     * Validates the form and shows an error under any field that is wrong.
     *
     * Returns false as soon as something fails, but clears old errors first so
     * a message from a previous attempt does not linger.
     */
    private boolean isFormValid() {
        layoutName.setError(null);
        layoutQuantity.setError(null);

        String name = textOf(editName);
        String quantityText = textOf(editQuantity);

        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.error_name_required));
            editName.requestFocus();
            return false;
        }

        if (name.length() < 2) {
            layoutName.setError(getString(R.string.error_name_too_short));
            editName.requestFocus();
            return false;
        }

        if (quantityText.isEmpty()) {
            layoutQuantity.setError(getString(R.string.error_quantity_required));
            editQuantity.requestFocus();
            return false;
        }

        double quantity;
        try {
            quantity = parseQuantity(quantityText);
        } catch (NumberFormatException e) {
            // Someone can still type "1.2.3" or "1/0", so this must be caught.
            layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            editQuantity.requestFocus();
            return false;
        }

        if (quantity <= 0) {
            layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            editQuantity.requestFocus();
            return false;
        }

        if (quantity > 100000) {
            layoutQuantity.setError(getString(R.string.error_quantity_too_large));
            editQuantity.requestFocus();
            return false;
        }

        return true;
    }

    /** The delete option only makes sense when editing, so it is hidden when adding. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (itemId != PantryItem.NOT_SAVED) {
            getMenuInflater().inflate(R.menu.menu_edit_ingredient, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Asks before deleting, because a delete cannot be undone. */
    private void confirmDelete() {
        String name = textOf(editName);

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, name))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    databaseHelper.deletePantryItem(itemId);
                    Toast.makeText(this, R.string.deleted_item, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    /**
     * Turns what the user typed into a number.
     *
     * Recipes are often written in quarters and halves, so plain decimals are not
     * enough. This accepts three shapes:
     *
     *   0.25      a decimal
     *   1/4       a fraction
     *   1 1/2     a whole number and a fraction
     *
     * Throws NumberFormatException if the text is not any of those, which the
     * validation method catches and turns into an error message.
     */
    private double parseQuantity(String text) {
        String value = text.trim().replaceAll("\\s+", " ");

        if (value.isEmpty()) {
            throw new NumberFormatException("empty");
        }

        // "1 1/2" — a whole number, a space, then a fraction.
        if (value.contains(" ")) {
            String[] parts = value.split(" ");
            if (parts.length != 2) {
                throw new NumberFormatException("too many parts");
            }
            return Double.parseDouble(parts[0]) + parseFraction(parts[1]);
        }

        // "1/4"
        if (value.contains("/")) {
            return parseFraction(value);
        }

        // "0.25"
        return Double.parseDouble(value);
    }

    /** Turns "3/4" into 0.75. */
    private double parseFraction(String text) {
        String[] parts = text.split("/");

        if (parts.length != 2) {
            throw new NumberFormatException("bad fraction");
        }

        double top = Double.parseDouble(parts[0]);
        double bottom = Double.parseDouble(parts[1]);

        // Dividing by zero gives Infinity rather than crashing, so block it here.
        if (bottom == 0) {
            throw new NumberFormatException("divide by zero");
        }

        return top / bottom;
    }

    /** Reads an input box as trimmed text, never null. */
    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    /** Shows 2 instead of 2.0, but leaves 1.5 alone. */
    private String trimNumber(double value) {
        if (value == Math.floor(value)) {
            return String.format(Locale.UK, "%.0f", value);
        }
        return String.valueOf(value);
    }

    @Override
    protected void onDestroy() {
        if (isFinishing() && databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
