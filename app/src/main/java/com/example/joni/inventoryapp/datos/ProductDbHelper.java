package com.example.joni.inventoryapp.datos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.joni.inventoryapp.datos.ProductContract.ProductEntry;

/**
 * Created by Joni on 23/06/2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    /**
     * Nombre del archivo de base de datos
     */
    private static final String DATABASE_NAME = "myInventory.db";

    /**
     * Versión de base de datos. Si cambia el esquema de la base de datos, debe incrementar la
     * versión de la base de datos.
     */
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Esto se llama cuando la base de datos se crea por primera vez.
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Cree una cadena que contenga la instrucción SQL para crear la tabla de productos.
        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_IMAGE_PRODUCT + " TEXT NOT NULL DEFAULT 'no image', "
                + ProductEntry.COLUMN_NAME_PRODUCT + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRICE_PRODUCT + " REAL NOT NULL, "
                + ProductEntry.COLUMN_PROVIDER_PRODUCT + " TEXT DEFAULT UNKOKNW, "
                + ProductEntry.COLUMN_QUANTITY_PRODUCT + " INTEGER DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_SALES + " REAL DEFAULT 0.0 );";

        // Ejecutar la instrucción SQL
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
    }

    //Esto se llama cuando la base de datos necesita ser actualizada.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME);
        onCreate(db);
    }
}
