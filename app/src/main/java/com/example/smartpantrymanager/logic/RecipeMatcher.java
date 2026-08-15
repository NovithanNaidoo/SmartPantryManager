package com.example.smartpantrymanager.logic;

import androidx.annotation.NonNull;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Decides which recipes the user can actually cook right now.
 *
 * THE RULE: a recipe is only suggested if EVERY ingredient it needs is in the
 * pantry, in at least the required amount. Four out of five ingredients is not
 * good enough. The recipe is left out.
 *
 * This is the core of the app, so it lives in its own class rather than being
 * buried inside a screen. That keeps it easy to find, easy to read, and means
 * the screens only have to display the result.
 */
public final class RecipeMatcher {

    private RecipeMatcher() {
        // Utility class, never instantiated.
    }

    /**
     * Recipes the user can cook right now, with nothing missing.
     */
    @NonNull
    public static List<Recipe> findMakeable(@NonNull List<Recipe> allRecipes,
                                            @NonNull List<PantryItem> pantry) {
        Map<String, PantryItem> pantryByName = buildPantryLookup(pantry);
        List<Recipe> makeable = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (countMissing(recipe, pantryByName) == 0) {
                makeable.add(recipe);
            }
        }

        return makeable;
    }

    /**
     * Recipes missing exactly one ingredient.
     *
     * This is the optional bonus list from the brief. It is worked out and shown
     * completely separately from the makeable list, so the two can never be
     * confused with each other.
     */
    @NonNull
    public static List<Recipe> findAlmostThere(@NonNull List<Recipe> allRecipes,
                                               @NonNull List<PantryItem> pantry) {
        Map<String, PantryItem> pantryByName = buildPantryLookup(pantry);
        List<Recipe> almost = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (countMissing(recipe, pantryByName) == 1) {
                almost.add(recipe);
            }
        }

        return almost;
    }

    /**
     * The names of whatever a recipe is missing. Used to tell the user what to
     * buy on the "almost there" list.
     */
    @NonNull
    public static List<String> findMissingIngredients(@NonNull Recipe recipe,
                                                      @NonNull List<PantryItem> pantry) {
        Map<String, PantryItem> pantryByName = buildPantryLookup(pantry);
        List<String> missing = new ArrayList<>();

        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!isSatisfied(needed, pantryByName)) {
                missing.add(needed.getName());
            }
        }

        return missing;
    }

    /**
     * Counts how many of a recipe's ingredients the pantry cannot cover.
     *
     * Zero means the recipe is makeable. This single method drives both lists,
     * so the strict rule is written once and cannot drift apart.
     */
    private static int countMissing(@NonNull Recipe recipe,
                                    @NonNull Map<String, PantryItem> pantryByName) {
        int missing = 0;

        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!isSatisfied(needed, pantryByName)) {
                missing++;
            }
        }

        return missing;
    }

    /**
     * Checks one ingredient: is it in the pantry, and is there enough of it?
     *
     * Both halves must pass. Having the right ingredient but not enough of it
     * counts as missing, which is what makes the rule strict about quantity and
     * not just about names.
     */
    private static boolean isSatisfied(@NonNull RecipeIngredient needed,
                                       @NonNull Map<String, PantryItem> pantryByName) {
        String key = IngredientMatcher.normaliseName(needed.getName());

        PantryItem held = pantryByName.get(key);
        if (held == null) {
            return false;
        }

        return IngredientMatcher.hasEnough(
                held.getQuantity(), held.getUnit(),
                needed.getQuantity(), needed.getUnit());
    }

    /**
     * Builds a lookup of pantry items keyed by their tidied-up name.
     *
     * Doing this once per screen load means each ingredient check is an instant
     * map lookup instead of a scan through the whole pantry. With 18 recipes of
     * about 5 ingredients each, that is roughly 90 lookups per refresh.
     *
     * If the user has the same ingredient listed twice, the larger quantity is
     * kept, since that is the one more likely to satisfy a recipe.
     */
    @NonNull
    private static Map<String, PantryItem> buildPantryLookup(@NonNull List<PantryItem> pantry) {
        Map<String, PantryItem> lookup = new HashMap<>();

        for (PantryItem item : pantry) {
            String key = IngredientMatcher.normaliseName(item.getName());

            PantryItem existing = lookup.get(key);
            if (existing == null) {
                lookup.put(key, item);
                continue;
            }

            double existingAmount =
                    IngredientMatcher.toBaseAmount(existing.getQuantity(), existing.getUnit());
            double newAmount =
                    IngredientMatcher.toBaseAmount(item.getQuantity(), item.getUnit());

            if (newAmount > existingAmount) {
                lookup.put(key, item);
            }
        }

        return lookup;
    }
}
