package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.adapters.StorageOrderAdapter;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Roles;
import co.com.imagenybelleza.imagenybelleza.enums.States;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;

/*
* Actividad que muestra los pedidos en proceso de empaque, empacados o aprobados
* Muestra el progreso de empaque de cada pedido. Permite acceder al detalle de los
* mismos
*
* */

public class StorageActivity extends AppCompatActivity {

    private List<Order> orders;
    private ListView orderListView;
    private Context context;
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        context = StorageActivity.this;
        database = new DatabaseHelper(context);

        orderListView = (ListView) findViewById(R.id.order_list);
        orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(StorageActivity.this, StorageDetailActivity.class).putExtra("order_id", orders.get(position).getId()));
                finish();
            }
        });

        new AsyncGetOrdersFromServer().execute();

    }

    private class AsyncGetOrders extends AsyncTask<Void, Void, Void> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
            orders = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (database.getCurrentUser().getRole().equals(Roles.ROLE_PACKER) || database.getCurrentUser().getRole().equals(Roles.ROLE_ADMIN)) {
                orders = database.getStorageOrders();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            orderListView.setAdapter(new StorageOrderAdapter(context, R.layout.item_storage_order, orders));
        }
    }

    private class AsyncGetOrdersFromServer extends AsyncTask<Void, Void, Void> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (ConnectivityReceiver.isConnected()) {
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest request = new StringRequest(Request.Method.GET, database.getIpAdress() + Url.GET_STORAGE_ORDERS_SERVICE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("La respuesta: " + response);
                        try {
                            JSONObject mainObject = new JSONObject(response);
                            JSONArray ordersResponse = mainObject.getJSONArray("orders");
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

                                /*List<Order> ordersByDate = new ArrayList<>();
                                DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat currentFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                String todayDate = currentFormatter.format(new Date());

                                for (Order order : orders) {
                                    DateTime currentDate = format.parseDateTime(todayDate.toString());
                                    DateTime orderDate = format.parseDateTime(order.getModifiedDate());
                                    System.out.println("Fechas: " + currentDate.toString() + " - " + orderDate.toString());
                                    System.out.println("Dias: " + String.valueOf(Days.daysBetween(orderDate, currentDate).getDays()));
                                    if (Days.daysBetween(currentDate, orderDate).getDays() <= 15) {
                                        ordersByDate.add(order);
                                    }
                                }*/

                                database.insertOrders(orders);
                            }

                            JSONArray orderItemsResponse = mainObject.getJSONArray("order_items");
                            if (orderItemsResponse.length() > 0) {
                                List<OrderItem> orderItems = new ArrayList<>();
                                for (int i = 0; i < orderItemsResponse.length(); i++) {
                                    JSONObject orderItemObject = orderItemsResponse.getJSONObject(i);
                                    if ((database.getOrder(orderItemObject.getInt("order_id")) == null) || (database.getOrder(orderItemObject.getInt("order_id")) != null && database.getOrder(orderItemObject.getInt("order_id")).getState().getId() != States.ORDER_STATE_APPROVED)) {
                                        OrderItem orderItem = new OrderItem();
                                        orderItem.setNotes(orderItemObject.getString("notes"));
                                        orderItem.setModified(orderItemObject.getString("modified"));
                                        orderItem.setDiscount(orderItemObject.getDouble("discount"));
                                        orderItem.setFreeUnits(orderItemObject.getInt("free_units"));
                                        orderItem.setUnitPrice(orderItemObject.getInt("unit_price"));
                                        orderItem.setOrder(database.getOrder(orderItemObject.getInt("order_id")));
                                        orderItem.setItem(database.getItem(orderItemObject.getInt("item_id")));
                                        orderItem.setStorageNotes(orderItemObject.getString("storage_notes"));
                                        orderItem.setStorageUnits(orderItemObject.getInt("storage_units"));
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
                                        orderItems.add(orderItem);
                                    }
                                }

                                database.insertOrderItems(orderItems);
                                Utils.showSnackbar("Actualizado con exito", StorageActivity.this, R.id.activity_storage);
                            }
                        } catch (JSONException e) {
                            Utils.showSnackbar("Error en la respuesta del servidor", StorageActivity.this, R.id.activity_storage);
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.showSnackbar("Error interno del servidor", StorageActivity.this, R.id.activity_storage);
                        dialog.dismiss();
                    }
                }) {
                    @Override
                    public Priority getPriority() {
                        return Priority.IMMEDIATE;
                    }
                };
                queue.add(request);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            new AsyncGetOrders().execute();
        }
    }
}
