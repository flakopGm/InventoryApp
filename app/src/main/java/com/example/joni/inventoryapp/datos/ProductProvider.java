package com.example.joni.inventoryapp.datos;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.joni.inventoryapp.datos.ProductContract.ProductEntry;

/**
 * Clase Provider para la tabla o id individuales de productos.
 */

public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductEntry.class.getSimpleName();

    //URI matcher código para el contenido URI de la tabla de productos
    private static final int TABLA_COMPLETA = 100;

    //URI matcher código para el contenido URI de un solo producto en la tabla de productos
    private static final int ID_TABLA = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.joni.inventoryapp" will map to the
        // integer code {@link #TABLA_COMPLETA}. This URI is used to provide access to MULTIPLE rows
        // of the products table.

        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT,
                TABLA_COMPLETA);

        // The content URI of the form "content://com.example.joni.inventoryapp/inventoryapp/#" will
        // map to the integer code {@link #ID_TABLA}. This URI is used to provide access to ONE
        // single row of the products table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.joni.inventoryapp/inventoryapp/3" matches, but
        // "content://content://com.example.joni.inventoryapp/inventoryapp" (without a number at
        // the end) doesn't match.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT
                + "/#", ID_TABLA);
    }

    /**
     * Objeto de ayuda de base de datos
     */
    private ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Obtener una base de datos legible.
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Este cursor contiene el resultado de la consulta.
        Cursor cursor;

        // Determine si el correlador de URI puede coincidir con el URI con un código específico.
        int match = sUriMatcher.match(uri);
        switch (match) {
            case TABLA_COMPLETA:
                // For the TABLA_COMPLETA code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ID_TABLA:
                // For the ID_TABLA code, extract out the ID from the URI.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Establecer URI de notificación en el Cursor, por lo que sabemos qué URI contenido para
        // el Cursor fue creado.Si los datos de este URI cambia, entonces sabemos que tenemos
        // que actualizar el Cursor.

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Devolver el cursor.
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TABLA_COMPLETA:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Inserte un producto en la base de datos con los valores de contenido especificados.
     * Devuelve el nuevo URI de contenido para esa fila específica en la base de datos.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        // Compruebe que el nombre no es nulo.
        String name = values.getAsString(ProductEntry.COLUMN_NAME_PRODUCT);
        if (name == null) {
            throw new IllegalArgumentException("Producto requiere un nombre.");
        }

        // Si el precio es nulo o igual a 0.
        Float price = values.getAsFloat(ProductEntry.COLUMN_PRICE_PRODUCT);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Producto requiere un precio.");
        }

        // Comprobacion de que el campo proveedor no sea nulo.
        String provider = values.getAsString(ProductEntry.COLUMN_PROVIDER_PRODUCT);
        if (provider == null) {
            throw new IllegalArgumentException("Se necesita un campo PROVEEDOR");
        }

        // Obtener base de datos de escritura
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Inserte el nuevo producto con los valores dados
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        // Si la ID es -1, entonces la inserción falló. Registrar un error y devolver null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notificar a todos los oyentes que los datos han cambiado para el URI del contenido del producto
        getContext().getContentResolver().notifyChange(uri, null);

        // Devuelve el nuevo URI con el ID (de la fila recién insertada) añadido al final
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TABLA_COMPLETA:
                return updateProduct(uri, values, selection, selectionArgs);
            case ID_TABLA:
                // For the ID_TABLA code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Actualice los productos de la base de datos con los valores de contenido especificados.
     * Aplique los cambios a las filas especificadas en los argumentos de selección y selección
     * (que podrían ser 0 o 1 o más productos). Devuelve el número de filas que se actualizaron
     * correctamente.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Si la clave {@link ProductEntry # COLUMN_NAME_PRODUCT} está presente, compruebe que el
        // valor del nombre no es nulo.
        if (values.containsKey(ProductEntry.COLUMN_NAME_PRODUCT)) {
            String name = values.getAsString(ProductEntry.COLUMN_NAME_PRODUCT);
            if (name == null) {
                return 0;
            }
        }

        //Si la clave {@link ProductEntry.COLUMN_PRICE_PRODUCT} está presente, compruebe que el
        // valor del precio no sea nulo o igual a 0.
        if (values.containsKey(ProductEntry.COLUMN_PRICE_PRODUCT)) {
            Float precio = values.getAsFloat(ProductEntry.COLUMN_PRICE_PRODUCT);
            if (precio == null || precio == 0) {
                return 0;
            }
        }

        // Si la clave {@link ProductEntry.COLUMN_PROVIDER_PRODUCT} está presente, compruebe que el
        // valor del proveedor no es nulo.
        if (values.containsKey(ProductEntry.COLUMN_PROVIDER_PRODUCT)) {
            String name = values.getAsString(ProductEntry.COLUMN_PROVIDER_PRODUCT);
            if (name == null) {
                return 0;
            }
        }

        // Si no hay valores para actualizar, no intente actualizar la base de datos.
        if (values.size() == 0) {
            return 0;
        }

        // De lo contrario, obtenga una base de datos con capacidad de escritura para actualizar
        // los datos.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Realice la actualización en la base de datos y obtenga el número de filas afectadas.
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // Si se actualizaron una o más filas, notifique a todos los oyentes que los datos del URI
        // dado han cambiado.
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Devuelve el número de filas actualizadas.
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Obtener base de datos de escritura.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Rastrear el número de filas que se eliminaron.
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TABLA_COMPLETA:
                // Eliminar todas las filas que coinciden con los args de selección y selección.
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ID_TABLA:
                // Eliminar una sola fila dada por el ID en el URI.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // Si se actualizaron una o más filas, notifique a todos los oyentes que los datos del URI
        // dado han cambiado.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Devuelve el número de filas actualizadas.
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TABLA_COMPLETA:
                return ProductEntry.CONTENT_LIST_TYPE;
            case ID_TABLA:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
