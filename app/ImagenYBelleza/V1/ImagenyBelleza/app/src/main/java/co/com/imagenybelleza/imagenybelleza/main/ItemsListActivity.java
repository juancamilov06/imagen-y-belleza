package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.adapters.ItemsAdapter;
import co.com.imagenybelleza.imagenybelleza.adapters.SubItemsAdapter;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Brand;
import co.com.imagenybelleza.imagenybelleza.models.Category;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Filter;
import co.com.imagenybelleza.imagenybelleza.models.Item;

public class ItemsListActivity extends AppCompatActivity {

    private Context context;
    private DatabaseHelper database;
    private ListView itemsListView;
    private List<Item> items;
    private int orderId;
    private int clientId;
    private EditText codeInput, nameInput;
    private Spinner brandsSpinner, categoriesSpinner;
    private List<Item> originalItems;
    private ItemsAdapter adapter;
    private float height;
    private LinearLayout searchView;
    private boolean isOpen = true;
    private boolean comesFromFilter = false;

    private Category currentCategory = null;
    private Brand currentBrand = null;

    private List<Brand> brands;
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);

        context = ItemsListActivity.this;
        database = new DatabaseHelper(context);
        orderId = getIntent().getIntExtra("order_id", 0);
        clientId = getIntent().getIntExtra("client_id", 0);

        searchView = (LinearLayout) findViewById(R.id.search_view);
        height = searchView.getHeight();

        codeInput = (EditText) findViewById(R.id.code_input);
        nameInput = (EditText) findViewById(R.id.name_input);

        brandsSpinner = (Spinner) findViewById(R.id.brands_spinner);
        brandsSpinner.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        brandsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                categories = new ArrayList<>();

                Category allCategories = new Category();
                allCategories.setId(900);
                allCategories.setName("Todas");
                categories.add(allCategories);

                currentBrand = brands.get(position);

                switch (currentBrand.getName()) {
                    case "Todas": {
                        categories.addAll(database.getCategories());
                        break;
                    }
                    case "Nuevos": {
                        categories.addAll(database.getNewItemsCategories());
                        break;
                    }
                    default: {
                        categories.addAll(database.getCategoriesByBrand(currentBrand.getId()));
                        break;
                    }
                }

                List<String> categoriesNames = getCategoriesNames(categories);
                categoriesSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_spinner_item, categoriesNames));

                if (currentCategory != null && comesFromFilter) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getName().equals(currentCategory.getName())) {
                            categoriesSpinner.setSelection(i);
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        categoriesSpinner = (Spinner) findViewById(R.id.categories_spinner);
        categoriesSpinner.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!comesFromFilter) {
                    currentCategory = categories.get(position);
                }
                comesFromFilter = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button searchButton = (Button) findViewById(R.id.search_item_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.hideKeyboard(context, ItemsListActivity.this);

                String name = nameInput.getText().toString();
                String code = codeInput.getText().toString();

                isOpen = false;
                Filter filter = new Filter();
                filter.setBrand(currentBrand);
                filter.setCategory(currentCategory);
                filter.setName(name);
                filter.setId(1);

                database.insertFilter(filter);

                items.clear();
                List<Item> itemsFiltered = new ArrayList<>();
                itemsFiltered.addAll(originalItems);
                if (!currentBrand.getName().equals("Todas")) {
                    if (!currentCategory.getName().equals("Todas")) {
                        for (Item item : itemsFiltered) {
                            if (Utils.contains(item.getName().toLowerCase(), name.toLowerCase()) && String.valueOf(item.getId()).contains(code)
                                    && item.getBrand().getName().toLowerCase().contains(currentBrand.getName().toLowerCase())
                                    && item.getCategory().getName().toLowerCase().contains(currentCategory.getName().toLowerCase())) {
                                items.add(item);
                            }
                        }

                    } else {
                        for (Item item : itemsFiltered) {
                            if (Utils.contains(item.getName().toLowerCase(), name.toLowerCase()) && String.valueOf(item.getId()).contains(code)
                                    && item.getBrand().getName().toLowerCase().contains(currentBrand.getName().toLowerCase())) {
                                items.add(item);
                            }
                        }
                    }
                } else {
                    if (!currentCategory.getName().equals("Todas")) {
                        for (Item item : itemsFiltered) {
                            if (Utils.contains(item.getName().toLowerCase(), name.toLowerCase()) && String.valueOf(item.getId()).contains(code)
                                    && item.getCategory().getName().toLowerCase().contains(currentCategory.getName().toLowerCase())) {
                                items.add(item);
                            }
                        }
                    } else {
                        for (Item item : itemsFiltered) {
                            if (Utils.contains(item.getName().toLowerCase(), name.toLowerCase()) && String.valueOf(item.getId()).contains(code)) {
                                items.add(item);
                            }
                        }
                    }
                }

                if (currentBrand.getName().equals("Nuevos")) {
                    if (!currentCategory.getName().equals("Todas")) {
                        List<Item> newItems = database.getNewItemsByCategory(currentCategory);
                        for (Item item : newItems) {
                            if (item.getName().toLowerCase().contains(name.toLowerCase())) {
                                items.add(item);
                            }
                        }
                    } else {
                        List<Item> newItems = database.getNewItems();
                        for (Item item : newItems) {
                            if (item.getName().toLowerCase().contains(name.toLowerCase())) {
                                items.add(item);
                            }
                        }
                    }
                }


                if (items.size() == 0) {
                    Toast.makeText(context, "No hay productos con los criterios seleccionados", Toast.LENGTH_LONG).show();
                } else {
                    database.insertFilter(filter);
                    searchView.setVisibility(View.GONE);
                }

                Client client = database.getClient(clientId);
                adapter = new ItemsAdapter(context, R.layout.item_item, items, client);
                itemsListView.setAdapter(adapter);
            }
        });

        ImageView clearCodeButton = (ImageView) findViewById(R.id.clear_code_button);
        clearCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeInput.setText("");
            }
        });

        ImageView clearNameButton = (ImageView) findViewById(R.id.clear_name_button);
        clearNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameInput.setText("");
            }
        });

        Button clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeInput.setText("");
                nameInput.setText("");
                brandsSpinner.setSelection(0);
                categoriesSpinner.setSelection(0);
                reload();
                database.deleteCurrentFilter();
            }
        });

        itemsListView = (ListView) findViewById(R.id.items_list);
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                final Item item = items.get(position);
                File file = new File(database.getDirectory());
                file = new File(file, "items");
                file = new File(file, String.valueOf(item.getId()));
                System.out.println("Archivo: " + file.getPath());
                if (file.isDirectory()) {
                    final Client client = database.getClient(clientId);
                    final List<Item> subItems = new ArrayList<>();
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
                        subItem.setDiscountFive(item.getDiscountFive());
                        subItem.setDiscountFour(item.getDiscountFour());
                        subItem.setDiscountThree(item.getDiscountThree());
                        subItem.setDiscountTwo(item.getDiscountTwo());
                        subItem.setDiscountOne(item.getDiscountOne());
                        subItem.setPaymentOne(item.getPaymentOne());
                        subItem.setPaymentTwo(item.getPaymentTwo());
                        subItem.setPaymentThree(item.getPaymentThree());
                        subItem.setPaymentFour(item.getPaymentFour());
                        if (client.getClientType().getId() == 1) {
                            discount = item.getDiscountOne();
                        }
                        if (client.getClientType().getId() == 2) {
                            discount = item.getDiscountTwo();
                        }
                        if (client.getClientType().getId() == 3) {
                            discount = item.getDiscountThree();
                        }
                        if (client.getClientType().getId() == 4) {
                            discount = item.getDiscountFour();
                        }
                        if (client.getClientType().getId() == 5) {
                            discount = item.getDiscountFive();
                        }
                        subItem.setPrice(item.getPrice() * (1 - discount / 100));
                        subItems.add(subItem);
                    }

                    final Dialog dialog = new Dialog(context, R.style.FullDialog);
                    final boolean[] fromButton = {false};
                    View dialogView = View.inflate(context, R.layout.dialog_sub_items, null);
                    dialog.setContentView(dialogView);
                    ListView subItemListView = (ListView) dialog.findViewById(R.id.subitem_list_view);
                    LinearLayout fab = (LinearLayout) dialog.findViewById(R.id.home_button);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fromButton[0] = true;
                            dialog.dismiss();
                            startActivity(new Intent(new Intent(ItemsListActivity.this, CreateOrderActivity.class).putExtra("client_id", clientId).putExtra("order_id", orderId)));
                            finish();
                        }
                    });

                    subItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                            Utils.showAddSubItemItemDialog(context, subItems.get(position), database, item, clientId, orderId);
                        }
                    });

                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (!fromButton[0]) {
                                startActivity(new Intent(ItemsListActivity.this, CreateOrderActivity.class).putExtra("client_id", clientId).putExtra("order_id", orderId));
                                finish();
                            }
                        }
                    });

                    subItemListView.setAdapter(new SubItemsAdapter(context, R.layout.item_item, subItems, client));
                    dialog.show();

                } else {
                    Utils.showAddItemDialog(context, clientId, items.get(position), database, orderId, ItemsListActivity.this);
                }

            }
        });

        setUpList();
        setSpinners();
        setFilters();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ItemsListActivity.this, CreateOrderActivity.class).putExtra("client_id", clientId).putExtra("order_id", orderId));
                finish();
            }
        });
    }

    private List<String> getCategoriesNames(List<Category> categories) {
        List<String> categoriesNames = new ArrayList<>();
        for (Category category : categories) {
            categoriesNames.add(category.getName());
        }
        return categoriesNames;
    }

    private void setFilters() {
        Filter filter = database.getCurrentFilter();
        if (filter != null) {

            comesFromFilter = true;
            currentCategory = filter.getCategory();
            nameInput.setText(filter.getName());
            currentBrand = filter.getBrand();

            if (currentBrand != null) {
                switch (currentBrand.getName()) {
                    case "Todas":
                        brandsSpinner.setSelection(0);
                        break;
                    case "Nuevos":
                        brandsSpinner.setSelection(1);
                        break;
                    default:
                        for (int i = 0; i < brands.size(); i++) {
                            if (currentBrand.getName().equals(brands.get(i).getName())) {
                                brandsSpinner.setSelection(i);
                            }
                        }
                        break;
                }
            }
        }
    }

    private void setUpList() {
        items = database.getItems();
        setOriginalList(items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_catalog:
                startActivity(new Intent(ItemsListActivity.this, CatalogActivity.class).putExtra("order_id", orderId).putExtra("client_id", clientId));
                finish();
                return true;
            case R.id.action_search:
                if (!isOpen) {
                    searchView.setVisibility(View.VISIBLE);
                    searchView.animate()
                            .translationY(height)
                            .alpha(1.0f);
                    isOpen = true;
                } else {
                    searchView.setVisibility(View.GONE);
                    searchView.animate()
                            .translationY(0)
                            .alpha(0.0f);
                    isOpen = false;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSpinners() {
        brands = new ArrayList<>();

        Brand all = new Brand();
        all.setId(800);
        all.setName("Todas");

        Brand newBrand = new Brand();
        newBrand.setId(801);
        newBrand.setName("Nuevos");

        brands.add(all);
        brands.add(newBrand);

        brands.addAll(database.getBrands());

        List<String> brandsNames = new ArrayList<>();

        for (Brand brand : brands) {
            brandsNames.add(brand.getName());
        }
        brandsSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_spinner_item, brandsNames));
    }

    private void reload() {
        items.clear();
        items.addAll(originalItems);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setOriginalList(List<Item> list) {
        this.originalItems = new ArrayList<>();
        originalItems.addAll(list);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ItemsListActivity.this, CreateOrderActivity.class).putExtra("client_id", clientId).putExtra("order_id", orderId));
        finish();
    }
}
