package com.example.joni.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joni.inventoryapp.datos.ProductContract.ProductEntry;
import com.squareup.picasso.Picasso;

import java.io.File;

public class EdicionProductoActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identificador del cargador de datos del producto
    private static final int EXISTING_PRODUCT_LOADER = 0;

    // URI de contenido para el producto existente (null si se trata de un producto nuevo).
    private Uri currentProductUri;

    // Imagen para el producto.
    private ImageView imageViewP;

    // EditText para el nombre del producto.
    private EditText editTextNameP;

    // EditText pare el precio del producto.
    private EditText editTextPriceP;

    // EditText pare el proveedor del producto.
    private EditText editTextProvider;

    // EditText pare la cantidad de stock del producto.
    private EditText editTextQuantity;

    // Código de respuesta de solicitud de foto.
    public static final int PHOTO_REQUEST_CODE = 20;

    // Codigo de respuesta de solicitud de permiso.
    public static final int EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE = 21;

    // Uri default para las imagenes de los productos.
    private String currentPhotoUri = "no image";

    // Define una variable para contener el número de filas eliminadas.
    private int filasEliminadas = 0;

    // Determina si los campos fueron cambiados.
    private boolean datosProductoCambiado = false;


    // Comprobación de toques, cambios de campos.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            datosProductoCambiado = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion_producto);

        // Localización de elementos de la vista.
        ScrollView scrollview = (ScrollView) findViewById(R.id.scrollview);
        imageViewP = (ImageView) findViewById(R.id.imagen_producto);
        editTextNameP = (EditText) findViewById(R.id.textview_product_name);
        editTextPriceP = (EditText) findViewById(R.id.textview_text_price);
        editTextProvider = (EditText) findViewById(R.id.textview_text_provider);
        editTextQuantity = (EditText) findViewById(R.id.quantity);
        TextView textoFoto = (TextView) findViewById(R.id.text_image_product);
        final TextView instrucionesFoto = (TextView) findViewById(R.id.instrucciones_img);

        // Mantenemos el scrollview en la posición superior.
        scrollview.fullScroll(ScrollView.FOCUS_UP);

        // Comprobación de toques para cada editText
        imageViewP.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                datosProductoCambiado = true;
                instrucionesFoto.setVisibility(View.GONE);
                return false;
            }
        });
        editTextNameP.setOnTouchListener(mTouchListener);
        editTextPriceP.setOnTouchListener(mTouchListener);
        editTextProvider.setOnTouchListener(mTouchListener);
        editTextQuantity.setOnTouchListener(mTouchListener);

        // Actualización de foto al tocarla.
        imageViewP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarFotoProducto(v);
            }
        });

        // Examine la intención que se utilizó para iniciar esta actividad, a fin de determinar si
        // estamos creando un nuevo producto o editando uno existente.
        Intent intent = getIntent();
        currentProductUri = intent.getData();

        // Si la intención NO contiene un URI de contenido de producto, entonces sabemos que estamos
        // creando un nuevo producto.
        if (currentProductUri == null) {
            // Nuevo producto, definimos título activity a "Producto Nuevo".
            setTitle(getString(R.string.titulo_activity_producto_nuevo));
            // Ocultamos el texto informativo para el cambio de foto.
            // Inavilitamos la opción de borrar del menú de opciones ya que es un producto nuevo y
            // no se a creado aún, por lo tanto no se puede borrar.
            textoFoto.setVisibility(View.VISIBLE);
            invalidateOptionsMenu();

        } else {
            // Sobreescribimos el producto y definimos el título de la activity a "Modificar Datos
            // Producto".
            setTitle(getString(R.string.titulo_activity_modificar_producto));
            textoFoto.setVisibility(View.GONE);
            instrucionesFoto.setVisibility(View.GONE);
            imageViewP.setScaleType(ImageView.ScaleType.FIT_XY);
            editTextNameP.setHintTextColor(getResources().getColor(R.color.colorHint));
            // Inicializar un cargador para leer los datos del producto de la base de datos y
            // mostrar los valores actuales en el editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
    }

    private void showDeleteConfirmationDialog() {
        // Cree un AlertDialog.Builder y configure el mensaje, y haga clic en los oyentes para los
        // botones positivos y negativos en el diálogo.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Al hacer click se borra el producto actual.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Al hacer click en la opción Cancelar se regresa a la modificación del producto.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Crea y muestra el DialogAlert
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    //Realice la eliminación del producto en la base de datos.
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (currentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the currentProductUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (filasEliminadas == 0) {
                Toast.makeText(this, R.string.producto_borrado, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EdicionProductoActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.error_borrando_producto, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
    // Método para mostrar Pop up
    private void mostrarDialogoCancelarCambios(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.descartar_cambios_dialog_msg);
        builder.setPositiveButton(R.string.descartar, discardButtonClickListener);
        builder.setNegativeButton(R.string.seguir_editando, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Si el usuario hace clic en el botón "Mantener la edición", por lo tanto, se debe
                // descartar el diálogo y continuar editando el producto.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Crea y muestra el cuadro de diálogo.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // Si el producto no ha cambiado, continúe con la manipulación.
        if (!datosProductoCambiado) {
            super.onBackPressed();
            return;
        }
        // De lo contrario, si hay cambios no guardados, configure un cuadro de diálogo para
        // advertir al usuario.
        // Cree un oyente de clics para que el usuario confirme que los cambios deben descartarse.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // El usuario hizo clic en el botón "Descartar", cierre la actividad actual.
                        finish();
                    }
                };
        // Mostrar diálogo de que hay cambios no guardados.
        mostrarDialogoCancelarCambios(discardButtonClickListener);
    }
    // Actualizar la foto del producto si tenemos los permisos concedidos.
    public void actualizarFotoProducto(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Estamos en M o por encima de lo que tenemos que pedir permisos de tiempo de ejecución.
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                eleccionFotoProducto();
            } else {
                // Estamos aquí si no todos tenemos permisos
                String[] permisionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permisionRequest, EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE);
            }
        } else {
            //Estamos en un dispositivo más viejo por lo que no tiene que pedir permisos de tiempo
            // de ejecución
            eleccionFotoProducto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            //Conseguimos un GO del usuario y procedemos.
            eleccionFotoProducto();
        } else {
            // Informamos de la situación.
            Toast.makeText(this, R.string.permiso, Toast.LENGTH_LONG).show();
        }
    }

    private void eleccionFotoProducto() {
        // Invoque la galería de imágenes con una intención implícita.
        Intent selectorFoto = new Intent(Intent.ACTION_PICK);

        // Directorio datos.
        File directorioFoto = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = directorioFoto.getPath();
        // Finalmente, obtener una representación URI
        Uri data = Uri.parse(pictureDirectoryPath);

        // Establecer los datos y el tipo. Obtenga todos los tipos de imagen.
        selectorFoto.setDataAndType(data, "image/*");

        // Invocaremos esta actividad y obtendremos algo de ella.
        startActivityForResult(selectorFoto, PHOTO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
            }
            // Recogemos los datos y definimos el currentPhotoUri de la imagen.
            Uri mProductPhotoUri = data.getData();
            currentPhotoUri = mProductPhotoUri.toString();


            // Usamos Picasso para la gestión de fotografias.
            Picasso.with(this).load(mProductPhotoUri)
                    .placeholder(R.drawable.new_image)
                    .fit()
                    .into(imageViewP);
        }
    }

    private void AddNewProduct() {
        String nombre = editTextNameP.getText().toString();
        String precio = editTextPriceP.getText().toString();
        String proveedor = editTextProvider.getText().toString();
        String cantidad = editTextQuantity.getText().toString();

        if (nombre.isEmpty() || precio.isEmpty() || proveedor.isEmpty() || cantidad.isEmpty()) {
            Toast.makeText(this, R.string.rellenado, Toast.LENGTH_SHORT).show();
            return;
        }
        // Cree un objeto ContentValues donde los nombres de columna son las claves y los atributos
        // pet del editor son los valores.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_IMAGE_PRODUCT, currentPhotoUri);
        values.put(ProductEntry.COLUMN_NAME_PRODUCT, nombre);
        values.put(ProductEntry.COLUMN_PRICE_PRODUCT, precio);
        values.put(ProductEntry.COLUMN_PROVIDER_PRODUCT, proveedor);
        values.put(ProductEntry.COLUMN_QUANTITY_PRODUCT, cantidad);
        values.put(ProductEntry.COLUMN_PRODUCT_SALES, 0.0);

        // Si el currentProductUri es nulo debemos entender que es un prooducto nuevo y debemos
        // proceder a insertar el elemento en una nueva fila e informar de lo sucedido.
        if (currentProductUri == null) {

            Uri insertedRow = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (insertedRow == null) {
                Toast.makeText(this, R.string.error_guardar_modificado, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.modificado, Toast.LENGTH_LONG).show();
            }
        } else {
            // De lo contrario procederemos a actualizar e informar de lo sucedido.
            int rowUpdated = getContentResolver().update(currentProductUri, values, null, null);

            if (rowUpdated == 0) {
                Toast.makeText(this, R.string.error_guardar_modificado, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.modificado, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar las opciones de menú desde el archivo res / menu / menu_edicion_producto.xml.
        // Esto agrega elementos de menú a la barra de aplicaciones.
        getMenuInflater().inflate(R.menu.menu_edicion_producto, menu);
        return true;
    }

    /**
     * Este método se llama después de invalidateOptionsMenu (), para que el menú se pueda
     * actualizar.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Si se trata de un producto nuevo, ocultar el elemento de menú "Eliminar".
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_product);
            menuItem.setVisible(false);
        }
        // Si se trata de una modificación de producto existente cambiar el icono para guardar dicha
        // modificación.
        if (currentProductUri != null) {
            MenuItem menuItem = menu.findItem(R.id.saved_product);
            menuItem.setIcon(R.drawable.ic_done_white_18dp);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // El usuario hizo clic en una opción de menú en el menú desbordamiento de la barra de
        // aplicaciones.
        switch (item.getItemId()) {
            case R.id.saved_product:
                // Opción Guardar Nuevo Producto / Guardar Modificación Producto.
                AddNewProduct();
                // Finalizamos la actividad.
                finish();
                return true;
            case R.id.delete_product:
                // Opción para borrar, mostramos cuadro de diálogo antes de proceder.
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        return new CursorLoader(this,   // Parent activity context
                currentProductUri,         // Consultar el URI de contenido para la producto actual
                projection,             // Columnas a incluir en el Cursor resultante
                null,                   // Sin cláusula de selección
                null,                   // Sin argumentos de selección
                null);                  // Orden de clasificación predeterminada
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //  Si el cursor es nulo o hay menos de 1 fila en el cursor, nada.
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceda con el desplazamiento a la primera fila del cursor y la lectura de los datos de
        // ella (Esta debe ser la única fila en el cursor)
        if (cursor.moveToFirst()) {
            // Encuentre las columnas de los atributos del producto que nos interesan.
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE_PRODUCT);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME_PRODUCT);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE_PRODUCT);
            int providerColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PROVIDER_PRODUCT);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY_PRODUCT);

            // Extraiga el valor del Cursor para el índice de columna dado.
            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String provider = cursor.getString(providerColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            currentPhotoUri = cursor.getString(imageColumnIndex);

            // Actualizar las vistas en la pantalla con los valores de la base de datos.
            editTextNameP.setText(name);
            editTextPriceP.setText(String.valueOf(price));
            editTextProvider.setText(provider);
            editTextQuantity.setText(String.valueOf(quantity));

            // Actualizamos la foto con Picasso.
            Picasso.with(this).load(currentPhotoUri)
                    .placeholder(R.drawable.new_image)
                    .fit()
                    .into(imageViewP);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Reseteamos los campos.
        editTextNameP.setText("");
        editTextProvider.setText("");
        editTextQuantity.setText("");
        editTextPriceP.setText("");
    }
}
