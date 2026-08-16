package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
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
 * Lists every recipe in the app, cookable or not.
 *
 * The suggestions screen deliberately hides recipes the user cannot make, which
 * means a new user with an empty pantry sees nothing at all. This screen exists
 * so the cookbook is always visible, and each row says how close the user is.
 */
public class AllRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper databaseHelper;
    private RecipeAdapter adapter;
    private TextView textSummary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recipes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHelper = new DatabaseHelper(this);
        textSummary = findViewById(R.id.textSummary);

        RecyclerView recycler = findViewById(R.id.recyclerAllRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // The same adapter used on the suggestions screen. Only the subtitle text
        // differs, and that is passed in, so no new adapter is needed.
        adapter = new RecipeAdapter(this);
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavHelper.setup(this, R.id.nav_recipes);
        loadRecipes();
    }

    private void loadRecipes() {
        List<Recipe> allRecipes = databaseHelper.getAllRecipes();
        List<PantryItem> pantry = databaseHelper.getAllPantryItems();

        List<RecipeAdapter.Row> rows = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            int missing = RecipeMatcher.findMissingIngredients(recipe, pantry).size();

            // Say "ready to cook" when nothing is missing, otherwise show how far off
            // the user is. This turns the browse list into a quick progress view.
            String subtitle = (missing == 0)
                    ? getString(R.string.recipe_ready)
                    : getString(R.string.recipe_needs, recipe.getIngredientCount(), missing);

            rows.add(new RecipeAdapter.Row(recipe, subtitle));
        }

        adapter.setRows(rows);
        textSummary.setText(getString(R.string.all_recipes_summary, allRecipes.size()));
    }

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
