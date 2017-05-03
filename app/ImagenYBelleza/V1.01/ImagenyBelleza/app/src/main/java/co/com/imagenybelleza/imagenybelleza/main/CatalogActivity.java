package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.adapters.BrandsAdapter;
import co.com.imagenybelleza.imagenybelleza.adapters.CatalogItemsAdapter;
import co.com.imagenybelleza.imagenybelleza.adapters.CategoriesAdapter;
import co.com.imagenybelleza.imagenybelleza.adapters.SubItemsAdapter;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.fragments.FragmentDialog;
import co.com.imagenybelleza.imagenybelleza.fragments.MainFragmentDialog;
import co.com.imagenybelleza.imagenybelleza.helpers.AdjustableGridView;
import co.com.imagenybelleza.imagenybelleza.helpers.GPSReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Brand;
import co.com.imagenybelleza.imagenybelleza.models.Category;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Filter;
import co.com.imagenybelleza.imagenybelleza.models.Item;

public class CatalogActivity extends AppCompatActivity implements GPSReceiver.LocationReceiverListener {

    private AdjustableGridView itemsGridView;
    private List<Brand> brands;
    private List<Item> filteredItems;
    private List<Category> categories;
    private Context context;
    private DatabaseHelper database;
    private FloatingActionButton fab;
    private int orderId, clientId;

    private AdjustableGridView brandsGrid, categoriesGrid;

    private EditText nameInput;
    private List<Item> subItems;
    private AlertDialog alertDialog;
    private RobotoRegularTextView currentCategoryLabel, currentBrandLabel;
    private LinearLayout brandsHeader, categoriesHeader;

    private boolean brandsOpened = false;
    private boolean categoriesOpened = false;

    private Brand currentBrand = null;
    private Category currentCategory = null;
    private int currentSeq = 0;
    private CatalogItemsAdapter adapter;
    private Button loadMoreButton;
    private MainFragmentDialog fragmentDialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fragmentDialog != null) {
            if (fragmentDialog.isVisible()) {
                fragmentDialog.dismiss();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        context = CatalogActivity.this;
        database = new DatabaseHelper(context);

        findViewById(R.id.activity_catalog).requestFocus();

        orderId = getIntent().getIntExtra("order_id", 0);
        clientId = getIntent().getIntExtra("client_id", 0);

        currentCategoryLabel = (RobotoRegularTextView) findViewById(R.id.category_label);
        currentBrandLabel = (RobotoRegularTextView) findViewById(R.id.brand_label);

        brandsGrid = (AdjustableGridView) findViewById(R.id.brands_grid);
        brandsGrid.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {

                categories = new ArrayList<>();

                brandsGrid.setVisibility(View.GONE);
                brandsOpened = false;

                currentBrand = brands.get(position);
                currentBrandLabel.setText(currentBrand.getName());
                Category allCategories = new Category();
                allCategories.setId(900);
                allCategories.setName("Todas");
                categories.add(allCategories);

                switch (currentBrand.getName()) {
                    case "Todas":
                        categories.addAll(database.getCategories());
                        break;
                    case "Nuevos":
                        categories.addAll(database.getNewItemsCategories());
                        break;
                    default:
                        categories.addAll(database.getCategoriesByBrand(currentBrand.getId()));
                        break;
                }

                categoriesGrid.setAdapter(new CategoriesAdapter(context, R.layout.item_category, categories, getListFiles(new File(database.getDirectory(), "categories"))));
                categoriesGrid.setVisibility(View.VISIBLE);
                categoriesOpened = true;
            }
        });

        categoriesGrid = (AdjustableGridView) findViewById(R.id.categories_grid);
        categoriesGrid.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {

                categoriesGrid.setVisibility(View.GONE);
                categoriesOpened = false;

                currentCategory = categories.get(position);
                currentCategoryLabel.setText(currentCategory.getName());

            }
        });

        categoriesHeader = (LinearLayout) findViewById(R.id.categories_header);
        categoriesHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!categoriesOpened) {
                    categoriesGrid.setVisibility(View.VISIBLE);
                    categoriesOpened = true;
                } else {
                    categoriesGrid.setVisibility(View.GONE);
                    categoriesOpened = false;
                }
            }
        });

        brandsHeader = (LinearLayout) findViewById(R.id.brands_header);
        brandsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!brandsOpened) {
                    brandsGrid.setVisibility(View.VISIBLE);
                    brandsOpened = true;
                } else {
                    brandsGrid.setVisibility(View.GONE);
                    brandsOpened = false;
                }
            }
        });

        itemsGridView = (AdjustableGridView) findViewById(R.id.items_list);
        itemsGridView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                final Item item = filteredItems.get(position);
                File file = new File(database.getDirectory());
                file = new File(file, "items");
                file = new File(file, String.valueOf(item.getId()));
                System.out.println("Archivo: " + file.getPath());
                if (file.isDirectory()) {
                    final List<File> files = getListFiles(file);
                    Client client = database.getClient(clientId);
                    subItems = new ArrayList<>();
                    File mydir = new File(file.getPath());
                    File lister = mydir.getAbsoluteFile();
                    int index = 0;

                    for (String list : lister.list()) {
                        Item subItem = new Item();
                        list = list.replace(".jpg", "");
                        index++;
                        double discount = 0;
                        subItem.setId(item.getId());
                        subItem.setName(list);
                        subItem.setIva(item.getIva());
                        subItem.setBrand(item.getBrand());
                        subItem.setCategory(item.getCategory());
                        subItem.setActive(item.isActive());
                        subItem.setSubItemId(index);
                        subItem.setPriceFive(item.getPriceFive());
                        subItem.setPriceFour(item.getPriceFour());
                        subItem.setPriceThree(item.getPriceThree());
                        subItem.setPriceTwo(item.getPriceTwo());
                        subItem.setPriceOne(item.getPriceOne());
                        subItem.setPaymentOne(item.getPaymentOne());
                        subItem.setPaymentTwo(item.getPaymentTwo());
                        subItem.setPaymentThree(item.getPaymentThree());
                        subItem.setPaymentFour(item.getPaymentFour());
                        subItems.add(subItem);
                    }

                    AlertDialog.Builder dialog = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
                    View dialogView = View.inflate(context, R.layout.dialog_sub_items, null);
                    dialog.setView(dialogView);
                    ListView subItemListView = (ListView) dialogView.findViewById(R.id.subitem_list_view);

                    subItemListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            Utils.showAddSubItemItemDialog(context, subItems.get(position), database, item, clientId, orderId);
                            return true;
                        }
                    });
                    subItemListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(android.widget.AdapterView<?> parent, View view, final int position, final long id) {
                            if (files != null) {
                                FragmentDialog fragmentDialog = new FragmentDialog();
                                fragmentDialog.setArgs(files, position, context, subItems, database.getOrder(orderId));
                                fragmentDialog.show(getSupportFragmentManager(), "FragmentDialog");
                            }
                        }
                    });
                    subItemListView.setAdapter(new SubItemsAdapter(context, R.layout.item_item, subItems, client));
                    final AlertDialog alertDialog = dialog.create();
                    alertDialog.show();
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            nameInput.clearFocus();
                        }
                    });

                    LinearLayout fab = (LinearLayout) dialogView.findViewById(R.id.home_button);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            startActivity(new Intent(new Intent(CatalogActivity.this, CreateOrderActivity.class).putExtra("client_id", clientId).putExtra("order_id", orderId)));
                            finish();
                        }
                    });

                } else {
                    Utils.showAddItemDialog(context, clientId, filteredItems.get(position), database, orderId, CatalogActivity.this);
                }
                return true;
            }
        });

        itemsGridView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, final int position, long id) {
                final Item item = filteredItems.get(position);
                File file = new File(database.getDirectory());
                file = new File(file, "items");
                file = new File(file, String.valueOf(item.getId()));
                System.out.println("Archivo: " + file.getPath());
                if (file.isDirectory()) {
                    final List<File> files = getListFiles(file);
                    Client client = database.getClient(clientId);
                    subItems = new ArrayList<>();
                    File mydir = new File(file.getPath());
                    File lister = mydir.getAbsoluteFile();
                    int index = 0;

                    for (String list : lister.list()) {
                        Item subItem = new Item();
                        list = list.replace(".jpg", "");
                        index++;
                        double discount = 0;
                        subItem.setId(item.getId());
                        subItem.setName(list);
                        subItem.setIva(item.getIva());
                        subItem.setBrand(item.getBrand());
                        subItem.setCategory(item.getCategory());
                        subItem.setActive(item.isActive());
                        subItem.setSubItemId(index);
                        subItem.setPriceFive(item.getPriceFive());
                        subItem.setPriceFour(item.getPriceFour());
                        subItem.setPriceThree(item.getPriceThree());
                        subItem.setPriceTwo(item.getPriceTwo());
                        subItem.setPriceOne(item.getPriceOne());
                        subItem.setPaymentOne(item.getPaymentOne());
                        subItem.setPaymentTwo(item.getPaymentTwo());
                        subItem.setPaymentThree(item.getPaymentThree());
                        subItem.setPaymentFour(item.getPaymentFour());
                        if (client.getClientType().getId() == 1) {
                            discount = item.getPriceOne();
                        }
                        if (client.getClientType().getId() == 2) {
                            discount = item.getPriceTwo();
                        }
                        if (client.getClientType().getId() == 3) {
                            discount = item.getPriceThree();
                        }
                        if (client.getClientType().getId() == 4) {
                            discount = item.getPriceFour();
                        }
                        if (client.getClientType().getId() == 5) {
                            discount = item.getPriceFive();
                        }
                    }

                    AlertDialog.Builder dialog = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
                    View dialogView = View.inflate(context, R.layout.dialog_sub_items, null);
                    dialog.setView(dialogView);
                    ListView subItemListView = (ListView) dialogView.findViewById(R.id.subitem_list_view);
                    subItemListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            Utils.showAddSubItemItemDialog(context, subItems.get(position), database, item, clientId, orderId);
                            return true;
                        }
                    });
                    subItemListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(android.widget.AdapterView<?> parent, View view, final int position, final long id) {
                            if (files != null) {
                                FragmentDialog fragmentDialog = new FragmentDialog();
                                fragmentDialog.setArgs(files, position, context, subItems, database.getOrder(orderId));
                                fragmentDialog.show(getSupportFragmentManager(), "FragmentDialog");
                            }
                        }
                    });
                    subItemListView.setAdapter(new SubItemsAdapter(context, R.layout.item_item, subItems, client));
                    alertDialog = dialog.create();
                    alertDialog.show();
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            nameInput.clearFocus();
                        }
                    });
                    LinearLayout fab = (LinearLayout) dialogView.findViewById(R.id.home_button);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            startActivity(new Intent(new Intent(CatalogActivity.this, CreateOrderActivity.class).putExtra("client_id", clientId).putExtra("order_id", orderId)));
                            finish();
                        }
                    });
                } else {
                    new AsyncShowDialog().execute(position);
                }
            }
        });

        nameInput = (EditText) findViewById(R.id.name_input);
        Button searchButton = (Button) findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncQuery().execute();
            }
        });

        loadMoreButton = (Button) findViewById(R.id.load_button);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncGetMore().execute();
            }
        });

        ImageView clearButton = (ImageView) findViewById(R.id.clear_city_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameInput.setText("");
                database.deleteCurrentFilter();
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatalogActivity.this, CreateOrderActivity.class).putExtra("order_id", orderId).putExtra("client_id", clientId));
                finish();
            }
        });

        new AsyncSetup().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    inFiles.addAll(getListFiles(file));
                } else {
                    if (file.getName().endsWith(".jpg")) {
                        inFiles.add(file);
                    }
                }
            }
            return inFiles;
        } else {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_back:
                startActivity(new Intent(CatalogActivity.this, ItemsListActivity.class).putExtra("order_id", orderId).putExtra("client_id", clientId));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CatalogActivity.this, ItemsListActivity.class).putExtra("order_id", orderId).putExtra("client_id", clientId));
        finish();
    }

    @Override
    public void onGpsStateChangedListener(boolean isConnected) {
        if (!isConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(
                    "El GPS esta desactivado, por favor activalo para continuar")
                    .setCancelable(false)
                    .setPositiveButton("Activar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    final ComponentName toLaunch = new ComponentName(
                                            "com.android.settings",
                                            "com.android.settings.SecuritySettings");
                                    final Intent intent = new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                    intent.setComponent(toLaunch);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivityForResult(intent, 1);
                                    dialog.dismiss();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private class AsyncShowDialog extends AsyncTask<Integer, Void, Void> {

        private Dialog dialog;
        private List<Item> photoItems;
        private List<File> photoFiles;
        private int startingPosition;
        private int position;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Integer... params) {

            position = params[0];

            photoItems = new ArrayList<>();
            photoFiles = new ArrayList<>();

            List<File> allFiles = getListFiles(new File(database.getDirectory(), "items"));
            if (allFiles != null) {
                for (File imageFile : allFiles) {
                    for (Item filteredItem : filteredItems) {
                        if (imageFile.getName().replace(".jpg", "").equals(String.valueOf(filteredItem.getId()))) {
                            photoItems.add(filteredItem);
                            photoFiles.add(imageFile);
                        }
                    }
                }
            }

            Item selected = filteredItems.get(position);
            startingPosition = -1;
            for (int i = 0; i < photoItems.size(); i++) {
                if (selected.getId() == photoItems.get(i).getId()) {
                    startingPosition = i;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (photoItems.size() > 0) {
                if (startingPosition != -1) {
                    fragmentDialog = new MainFragmentDialog();
                    fragmentDialog.setArgs(photoFiles, startingPosition, context, photoItems, database.getOrder(orderId));
                    fragmentDialog.show(getSupportFragmentManager(), "FragmentDialog");
                } else {
                    Utils.showAddItemCatalogDialog(context, database.getOrder(orderId).getClient().getId(), filteredItems.get(position), database, orderId);
                }
            } else {
                Utils.showAddItemCatalogDialog(context, database.getOrder(orderId).getClient().getId(), filteredItems.get(position), database, orderId);
            }
        }
    }

    private class AsyncGetMore extends AsyncTask<Void, Void, Void> {

        private Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            currentSeq = currentSeq + 30;
            filteredItems.addAll(database.getItemsSeq(currentSeq));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    private class AsyncQuery extends AsyncTask<Void, Void, Void> {

        String query = "";
        Dialog dialog;
        private boolean makeVisible = false;

        @Override
        protected void onPreExecute() {
            query = nameInput.getText().toString();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            filteredItems = new ArrayList<>();

            if (!currentBrand.getName().equals("Todas")) {
                currentSeq = 0;
                if (!currentCategory.getName().equals("Todas")) {
                    List<Item> items = database.getItemByCategoryAndBrand(currentCategory, currentBrand);
                    for (Item item : items) {
                        if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredItems.add(item);
                        }
                    }
                } else {
                    List<Item> items = database.getItemsByBrand(currentBrand);
                    for (Item item : items) {
                        if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredItems.add(item);
                        }
                    }
                }
            } else {
                if (!currentCategory.getName().equals("Todas")) {
                    currentSeq = 0;
                    List<Item> items = database.getItemsByCategory(currentCategory);
                    for (Item item : items) {
                        if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredItems.add(item);
                        }
                    }
                } else {
                    currentSeq = 0;
                    if (TextUtils.isEmpty(query)) {
                        makeVisible = true;
                        List<Item> items = database.getItemsSeq(currentSeq);
                        for (Item item : items) {
                            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                                filteredItems.add(item);
                            }
                        }
                    } else {
                        List<Item> items = database.getItems();
                        for (Item item : items) {
                            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                                filteredItems.add(item);
                            }
                        }
                    }
                }
            }

            if (currentBrand.getName().equals("Nuevos")) {
                currentSeq = 0;
                if (!currentCategory.getName().equals("Todas")) {
                    List<Item> items = database.getNewItemsByCategory(currentCategory);
                    for (Item item : items) {
                        if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredItems.add(item);
                        }
                    }
                } else {
                    List<Item> items = database.getNewItems();
                    for (Item item : items) {
                        if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredItems.add(item);
                        }
                    }
                }
            }

            Filter filter = new Filter();
            filter.setId(1);
            filter.setName(query);
            filter.setBrand(currentBrand);
            filter.setCategory(currentCategory);

            database.insertFilter(filter);

            adapter = new CatalogItemsAdapter(context, R.layout.item_catalog_item, filteredItems, database.getClient(clientId), getListFiles(new File(database.getDirectory(), "th")));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (makeVisible) {
                loadMoreButton.setVisibility(View.VISIBLE);
            } else {
                loadMoreButton.setVisibility(View.GONE);
            }
            if (filteredItems.size() > 0) {
                if (itemsGridView.getVisibility() == View.INVISIBLE) {
                    Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                    itemsGridView.startAnimation(slideUp);
                    itemsGridView.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(context, "No hay productos para tu busqueda", Toast.LENGTH_SHORT).show();
            }

            itemsGridView.setAdapter(adapter);
        }
    }

    private class AsyncSetup extends AsyncTask<Void, Void, Void> {

        Dialog dialog;
        Brand all = new Brand();
        Brand newBrand = new Brand();
        Category allCategories = new Category();
        String currentName = "";

        private BrandsAdapter adapter;
        private CategoriesAdapter categoriesAdapter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
            all.setId(800);
            all.setName("Todas");
            newBrand.setId(801);
            newBrand.setName("Nuevos");
            allCategories.setId(900);
            allCategories.setName("Todas");
        }

        @Override
        protected Void doInBackground(Void... params) {

            brands = new ArrayList<>();
            categories = new ArrayList<>();

            brands.add(all);
            brands.add(newBrand);
            brands.addAll(database.getBrands());

            currentBrand = all;
            currentCategory = allCategories;

            categories.add(allCategories);
            Filter filter = database.getCurrentFilter();

            if (filter != null) {
                currentBrand = filter.getBrand();
                switch (currentBrand.getName()) {
                    case "Todas":
                        categories.addAll(database.getCategories());
                        break;
                    case "Nuevos":
                        categories.addAll(database.getNewItemsCategories());
                        break;
                    default:
                        categories.addAll(database.getCategoriesByBrand(currentBrand.getId()));
                        break;
                }
                currentCategory = filter.getCategory();
                currentName = filter.getName();
            } else {
                categories.addAll(database.getCategories());
            }

            adapter = new BrandsAdapter(context, R.layout.item_brand, brands, getListFiles(new File(database.getDirectory(), "brands")));
            categoriesAdapter = new CategoriesAdapter(context, R.layout.item_category, categories, getListFiles(new File(database.getDirectory(), "brands")));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            currentBrandLabel.setText(currentBrand.getName());
            nameInput.setText(currentName);
            currentCategoryLabel.setText(currentCategory.getName());
            brandsGrid.setAdapter(adapter);
            categoriesGrid.setAdapter(categoriesAdapter);
            categoriesGrid.setVisibility(View.GONE);
            categoriesOpened = false;
        }
    }

}