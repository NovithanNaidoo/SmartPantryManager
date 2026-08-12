package com.example.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A single recipe: its name, the ingredients it requires, and how to cook it.
 *
 * <p>A Recipe corresponds to one row in the {@code recipes} table, plus several rows
 * in the {@code recipe_ingredients} table. When the database helper loads a recipe it
 * reads the recipe row first, then fetches that recipe's ingredients and attaches them
 * to the {@link #ingredients} list here.</p>
 *
 * <p>Because a Recipe carries its own ingredient list in memory, the matching
 * algorithm can check a recipe against the pantry without going back to the database
 * for every single ingredient.</p>
 */
public class Recipe {

    private long id;
    private String name;
    private String steps;
    private final List<RecipeIngredient> ingredients;

    /**
     * Creates a recipe with an empty ingredient list.
     * Ingredients are added afterwards using {@link #addIngredient(RecipeIngredient)}.
     *
     * @param id    the primary key from the database
     * @param name  the recipe name, e.g. "Tomato Pasta"
     * @param steps the preparation method, with each step on its own line
     */
    public Recipe(long id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.ingredients = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the preparation method. Steps are stored as a single string with
     *         newline characters between them, which keeps the database simple —
     *         the order of steps matters and is preserved naturally by the text
     *         itself, so a separate table would add complexity for no benefit.
     */
    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    /**
     * @return the ingredients this recipe requires. The list is returned directly
     *         rather than copied, so callers should treat it as read-only unless
     *         they intend to modify the recipe.
     */
    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }

    /**
     * @return how many ingredients this recipe needs. Used on the suggestions screen
     *         to show a short summary such as "5 ingredients".
     */
    public int getIngredientCount() {
        return ingredients.size();
    }
}
