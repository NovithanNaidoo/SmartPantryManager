package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows which recipes the user can cook with what they already have.
 *
 * This screen does no matching itself. It asks RecipeMatcher for the answer and
 * displays it. Keeping the rule in its own class means this file stays about
 * screen layout, and the rule can be read and explained on its own.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper databaseHelper;

    private RecipeAdapter makeableAdapter;
    private RecipeAdapter almostAdapter;

    private TextView textEmptyState;
    private TextView textSummary;
    private TextView headerMakeable;
    private RecyclerView recyclerMakeable;
    private View sectionAlmost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        // No back arrow here. This screen is reached from the navigation bar, not
        // opened on top of another screen, so a back arrow would be misleading.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavHelper.setup(this, R.id.nav_suggested);

        databaseHelper = new DatabaseHelper(this);

        textEmptyState = findViewById(R.id.textEmptyState);
        textSummary = findViewById(R.id.textSummary);
        headerMakeable = findViewById(R.id.headerMakeable);
        recyclerMakeable = findViewById(R.id.recyclerMakeable);
        sectionAlmost = findViewById(R.id.sectionAlmost);

        RecyclerView recyclerAlmost = findViewById(R.id.recyclerAlmost);

        makeableAdapter = new RecipeAdapter(this);
        recyclerMakeable.setLayoutManager(new LinearLayoutManager(this));
        recyclerMakeable.setAdapter(makeableAdapter);

        almostAdapter = new RecipeAdapter(this);
        recyclerAlmost.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlmost.setAdapter(almostAdapter);
    }

    /**
     * Recalculated every time the screen appears, so changing the pantry and
     * coming back here always shows an up to date answer.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    private void refreshSuggestions() {
        List<PantryItem> pantry = databaseHelper.getAllPantryItems();
        List<Recipe> allRecipes = databaseHelper.getAllRecipes();

        // The strict rule. Only recipes with every ingredient present end up here.
        List<Recipe> makeable = RecipeMatcher.findMakeable(allRecipes, pantry);

        // The bonus list, worked out separately and shown separately.
        List<Recipe> almost = RecipeMatcher.findAlmostThere(allRecipes, pantry);

        showMakeable(makeable, allRecipes.size(), pantry.isEmpty());
        showAlmost(almost, pantry);
    }

    /** Fills the main list, or shows a message explaining why it is empty. */
    private void showMakeable(List<Recipe> makeable, int totalRecipes, boolean pantryIsEmpty) {
        List<RecipeAdapter.Row> rows = new ArrayList<>();

        for (Recipe recipe : makeable) {
            String subtitle = getString(R.string.recipe_ingredient_count,
                    recipe.getIngredientCount());
            rows.add(new RecipeAdapter.Row(recipe, subtitle));
        }

        makeableAdapter.setRows(rows);

        boolean nothingToShow = rows.isEmpty();

        // The brief asks for real feedback rather than a blank screen, and the
        // reason for an empty list matters. An empty pantry needs different advice
        // from a full pantry that simply does not complete any recipe yet.
        if (nothingToShow) {
            textEmptyState.setText(pantryIsEmpty
                    ? R.string.empty_suggested_pantry
                    : R.string.empty_suggested_none);
        }

        textEmptyState.setVisibility(nothingToShow ? View.VISIBLE : View.GONE);
        headerMakeable.setVisibility(nothingToShow ? View.GONE : View.VISIBLE);
        textSummary.setVisibility(nothingToShow ? View.GONE : View.VISIBLE);
        recyclerMakeable.setVisibility(nothingToShow ? View.GONE : View.VISIBLE);

        textSummary.setText(getString(R.string.suggested_summary, makeable.size(), totalRecipes));
    }

    /** Fills the almost-there list, or hides that whole section. */
    private void showAlmost(List<Recipe> almost, List<PantryItem> pantry) {
        List<RecipeAdapter.Row> rows = new ArrayList<>();

        for (Recipe recipe : almost) {
            List<String> missing = RecipeMatcher.findMissingIngredients(recipe, pantry);

            // There is exactly one missing item by definition, but guard anyway
            // rather than assuming and risking a crash.
            String missingName = missing.isEmpty() ? "" : missing.get(0);

            rows.add(new RecipeAdapter.Row(recipe,
                    getString(R.string.recipe_missing, missingName)));
        }

        almostAdapter.setRows(rows);
        sectionAlmost.setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /** Opens the detail screen, passing only the recipe id in the Intent. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (isFinishing() && databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
