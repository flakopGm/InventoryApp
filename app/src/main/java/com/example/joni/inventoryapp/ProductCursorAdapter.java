package com.example.joni.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joni.inventoryapp.datos.ProductContract.ProductEntry;
import com.squareup.picasso.Picasso;


/**
 * Created by Joni on 23/06/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflar una vista de elemento de lista utilizando el diseño especificado en list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.product_list, parent, false);
    }

    /**
     * Este método enlaza los datos del producto (en la fila actual señalada por el cursor) a la
     * disposición de elementos de lista dada.
     *
     * @param view    Vista existente, devuelta anteriormente por el método newView ()
     * @param context Contexto de la aplicación
     * @param cursor  El cursor desde el cual se obtienen los datos. El cursor ya se mueve a la fila
     *                correcta.
     */

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Localización de vistas.
        ImageView imageViewProducto = (ImageView) view.findViewById(R.id.foto_producto);
        TextView textViewNameP = (TextView) view.findViewById(R.id.producto_name);
        TextView textViewPrecioP = (TextView) view.findViewById(R.id.precio);
        TextView textViewProveedorP = (TextView) view.findViewById(R.id.proveedor_producto);
        TextView textViewCantidadP = (TextView) view.findViewById(R.id.cantidad);
        final TextView totalVentasProducto = (TextView) view.findViewById(R.id.text_view_ventas);
        ImageView botonCompraProducto = (ImageView) view.findViewById(R.id.boton_compra);

        // Encuentra las columnas de los atributos de los productos que nos interesan.
        int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE_PRODUCT);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME_PRODUCT);
        final int precioColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE_PRODUCT);
        int proveedorColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PROVIDER_PRODUCT);
        int cantidadColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY_PRODUCT);
        int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);

        // Leer los atributos del producto desde el Cursor para el producto actual.
        int id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        Uri productPicture = Uri.parse(cursor.getString(pictureColumnIndex));
        final String productName = cursor.getString(nameColumnIndex);
        final double precioProduct = precioColumnIndex;
        final double precioPvp = cursor.getDouble(precioColumnIndex);
        String productPrice = "PVP: " + cursor.getString(precioColumnIndex) + " €";
        String productProvider = cursor.getString(proveedorColumnIndex);
        final int cantidad = cursor.getInt(cantidadColumnIndex);
        String productQuantity = "Stock\n" + cursor.getString(cantidadColumnIndex);
        final double sumaVentasProducto = cursor.getDouble(salesColumnIndex);
        String ventasTotalesProduct = "Ventas: " + cursor.getString(salesColumnIndex) +
                " €";

        final Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

        // Actualizar TextViews con los atributos para el producto actual
        textViewNameP.setText(productName);
        textViewPrecioP.setText(productPrice);
        textViewProveedorP.setText(productProvider);
        textViewCantidadP.setText(productQuantity);
        totalVentasProducto.setText(ventasTotalesProduct);

        // Usamos Picasso para actualizar la foto del producto.
        Picasso.with(context).load(productPicture)
                .placeholder(R.drawable.new_image)

                .fit()
                .into(imageViewProducto);

        // Escucha para el botón de compra de producto.
        botonCompraProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                // Si la cantidad del stock es mayor a 0 restaremos la cantidad en 1 y sumaremos
                // a la lista de venta el precio del producto cada vez que se pulse el botón,
                // finalmente actualizamos los campos.
                if (cantidad > 0) {
                    int stock = cantidad;
                    double pvp = precioPvp;
                    double sumaTot = sumaVentasProducto + pvp;
                    values.put(ProductEntry.COLUMN_PRODUCT_SALES, sumaTot);
                    values.put(ProductEntry.COLUMN_QUANTITY_PRODUCT, --stock);
                    resolver.update(
                            currentProductUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentProductUri, null);
                } else {
                    // Informamos al usuario cuando el almacén está vacío.
                    Toast.makeText(context, R.string.almacen_vacio, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
