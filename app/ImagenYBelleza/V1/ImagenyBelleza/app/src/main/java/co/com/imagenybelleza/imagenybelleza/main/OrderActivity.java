package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.adapters.OrderAdapter;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Roles;
import co.com.imagenybelleza.imagenybelleza.enums.States;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;

/*
*
* Actividad que muestra la lista de pedidos con su respectivo estado
* Permite ingresar a la actividad de creacion de pedido para ser editado
* o simplemente visualizado. Desde estta actividad se realiza el envio de pedidos
* y de clientes pendientes de envio
*
* */

public class OrderActivity extends AppCompatActivity {

    private ListView pedidosListView;
    private Dialog dialog;
    private Context context;
    private List<Order> orders;
    private LinearLayout notFoundView;
    private DatabaseHelper database;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        context = OrderActivity.this;
        database = new DatabaseHelper(context);

        pedidosListView = (ListView) findViewById(R.id.pedidos_list_view);
        pedidosListView.setVisibility(View.GONE);
        notFoundView = (LinearLayout) findViewById(R.id.not_found_view);
        notFoundView.setVisibility(View.GONE);

        pedidosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (orders.get(position).getState().getId() != States.ORDER_STATE_CAPTURE
                        && orders.get(position).getState().getId() != States.ORDER_STATE_PENDING && orders.get(position).getState().getId() != States.ORDER_STATE_CANCELLED) {
                    Order order = orders.get(position);
                    startActivity(new Intent(OrderActivity.this, CreateOrderActivity.class).putExtra("order_id", order.getId()).putExtra("client_id", order.getClient().getId()));
                    finish();
                } else {
                    new UpdateOrderAsync().execute(orders.get(position));
                }
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OrderActivity.this, ClientActivity.class));
                finish();
            }
        });

        if (database.getCurrentUser().getRole().equals(Roles.ROLE_SELLER)) {
            new AsyncOrders().execute();
        }
        if (database.getCurrentUser().getRole().equals(Roles.ROLE_ADMIN)) {
            new AsyncOrdersAdmin().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.isGpsEnabled(context);
    }

    private JSONObject getPendingClients() throws JSONException {
        JSONObject mainObject = new JSONObject();
        JSONArray array = new JSONArray();
        List<Client> clients = database.getPendingClients();
        for (Client client : clients) {
            JSONObject object = new JSONObject();
            object.put("id", String.valueOf(client.getId()));
            object.put("code", String.valueOf(client.getCode()));
            object.put("company", client.getCompany());
            object.put("address", client.getAddress());
            object.put("city", String.valueOf(client.getCity().getId()));
            object.put("phone_one", client.getPhoneOne());
            object.put("phone_two", client.getPhoneTwo());
            object.put("phone_three", client.getPhoneThree());
            object.put("nit", client.getNit());
            object.put("mail", client.getMail());
            object.put("contact", client.getContact());
            object.put("client_type_id", String.valueOf(client.getClientType().getId()));
            object.put("neighborhood", client.getNeighborhood());
            object.put("user_id", String.valueOf(client.getUser().getId()));
            object.put("latitude", String.valueOf(client.getLatitude()));
            object.put("longitude", String.valueOf(client.getLongitude()));
            array.put(object);
        }
        mainObject.put("clients", array);
        System.out.println("All clients: " + mainObject.get("clients").toString());
        return mainObject;
    }

    private JSONObject getAllOrderItems() throws JSONException {
        JSONObject mainObject = new JSONObject();
        JSONArray array = new JSONArray();
        List<OrderItem> orderItems = new ArrayList<>();
        List<Order> orders = new ArrayList<>(this.orders);
        for (Order order : orders) {
            orderItems.clear();
            orderItems = database.getOrderItems(order.getId());
            for (OrderItem orderItem : orderItems) {
                if (orderItem.getOrder().getState().getId() == States.ORDER_STATE_PENDING) {
                    JSONObject object = new JSONObject();
                    object.put("order_id", order.getId());
                    object.put("item_id", orderItem.getItem().getId());
                    object.put("subitem_id", orderItem.getSubItemId());
                    object.put("units", orderItem.getUnits());
                    object.put("unit_price", orderItem.getUnitPrice());
                    object.put("free_units", orderItem.getFreeUnits());
                    object.put("storage_units", orderItem.getStorageUnits());
                    object.put("storage_notes", orderItem.getStorageNotes());
                    object.put("notes", orderItem.getNotes());
                    object.put("order_items_state_id", orderItem.getOrderItemsState().getId());
                    object.put("iva", orderItem.getIva());
                    object.put("discount", orderItem.getDiscount());
                    object.put("total", orderItem.getTotal());
                    object.put("value", orderItem.getValue());
                    object.put("subitem_name", orderItem.getSubItemName());
                    object.put("eq_value", orderItem.getEqValue());
                    array.put(object);
                }
            }
        }
        mainObject.put("allOrdersItems", array);
        System.out.println("Main all: " + mainObject.toString());
        return mainObject;
    }

    private JSONObject getAllJSONOrders() throws JSONException {
        JSONObject mainObject = new JSONObject();
        JSONArray array = new JSONArray();
        for (Order order : orders) {
            if (order.getState().getId() == States.ORDER_STATE_PENDING) {
                JSONObject object = new JSONObject();
                object.put("id", String.valueOf(order.getId()));
                object.put("made", order.getMade());
                object.put("deliver", order.getDeliver());
                object.put("notes", order.getNotes());
                object.put("payment_id", order.getPayment().getId());
                object.put("seller_id", String.valueOf(order.getSeller().getId()));
                object.put("client_id", String.valueOf(order.getClient().getId()));
                object.put("order_state_id", String.valueOf(States.ORDER_STATE_SENT));
                object.put("payment_notes", String.valueOf(order.getPaymentNotes()));
                array.put(object);
            }
        }
        mainObject.put("orders", array);
        System.out.println("Ordenes: " + mainObject.get("orders").toString());
        return mainObject;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_orders, menu);
        return true;
    }

    private boolean arePendingOrders() {
        for (Order order : orders) {
            if (order.getState().getId() == States.ORDER_STATE_PENDING) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            if (ConnectivityReceiver.isConnected()) {
                if (database.getPendingClients().size() == 0) {
                    sendData();
                } else {
                    sendPendingClients();
                }
            } else {
                Utils.showSnackbar("Activa tu conexion para enviar los pedidos", OrderActivity.this, R.id.order_layout);
            }
        }

        return true;
    }

    private void sendPendingClients() {
        final Dialog dialog = Utils.getAlertDialog(context);
        dialog.show();
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.CREATE_ALL_CLIENTS_SERVICE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    System.out.println("Response: " + response);
                    JSONObject object = new JSONObject(response);
                    String message = object.getString("mensaje");
                    if (message.equals("Creacion exitosa")) {
                        List<Client> clients = database.getPendingClients();
                        for (Client client : clients) {
                            client.setSent(true);
                        }
                        database.updatePendingClients(clients);
                        Utils.showSnackbar("Actualizacion exitosa", OrderActivity.this, R.id.order_layout);
                        sendData();
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        Utils.showSnackbar("Hubo un problema subiendo el pedido al servidor, intenta mas tarde", OrderActivity.this, R.id.order_layout);
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    Utils.showSnackbar("Error en la respuesta del servidor", OrderActivity.this, R.id.order_layout);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Utils.showSnackbar("Tiempo de espera con el servidor agotado", OrderActivity.this, R.id.order_layout);
                System.out.println("Error: " + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                try {
                    JSONObject object = getPendingClients();
                    params.put("clients", object.get("clients").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }

            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private void sendData() {
        if (arePendingOrders()) {
            if (ConnectivityReceiver.isConnected()) {
                final Dialog dialog = Utils.getAlertDialog(context);
                dialog.show();
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.CREATE_ORDERS_SERVICE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            System.out.println("Response: " + response);
                            JSONObject object = new JSONObject(response);
                            String message = object.getString("mensaje");
                            if (message.equals("Creacion exitosa")) {
                                Utils.showSnackbar("El pedido fue enviado correctamente al servidor", OrderActivity.this, R.id.order_layout);
                                for (Order order : orders) {
                                    if (order.getState().getId() == States.ORDER_STATE_PENDING) {
                                        order.setState(database.getOrderState(States.ORDER_STATE_SENT));
                                        order.setSent(true);
                                        database.updateOrderStateAndSent(order);
                                    }
                                }
                                Utils.showSnackbar("Actualizacion exitosa", OrderActivity.this, R.id.order_layout);
                                reload();
                                dialog.dismiss();
                            } else {
                                dialog.dismiss();
                                Utils.showSnackbar("Hubo un problema subiendo el pedido al servidor, intenta mas tarde", OrderActivity.this, R.id.order_layout);
                            }
                        } catch (JSONException e) {
                            dialog.dismiss();
                            Utils.showSnackbar("Error en la respuesta del servidor", OrderActivity.this, R.id.order_layout);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Utils.showSnackbar("Tiempo de espera con el servidor agotado", OrderActivity.this, R.id.order_layout);
                        System.out.println("Error: " + error.toString());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        try {
                            JSONObject object = getAllJSONOrders();
                            params.put("orders", object.get("orders").toString());
                            JSONObject items = getAllOrderItems();
                            params.put("items", items.get("allOrdersItems").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return params;
                    }

                    @Override
                    public Priority getPriority() {
                        return Priority.HIGH;
                    }
                };
                request.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(request);
            } else {
                Utils.showSnackbar("Debes conectarte a la red para enviar pedidos", OrderActivity.this, R.id.order_layout);
            }
        } else {
            Utils.showSnackbar("No hay ordenes pendientes por enviar", OrderActivity.this, R.id.order_layout);
        }
    }

    private void reload() {
        orders.clear();
        if (database.getCurrentUser().getRole().equals(Roles.ROLE_SELLER)) {
            new AsyncOrders().execute();
        }
        if (database.getCurrentUser().getRole().equals(Roles.ROLE_ADMIN)) {
            new AsyncOrdersAdmin().execute();
        }
    }

    private class UpdateOrderAsync extends AsyncTask<Order, Void, Order> {

        @Override
        protected void onPreExecute() {
            dialog = Utils.getAlertDialog(context);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Order doInBackground(Order... params) {
            Order order = params[0];
            order.setState(database.getOrderState(States.ORDER_STATE_CAPTURE));
            order.setModifiedDate(new Timestamp(System.currentTimeMillis()).toString());
            database.updateOrderState(order);
            database.updateOrderModifiedDate(order);
            return order;
        }

        @Override
        protected void onPostExecute(Order order) {
            super.onPostExecute(order);
            dialog.dismiss();
            startActivity(new Intent(OrderActivity.this, CreateOrderActivity.class).putExtra("order_id", order.getId()).putExtra("client_id", order.getClient().getId()));
            finish();
        }
    }

    private class AsyncOrdersAdmin extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            orders = database.getAllOrders();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (orders.size() > 0) {
                pedidosListView.setVisibility(View.VISIBLE);
                OrderAdapter adapter = new OrderAdapter(context, R.layout.item_order, orders);
                pedidosListView.setAdapter(adapter);
            } else {
                notFoundView.setVisibility(View.VISIBLE);
            }
        }
    }

    private class AsyncOrders extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            dialog = Utils.getAlertDialog(context);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                orders = database.getMyOrders();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (orders.size() > 0) {
                pedidosListView.setVisibility(View.VISIBLE);
                OrderAdapter adapter = new OrderAdapter(context, R.layout.item_order, orders);
                pedidosListView.setAdapter(adapter);
            } else {
                notFoundView.setVisibility(View.VISIBLE);
            }
        }
    }
}
