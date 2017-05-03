package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.application.CombellezaApp;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Brand;
import co.com.imagenybelleza.imagenybelleza.models.Category;
import co.com.imagenybelleza.imagenybelleza.models.City;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.ClientType;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;
import co.com.imagenybelleza.imagenybelleza.models.OrderItemsState;
import co.com.imagenybelleza.imagenybelleza.models.OrderState;
import co.com.imagenybelleza.imagenybelleza.models.Payment;
import co.com.imagenybelleza.imagenybelleza.models.User;
import co.com.imagenybelleza.imagenybelleza.services.LocationService;

/*
*
* Actividad que se muestra al iniciar la app, descarga todos los datos y los inserta localmente
* o actualiza si no es la primera vez que se ejecuta la app. Verifica tambien si la ip del servidor
* es correcta o no
*
* */

public class SplashActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private AVLoadingIndicatorView loadingIndicator;
    private RobotoRegularTextView progressLabel;
    private Context context;
    private DatabaseHelper database;
    private AsyncGetData asyncGetData;
    private UpdateAsync updateAsync;
    private LinearLayout downloadView, notConnectedView;
    private boolean delete = true;
    private Button retryButton, continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = SplashActivity.this;
        database = new DatabaseHelper(context);

        Utils.lockOrientation(SplashActivity.this);

        loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.loading_indicator);
        progressLabel = (RobotoRegularTextView) findViewById(R.id.progress_label);

        downloadView = (LinearLayout) findViewById(R.id.download_view);
        notConnectedView = (LinearLayout) findViewById(R.id.not_connected_view);

        continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFirstRun()) {
                    Utils.showSnackbar("Para usar la app la primera vez debes tener conexion a internet", SplashActivity.this, R.id.activity_splash);
                } else {
                    delete = false;
                    User user = database.getCurrentUser();
                    if (user != null) {
                        startService(new Intent(SplashActivity.this, LocationService.class));
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }
        });

        retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectivityReceiver.isConnected()) {
                    downloadView.setVisibility(View.VISIBLE);
                    notConnectedView.setVisibility(View.GONE);

                    asyncGetData = new AsyncGetData();
                    asyncGetData.execute();
                }
            }
        });

        asyncGetData = new AsyncGetData();
        asyncGetData.execute();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (asyncGetData != null) {
            if (!asyncGetData.isCancelled() && delete) {
                asyncGetData.cancel(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CombellezaApp.getInstance().setConnectivityListener(this);
    }

    private void setFirstTime(boolean firstTime) {
        SharedPreferences prefs = getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        prefs.edit().putBoolean("firstrun", firstTime).apply();
    }

    private boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        return prefs.getBoolean("firstrun", true);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (!isConnected) {
            if (asyncGetData != null) {
                if (!asyncGetData.isCancelled() && delete) {
                    asyncGetData.cancel(true);
                }
            }
            notConnectedView.setVisibility(View.VISIBLE);
            downloadView.setVisibility(View.GONE);
            Utils.showSnackbar("Se perdio la conexion a la red", SplashActivity.this, R.id.activity_splash);
        }
    }

    private void insertIp(String host) {
        if (database.getIpAdress() == null) {
            database.insertIp(host);
            System.out.println(database.getIpAdress());
        }
    }

    private void changeIpAddress() {
        final Dialog dialog = new Dialog(context, R.style.StyledDialog);
        View view = View.inflate(context, R.layout.dialog_ip, null);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        final TextInputEditText ipInput = (TextInputEditText) dialog.findViewById(R.id.ip_input);
        ipInput.setText(database.getIpAdress().replace("http://", ""));
        Button acceptButton = (Button) dialog.findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = ipInput.getText().toString();
                if (TextUtils.isEmpty(ip)) {
                    Utils.showSnackbar("Este campo no puede estar vacio", SplashActivity.this, R.id.activity_splash);
                    return;
                }

                if (database.insertIp("http://" + ip)) {
                    System.out.println("Ip nueva: " + database.getIpAdress());
                    Utils.showSnackbar("Ip cambiada con exito, conectando de nuevo...", SplashActivity.this, R.id.activity_splash);
                    asyncGetData.cancel(true);
                    asyncGetData = new AsyncGetData();
                    asyncGetData.execute();
                    dialog.dismiss();
                } else {
                    Utils.showSnackbar("Error cambiando la ip, intenta de nuevo", SplashActivity.this, R.id.activity_splash);
                }
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFirstRun()) {
                    dialog.dismiss();
                    delete = false;
                    User user = database.getCurrentUser();
                    if (user != null) {
                        startService(new Intent(SplashActivity.this, LocationService.class));
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                } else {
                    Utils.showSnackbar("La primera vez debe descargar datos", SplashActivity.this, R.id.activity_splash);
                }
            }
        });
        dialog.show();
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

    private class AsyncGetData extends AsyncTask<Void, Void, Void> {

        boolean disconnected = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingIndicator.setVisibility(View.VISIBLE);
            loadingIndicator.smoothToShow();
            progressLabel.setText("Descargando datos...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (isFirstRun()) {
                insertIp(Url.HOST);
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                path = new File(path, "combelleza");
                System.out.println("Path: " + path.getPath());
                if (!path.exists()) {
                    Utils.showSnackbar("La carpeta de subproductos no ha sido creada aun, creando...", SplashActivity.this, R.id.activity_splash);
                    boolean success = path.mkdirs();
                    if (success) {
                        database.insertDirectory(path.getPath());
                    } else {
                        Utils.showSnackbar("Error creando la carpeta, intenta de nuevo", SplashActivity.this, R.id.activity_splash);
                    }
                } else {
                    database.insertDirectory(path.getPath());
                    List<File> filesList = getListFiles(new File(database.getDirectory(), "items"));
                    if (filesList != null) {
                        if (filesList.size() > 0) {
                            Utils.showSnackbar("La carpeta creada contiene productos", SplashActivity.this, R.id.activity_splash);
                        } else {
                            Utils.showSnackbar("No se encontraron imágenes para el catálogo, verifique por favor", SplashActivity.this, R.id.activity_splash);
                        }
                    } else {
                        Utils.showSnackbar("No se encontraron imágenes para el catálogo, verifique por favor", SplashActivity.this, R.id.activity_splash);
                    }
                }

                if (ConnectivityReceiver.isConnected()) {

                    RequestQueue queue = Volley.newRequestQueue(context);
                    StringRequest request = new StringRequest(Request.Method.GET, database.getIpAdress() + Url.GET_SERVICE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            new InsertAsync().execute(response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error.networkResponse + " " + error.getMessage());
                            changeIpAddress();
                        }
                    });
                    request.setRetryPolicy(new DefaultRetryPolicy(
                            5000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(request);

                } else {
                    disconnected = true;
                }
            } else {
                if (ConnectivityReceiver.isConnected()) {
                    RequestQueue queue = Volley.newRequestQueue(context);
                    StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.CHECK_UPDATES_SERVICE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                System.out.println("Respuesta: " + response);
                                JSONObject object = new JSONObject(response);
                                String upd = object.optString("update");
                                System.out.println(upd);
                                if (!object.optString("update").equals("")) {
                                    delete = false;
                                    User user = database.getCurrentUser();
                                    if (user != null) {
                                        startService(new Intent(SplashActivity.this, LocationService.class));
                                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                } else {
                                    int count = object.getInt("count");
                                    System.out.println("Conteo: " + count);
                                    if (count > 0) {
                                        setFirstTime(true);
                                        updateAsync = new UpdateAsync();
                                        updateAsync.execute();
                                    } else {
                                        delete = false;
                                        User user = database.getCurrentUser();
                                        if (user != null) {
                                            startService(new Intent(SplashActivity.this, LocationService.class));
                                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse response = error.networkResponse;
                            if (error instanceof ServerError && response != null) {
                                try {
                                    String res = new String(response.data,
                                            HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                    JSONObject obj = new JSONObject(res);
                                } catch (UnsupportedEncodingException | JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            changeIpAddress();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("date", database.getLastModifiedDate());
                            System.out.println("Date " + database.getLastModifiedDate());
                            return params;
                        }

                        @Override
                        public Priority getPriority() {
                            return Priority.IMMEDIATE;
                        }
                    };
                    request.setRetryPolicy(new DefaultRetryPolicy(
                            5000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(request);
                } else {
                    delete = false;
                    User user = database.getCurrentUser();
                    if (user != null) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (disconnected) {
                notConnectedView.setVisibility(View.VISIBLE);
                downloadView.setVisibility(View.GONE);
            }
        }
    }

    private class InsertAsync extends AsyncTask<String, Void, Void> {

        String response = null;

        @Override
        protected Void doInBackground(String... params) {
            response = params[0];
            try {
                System.out.println(response);
                JSONObject object = new JSONObject(response);
                JSONArray clientTypesResponse = object.getJSONArray("types");
                if (clientTypesResponse.length() > 0) {
                    List<ClientType> clientTypes = new ArrayList<>();
                    for (int i = 0; i < clientTypesResponse.length(); i++) {
                        JSONObject typeObject = clientTypesResponse.getJSONObject(i);
                        ClientType clientType = new ClientType();
                        clientType.setId(typeObject.getInt("id"));
                        clientType.setName(typeObject.getString("name"));
                        clientTypes.add(clientType);
                    }
                    database.insertClientTypes(clientTypes);
                }

                JSONArray orderStatesResponse = object.getJSONArray("order_states");
                if (orderStatesResponse.length() > 0) {
                    List<OrderState> orderStates = new ArrayList<>();
                    for (int i = 0; i < orderStatesResponse.length(); i++) {
                        JSONObject stateObject = orderStatesResponse.getJSONObject(i);
                        OrderState state = new OrderState();
                        state.setId(stateObject.getInt("id"));
                        state.setState(stateObject.getString("state"));
                        state.setHexColor(stateObject.getString("hex_color"));
                        orderStates.add(state);
                    }
                    database.insertOrderStates(orderStates);
                }


                JSONArray orderItemsStatesResponse = object.getJSONArray("order_item_states");
                if (orderItemsStatesResponse.length() > 0) {
                    List<OrderItemsState> orderItemsStates = new ArrayList<>();
                    for (int i = 0; i < orderItemsStatesResponse.length(); i++) {
                        JSONObject stateObject = orderItemsStatesResponse.getJSONObject(i);
                        OrderItemsState state = new OrderItemsState();
                        state.setId(stateObject.getInt("id"));
                        state.setState(stateObject.getString("state"));
                        state.setHexColor(stateObject.getString("hex_color"));
                        orderItemsStates.add(state);
                    }
                    database.insertOrderItemStates(orderItemsStates);
                }

                JSONArray paymentsResponse = object.getJSONArray("payments");
                if (paymentsResponse.length() > 0) {
                    List<Payment> payments = new ArrayList<>();
                    for (int i = 0; i < paymentsResponse.length(); i++) {
                        JSONObject paymentObject = paymentsResponse.getJSONObject(i);
                        Payment payment = new Payment();
                        payment.setId(paymentObject.getInt("id"));
                        payment.setName(paymentObject.getString("name"));
                        payment.setTerm(paymentObject.getInt("term"));
                        payments.add(payment);
                    }
                    database.insertPayments(payments);
                }


                JSONArray brandsResponse = object.getJSONArray("brands");
                if (brandsResponse.length() > 0) {
                    List<Brand> brands = new ArrayList<>();
                    for (int i = 0; i < brandsResponse.length(); i++) {
                        JSONObject brandObject = brandsResponse.getJSONObject(i);
                        Brand brand = new Brand();
                        brand.setName(brandObject.getString("name"));
                        brand.setId(brandObject.getInt("id"));
                        brands.add(brand);
                    }
                    database.insertBrands(brands);
                }

                JSONArray categoriesResponse = object.getJSONArray("categories");
                if (categoriesResponse.length() > 0) {
                    List<Category> categories = new ArrayList<>();
                    for (int i = 0; i < categoriesResponse.length(); i++) {
                        JSONObject categoryObject = categoriesResponse.getJSONObject(i);
                        Category category = new Category();
                        category.setId(categoryObject.getInt("id"));
                        category.setName(categoryObject.getString("name"));
                        categories.add(category);
                    }
                    database.insertCategories(categories);
                }

                JSONArray citiesResponse = object.getJSONArray("cities");
                if (citiesResponse.length() > 0) {
                    List<City> cities = new ArrayList<>();
                    for (int i = 0; i < citiesResponse.length(); i++) {
                        JSONObject cityObject = citiesResponse.getJSONObject(i);
                        City city = new City();
                        city.setCity(cityObject.getString("city"));
                        city.setId(cityObject.getInt("id"));
                        cities.add(city);
                    }
                    database.insertCities(cities);
                }

                JSONArray usersResponse = object.getJSONArray("users");
                if (usersResponse.length() > 0) {
                    List<User> users = new ArrayList<>();
                    for (int i = 0; i < usersResponse.length(); i++) {
                        JSONObject userObject = usersResponse.getJSONObject(i);
                        User user = new User();
                        user.setContact(userObject.getString("contact"));
                        user.setIdentificator(userObject.getString("identificator"));
                        user.setId(userObject.getInt("id"));
                        System.out.println("Boolean " + userObject.getInt("is_active"));
                        user.setActive(userObject.getInt("is_active") > 0);
                        user.setRole(userObject.getString("role"));
                        user.setUsername(userObject.getString("username"));
                        users.add(user);
                    }
                    database.insertUsers(users);
                }

                JSONArray clientsResponse = object.getJSONArray("clients");
                if (clientsResponse.length() > 0) {
                    List<Client> clients = new ArrayList<>();
                    for (int i = 0; i < clientsResponse.length(); i++) {
                        JSONObject clientObject = clientsResponse.getJSONObject(i);
                        Client client = new Client();
                        client.setId(clientObject.getInt("id"));
                        client.setContact(clientObject.getString("contact"));
                        client.setPhoneTwo(clientObject.getString("phone_two"));
                        client.setPhoneThree(clientObject.getString("phone_three"));
                        client.setCode(clientObject.getInt("code"));
                        client.setCompany(clientObject.getString("company"));
                        client.setAddress(clientObject.getString("address"));
                        client.setNit(clientObject.getString("nit"));
                        client.setPhoneOne(clientObject.getString("phone_one"));
                        client.setMail(clientObject.getString("mail_address"));
                        client.setSent(true);
                        client.setActive(clientObject.getInt("is_active") > 0);
                        client.setNeighborhood(clientObject.getString("neighborhood"));
                        client.setLatitude(clientObject.getDouble("latitude"));
                        client.setLongitude(clientObject.getDouble("longitude"));
                        client.setUser(database.getUser(clientObject.getInt(("user_id"))));

                        ClientType clientType = database.getClientType(clientObject.getInt("client_type_id"));
                        client.setClientType(clientType);

                        City city = database.getCity(clientObject.getInt("city_id"));
                        client.setCity(city);

                        clients.add(client);
                    }
                    database.insertClients(clients);
                }

                JSONArray itemsResponse = object.getJSONArray("items");
                if (itemsResponse.length() > 0) {
                    List<Item> items = new ArrayList<>();
                    for (int i = 0; i < itemsResponse.length(); i++) {
                        JSONObject itemObject = itemsResponse.getJSONObject(i);
                        Item item = new Item();
                        item.setActive(itemObject.getInt("is_active") > 0);
                        item.setId(itemObject.getInt("id"));
                        item.setName(itemObject.getString("name"));
                        item.setNewItem(itemObject.getInt("is_new") > 0);
                        item.setSubItemId(itemObject.getInt("subitem_id"));
                        item.setPriceOne(itemObject.getDouble("price_one"));
                        item.setPriceTwo(itemObject.getDouble("price_two"));
                        item.setPriceThree(itemObject.getDouble("price_three"));
                        item.setPriceFour(itemObject.getDouble("price_four"));
                        item.setPriceFive(itemObject.getDouble("price_five"));
                        item.setPaymentOne(itemObject.getDouble("payment_one"));
                        item.setPaymentTwo(itemObject.getDouble("payment_two"));
                        item.setPaymentThree(itemObject.getDouble("payment_three"));
                        item.setPaymentFour(itemObject.getDouble("payment_four"));
                        item.setIva(itemObject.getDouble("iva"));

                        Category category = database.getCategory(itemObject.getInt("category_id"));
                        item.setCategory(category);

                        Brand brand = database.getBrand(itemObject.getInt("brand_id"));
                        item.setBrand(brand);
                        items.add(item);
                    }

                    database.insertItems(items);

                }

                JSONArray ordersResponse = object.getJSONArray("orders");
                if (ordersResponse.length() > 0) {
                    List<Order> orders = new ArrayList<>();
                    for (int i = 0; i < ordersResponse.length(); i++) {
                        JSONObject orderObject = ordersResponse.getJSONObject(i);
                        Order order = new Order();
                        order.setId(orderObject.getInt("id"));
                        order.setClient(database.getClient(orderObject.getInt("client_id")));
                        order.setDeliver(orderObject.getString("deliver"));
                        order.setInProgress(false);
                        order.setSent(true);
                        order.setMade(orderObject.getString("made"));
                        order.setNotes(orderObject.getString("notes"));
                        order.setPayment(database.getPayment(orderObject.getInt("payment_id")));
                        order.setModifiedDate(orderObject.getString("modified"));
                        order.setState(database.getOrderState(orderObject.getInt("order_state_id")));
                        order.setBiller(database.getUser(orderObject.getInt("biller_id")));
                        order.setSeller(database.getUser(orderObject.getInt("seller_id")));
                        orders.add(order);
                    }
                    database.insertOrders(orders);
                }

                JSONArray orderItemsResponse = object.getJSONArray("order_items");
                if (orderItemsResponse.length() > 0) {
                    List<OrderItem> orderItems = new ArrayList<>();
                    for (int i = 0; i < orderItemsResponse.length(); i++) {
                        JSONObject orderItemObject = orderItemsResponse.getJSONObject(i);
                        OrderItem orderItem = new OrderItem();
                        orderItem.setNotes(orderItemObject.getString("notes"));
                        orderItem.setModified(orderItemObject.getString("modified"));
                        orderItem.setDiscount(orderItemObject.getDouble("discount"));
                        orderItem.setStorageUnits(orderItemObject.getInt("storage_units"));
                        orderItem.setStorageNotes(orderItemObject.getString("storage_units"));
                        orderItem.setFreeUnits(orderItemObject.getInt("free_units"));
                        orderItem.setUnitPrice(orderItemObject.getInt("unit_price"));
                        orderItem.setOrder(database.getOrder(orderItemObject.getInt("order_id")));
                        orderItem.setItem(database.getItem(orderItemObject.getInt("item_id")));
                        orderItem.setSubItemId(orderItemObject.getInt("subitem_id"));
                        orderItem.setUnits(orderItemObject.getInt("units"));
                        orderItem.setIva(orderItemObject.getInt("iva"));
                        if (orderItem.getSubItemId() != 0) {
                            orderItem.setSubItemName(orderItemObject.getString("subitem_name"));
                        }
                        orderItem.setOrderItemsState(database.getOrderItemState(orderItemObject.getInt("order_items_state_id")));
                        if (!orderItemObject.isNull("packer_id")) {
                            orderItem.setPacker(database.getUser(orderItemObject.getInt("packer_id")));
                        } else {
                            orderItem.setPacker(null);
                        }
                        orderItem.setValue(orderItemObject.getDouble("value"));
                        orderItem.setTotal(orderItemObject.getDouble("total"));
                        orderItem.setStorageFreeUnits(orderItemObject.getInt("storage_free_units"));
                        orderItem.setEqValue(orderItemObject.getDouble("eq_value"));
                        orderItems.add(orderItem);
                    }
                    database.insertOrderItems(orderItems);
                }

                String date = object.getString("last_modified");
                Log.d("", "date: " + date);
                database.insertVersion(date);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setFirstTime(false);
            delete = false;

            User user = database.getCurrentUser();
            if (user != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }
    }

    private class UpdateAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressLabel.setText("Actualizando informacion");
        }

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.UPDATE_SERVICE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject object = new JSONObject(response);

                        JSONArray brandsResponse = object.getJSONArray("brands");
                        if (brandsResponse.length() > 0) {
                            List<Brand> brands = new ArrayList<>();
                            for (int i = 0; i < brandsResponse.length(); i++) {
                                JSONObject brandObject = brandsResponse.getJSONObject(i);
                                Brand brand = new Brand();
                                brand.setName(brandObject.getString("name"));
                                brand.setId(brandObject.getInt("id"));
                                brands.add(brand);
                            }
                            database.insertBrands(brands);
                        }

                        JSONArray categoriesResponse = object.getJSONArray("categories");
                        if (categoriesResponse.length() > 0) {
                            List<Category> categories = new ArrayList<>();
                            for (int i = 0; i < categoriesResponse.length(); i++) {
                                JSONObject categoryObject = categoriesResponse.getJSONObject(i);
                                Category category = new Category();
                                category.setId(categoryObject.getInt("id"));
                                category.setName(categoryObject.getString("name"));
                                categories.add(category);
                            }
                            database.insertCategories(categories);
                        }

                        JSONArray citiesResponse = object.getJSONArray("cities");
                        if (citiesResponse.length() > 0) {
                            List<City> cities = new ArrayList<>();
                            for (int i = 0; i < citiesResponse.length(); i++) {
                                JSONObject cityObject = citiesResponse.getJSONObject(i);
                                City city = new City();
                                city.setCity(cityObject.getString("city"));
                                city.setId(cityObject.getInt("id"));
                                cities.add(city);
                            }
                            database.insertCities(cities);
                        }

                        JSONArray usersResponse = object.getJSONArray("users");
                        if (usersResponse.length() > 0) {
                            List<User> users = new ArrayList<>();
                            for (int i = 0; i < usersResponse.length(); i++) {
                                JSONObject userObject = usersResponse.getJSONObject(i);
                                User user = new User();
                                user.setContact(userObject.getString("contact"));
                                user.setIdentificator(userObject.getString("identificator"));
                                user.setId(userObject.getInt("id"));
                                user.setActive(userObject.getBoolean("is_active"));
                                user.setRole(userObject.getString("role"));
                                user.setUsername(userObject.getString("username"));
                                users.add(user);
                            }
                            database.insertUsers(users);
                        }

                        JSONArray clientsResponse = object.getJSONArray("clients");
                        System.out.println("Clients response: " + clientsResponse.toString());
                        if (clientsResponse.length() > 0) {
                            List<Client> clients = new ArrayList<>();
                            for (int i = 0; i < clientsResponse.length(); i++) {
                                JSONObject clientObject = clientsResponse.getJSONObject(i);
                                Client client = new Client();
                                client.setId(clientObject.getInt("id"));
                                client.setContact(clientObject.getString("contact"));
                                client.setPhoneTwo(clientObject.getString("phone_two"));
                                client.setPhoneThree(clientObject.getString("phone_three"));
                                client.setCode(clientObject.getInt("code"));
                                client.setCompany(clientObject.getString("company"));
                                client.setAddress(clientObject.getString("address"));
                                client.setNit(clientObject.getString("nit"));
                                client.setPhoneOne(clientObject.getString("phone_one"));
                                client.setMail(clientObject.getString("mail_address"));
                                client.setSent(true);
                                client.setNeighborhood(clientObject.getString("neighborhood"));
                                client.setUser(database.getUser(clientObject.getInt("user_id")));

                                ClientType clientType = database.getClientType(clientObject.getInt("client_type_id"));
                                client.setClientType(clientType);

                                City city = database.getCity(clientObject.getInt("city_id"));
                                client.setCity(city);

                                clients.add(client);
                            }
                            database.insertClients(clients);
                        }

                        JSONArray itemsResponse = object.getJSONArray("items");
                        if (itemsResponse.length() > 0) {
                            List<Item> items = new ArrayList<>();
                            for (int i = 0; i < itemsResponse.length(); i++) {
                                JSONObject itemObject = itemsResponse.getJSONObject(i);
                                Item item = new Item();
                                item.setActive(itemObject.getInt("is_active") > 0);
                                item.setId(itemObject.getInt("id"));
                                item.setName(itemObject.getString("name"));
                                item.setNewItem(itemObject.getInt("is_new") > 0);
                                item.setSubItemId(itemObject.getInt("subitem_id"));
                                item.setPriceOne(itemObject.getDouble("price_one"));
                                item.setPriceTwo(itemObject.getDouble("price_two"));
                                item.setPriceThree(itemObject.getDouble("price_three"));
                                item.setPriceFour(itemObject.getDouble("price_four"));
                                item.setPriceFive(itemObject.getDouble("price_five"));
                                item.setPaymentOne(itemObject.getDouble("payment_one"));
                                item.setPaymentTwo(itemObject.getDouble("payment_two"));
                                item.setPaymentThree(itemObject.getDouble("payment_three"));
                                item.setPaymentFour(itemObject.getDouble("payment_four"));
                                item.setIva(itemObject.getDouble("iva"));

                                Category category = database.getCategory(itemObject.getInt("category_id"));
                                item.setCategory(category);

                                Brand brand = database.getBrand(itemObject.getInt("brand_id"));
                                item.setBrand(brand);
                                items.add(item);
                            }

                            database.insertItems(items);
                        }

                        JSONArray ordersResponse = object.getJSONArray("orders");
                        if (ordersResponse.length() > 0) {
                            List<Order> orders = new ArrayList<>();
                            for (int i = 0; i < ordersResponse.length(); i++) {
                                JSONObject orderObject = ordersResponse.getJSONObject(i);
                                Order order = new Order();
                                order.setId(orderObject.getInt("id"));
                                order.setClient(database.getClient(orderObject.getInt("client_id")));
                                order.setDeliver(orderObject.getString("deliver"));
                                order.setInProgress(false);
                                order.setSent(true);
                                order.setMade(orderObject.getString("made"));
                                order.setNotes(orderObject.getString("notes"));
                                order.setPayment(database.getPayment(orderObject.getInt("payment_id")));
                                order.setModifiedDate(orderObject.getString("modified"));
                                order.setState(database.getOrderState(orderObject.getInt("order_state_id")));
                                order.setBiller(null);
                                order.setSeller(database.getUser(orderObject.getInt("seller_id")));
                                orders.add(order);
                            }
                            database.insertOrders(orders);
                        }

                        JSONArray orderItemsResponse = object.getJSONArray("order_items");
                        if (orderItemsResponse.length() > 0) {
                            List<OrderItem> orderItems = new ArrayList<>();
                            for (int i = 0; i < orderItemsResponse.length(); i++) {
                                JSONObject orderItemObject = orderItemsResponse.getJSONObject(i);
                                OrderItem orderItem = new OrderItem();
                                orderItem.setNotes(orderItemObject.getString("notes"));
                                orderItem.setModified(orderItemObject.getString("modified"));
                                orderItem.setDiscount(orderItemObject.getDouble("discount"));
                                orderItem.setFreeUnits(orderItemObject.getInt("free_units"));
                                orderItem.setUnitPrice(orderItemObject.getInt("unit_price"));
                                orderItem.setOrder(database.getOrder(orderItemObject.getInt("order_id")));
                                orderItem.setItem(database.getItem(orderItemObject.getInt("item_id")));
                                orderItem.setSubItemId(orderItemObject.getInt("subitem_id"));
                                orderItem.setUnits(orderItemObject.getInt("units"));
                                orderItem.setStorageUnits(orderItemObject.getInt("storage_units"));
                                orderItem.setStorageNotes(orderItemObject.getString("storage_units"));
                                orderItem.setIva(orderItemObject.getInt("iva"));
                                if (orderItem.getSubItemId() != 0) {
                                    orderItem.setSubItemName(orderItemObject.getString("subitem_name"));
                                }
                                orderItem.setOrderItemsState(database.getOrderItemState(orderItemObject.getInt("order_items_state_id")));
                                if (!orderItemObject.isNull("packer_id")) {
                                    orderItem.setPacker(database.getUser(orderItemObject.getInt("packer_id")));
                                } else {
                                    orderItem.setPacker(null);
                                }
                                orderItem.setValue(orderItemObject.getDouble("value"));
                                orderItem.setTotal(orderItemObject.getDouble("total"));
                                orderItem.setStorageFreeUnits(orderItemObject.getInt("storage_free_units"));
                                orderItem.setEqValue(orderItemObject.getDouble("eq_value"));
                                orderItems.add(orderItem);
                            }
                            database.insertOrderItems(orderItems);
                        }


                        String date = object.getString("last_modified");
                        Log.d("", "date: " + date);
                        database.insertVersion(date);
                        setFirstTime(false);

                        delete = false;

                        User user = database.getCurrentUser();
                        if (user != null) {
                            startService(new Intent(SplashActivity.this, LocationService.class));
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && response != null) {
                        try {
                            String res = new String(response.data,
                                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            // Now you can use any deserializer to make sense of data
                            JSONObject obj = new JSONObject(res);
                        } catch (UnsupportedEncodingException e1) {
                            // Couldn't properly decode data to string
                            e1.printStackTrace();
                        } catch (JSONException e2) {
                            // returned data is not JSONObject?
                            e2.printStackTrace();
                        }
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("date", database.getLastModifiedDate());
                    System.out.println("Date" + database.getLastModifiedDate());
                    return params;
                }

                @Override
                public Priority getPriority() {
                    return Priority.IMMEDIATE;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
            return null;
        }
    }

}
