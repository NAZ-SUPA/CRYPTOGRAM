/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Android database utility imports.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper:
 * - Manages the SQLite database for storing game levels.
 * - Handles table creation, initial data insertion, and data retrieval.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // --- DATABASE CONSTANTS ---
    // The name of the SQLite database file.
    private static final String DATABASE_NAME = "CryptogramDB";
    // Current version of the database; used for migration logic.
    private static final int DATABASE_VERSION = 1;
    // Table name for storing levels.
    private static final String TABLE_LEVELS = "levels";
    // Column name for the unique level identifier.
    private static final String COLUMN_ID = "id";
    // Column name for the text sentence to be decrypted.
    private static final String COLUMN_SENTENCE = "sentence";

    /**
     * Constructor for DatabaseHelper.
     * @param context The application context used to locate the database.
     */
    public DatabaseHelper(Context context) {
        // Initialize the superclass with DB name and version.
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Triggered automatically when the database is first created.
     * @param db The SQLiteDatabase object.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create the levels table with an autoincrementing ID.
        String createTable = "CREATE TABLE " + TABLE_LEVELS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SENTENCE + " TEXT)";
        // Execute the creation command.
        db.execSQL(createTable);

        // Populate the database with default levels.
        insertInitialLevels(db);
    }

    /**
     * Triggered when the DATABASE_VERSION is incremented.
     * @param db The SQLiteDatabase object.
     * @param oldVersion The previous version number.
     * @param newVersion The new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple upgrade strategy: drop the old table and recreate it.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEVELS);
        onCreate(db);
    }

    /**
     * Inserts a predefined set of levels into the database.
     * @param db The SQLiteDatabase object.
     */
    private void insertInitialLevels(SQLiteDatabase db) {
        insertLevel(db, "EVERYONE HAS A WEAK SPOT");
        insertLevel(db, "WHATEVER YOU BELIEVE YOU CAN ACHIEVE");
        insertLevel(db, "THERE IS NO LOGIC IN LOVE");
        insertLevel(db, "IF IT WORKS PLEASE DON'T TOUCH IT");
        insertLevel(db, "CAT GOT YOUR TONGUE");
        insertLevel(db, "CURIOSITY KILLED THE CAT");
        insertLevel(db, "SPEAK TO THE DEVIL");
        insertLevel(db, "LET BYGONE BE BYGONE");
        insertLevel(db, "TIME IS MONEY");
        insertLevel(db, "PEAKS ARE DESERVE TO TRY FOR");
    }

    /**
     * Helper method to insert a single sentence into the levels table.
     * @param db The SQLiteDatabase object.
     * @param sentence The text of the level.
     */
    private void insertLevel(SQLiteDatabase db, String sentence) {
        // Prepare key-value pairs for the insertion.
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENTENCE, sentence);
        // Insert the row into the database.
        db.insert(TABLE_LEVELS, null, values);
    }

    /**
     * Retrieves the sentence text for a specific level ID.
     * @param levelId The ID of the level to fetch.
     * @return The sentence string.
     */
    public String getSentenceForLevel(int levelId) {
        // Open a readable connection to the database.
        SQLiteDatabase db = this.getReadableDatabase();
        String sentence = "";

        // Query the table for the sentence where the ID matches the provided levelId.
        Cursor cursor = db.query(TABLE_LEVELS, new String[]{COLUMN_SENTENCE},
                COLUMN_ID + "=?", new String[]{String.valueOf(levelId)},
                null, null, null);

        // Iterate through the results and extract the sentence.
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                sentence = cursor.getString(0);
            }
            // Always close the cursor to free resources.
            cursor.close();
        }
        // Close the database connection.
        db.close();
        return sentence;
    }

    /**
     * Counts the total number of levels available in the database.
     * @return Total count of rows in the levels table.
     */
    public int getTotalLevelsCount() {
        // Open a readable connection to the database.
        SQLiteDatabase db = this.getReadableDatabase();
        // Execute a raw query to select all rows.
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LEVELS, null);
        // Get the count from the cursor.
        int count = cursor.getCount();
        // Close resources.
        cursor.close();
        db.close();
        return count;
    }
}
