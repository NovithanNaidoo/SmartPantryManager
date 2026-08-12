package com.example.smartpantrymanager.model;

/**
 * One ingredient line inside a recipe, for example "200 g flour".
 *
 * <p>This is deliberately a separate class from {@link Recipe}. A recipe needs many
 * ingredients, and a database table cannot sensibly store a list inside a single
 * column. Instead the ingredients live in their own {@code recipe_ingredients} table,
 * where each row points back at its recipe through {@link #getRecipeId()}. That is a
 * one-to-many relationship, and it is the normal way to model "one thing owns several
 * other things" in a relational database.</p>
 *
 * <p>The alternative — stuffing all the ingredients into one text column separated by
 * commas — would make the strict-matching query far harder to write and impossible to
 * search properly.</p>
 *
 * <p>Note this class stores what a recipe <em>requires</em>. Compare it against a
 * {@link PantryItem}, which stores what the user actually <em>has</em>.</p>
 */
public class RecipeIngredient {

    private long id;
    private long recipeId;
    private String name;
    private double quantity;
    private String unit;

    /**
     * Creates an ingredient requirement.
     *
     * @param recipeId the id of the recipe this ingredient belongs to
     * @param name     the ingredient name, e.g. "flour"
     * @param quantity how much the recipe needs, e.g. 200
     * @param unit     the unit that quantity is measured in, e.g. "g"
     */
    public RecipeIngredient(long recipeId, String name, double quantity, String unit) {
        this(PantryItem.NOT_SAVED, recipeId, name, quantity, unit);
    }

    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Formats this requirement for display on the recipe detail screen,
     * for example "200 g flour".
     */
    public String getDisplayText() {
        String amount;
        if (quantity == Math.floor(quantity)) {
            amount = String.format(java.util.Locale.UK, "%.0f", quantity);
        } else {
            amount = String.valueOf(quantity);
        }
        return amount + " " + unit + " " + name;
    }
}
