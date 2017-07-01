package com.example.joni.inventoryapp.datos;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Joni on 23/06/2017.
 */

public final class ProductContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.joni.inventoryapp";

    /**
     * Utilice CONTENT_AUTHORITY para crear la base de todos los URI que las aplicaciones
     * utilizarán para ponerse en contacto con el proveedor de contenido.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCT = "myinventory";

    // Constructor privado para que no se creen clases.
    private ProductContract() {
    }

    public static class ProductEntry implements BaseColumns {

        /**
         * El URI de contenido para acceder a los datos del producto en el proveedor
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);

        /**
         * El tipo MIME de la {@link #CONTENT_URI} para obtener una lista de productos.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * El tipo MIME de {@link #CONTENT_URI} para un solo producto.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        public static final String TABLE_NAME = "myinventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_IMAGE_PRODUCT = "Product_Image";
        public static final String COLUMN_NAME_PRODUCT = "Product_Name";
        public static final String COLUMN_PRICE_PRODUCT = "Product_Price";
        public static final String COLUMN_PROVIDER_PRODUCT = "Product_Provider";
        public static final String COLUMN_QUANTITY_PRODUCT = "Product_Quantity";
        public static final String COLUMN_PRODUCT_SALES = "Product_Sales";
    }
}
