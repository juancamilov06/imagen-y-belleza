package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.ArraySwipeAdapter;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.application.CombellezaApp;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.States;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.CircleView;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;

/*
* Actividad que maneja el listado de pedidos que puede
Ver el facturador. Desde aqui se accede al modulo de detalle
de facturacion donde se puede aprobar, rechazar y finalizar un
pedido
*/

public class BillActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private Context context;
    private DatabaseHelper database;
    private ListView unbilledListView;
    private List<Order> orders;
    private LinearLayout notFoundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        context = BillActivity.this;
        database = new DatabaseHelper(context);

        Utils.lockOrientation(BillActivity.this);

        unbilledListView = (ListView) findViewById(R.id.unbilled_list_view);
        unbilledListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order = orders.get(position);
                if (order.getState().getId() != States.ORDER_STATE_FINISHED) {
                    startActivity(new Intent(BillActivity.this, BillDetailActivity.class).putExtra("order_id", order.getId())
                            .putExtra("client_id", order.getClient().getId()));
                    finish();
                } else {

                }
            }
        });
        notFoundView = (LinearLayout) findViewById(R.id.not_found_view);

        if (ConnectivityReceiver.isConnected()) {
            new CheckUnsentBilledAsync().execute();
        } else {
            new GetSentOrdersAsync().execute();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bill, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                new GetSentOrdersFromServerAsync().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CombellezaApp.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            new CheckUnsentBilledAsync().execute();
        }
    }

    private JSONObject getBilledParams(List<Order> orders) throws JSONException {
        JSONObject mainObject = new JSONObject();
        JSONArray array = new JSONArray();
        for (Order order : orders) {
            JSONObject orderObject = new JSONObject();
            orderObject.put("id", String.valueOf(order.getId()));
            orderObject.put("state_id", String.valueOf(order.getState().getId()));
            orderObject.put("biller_id", String.valueOf(database.getCurrentUser().getId()));
            array.put(orderObject);
        }
        mainObject.put("orders", array);
        System.out.println("Main Object: " + mainObject.get("orders").toString());
        return mainObject;
    }

    private class UnbilledSwipeAdapter extends ArraySwipeAdapter<Order> {

        private Context context;
        private int resource;
        private List<Order> orders;

        UnbilledSwipeAdapter(Context context, int resource, List<Order> orders) {
            super(context, resource, orders);
            this.context = context;
            this.resource = resource;
            this.orders = orders;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {

                holder = new ViewHolder();
                convertView = View.inflate(context, resource, null);
                holder.layout = (SwipeLayout) convertView.findViewById(R.id.swipe_layout);
                holder.datePriceLabel = (RobotoRegularTextView) convertView.findViewById(R.id.date_price_label);
                holder.clientCompanyLabel = (RobotoLightTextView) convertView.findViewById(R.id.client_company_label);
                holder.stateLabel = (RobotoRegularTextView) convertView.findViewById(R.id.state_label);
                holder.stateView = (CircleView) convertView.findViewById(R.id.state_view);
                holder.billButton = (LinearLayout) convertView.findViewById(R.id.bill_button);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Order order = orders.get(position);
            if (order != null) {
                holder.layout.setShowMode(SwipeLayout.ShowMode.LayDown);
                if (order.getState().getId() != States.ORDER_STATE_SENT) {
                    holder.layout.setSwipeEnabled(false);
                }
                holder.datePriceLabel.setText(order.getModifiedDate() + " - $" + String.format("%,.0f", getOrderTotal(order.getId())));
                holder.clientCompanyLabel.setText(order.getClient().getContact() + " - " + order.getClient().getCompany());
                holder.stateLabel.setText(order.getState().getState());
                if (order.getState().getHexColor() == null) {
                    holder.stateView.setCircleColor(Color.parseColor("#FFFFFF"));
                } else {
                    holder.stateView.setCircleColor(Color.parseColor(order.getState().getHexColor()));
                }
                holder.billButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (order.getState().getId() == States.ORDER_STATE_SENT) {
                            if (ConnectivityReceiver.isConnected()) {
                                order.setState(database.getOrderState(States.ORDER_STATE_APPROVED));
                                order.setSent(false);
                                new SendBilledOrderAsync().execute(order);
                            } else {
                                order.setState(database.getOrderState(States.ORDER_STATE_APPROVED));
                                order.setSent(false);
                                database.updateOrderStateAndSent(order);
                                Utils.showSnackbar("No se detecto conexion a internet, \n active la conexion para enviar \n automaticamente las facturaciones pendientes", BillActivity.this, R.id.activity_bill);
                                new GetSentOrdersAsync().execute();
                            }
                        } else {
                            Utils.showSnackbar("La order ya se facturo pero esta pendiente de envio \n Active la conexion para enviarla automaticamente", BillActivity.this, R.id.activity_bill);
                        }
                    }
                });
            }

            return convertView;
        }

        private double getOrderTotal(int id) {
            double totalOrder = 0;
            double discount = 0;
            List<OrderItem> orderItems = database.getOrderItems(id);
            for (OrderItem orderItem : orderItems) {
                totalOrder += orderItem.getTotal();
            }
            for (OrderItem orderItem : orderItems) {
                if (orderItem.getSubItemId() == 0) {
                    discount += (database.getParentPrice(orderItem.getItem().getId(), orderItem.getOrder().getClient().getClientType().getId()) - orderItem.getValue()) * orderItem.getUnits();
                }
            }
            return totalOrder;
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return 0;
        }

        private class ViewHolder {
            RobotoRegularTextView datePriceLabel, stateLabel;
            RobotoLightTextView clientCompanyLabel;
            CircleView stateView;
            LinearLayout billButton;
            SwipeLayout layout;
        }

    }

    private class GetSentOrdersFromServerAsync extends AsyncTask<Void, Void, Void> {

        Dialog dialog = Utils.getAlertDialog(context);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (ConnectivityReceiver.isConnected()) {
                if (database.getSentOrders().size() == 0) {
                    RequestQueue queue = Volley.newRequestQueue(context);
                    StringRequest request = new StringRequest(Request.Method.GET, database.getIpAdress() + Url.GET_UNBILLED_ORDERS_SERVICE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            dialog.dismiss();
                            System.out.println(response);
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
                                        order.setBiller(database.getUser(orderObject.optInt("biller_id")));
                                        order.setSeller(database.getUser(orderObject.getInt("seller_id")));
                                        orders.add(order);
                                    }

                                    List<Order> ordersByDate = new ArrayList<>();
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
                                    }

                                    database.insertOrders(ordersByDate);
                                }

                                JSONArray orderItemsResponse = mainObject.getJSONArray("order_items");
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

                                    database.insertOrderItems(orderItems);
                                    Utils.showSnackbar("Actualizado con exito", BillActivity.this, R.id.activity_bill);

                                }
                            } catch (JSONException e) {
                                dialog.dismiss();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            Utils.showSnackbar("Error en el servidor, intenta de nuevo", BillActivity.this, R.id.activity_bill);
                        }
                    }) {
                        @Override
                        public Priority getPriority() {
                            return Priority.IMMEDIATE;
                        }
                    };
                    queue.add(request);
                } else {
                    dialog.dismiss();
                }
            } else {
                dialog.dismiss();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new GetSentOrdersAsync().execute();
        }
    }

    private class CheckUnsentBilledAsync extends AsyncTask<Void, Void, Void> {

        Dialog dialog;
        List<Order> orders = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            orders = database.getBilledButUnsentOrders();
            if (orders.size() > 0) {
                if (ConnectivityReceiver.isConnected()) {
                    RequestQueue queue = Volley.newRequestQueue(context);
                    StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.UPDATE_ORDERS_SERVICE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                dialog.dismiss();
                                System.out.println("Respuesta: " + response);
                                JSONObject object = new JSONObject(response);
                                String message = object.getString("mensaje");
                                if (message.equals("Creacion exitosa")) {
                                    boolean error = false;
                                    for (Order order : orders) {
                                        order.setSent(true);
                                        if (!database.updateOrderStateAndSent(order)) {
                                            error = true;
                                        }
                                    }
                                    if (!error) {
                                        Utils.showSnackbar("Facturacion de ordernes pendientes exitosa", BillActivity.this, R.id.activity_bill);
                                        new GetSentOrdersAsync().execute();
                                    } else {
                                        Utils.showSnackbar("Actualizacion correcta en base de datos pero no en dispositivo, elimine los datos de la aplicacion y reiniciela por favor", BillActivity.this, R.id.activity_bill);
                                    }
                                } else {
                                    Utils.showSnackbar("Error en el servidor, intente de nuevo", BillActivity.this, R.id.activity_bill);
                                }
                            } catch (JSONException e) {
                                dialog.dismiss();
                                Utils.showSnackbar("Error en la respuesta del servidor, intente de nuevo", BillActivity.this, R.id.activity_bill);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            Utils.showSnackbar("Error en el servidor, intente de nuevo", BillActivity.this, R.id.activity_bill);
                        }
                    }) {
                        @Override
                        public Priority getPriority() {
                            return Priority.IMMEDIATE;
                        }

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            try {
                                Map<String, String> params = new HashMap<>();
                                JSONObject object = getBilledParams(orders);
                                params.put("orders", object.get("orders").toString());
                                params.put("array", String.valueOf(1));
                                return params;
                            } catch (JSONException e) {
                                return null;
                            }
                        }
                    };
                    queue.add(request);
                } else {
                    dialog.dismiss();
                    Utils.showSnackbar("No hay facturaciones pendientes por enviar", BillActivity.this, R.id.activity_bill);
                }
            } else {
                dialog.dismiss();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new GetSentOrdersFromServerAsync().execute();
        }
    }

    private class SendBilledOrderAsync extends AsyncTask<Order, Void, Void> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Order... params) {
            final Order order = params[0];
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.UPDATE_ORDER_SERVICE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        System.out.println(response);
                        JSONObject object = new JSONObject(response);
                        String message = object.getString("mensaje");
                        if (message.equals("Creacion exitosa")) {
                            order.setSent(true);
                            if (database.updateOrderStateAndSent(order)) {
                                dialog.dismiss();
                                Utils.showSnackbar("Facturacion exitosa", BillActivity.this, R.id.activity_bill);
                                new GetSentOrdersAsync().execute();
                            } else {
                                dialog.dismiss();
                                Utils.showSnackbar("Actualizacion correcta en base de datos pero no en dispositivo, elimine los datos de la aplicacion y reiniciela por favor", BillActivity.this, R.id.activity_bill);
                            }
                        } else {
                            dialog.dismiss();
                            Utils.showSnackbar("Error en el servidor, intente de nuevo", BillActivity.this, R.id.activity_bill);
                        }
                    } catch (JSONException e) {
                        dialog.dismiss();
                        Utils.showSnackbar("No se pudo enviar la factura al servidor, intente de nuevo", BillActivity.this, R.id.activity_bill);
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismiss();
                }
            }) {
                @Override
                public Priority getPriority() {
                    return Priority.IMMEDIATE;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", String.valueOf(order.getId()));
                    params.put("biller_id", String.valueOf(database.getCurrentUser().getId()));
                    params.put("state_id", String.valueOf(order.getState().getId()));
                    params.put("array", String.valueOf(0));
                    return params;
                }
            };
            queue.add(request);
            return null;
        }
    }

    private class GetSentOrdersAsync extends AsyncTask<Void, Void, Void> {

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
            orders = database.getSentOrders();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (orders.size() > 0) {
                unbilledListView.setAdapter(new UnbilledSwipeAdapter(context, R.layout.item_unbilled_order, orders));
            } else {
                unbilledListView.setVisibility(View.GONE);
                notFoundView.setVisibility(View.VISIBLE);
            }
            Utils.unlockOrientation(BillActivity.this);
        }
    }
}
