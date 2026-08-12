package com.example.smartpantrymanager.model;

/**
 * Represents a single ingredient the user currently has at home.
 *
 * <p>This is a plain Java object (sometimes called a POJO or a model class). It holds
 * data and nothing else — it does not know about the database, the screen, or how it
 * will be displayed. Keeping it that simple means the same object can be created from
 * a database row, passed between Activities, and read by the matching algorithm
 * without any of those parts needing to know about each other.</p>
 *
 * <p>One PantryItem corresponds to one row in the {@code pantry_items} table.</p>
 */
public class PantryItem {

    /**
     * Database primary key. SQLite generates this automatically when a row is
     * inserted, so a brand new item that has not been saved yet uses
     * {@link #NOT_SAVED} instead.
     */
    public static final long NOT_SAVED = -1;

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate;

    /**
     * Creates an item that has not yet been saved to the database.
     * Used by the Add Ingredient screen, before an id exists.
     */
    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this(NOT_SAVED, name, quantity, unit, expiryDate);
    }

    /**
     * Creates an item with a known database id.
     * Used when reading existing rows back out of the database.
     *
     * @param id         the primary key from the database
     * @param name       the ingredient name as the user typed it, e.g. "Tomatoes"
     * @param quantity   how much the user has, e.g. 500
     * @param unit       the unit that quantity is measured in, e.g. "g"
     * @param expiryDate expiry in "yyyy-MM-dd" format, or null if the user left it blank
     */
    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
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
     * @return the expiry date as "yyyy-MM-dd", or null if the user did not set one.
     *         Expiry is optional, so null is a normal value here and callers must
     *         check for it.
     */
    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * @return true if this item has never been written to the database
     */
    public boolean isNew() {
        return id == NOT_SAVED;
    }

    /**
     * Builds the quantity and unit into one readable string for display in a list,
     * for example "500 g" or "2 pieces".
     *
     * <p>Whole numbers are shown without a trailing ".0", because "2 eggs" reads
     * better than "2.0 eggs".</p>
     */
    public String getDisplayQuantity() {
        if (quantity == Math.floor(quantity)) {
            return String.format(java.util.Locale.UK, "%.0f %s", quantity, unit);
        }
        return String.format(java.util.Locale.UK, "%s %s", quantity, unit);
    }
}
