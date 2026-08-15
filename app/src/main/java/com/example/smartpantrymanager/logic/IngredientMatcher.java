package com.example.smartpantrymanager.logic;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Tidies up ingredient names and units so they can be compared fairly.
 *
 * People do not type things consistently. A recipe says "tomato" and the user
 * types "Tomatoes". A recipe wants 500 g and the user has 1 kg. Comparing those
 * as plain text would fail, even though the user clearly has what they need.
 *
 * This class handles that clean-up. It is kept separate from the matching rule
 * itself so each part does one job and can be explained on its own.
 */
public final class IngredientMatcher {

    /** Groups of units that can be compared with each other. */
    public enum UnitType {
        MASS,     // g, kg
        VOLUME,   // ml, l, tsp, tbsp, cup
        COUNT,    // piece
        UNKNOWN
    }

    private IngredientMatcher() {
        // Utility class, never instantiated.
    }

    /**
     * Turns an ingredient name into a standard form for comparing.
     *
     * "Tomatoes " and "tomato" both become "tomato", so they match.
     */
    @NonNull
    public static String normaliseName(String rawName) {
        if (rawName == null) {
            return "";
        }

        String name = rawName.trim().toLowerCase(Locale.UK);

        // Collapse double spaces so "olive  oil" matches "olive oil".
        name = name.replaceAll("\\s+", " ");

        return singular(name);
    }

    /**
     * Removes a simple plural ending.
     *
     * This is not a full grammar engine and does not need to be. It handles the
     * everyday cases that actually appear in a kitchen: tomatoes, potatoes,
     * berries, eggs, carrots.
     */
    @NonNull
    private static String singular(@NonNull String word) {
        // Too short to safely strip anything. "peas" is fine, "gas" is not,
        // so very short words are left alone.
        if (word.length() <= 3) {
            return word;
        }

        // berries -> berry, pastries -> pastry
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        }

        // tomatoes -> tomato, potatoes -> potato
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);
        }

        // dishes -> dish, boxes -> box, peaches -> peach
        if (word.endsWith("shes") || word.endsWith("ches") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);
        }

        // Words that genuinely end in "ss" are not plurals, so leave them.
        if (word.endsWith("ss")) {
            return word;
        }

        // eggs -> egg, carrots -> carrot
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        }

        return word;
    }

    /**
     * Says which family a unit belongs to.
     *
     * Grams and millilitres both convert to a number, but comparing them would be
     * meaningless, so the type is checked before any comparison happens.
     */
    @NonNull
    public static UnitType typeOf(String rawUnit) {
        String unit = rawUnit == null ? "" : rawUnit.trim().toLowerCase(Locale.UK);

        switch (unit) {
            case "g":
            case "gram":
            case "grams":
            case "kg":
            case "kilogram":
            case "kilograms":
                return UnitType.MASS;

            case "ml":
            case "millilitre":
            case "millilitres":
            case "l":
            case "litre":
            case "litres":
            case "tsp":
            case "teaspoon":
            case "teaspoons":
            case "tbsp":
            case "tablespoon":
            case "tablespoons":
            case "cup":
            case "cups":
                return UnitType.VOLUME;

            case "piece":
            case "pieces":
            case "":
                return UnitType.COUNT;

            default:
                return UnitType.UNKNOWN;
        }
    }

    /**
     * Converts a quantity into the base unit for its family, so two amounts can
     * be compared as plain numbers.
     *
     * Mass converts to grams, volume to millilitres, count stays as it is.
     * So 1 kg becomes 1000 and 2 tbsp becomes 30.
     */
    public static double toBaseAmount(double quantity, String rawUnit) {
        String unit = rawUnit == null ? "" : rawUnit.trim().toLowerCase(Locale.UK);

        switch (unit) {
            case "kg":
            case "kilogram":
            case "kilograms":
                return quantity * 1000;

            case "l":
            case "litre":
            case "litres":
                return quantity * 1000;

            case "tsp":
            case "teaspoon":
            case "teaspoons":
                return quantity * 5;

            case "tbsp":
            case "tablespoon":
            case "tablespoons":
                return quantity * 15;

            case "cup":
            case "cups":
                return quantity * 250;

            // g, ml, piece and anything unrecognised are already the base amount.
            default:
                return quantity;
        }
    }

    /**
     * Checks whether the amount held covers the amount needed.
     *
     * If the two units are from different families there is no honest way to
     * compare them, so the check falls back to "the ingredient is at least
     * present". This is a deliberate trade-off: butter is sold in grams but
     * recipes often ask for tablespoons, and refusing to match those would be
     * more wrong than allowing it.
     */
    public static boolean hasEnough(double heldQuantity, String heldUnit,
                                    double neededQuantity, String neededUnit) {
        UnitType heldType = typeOf(heldUnit);
        UnitType neededType = typeOf(neededUnit);

        if (heldType != neededType || heldType == UnitType.UNKNOWN) {
            return true;
        }

        double held = toBaseAmount(heldQuantity, heldUnit);
        double needed = toBaseAmount(neededQuantity, neededUnit);

        // Tiny tolerance so floating point rounding cannot fail an exact match.
        return held >= needed - 0.0001;
    }
}
