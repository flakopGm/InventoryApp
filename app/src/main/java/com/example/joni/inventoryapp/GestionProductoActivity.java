package com.example.joni.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.joni.inventoryapp.datos.ProductContract.ProductEntry;
import com.squareup.picasso.Picasso;


/**
 * Created by Joni on 28/06/2017.
 */

public class GestionProductoActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private final static String MSG_PRODUCTO_NUEVO = "Hola, ¿has visto todas las novedades que que " +
            "nos han llegado?, mira este producto, está genial seguro te gusta. ¡Ven a verlo!. :)";

    //URI de contenido para el producto existente
    private Uri currentProductUri;

    // String de uri para la foto por defecto.
    private String currentPhotoUri = "no image";

    /**
     * Elementos de TextView del producto (Nombre, precio, proveedor, ventas de producto), editText
     * inicialmente con la cantidad de stock disponible y la Imagen del producto.
     */
    private TextView nombreProd;
    private TextView precioProd;
    private TextView proveedorProd;
    private TextView ventasProd;
    private EditText stockProd;
    private ImageView imagenProd;
    private ImageView solicitarMercancia;
    private TextView cantidadProd;

    // Stock almacén.
    private int cantidad = 0;
    // Pulsaciones de los botones (Sumar y restar cantidad de stock).
    private int pulsaciones = 0;
    // Snackbar informativo.
    private Snackbar snackbar;
    // Mercancía solicitada.
    private Boolean solicitado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gestion_producto);

        // Localización de elementos de vista.
        nombreProd = (TextView) findViewById(R.id.tv_product_name);
        precioProd = (TextView) findViewById(R.id.textview_text_price);
        proveedorProd = (TextView) findViewById(R.id.textview_text_provider);
        ventasProd = (TextView) findViewById(R.id.ventas_producto);
        stockProd = (EditText) findViewById(R.id.quantity);
        Button botonRestar = (Button) findViewById(R.id.restar_stock);
        Button botonSumar = (Button) findViewById(R.id.sumar_stock);
        imagenProd = (ImageView) findViewById(R.id.imagen_producto);
        solicitarMercancia = (ImageView) findViewById(R.id.solicitar_mercancia);
        cantidadProd = (TextView) findViewById(R.id.cantidad_producto);

        // Examine la intención que se utilizó para iniciar esta actividad, a fin de determinar si
        // estamos creando un nuevo producto o editando uno existente.
        final Intent intent = getIntent();
        currentProductUri = intent.getData();

        // Inicializar un cargador para leer los datos del producto de la base de datos y mostrar
        // los valores actuales en el editor.
        getLoaderManager().initLoader(0, null, this);

        // Escucha para el botón de restar cantidad de stock a solicitar.
        botonRestar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambiamos el texto definido de "Stock actual" a "Cantidad a Solicitar" y restamos.
                cantidadProd.setText(getString(R.string.cantidad_solicitar));
                RestarStock();
            }
        });

        // Escucha para el botón de sumar cantidad stock a solicitar.
        botonSumar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambiamos el texto definido de "Stock actual" a "Cantidad a Solicitar" y sumamos.
                cantidadProd.setText(getString(R.string.cantidad_solicitar));
                SumarStock();
            }
        });

        // Escucha para el botón de solicitar mercancía.
        solicitarMercancia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRequestMerchandise();
                solicitado = true;
            }
        });
    }

    // Escuccha para la actuaclización de stock.
    private void ActualizarStockProducto() {
        if (solicitado != false) {
            String cantidad = stockProd.getText().toString();

            // Cree un objeto ContentValues donde los nombres de columna son las claves y los
            // atributos del producto del editor son los valores.
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_QUANTITY_PRODUCT, cantidad);

            if (currentProductUri != null) {
                // Si la Uri del producto es diferente a null ACTUALIZAMOS.
                int rowUpdated = getContentResolver().update(currentProductUri, values, null, null);

                if (rowUpdated == 0) {
                    Toast.makeText(this, R.string.error_stock, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.stock_guardado, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);

                    finish();
                }
            }
        } else {
            // Si no creo una vista que contiene el layout toast_aviso_mercancia.xml
            //incluyendolo en la vista de la actividad principal
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_aviso_mercancia,
                    (ViewGroup) findViewById(R.id.contenedor_toast_mercancia));
            //Toast
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CLIP_HORIZONTAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    }

    // Botón restar stock. (Cantidad predefinida de resta {10}).
    private void RestarStock() {
        cantidad = Integer.parseInt(stockProd.getText().toString());
        if (cantidad >= 10) {
            // Si la cantidad es 10 o mayor si puede restar la cantidad mínima predefinida.
            int restaStock = cantidad - 10;
            String stockActualizado = String.valueOf(restaStock);
            stockProd.setText(stockActualizado);
            pulsaciones++;

            // Cada dos pulsaciones en el botón de restar
            // (el resultado de ({@pulsaciones} %2 == 1 debe ser impar)
            // stock se mostrará un snackbar informativo
            // para grabar antes de salir siempre y cuando no modifique ningún campo más.
            if (pulsaciones % 2 == 1) {
                snackbar = Snackbar
                        .make(getCurrentFocus(), R.string.aviso,
                                Snackbar.LENGTH_LONG)
                        .setActionTextColor(getResources().getColor(R.color.colorStock));
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                snackbar.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // De momento nada.
                    }
                }).show();
            }
        }
        // Si la cantidad es menor a 10, por lo tanto no se puede restar la cantidad mínima, restar
        // la cantidad actual para ser resultado 0 y nunca negativo.
        if (cantidad < 10) {
            int restaStock = cantidad - cantidad;
            String stockActualizado = String.valueOf(restaStock);
            stockProd.setText(stockActualizado);
            pulsaciones++;

            // Cada dos pulsaciones en el botón de restar
            // (el resultado de ({@pulsaciones} %2 == 1 debe ser impar)
            // stock se mostrará un snackbar informativo
            // para grabar antes de salir siempre y cuando no modifique ningún campo más.
            if (pulsaciones % 2 == 1) {
                snackbar = Snackbar
                        .make(getCurrentFocus(), R.string.aviso,
                                Snackbar.LENGTH_LONG)
                        .setActionTextColor(getResources().getColor(R.color.colorStock));
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                snackbar.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Nada de momento.
                    }
                }).show();
            }
            if (cantidad == 0) {
                Toast.makeText(this, R.string.almacen_vacio, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Botón sumar stock. (Cantidad predefinida de suma {10}).
    private void SumarStock() {
        cantidad = Integer.parseInt(stockProd.getText().toString());
        int algo = cantidad + 10;
        String suma = String.valueOf(algo);
        stockProd.setText(suma);
        pulsaciones++;

        // Cada dos pulsaciones en el botón de sumar
        // (el resultado de ({@pulsaciones} %2 == 0 debe ser par)
        // stock se mostrará un snackbar informativo
        // para grabar antes de salir siempre y cuando no modifique ningún campo más.
        if (pulsaciones % 2 == 0) {
            snackbar = Snackbar
                    .make(getCurrentFocus(), R.string.aviso,
                            Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(R.color.colorStock));
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            snackbar.setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Nada de momento.
                }
            })
                    .show();
        }
    }

    // Solicitar mercancía pasándo texto predefinido, nombre producto y cantidad a solicitar.
    private void solicitudMercancia() {
        String producto = nombreProd.getText().toString();
        String proveedor = proveedorProd.getText().toString();
        // Datos ficticios pasados para crear una dirección mail.
        String correoProveedor = "www." + proveedor + ".es";
        String[] destinatario = {correoProveedor};
        String stockASolicitar = stockProd.getText().toString();

        // Construimos nuestro mensaje para solicitar la mercancía.
        StringBuilder builder = new StringBuilder();
        builder.append("Hola " + proveedor + " :\n");
        builder.append("Me encuentro en la necesidad de solicitar la siguiente mercancía:\n");
        builder.append("PRODUCTO: " + producto + "\n");
        builder.append("CANTIDAD: " + stockASolicitar + "\n");
        builder.append("\nGracias, le mando un cordial saludo.");
        String solicitud = builder.toString();

        // Realizamos la solicitud con el envío, en principio por correo electronico.
        Intent intentMercancia = new Intent(Intent.ACTION_SEND);
        intentMercancia.setData(Uri.parse("mailto:"));
        intentMercancia.setType("text/plain");
        intentMercancia.putExtra(Intent.EXTRA_EMAIL, destinatario);
        intentMercancia.putExtra(Intent.EXTRA_SUBJECT, "Solicitud Mercancía ");
        intentMercancia.putExtra(Intent.EXTRA_TEXT, solicitud);
        //Iniciamos intentMercancia.
        startActivity(Intent.createChooser(intentMercancia, "Solicitud Mercancía"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar las opciones del menú desde el archivo res / menu / menu_editor.xml. Esto agrega
        // elementos de menú a la barra de aplicaciones.
        getMenuInflater().inflate(R.menu.menu_gestion_productos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // El usuario hizo clic en una opción de menú en el menú desbordamiento de la barra de
        // aplicaciones
        switch (item.getItemId()) {
            // Modificar Producto.
            case R.id.edit_product_atributes:
                ModificarCamposProducto();
                // Finalizamos la actividad
                finish();
                return true;
            // Actualizar Stock
            case R.id.saved_stock:
                ActualizarStockProducto();
                return true;
            // Compartir Info Producto.
            case R.id.action_share:
                CompartirProducto();
                return true;
            // Volver atrás.
            case R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Share Product.
    private void CompartirProducto() {
        // Recogemos los datos del nombre, proveedor y precio del producto.
        String producto = nombreProd.getText().toString();
        String proveedor = proveedorProd.getText().toString();
        String precio = precioProd.getText().toString();

        // Creamos el mensaje a enviar con los datos asociados.
        StringBuilder builderProductoNuevo = new StringBuilder();
        builderProductoNuevo.append(MSG_PRODUCTO_NUEVO + " \n\n"
                + producto + "\n" + proveedor + "\n" + precio);

        String algo = builderProductoNuevo.toString();

        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.setType("text/plain");
        intentShare.putExtra(Intent.EXTRA_SUBJECT, "Producto Nuevo");
        intentShare.putExtra(Intent.EXTRA_TEXT, algo);
        // Iniciamos intentShare.
        startActivity(Intent.createChooser(intentShare, "Compartir Producto Nuevo"));
    }

    // Modificar Producto.
    private void ModificarCamposProducto() {
        // Redirige a la pantalla de edición del producto.
        Intent intent = new Intent(GestionProductoActivity.this, EdicionProductoActivity.class);
        // Uri actual del producto a modificar.
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,
                ContentUris.parseId(this.currentProductUri));
        // Definimos el currentProductUri y se inicia el intent, luego finalizamos.
        intent.setData(currentProductUri);
        startActivity(intent);
        finish();
    }

    // Confirmación para solicitar mercancía.
    private void showDialogRequestMerchandise() {
        // Cree un AlertDialog.Builder y configure el mensaje, y haga clic en los oyentes para los
        // botones positivos y negativos en el diálogo.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.solicitar_mercancia);
        builder.setPositiveButton(R.string.afirmativo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Si el usuario elige si, se solicita Merancía.
                solicitudMercancia();
            }
        });
        builder.setNegativeButton(R.string.negativo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Si el usuario elige no, se mantiene en la pantalla actual.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Creación, definición de título de cuadro de diálogo y finalmente lo mostramos.
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("SOLICITAR MERCANCIA");
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Dado que el editor muestra todos los atributos del producto, defina una proyección que
        // contenga todas las columnas de la tabla de productos
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_IMAGE_PRODUCT,
                ProductEntry.COLUMN_NAME_PRODUCT,
                ProductEntry.COLUMN_PRICE_PRODUCT,
                ProductEntry.COLUMN_PROVIDER_PRODUCT,
                ProductEntry.COLUMN_QUANTITY_PRODUCT,
                ProductEntry.COLUMN_PRODUCT_SALES};

        // Este cargador ejecutará el método de consulta de ContentProvider en un subproceso de fondo
        return new CursorLoader(this, currentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Regresa temprano si el cursor es nulo o hay menos de 1 fila en el cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceda con el desplazamiento a la primera fila del cursor y la lectura de los datos de
        // ella (Esta debe ser la única fila en el cursor)
        if (cursor.moveToFirst()) {
            // Encuentre las columnas de los atributos del producto que nos interesan
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE_PRODUCT);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME_PRODUCT);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE_PRODUCT);
            int providerColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PROVIDER_PRODUCT);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY_PRODUCT);
            int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);

            // Extraiga el valor del Cursor para el índice de columna dado.
            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String provider = cursor.getString(providerColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            currentPhotoUri = cursor.getString(imageColumnIndex);
            double pvp = cursor.getDouble(salesColumnIndex);

            // Actualizar las vistas en la pantalla con los valores de la base de datos.
            nombreProd.setText(name);
            precioProd.setText(String.valueOf(price) + " €");
            proveedorProd.setText(provider.toUpperCase());
            ventasProd.setText(String.valueOf(pvp) + " €");
            stockProd.setText(String.valueOf(quantity));

            // Actualizamos la foto utilizando Picasso.
            Picasso.with(this).load(currentPhotoUri)
                    .placeholder(R.drawable.new_image)
                    .fit()
                    .into(imagenProd);
        }
    }

    // Reseteamos los campos.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nombreProd.setText("");
        precioProd.setText(String.valueOf(""));
        proveedorProd.setText("");
        ventasProd.setText("");
        cantidadProd.setText(R.string.stock_almacen);
    }

    // Aviso personalizado.
    public void avisoCambioImagen(View view) {
        //Creo una vista que contiene el layout contenidotoast.xml
        //incluyendolo en la vista de la actividad principal
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_aviso_cambio_imagen,
                (ViewGroup) findViewById(R.id.contenedor_toast));
        //Configuro Toast
        Toast toast = new Toast(getApplicationContext());
        //gravedad
        toast.setGravity(Gravity.CLIP_HORIZONTAL, 0, 0);
        //tipo toast
        toast.setDuration(Toast.LENGTH_SHORT);
        //mostrar toast
        toast.setView(layout);
        toast.show();

    }

}
