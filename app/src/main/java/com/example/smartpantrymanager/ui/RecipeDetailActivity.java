package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shows one recipe in full: what it needs and how to cook it.
 *
 * Each ingredient gets a tick if the user has it or a cross if they do not, so
 * it is obvious at a glance why a recipe is or is not being suggested.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Key for the recipe id passed in through the Intent. */
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private DatabaseHelper databaseHelper;

    private LinearLayout containerIngredients;
    private LinearLayout containerSteps;
    private TextView textStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        databaseHelper = new DatabaseHelper(this);

        containerIngredients = findViewById(R.id.containerIngredients);
        containerSteps = findViewById(R.id.containerSteps);
        textStatus = findViewById(R.id.textStatus);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = databaseHelper.getRecipe(recipeId);

        // Close cleanly rather than crashing if the id was missing or wrong.
        if (recipe == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(recipe.getName());
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        showRecipe(recipe);
    }

    private void showRecipe(Recipe recipe) {
        List<PantryItem> pantry = databaseHelper.getAllPantryItems();

        // Ask the matcher what is missing, then use that to decide each icon.
        // A Set is used because the only question asked of it is "is this name in
        // here?", which a Set answers instantly.
        Set<String> missingNames = new HashSet<>(
                RecipeMatcher.findMissingIngredients(recipe, pantry));

        showStatus(missingNames.size(), recipe.getIngredientCount());
        showIngredients(recipe, missingNames);
        showSteps(recipe);
    }

    /** The summary banner at the top of the screen. */
    private void showStatus(int missingCount, int totalCount) {
        if (missingCount == 0) {
            textStatus.setText(R.string.detail_have_all);
            textStatus.setBackgroundResource(R.color.green_light);
            textStatus.setTextColor(getColor(R.color.green_dark));
        } else {
            textStatus.setText(getString(R.string.detail_missing_count, missingCount, totalCount));
            textStatus.setBackgroundResource(R.color.white);
            textStatus.setTextColor(getColor(R.color.expiring_soon));
        }
    }

    /**
     * Builds one row per ingredient.
     *
     * A plain loop is used rather than a RecyclerView because the list is short,
     * fixed and does not scroll on its own. Recycling would add complexity for no
     * benefit here.
     */
    private void showIngredients(Recipe recipe, Set<String> missingNames) {
        LayoutInflater inflater = LayoutInflater.from(this);
        containerIngredients.removeAllViews();

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            View row = inflater.inflate(R.layout.item_recipe_ingredient, containerIngredients, false);

            TextView text = row.findViewById(R.id.textIngredient);
            ImageView icon = row.findViewById(R.id.imageStatus);

            text.setText(ingredient.getDisplayText());

            boolean isMissing = missingNames.contains(ingredient.getName());
            icon.setImageResource(isMissing ? R.drawable.ic_missing : R.drawable.ic_check);
            icon.setContentDescription(getString(
                    isMissing ? R.string.missing_ingredient : R.string.have_ingredient));

            containerIngredients.addView(row);
        }
    }

    /**
     * Splits the method into numbered steps.
     *
     * Steps are stored as one block of text with a newline between each, so
     * splitting on the newline gives them back in the right order.
     */
    private void showSteps(Recipe recipe) {
        containerSteps.removeAllViews();

        String[] steps = recipe.getSteps().split("\n");
        int number = 1;

        for (String step : steps) {
            if (step.trim().isEmpty()) {
                continue;
            }

            TextView view = new TextView(this);
            view.setText(getString(R.string.step_format, number, step.trim()));
            view.setTextSize(15);
            view.setTextColor(getColor(R.color.text_primary));
            view.setPadding(0, 0, 0, 16);

            containerSteps.addView(view);
            number++;
        }
    }

    @Override
    protected void onDestroy() {
        if (isFinishing() && databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
