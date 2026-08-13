package com.example.smartpantrymanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

/**
 * Creates and manages the app's SQLite database.
 *
 * <p>SQLite is built into Android itself, so there is nothing to install and no server
 * to connect to. The database is a single file stored in this app's private storage,
 * which means it survives the app being closed and reopened, and no other app can read
 * it.</p>
 *
 * <p>This class extends {@link SQLiteOpenHelper}, which handles the awkward parts of
 * database lifecycle for us. We only have to answer two questions:</p>
 *
 * <ul>
 *   <li>{@link #onCreate(SQLiteDatabase)} — what tables should exist the very first
 *       time this app runs on a device?</li>
 *   <li>{@link #onUpgrade(SQLiteDatabase, int, int)} — what should happen if the app
 *       updates and the database structure has changed?</li>
 * </ul>
 *
 * <p>Android calls those methods automatically at the right moment. We never call them
 * ourselves.</p>
 *
 * <h3>The three tables</h3>
 *
 * <pre>
 *   pantry_items          what the user currently has at home
 *   recipes               the recipe name and its method
 *   recipe_ingredients    what each recipe requires (many rows per recipe)
 * </pre>
 *
 * <p>Ingredients live in their own table rather than inside the recipes table because
 * a recipe needs many ingredients, and a single database column can only hold one
 * value. Each ingredient row stores the id of the recipe it belongs to, which links
 * the two tables together. This is a one-to-many relationship.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * The filename of the database on the device. Changing this would create a
     * completely separate, empty database.
     */
    private static final String DATABASE_NAME = "smart_pantry.db";

    /**
     * The schema version. This must be increased by 1 whenever the table structure
     * changes, which is what triggers {@link #onUpgrade}. It stays at 1 unless we
     * later add or rename a column.
     */
    private static final int DATABASE_VERSION = 1;

    // ---------------------------------------------------------------------------
    // Table and column names.
    //
    // These are constants rather than strings typed directly into queries. A typo in
    // a constant is caught by the compiler immediately; a typo inside a query string
    // is only discovered when the app crashes at runtime.
    // ---------------------------------------------------------------------------

    /** Table holding the ingredients the user currently has at home. */
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    /** Table holding each recipe's name and preparation method. */
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    /** Table holding the ingredients each recipe requires. */
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    // ---------------------------------------------------------------------------
    // The SQL that builds each table.
    // ---------------------------------------------------------------------------

    /**
     * Creates the pantry table.
     *
     * <p>{@code INTEGER PRIMARY KEY AUTOINCREMENT} tells SQLite to generate a unique
     * id for every row automatically, so we never have to invent one ourselves.</p>
     *
     * <p>{@code NOT NULL} on name, quantity and unit means the database itself will
     * reject a row missing any of them. Our form validates these too, but the database
     * constraint is a second line of defence — validation in the user interface can be
     * bypassed by a bug, whereas the database cannot.</p>
     *
     * <p>{@code expiry_date} deliberately has no NOT NULL constraint, because expiry
     * is optional and blank is a legitimate value. It is stored as text in
     * "yyyy-MM-dd" format, which sorts correctly as plain text — so ordering by expiry
     * needs no date parsing at all.</p>
     */
    private static final String SQL_CREATE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " (" +
                    COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PANTRY_NAME + " TEXT NOT NULL, " +
                    COL_PANTRY_QUANTITY + " REAL NOT NULL, " +
                    COL_PANTRY_UNIT + " TEXT NOT NULL, " +
                    COL_PANTRY_EXPIRY + " TEXT" +
                    ")";

    /**
     * Creates the recipes table.
     *
     * <p>The method is stored as a single TEXT column with newline characters between
     * steps. Cooking steps have an order that matters, and the text preserves that
     * order for free — a separate table would need an extra position column to achieve
     * exactly what a newline already does.</p>
     */
    private static final String SQL_CREATE_RECIPES =
            "CREATE TABLE " + TABLE_RECIPES + " (" +
                    COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RECIPE_NAME + " TEXT NOT NULL, " +
                    COL_RECIPE_STEPS + " TEXT NOT NULL" +
                    ")";

    /**
     * Creates the recipe ingredients table.
     *
     * <p>The {@code FOREIGN KEY} line links each ingredient back to its recipe. It
     * tells SQLite that {@code recipe_id} must match a real id in the recipes table,
     * so an ingredient can never end up orphaned pointing at a recipe that does not
     * exist.</p>
     *
     * <p>{@code ON DELETE CASCADE} means that if a recipe is ever deleted, all of its
     * ingredient rows are automatically deleted with it. Without this we would have to
     * remember to delete them by hand every time, and forgetting once would leave junk
     * rows in the database forever.</p>
     */
    private static final String SQL_CREATE_RECIPE_INGREDIENTS =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                    COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                    COL_RI_NAME + " TEXT NOT NULL, " +
                    COL_RI_QUANTITY + " REAL NOT NULL, " +
                    COL_RI_UNIT + " TEXT NOT NULL, " +
                    "FOREIGN KEY (" + COL_RI_RECIPE_ID + ") REFERENCES " +
                    TABLE_RECIPES + "(" + COL_RECIPE_ID + ") ON DELETE CASCADE" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called by Android the first time the database file is created on a device —
     * in practice, the first time the user opens the app after installing it.
     *
     * <p>This runs exactly once. It will not run again on later launches, which is
     * precisely why the data persists between sessions.</p>
     */
    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PANTRY);
        db.execSQL(SQL_CREATE_RECIPES);
        db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);
    }

    /**
     * Called when {@link #DATABASE_VERSION} is higher than the version stored in the
     * database file on the device — that is, when the app has been updated with a
     * changed table structure.
     *
     * <p>This implementation drops every table and rebuilds them from scratch, which
     * destroys any data the user had. That is acceptable here because this is a
     * student project with no released version and therefore no real users with data
     * to lose. A production app would instead migrate the existing data across, using
     * ALTER TABLE statements to reshape it without throwing it away.</p>
     */
    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    /**
     * Turns on foreign key enforcement every time a connection to the database is
     * opened.
     *
     * <p>SQLite has foreign keys switched off by default for historical
     * compatibility reasons. Without this method the {@code ON DELETE CASCADE} rule
     * declared above would be silently ignored, which is a genuinely easy mistake to
     * make because nothing warns you — the constraint simply does nothing.</p>
     */
    @Override
    public void onConfigure(@NonNull SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
