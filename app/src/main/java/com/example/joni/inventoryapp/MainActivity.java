package com.example.joni.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.joni.inventoryapp.datos.ProductContract.ProductEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Adaptador para el ListView.
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Localización del botón flotante.
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list_product);

        // Al desplazar la lista el botón flotante se hace mini (Y así se mantiene mientras la lista
        // se esté desplazando).
        productListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                fab.setSize(FloatingActionButton.SIZE_AUTO);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                fab.setSize(FloatingActionButton.SIZE_MINI);
            }
        });

        // Escucha botón flotante para agregar un nuevo producto.
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EdicionProductoActivity.class);
                startActivity(intent);
            }
        });

        // Buscar y establecer vista vacía en el ListView, para que sólo muestra cuando la lista
        // tiene 0 elementos.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);
        // Configure un adaptador para crear un elemento de lista para cada fila de datos de
        // producto en el cursor. No hay datos del producto todavía (hasta que el cargador termine)
        // así que pase en nulo para el Cursor.
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);


        // Escucha para el click de Item de la lista de productos.
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Nuevo intent que nos redirige a la Actividad de Gestión del producto.
                Intent intent = new Intent(MainActivity.this, GestionProductoActivity.class);

                // Forme el URI de contenido que representa el producto específico al que se hizo
                // clic, agregando el "id" (pasado como entrada a este método) en
                // {@link ProductEntry # CONTENT_URI}.

                Uri currentPetUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                // Establecer el URI en el campo de datos de la intención.
                intent.setData(currentPetUri);

                // Inicie {@link GestionProductoActivity} para mostrar los datos del producto actual.
                startActivity(intent);
            }
        });
        // Inicia el cargador.
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Método auxiliar para insertar datos de producto codificados en la base de datos.
     * Sólo para fines de depuración.
     */
    private void addDummy() {
        // Creamos un ContentValues con un contenido ficticio predeterminado.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME_PRODUCT, "New Product");
        values.put(ProductEntry.COLUMN_PRICE_PRODUCT, "1.5");
        values.put(ProductEntry.COLUMN_PROVIDER_PRODUCT, "New Provider");
        values.put(ProductEntry.COLUMN_QUANTITY_PRODUCT, 10);
        values.put(ProductEntry.COLUMN_PRODUCT_SALES, 0.0);

        // Insertamos los datos fictios en una nueva fila e informamos al usuario.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        Toast.makeText(this, R.string.aviso_producto_ficticio, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog() {
        // Cree un AlertDialog.Builder y configure el mensaje, y haga clic en los oyentes para los
        // botones positivos y negativos en el diálogo.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.borrar_todos);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Si el usuario pulsa "Borrar" se elimina toda la lista de productos.
                deleteAllProducts();
                Toast.makeText(MainActivity.this, R.string.lista_borrada, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // De lo contrario continúa editando el producto.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Crea y muestra el cuadro de diálogo.
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("ELIMINAR LISTA COMPLETA");
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú; Esto agrega elementos a la barra de acción si está presente.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //Método auxiliar para eliminar todas los productos de la base de datos.
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Ajustes
            case R.id.action_settings:
                Toast.makeText(this, R.string.proximamente, Toast.LENGTH_SHORT)
                        .show();
                return true;
            // Producto ficticio
            case R.id.producto_ficticio:
                addDummy();
                return true;
            // Borrar toda la lista
            case R.id.delete_all:
                showDeleteConfirmationDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Defina una proyección que especifique las columnas de la tabla que nos interesa.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_IMAGE_PRODUCT,
                ProductEntry.COLUMN_NAME_PRODUCT,
                ProductEntry.COLUMN_PRICE_PRODUCT,
                ProductEntry.COLUMN_PROVIDER_PRODUCT,
                ProductEntry.COLUMN_QUANTITY_PRODUCT,
                ProductEntry.COLUMN_PRODUCT_SALES};

        // Este cargador ejecutará el método de consulta de ContentProvider en un subproceso de
        // fondo.
        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        // Actualice {@link ProductCursorAdapter} con este nuevo cursor que contiene datos
        // actualizados del producto
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        // Se llama llamada de llamada cuando los datos deben eliminarse
        mCursorAdapter.swapCursor(null);
    }
}
