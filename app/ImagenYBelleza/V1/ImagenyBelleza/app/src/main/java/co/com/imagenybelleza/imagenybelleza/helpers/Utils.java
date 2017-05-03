package co.com.imagenybelleza.imagenybelleza.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.ItemStates;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.main.CreateOrderActivity;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;
import co.com.imagenybelleza.imagenybelleza.models.UserLocation;
import co.com.imagenybelleza.imagenybelleza.services.LocationService;

/**
 * Clase para implemenar metodos estaticos utiles en varios modulos
 * de la aplicacion
 */

public class Utils {

    //verifica si el gps esta activado
    public static void isGpsEnabled(final Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Tu GPS esta desactivado, debes activarlo para poder continuar")
                    .setCancelable(false)
                    .setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    //Bloquea la orientacion del celular
    public static void lockOrientation(Activity activity) {
        final int orientation = activity.getResources().getConfiguration().orientation;
        final int rotation = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
        }
    }

    //Desbloquea la orientacion del celular
    public static void unlockOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    //Envia la localizacion enviada a la CDB
    public static void sendLocation(Location location, Context context) {
        final DatabaseHelper database = new DatabaseHelper(context);
        if (database.getCurrentUser() != null) {
            database.insertLocation(location, database.getCurrentUser());
            if (ConnectivityReceiver.isConnected()) {
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.CREATE_LOCATION_SERVICE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Location response: ", response);
                        try {
                            JSONObject object = new JSONObject(response);
                            String mensaje = object.getString("mensaje");
                            if (mensaje.equals("Creacion exitosa")) {
                                database.deleteLocations();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null) {
                            if (error.getMessage() != null) {
                                Log.d("Error: ", error.getMessage());
                            }
                        }
                    }
                }) {
                    @Override
                    public Priority getPriority() {
                        return Priority.HIGH;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        try {
                            JSONObject object = Utils.getSavedLocations(database.getLocations());
                            params.put("locations", object.get("locations").toString());
                            return params;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                };
                queue.add(request);
            }
        } else {
            context.stopService(new Intent(context, LocationService.class));
        }
    }

    //Metodo de comparacion de texto tipo like de SQL
    public static boolean contains(String container, String subs) {

        if (!TextUtils.isEmpty(subs)) {
            String[] containerArray = container.split(" ");
            String[] subsArray = subs.split(" ");

            int coincidences = 0;

            for (String cont : containerArray) {
                for (String sub : subsArray) {
                    if (cont.contains(sub)) {
                        coincidences++;
                    }
                }
            }

            if (coincidences == subsArray.length) {
                return true;
            }

            return false;
        } else {
            return true;
        }
    }

    //Actualiza una lista de productos en la CDB (Usado en bodega)
    public static void updateOrderItems(List<OrderItem> orderItems, final int orderId, Context context) {
        final DatabaseHelper database = new DatabaseHelper(context);
        database.insertTempOrderItems(orderItems);
        if (ConnectivityReceiver.isConnected()) {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.UPDATE_ORDER_ITEMS_SERVICE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("OI response: ", response);
                    try {
                        JSONObject object = new JSONObject(response);
                        String mensaje = object.getString("mensaje");
                        if (mensaje.equals("Creacion exitosa")) {
                            database.deleteTempOrderItems();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
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
                        JSONObject object = getSavedOrderItems(database.getTempOrderItems(orderId));
                        params.put("order_items", object.get("order_items").toString());
                        JSONObject orders = getSavedOrders(database.getTempOrders());
                        params.put("orders", orders.get("orders").toString());
                        return params;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
            queue.add(request);
        }
    }

    //Actualiza todos los productos de un pedido en la CDB (Usado en bodega)
    public static void updateAll(Context context, final int orderId) {
        final DatabaseHelper database = new DatabaseHelper(context);
        if (ConnectivityReceiver.isConnected()) {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.UPDATE_ORDER_ITEMS_SERVICE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("OI response: " + response);
                    try {
                        JSONObject object = new JSONObject(response);
                        String mensaje = object.getString("mensaje");
                        if (mensaje.equals("Creacion exitosa")) {
                            database.deleteTempOrderItems();
                            database.deleteTempOrderStates();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error.getMessage());
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
                        JSONObject object = getSavedOrderItems(database.getTempOrderItems(orderId));
                        params.put("order_items", object.get("order_items").toString());
                        JSONObject orders = getSavedOrders(database.getTempOrders());
                        params.put("orders", orders.get("orders").toString());
                        System.out.println("Order items sent: " + object.get("order_items").toString());
                        return params;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
            queue.add(request);
        }
    }



    //Actualiza un solo en la CDB (Usado en bodega)
    public static void updateOrderItem(final OrderItem item, final Context context) {
        final DatabaseHelper database = new DatabaseHelper(context);
        database.insertTempOrderItem(item);
        database.insertTempOrder(item.getOrder());
        if (ConnectivityReceiver.isConnected()) {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.UPDATE_ORDER_ITEMS_SERVICE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("OI response: " + response);
                    try {
                        JSONObject object = new JSONObject(response);
                        String mensaje = object.getString("mensaje");
                        if (mensaje.equals("Creacion exitosa")) {
                            database.deleteTempOrderItems();
                            database.deleteTempOrderStates();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error.getMessage());
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
                        JSONObject object = getSavedOrderItems(database.getTempOrderItems(item.getOrder().getId()));
                        params.put("order_items", object.get("order_items").toString());
                        JSONObject orders = getSavedOrders(database.getTempOrders());
                        params.put("orders", orders.get("orders").toString());
                        System.out.println("Order items sent: " + object.get("order_items").toString());
                        return params;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
            queue.add(request);
        }
    }

    //Obtiene los pedidos guardados en formato JSON
    private static JSONObject getSavedOrders(List<Order> orders) throws JSONException {
        if (orders.size() > 0) {
            JSONObject mainObject = new JSONObject();
            JSONArray array = new JSONArray();
            for (Order order : orders) {
                JSONObject object = new JSONObject();
                object.put("order_id", order.getId());
                object.put("order_state_id", order.getState().getId());
                array.put(object);
            }
            mainObject.put("orders", array);
            return mainObject;
        } else {
            JSONObject mainObject = new JSONObject();
            JSONObject object = new JSONObject();
            object.put("orders", "empty");
            mainObject.put("orders", object);
            return mainObject;
        }
    }

    //Obtiene los productos guardados de los pedidos en formato JSON
    private static JSONObject getSavedOrderItems(List<OrderItem> orderItems) throws JSONException {
        JSONObject mainObject = new JSONObject();
        JSONArray array = new JSONArray();
        for (OrderItem item : orderItems) {
            JSONObject object = new JSONObject();
            object.put("order_id", String.valueOf(item.getOrder().getId()));
            object.put("item_id", String.valueOf(item.getItem().getId()));
            object.put("subitem_id", String.valueOf(item.getSubItemId()));
            object.put("storage_units", String.valueOf(item.getStorageUnits()));
            object.put("storage_notes", item.getStorageNotes());
            object.put("order_items_state_id", String.valueOf(item.getOrderItemsState().getId()));
            object.put("units", String.valueOf(item.getUnits()));
            object.put("total", String.valueOf(item.getTotal()));
            object.put("free_units", String.valueOf(item.getFreeUnits()));
            array.put(object);
        }
        mainObject.put("order_items", array);
        return mainObject;
    }

    //Obtiene las ubicaciones guardadas en formato JSON
    private static JSONObject getSavedLocations(List<UserLocation> locations) throws JSONException {
        JSONObject mainObject = new JSONObject();
        JSONArray array = new JSONArray();
        for (UserLocation location : locations) {
            JSONObject object = new JSONObject();
            object.put("latitude", location.getLatitude());
            object.put("longitude", location.getLongitude());
            object.put("created", location.getCreated());
            object.put("seller_id", location.getSeller().getId());
            array.put(object);
        }

        mainObject.put("locations", array);
        return mainObject;
    }

    //Muestra el mensaje de SnackBar en todos los modulos de la app
    public static void showSnackbar(String message, Activity context, int id) {
        Snackbar.make(context.findViewById(id), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    //Verifica si un email es valido mas no que exista
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    //Obtiene la fecha actual
    public static String getCurrentDate() {
        Calendar date = new GregorianCalendar();
        String year = String.valueOf(date.get(Calendar.YEAR));
        String month = String.valueOf(date.get(Calendar.MONTH) + 1);
        String day = String.valueOf(date.get(Calendar.DAY_OF_MONTH));
        String hour = String.valueOf(date.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(date.get(Calendar.MINUTE));
        String second = String.valueOf(date.get(Calendar.SECOND));
        return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
    }

    public static void scrollToBottom(ScrollView scrollLayout) {
        View lastChild = scrollLayout.getChildAt(scrollLayout.getChildCount() - 1);
        int bottom = lastChild.getBottom() + scrollLayout.getPaddingBottom();
        int sy = scrollLayout.getScrollY();
        int sh = scrollLayout.getHeight();
        int delta = bottom - (sy + sh);

        scrollLayout.smoothScrollBy(0, delta);
    }

    //Obtiene el id de un nuevo usuario
    public static int getUserId() {
        Calendar date = new GregorianCalendar();
        String day = String.valueOf(date.get(Calendar.DAY_OF_MONTH));
        String hour = String.valueOf(date.get(Calendar.HOUR));
        String minute = String.valueOf(date.get(Calendar.MINUTE));
        String second = String.valueOf(date.get(Calendar.SECOND));
        return Integer.valueOf(day + hour + minute + second);
    }

    //Verifica si un String puede ser entero
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    //Muestra los dialogos de adicion de productos a un pedido
    public static void showAddItemDialog(final Context context, final int clientId, final Item it
            , final DatabaseHelper database, final int orderId, final AppCompatActivity currentActivity) {

        final Dialog dialog = new Dialog(context, R.style.StyledDialog);
        View dialogView = View.inflate(context, R.layout.dialog_add_item, null);
        dialog.setContentView(dialogView);

        final RobotoLightTextView eqPriceLabel = (RobotoLightTextView) dialog.findViewById(R.id.eq_price_label);
        eqPriceLabel.setText(String.valueOf(0));

        final TextInputEditText priceInput = (TextInputEditText) dialog.findViewById(R.id.price_input);
        priceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!priceInput.isFocusable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cambio de precio");
                    builder.setMessage("¿Estas seguro de cambiar el precio del producto?, necesitas autorizacion previa de un administrador");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            priceInput.setFocusableInTouchMode(true);
                            priceInput.requestFocus();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        double discount = 0;
        Client client = database.getClient(clientId);
        if (client.getClientType().getId() == 1) {
            discount = it.getDiscountOne() / 100;
        }
        if (client.getClientType().getId() == 2) {
            discount = it.getDiscountTwo() / 100;
        }
        if (client.getClientType().getId() == 3) {
            discount = it.getDiscountThree() / 100;
        }
        if (client.getClientType().getId() == 4) {
            discount = it.getDiscountFour() / 100;
        }
        if (client.getClientType().getId() == 5) {
            discount = it.getDiscountFive() / 100;
        }
        priceInput.setText(String.valueOf(Math.round(database.getParentPrice(it.getId()) * (1 - discount))));
        final TextInputEditText freeUnitsInput = (TextInputEditText) dialog.findViewById(R.id.free_units_input);
        final TextInputEditText unitsInput = (TextInputEditText) dialog.findViewById(R.id.units_input);
        unitsInput.setText(String.valueOf(0));
        final TextInputEditText notesInput = (TextInputEditText) dialog.findViewById(R.id.notes_input);

        unitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInteger(s.toString()) && isInteger(freeUnitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        int price = Double.valueOf(priceInput.getText().toString()).intValue();
                        double eqValue = (price * Double.valueOf(unitsInput.getText().toString())) / (Double.valueOf(unitsInput.getText().toString()) + Double.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    unitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        freeUnitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInteger(s.toString()) && isInteger(unitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        int price = Double.valueOf(priceInput.getText().toString()).intValue();
                        double eqValue = (price * Integer.valueOf(unitsInput.getText().toString())) / (Integer.valueOf(unitsInput.getText().toString()) + Integer.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    freeUnitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        freeUnitsInput.setText(String.valueOf(0));

        Button twelvePlusButton = (Button) dialog.findViewById(R.id.twelve_plus_button);
        twelvePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusButton = (Button) dialog.findViewById(R.id.five_plus_button);
        fivePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusButton = (Button) dialog.findViewById(R.id.two_plus_button);
        twoPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusButton = (Button) dialog.findViewById(R.id.one_plus_button);
        onePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessButton = (Button) dialog.findViewById(R.id.twelve_less_button);
        twelveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessButton = (Button) dialog.findViewById(R.id.five_less_button);
        fiveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessButton = (Button) dialog.findViewById(R.id.two_less_button);
        twoLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessButton = (Button) dialog.findViewById(R.id.one_less_button);
        oneLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 1));
                }
            }
        });

        Button twelvePlusFreeButton = (Button) dialog.findViewById(R.id.twelve_plus_free_button);
        twelvePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusFreeButton = (Button) dialog.findViewById(R.id.five_plus_free_button);
        fivePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusFreeButton = (Button) dialog.findViewById(R.id.two_plus_free_button);
        twoPlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusFreeButton = (Button) dialog.findViewById(R.id.one_plus_free_button);
        onePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessFreeButton = (Button) dialog.findViewById(R.id.twelve_less_free_button);
        twelveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessFreeButton = (Button) dialog.findViewById(R.id.five_less_free_button);
        fiveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessFreeButton = (Button) dialog.findViewById(R.id.two_less_free_button);
        twoLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessFreeButton = (Button) dialog.findViewById(R.id.one_less_free_button);
        oneLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 1));
                }
            }
        });

        Button addButon = (Button) dialog.findViewById(R.id.add_button);
        addButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = priceInput.getText().toString();
                String units = unitsInput.getText().toString();
                String freeUnits = freeUnitsInput.getText().toString();
                int freeUnitsData;
                String notes = notesInput.getText().toString();

                if (price.equals("") || units.equals("") || Double.valueOf(price) <= 0) {
                    Toast.makeText(context, "Cantidad invalida", Toast.LENGTH_LONG).show();
                    return;
                }

                if (freeUnits.equals("")) {
                    freeUnitsData = 0;
                } else {
                    freeUnitsData = Integer.valueOf(freeUnits);
                }

                if (Integer.valueOf(units) <= 0 && Integer.valueOf(freeUnits) <= 0) {
                    Toast.makeText(context, "Alguna entre unidades y unidades regalo debe ser mayor a 0", Toast.LENGTH_LONG).show();
                    return;
                }

                double unitPrice = Double.valueOf(price);
                double discount = 0;
                OrderItem orderItems = new OrderItem();
                orderItems.setItem(it);
                orderItems.setNotes(notes);
                orderItems.setFreeUnits(freeUnitsData);
                orderItems.setIva(it.getIva());
                orderItems.setOrder(database.getOrder(orderId));
                orderItems.setSubItemId(it.getSubItemId());
                orderItems.setUnits(Integer.valueOf(units));
                orderItems.setUnitPrice(unitPrice);
                orderItems.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                orderItems.setPacker(null);
                orderItems.setStorageUnits(0);
                orderItems.setStorageNotes("");
                orderItems.setDiscount(discount * 100);
                orderItems.setValue(unitPrice);
                orderItems.setTotal(orderItems.getValue() * orderItems.getUnits());
                orderItems.setEqValue(Math.round((orderItems.getUnitPrice() * orderItems.getUnits()) / (orderItems.getUnits() + orderItems.getFreeUnits())));

                System.out.println("Descuento: " + orderItems.getDiscount());
                System.out.println("Total" + orderItems.getTotal());

                if (database.insertOrderItem(orderItems)) {
                    dialog.dismiss();
                    Toast.makeText(context, "Añadido al pedido", Toast.LENGTH_LONG).show();
                    context.startActivity(new Intent(context, CreateOrderActivity.class).putExtra("client_id", clientId).putExtra("order_id", orderId));
                    currentActivity.finish();
                } else {
                    dialog.dismiss();
                    Toast.makeText(context, "Lo sentimos, intentalo de nuevo", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        OrderItem orderItem = database.getOrderItemInfo(orderId, it.getId(), it.getSubItemId());
        if (orderItem != null) {
            priceInput.setText(String.valueOf(orderItem.getUnitPrice()));
            unitsInput.setText(String.valueOf(orderItem.getUnits()));
            freeUnitsInput.setText(String.valueOf(orderItem.getFreeUnits()));
            notesInput.setText(String.valueOf(orderItem.getNotes()));
        }

        dialog.show();
    }

    //Muestra los dialogos de adicion de productos del catalogo a un pedido
    public static void showAddItemCatalogDialog(final Context context, final int clientId, final Item it
            , final DatabaseHelper database, final int orderId) {

        final Dialog dialog = new Dialog(context, R.style.StyledDialog);
        View dialogView = View.inflate(context, R.layout.dialog_add_item, null);
        dialog.setContentView(dialogView);

        final RobotoLightTextView eqPriceLabel = (RobotoLightTextView) dialog.findViewById(R.id.eq_price_label);
        eqPriceLabel.setText(String.valueOf(0));

        final TextInputEditText priceInput = (TextInputEditText) dialog.findViewById(R.id.price_input);
        priceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!priceInput.isFocusable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cambio de precio");
                    builder.setMessage("¿Estas seguro de cambiar el precio del producto?, necesitas autorizacion previa de un administrador");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            priceInput.setFocusableInTouchMode(true);
                            priceInput.requestFocus();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        double discount = 0;
        Client client = database.getClient(clientId);
        if (client.getClientType().getId() == 1) {
            discount = it.getDiscountOne() / 100;
        }
        if (client.getClientType().getId() == 2) {
            discount = it.getDiscountTwo() / 100;
        }
        if (client.getClientType().getId() == 3) {
            discount = it.getDiscountThree() / 100;
        }
        if (client.getClientType().getId() == 4) {
            discount = it.getDiscountFour() / 100;
        }
        if (client.getClientType().getId() == 5) {
            discount = it.getDiscountFive() / 100;
        }
        priceInput.setText(String.valueOf(Math.round(database.getParentPrice(it.getId()) * (1 - discount))));
        final TextInputEditText freeUnitsInput = (TextInputEditText) dialog.findViewById(R.id.free_units_input);
        final TextInputEditText unitsInput = (TextInputEditText) dialog.findViewById(R.id.units_input);
        unitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInteger(s.toString()) && isInteger(freeUnitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        int price = Double.valueOf(priceInput.getText().toString()).intValue();
                        double eqValue = (price * Integer.valueOf(unitsInput.getText().toString())) / (Integer.valueOf(unitsInput.getText().toString()) + Integer.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    unitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        unitsInput.setText(String.valueOf(0));
        freeUnitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInteger(s.toString()) && isInteger(unitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        int price = Double.valueOf(priceInput.getText().toString()).intValue();
                        double eqValue = (price * Integer.valueOf(unitsInput.getText().toString())) / (Integer.valueOf(unitsInput.getText().toString()) + Integer.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    freeUnitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final TextInputEditText notesInput = (TextInputEditText) dialog.findViewById(R.id.notes_input);
        freeUnitsInput.setText(String.valueOf(0));

        Button twelvePlusButton = (Button) dialog.findViewById(R.id.twelve_plus_button);
        twelvePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusButton = (Button) dialog.findViewById(R.id.five_plus_button);
        fivePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusButton = (Button) dialog.findViewById(R.id.two_plus_button);
        twoPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusButton = (Button) dialog.findViewById(R.id.one_plus_button);
        onePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessButton = (Button) dialog.findViewById(R.id.twelve_less_button);
        twelveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessButton = (Button) dialog.findViewById(R.id.five_less_button);
        fiveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessButton = (Button) dialog.findViewById(R.id.two_less_button);
        twoLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessButton = (Button) dialog.findViewById(R.id.one_less_button);
        oneLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 1));
                }
            }
        });

        Button twelvePlusFreeButton = (Button) dialog.findViewById(R.id.twelve_plus_free_button);
        twelvePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusFreeButton = (Button) dialog.findViewById(R.id.five_plus_free_button);
        fivePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusFreeButton = (Button) dialog.findViewById(R.id.two_plus_free_button);
        twoPlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusFreeButton = (Button) dialog.findViewById(R.id.one_plus_free_button);
        onePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessFreeButton = (Button) dialog.findViewById(R.id.twelve_less_free_button);
        twelveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessFreeButton = (Button) dialog.findViewById(R.id.five_less_free_button);
        fiveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessFreeButton = (Button) dialog.findViewById(R.id.two_less_free_button);
        twoLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessFreeButton = (Button) dialog.findViewById(R.id.one_less_free_button);
        oneLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 1));
                }
            }
        });

        Button addButon = (Button) dialog.findViewById(R.id.add_button);
        addButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = priceInput.getText().toString();
                String units = unitsInput.getText().toString();
                String freeUnits = freeUnitsInput.getText().toString();
                int freeUnitsData;
                String notes = notesInput.getText().toString();

                if (price.equals("") || units.equals("") || Double.valueOf(price) <= 0) {
                    Toast.makeText(context, "Cantidad invalida", Toast.LENGTH_LONG).show();
                    return;
                }

                if (freeUnits.equals("")) {
                    freeUnitsData = 0;
                } else {
                    freeUnitsData = Integer.valueOf(freeUnits);
                }

                if (Integer.valueOf(units) <= 0 && Integer.valueOf(freeUnits) <= 0) {
                    Toast.makeText(context, "Alguna entre unidades y unidades regalo debe ser mayor a 0", Toast.LENGTH_LONG).show();
                    return;
                }

                double unitPrice = Double.valueOf(price);
                double discount = 0;
                OrderItem orderItems = new OrderItem();
                orderItems.setItem(it);
                orderItems.setStorageNotes("");
                orderItems.setStorageUnits(0);
                orderItems.setNotes(notes);
                orderItems.setFreeUnits(freeUnitsData);
                orderItems.setIva(it.getIva());
                orderItems.setOrder(database.getOrder(orderId));
                orderItems.setSubItemId(it.getSubItemId());
                orderItems.setUnits(Integer.valueOf(units));
                orderItems.setUnitPrice(unitPrice);
                orderItems.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                orderItems.setPacker(null);
                orderItems.setDiscount(discount * 100);
                orderItems.setValue(unitPrice);
                orderItems.setTotal(orderItems.getValue() * orderItems.getUnits());
                orderItems.setEqValue(Math.round((orderItems.getUnitPrice() * orderItems.getUnits()) / (orderItems.getUnits() + orderItems.getFreeUnits())));

                System.out.println("Descuento: " + orderItems.getDiscount());
                System.out.println("Total" + orderItems.getTotal());

                if (database.insertOrderItem(orderItems)) {
                    dialog.dismiss();
                    Toast.makeText(context, "Añadido al pedido", Toast.LENGTH_LONG).show();
                } else {
                    dialog.dismiss();
                    Toast.makeText(context, "Lo sentimos, intentalo de nuevo", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        OrderItem orderItem = database.getOrderItemInfo(orderId, it.getId(), it.getSubItemId());
        if (orderItem != null) {
            priceInput.setText(String.valueOf(orderItem.getUnitPrice()));
            unitsInput.setText(String.valueOf(orderItem.getUnits()));
            freeUnitsInput.setText(String.valueOf(orderItem.getFreeUnits()));
            notesInput.setText(String.valueOf(orderItem.getNotes()));
        }

        dialog.show();
    }

    //Muestra los dialogos de adicion de subproductos a un pedido (Catalogo y lista)
    public static void showAddSubItemItemDialog(final Context context, final Item sub, final DatabaseHelper database
            , final Item it, final int clientId, final int orderId) {

        final Dialog dialog = new Dialog(context, R.style.StyledDialog);
        View dialogView = View.inflate(context, R.layout.dialog_add_item, null);
        dialog.setContentView(dialogView);

        final RobotoLightTextView eqPriceLabel = (RobotoLightTextView) dialog.findViewById(R.id.eq_price_label);
        eqPriceLabel.setText(String.valueOf(0));

        final TextInputEditText priceInput = (TextInputEditText) dialog.findViewById(R.id.price_input);
        priceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!priceInput.isFocusable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cambio de precio");
                    builder.setMessage("¿Estas seguro de cambiar el precio del producto?, necesitas autorizacion previa de un administrador");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            priceInput.setFocusableInTouchMode(true);
                            priceInput.requestFocus();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        double discount = 0;
        Client client = database.getClient(clientId);
        if (client.getClientType().getId() == 1) {
            discount = it.getDiscountOne() / 100;
        }
        if (client.getClientType().getId() == 2) {
            discount = it.getDiscountTwo() / 100;
        }
        if (client.getClientType().getId() == 3) {
            discount = it.getDiscountThree() / 100;
        }
        if (client.getClientType().getId() == 4) {
            discount = it.getDiscountFour() / 100;
        }
        if (client.getClientType().getId() == 5) {
            discount = it.getDiscountFive() / 100;
        }
        priceInput.setText(String.valueOf(Math.round(database.getParentPrice(it.getId()) * (1 - discount))));
        final TextInputEditText unitsInput = (TextInputEditText) dialog.findViewById(R.id.units_input);
        final TextInputEditText freeUnitsInput = (TextInputEditText) dialog.findViewById(R.id.free_units_input);
        unitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInteger(s.toString()) && isInteger(freeUnitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        double eqValue = (Integer.valueOf(priceInput.getText().toString()) * Integer.valueOf(unitsInput.getText().toString())) / (Integer.valueOf(unitsInput.getText().toString()) + Integer.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    unitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        unitsInput.setText(String.valueOf(0));
        final TextInputEditText notesInput = (TextInputEditText) dialog.findViewById(R.id.notes_input);
        freeUnitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInteger(s.toString()) && isInteger(unitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        int price = Double.valueOf(priceInput.getText().toString()).intValue();
                        double eqValue = (price * Integer.valueOf(unitsInput.getText().toString())) / (Integer.valueOf(unitsInput.getText().toString()) + Integer.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    freeUnitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        freeUnitsInput.setText(String.valueOf(0));

        Button twelvePlusButton = (Button) dialog.findViewById(R.id.twelve_plus_button);
        twelvePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusButton = (Button) dialog.findViewById(R.id.five_plus_button);
        fivePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusButton = (Button) dialog.findViewById(R.id.two_plus_button);
        twoPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusButton = (Button) dialog.findViewById(R.id.one_plus_button);
        onePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessButton = (Button) dialog.findViewById(R.id.twelve_less_button);
        twelveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessButton = (Button) dialog.findViewById(R.id.five_less_button);
        fiveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessButton = (Button) dialog.findViewById(R.id.two_less_button);
        twoLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessButton = (Button) dialog.findViewById(R.id.one_less_button);
        oneLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 1));
                }
            }
        });

        Button twelvePlusFreeButton = (Button) dialog.findViewById(R.id.twelve_plus_free_button);
        twelvePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusFreeButton = (Button) dialog.findViewById(R.id.five_plus_free_button);
        fivePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusFreeButton = (Button) dialog.findViewById(R.id.two_plus_free_button);
        twoPlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusFreeButton = (Button) dialog.findViewById(R.id.one_plus_free_button);
        onePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessFreeButton = (Button) dialog.findViewById(R.id.twelve_less_free_button);
        twelveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessFreeButton = (Button) dialog.findViewById(R.id.five_less_free_button);
        fiveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessFreeButton = (Button) dialog.findViewById(R.id.two_less_free_button);
        twoLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessFreeButton = (Button) dialog.findViewById(R.id.one_less_free_button);
        oneLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 1));
                }
            }
        });

        Button addButon = (Button) dialog.findViewById(R.id.add_button);
        addButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = priceInput.getText().toString();
                String units = unitsInput.getText().toString();
                String freeUnits = freeUnitsInput.getText().toString();
                int freeUnitsData;
                String notes = notesInput.getText().toString();

                if (price.equals("") || units.equals("") || Double.valueOf(price) <= 0) {
                    Toast.makeText(context, "Cantidad invalida, debe ser mayor a 0", Toast.LENGTH_LONG).show();
                    return;
                }
                if (freeUnits.equals("")) {
                    freeUnitsData = 0;
                } else {
                    freeUnitsData = Integer.valueOf(freeUnits);
                }

                if (Integer.valueOf(units) <= 0 && Integer.valueOf(freeUnits) <= 0) {
                    Toast.makeText(context, "Alguna entre unidades y unidades regalo debe ser mayor a 0", Toast.LENGTH_LONG).show();
                    return;
                }

                double unitPrice = Double.valueOf(price);
                double discount = 0;

                if (database.hasParentInOrder(orderId, it.getId())) {

                    OrderItem clickedOrderItem = database.getOrderItemInfo(orderId, sub.getId(), sub.getSubItemId());
                    int unitsData = Integer.valueOf(units);

                    OrderItem parent = database.getParent(orderId, it.getId());
                    if (clickedOrderItem != null) {
                        if (clickedOrderItem.getFreeUnits() > freeUnitsData) {
                            parent.setFreeUnits(parent.getFreeUnits() - (clickedOrderItem.getFreeUnits() - freeUnitsData));
                        }
                        if (clickedOrderItem.getFreeUnits() < freeUnitsData) {
                            parent.setFreeUnits(parent.getFreeUnits() + (freeUnitsData - clickedOrderItem.getFreeUnits()));
                        }
                        if (clickedOrderItem.getUnits() > unitsData) {
                            parent.setUnits(parent.getUnits() - (clickedOrderItem.getUnits() - unitsData));
                            parent.setStorageUnits(0);
                        }
                        if (clickedOrderItem.getUnits() < unitsData) {
                            parent.setUnits(parent.getUnits() + (unitsData - clickedOrderItem.getUnits()));
                            parent.setStorageUnits(0);
                        }
                    } else {
                        parent.setFreeUnits(parent.getFreeUnits() + freeUnitsData);
                        parent.setUnits(parent.getUnits() + Integer.valueOf(units));
                        parent.setStorageUnits(0);
                    }

                    parent.setStorageNotes("");
                    parent.setUnitPrice(unitPrice);
                    parent.setDiscount(discount * 100);
                    parent.setValue(unitPrice);
                    parent.setTotal(parent.getUnits() * unitPrice);
                    parent.setEqValue(Math.round((parent.getUnitPrice() * parent.getUnits()) / (parent.getUnits() + parent.getFreeUnits())));

                    OrderItem orderItems = new OrderItem();
                    orderItems.setItem(sub);
                    orderItems.setNotes(notes);
                    orderItems.setSubItemName(sub.getName());
                    orderItems.setFreeUnits(freeUnitsData);
                    orderItems.setIva(sub.getIva());
                    orderItems.setOrder(database.getOrder(orderId));
                    orderItems.setSubItemId(sub.getSubItemId());
                    orderItems.setUnits(Integer.valueOf(units));
                    orderItems.setStorageNotes("");
                    orderItems.setStorageUnits(0);
                    orderItems.setUnitPrice(unitPrice);
                    orderItems.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                    orderItems.setPacker(null);
                    orderItems.setDiscount(discount * 100);
                    orderItems.setValue(0);
                    orderItems.setTotal(0);
                    orderItems.setEqValue(0);

                    if (database.insertOrderItem(orderItems) && database.insertOrderItem(parent)) {
                        dialog.dismiss();
                        Toast.makeText(context, "Añadido al pedido", Toast.LENGTH_LONG).show();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(context, "Lo sentimos, intentalo de nuevo", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    OrderItem orderItems = new OrderItem();
                    orderItems.setItem(it);
                    orderItems.setNotes(notes);
                    orderItems.setStorageNotes("");
                    orderItems.setStorageUnits(0);
                    orderItems.setFreeUnits(freeUnitsData);
                    orderItems.setIva(it.getIva());
                    orderItems.setOrder(database.getOrder(orderId));
                    orderItems.setSubItemId(0);
                    orderItems.setUnits(Integer.valueOf(units));
                    orderItems.setUnitPrice(unitPrice);
                    orderItems.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                    orderItems.setPacker(null);
                    orderItems.setDiscount(discount * 100);
                    orderItems.setValue(unitPrice);
                    orderItems.setTotal(orderItems.getValue() * orderItems.getUnits());
                    orderItems.setEqValue(Math.round((orderItems.getUnitPrice() * orderItems.getUnits()) / (orderItems.getUnits() + orderItems.getFreeUnits())));

                    OrderItem secondaryItem = new OrderItem();
                    secondaryItem.setItem(sub);
                    secondaryItem.setNotes(notes);
                    secondaryItem.setFreeUnits(freeUnitsData);
                    secondaryItem.setSubItemName(sub.getName());
                    secondaryItem.setIva(sub.getIva());
                    secondaryItem.setOrder(database.getOrder(orderId));
                    secondaryItem.setSubItemId(sub.getSubItemId());
                    secondaryItem.setUnits(Integer.valueOf(units));
                    secondaryItem.setStorageNotes("");
                    secondaryItem.setStorageUnits(0);
                    secondaryItem.setUnitPrice(unitPrice);
                    secondaryItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                    secondaryItem.setPacker(null);
                    secondaryItem.setDiscount(discount * 100);
                    secondaryItem.setValue(0);
                    secondaryItem.setTotal(0);
                    secondaryItem.setEqValue(0);

                    if (database.insertOrderItem(secondaryItem) && database.insertOrderItem(orderItems)) {
                        dialog.dismiss();
                        Toast.makeText(context, "Añadido al pedido", Toast.LENGTH_LONG).show();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(context, "Lo sentimos, intentalo de nuevo", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        OrderItem orderItem = database.getOrderItemInfo(orderId, sub.getId(), sub.getSubItemId());
        if (orderItem != null) {
            priceInput.setText(String.valueOf(orderItem.getUnitPrice()));
            unitsInput.setText(String.valueOf(orderItem.getUnits()));
            freeUnitsInput.setText(String.valueOf(orderItem.getFreeUnits()));
            notesInput.setText(String.valueOf(orderItem.getNotes()));
        }

        dialog.show();
    }

    //Oculta el teclado cuando es visible
    public static void hideKeyboard(Context context, AppCompatActivity activity) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    //Obtiene el id de un nuevo pedido
    public static int getOrderId() {
        Calendar date = new GregorianCalendar();
        String year = String.valueOf(date.get(Calendar.YEAR) - 2000);
        String month = String.valueOf(date.get(Calendar.MONTH) + 1);
        String day = String.valueOf(date.get(Calendar.DAY_OF_MONTH));
        String hour = String.valueOf(date.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(date.get(Calendar.MINUTE));
        String second = String.valueOf(date.get(Calendar.SECOND));
        String ms = String.valueOf(date.get(Calendar.MILLISECOND));
        return Integer.valueOf(day + hour + minute + second);
    }

    //Obtiene el alert dialog de carga que se ejecuta en todas las actividades
    public static Dialog getAlertDialog(Context context) {

        Dialog dialog = new Dialog(context, R.style.LoadDialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        View view = View.inflate(context, R.layout.dialog_loading, null);
        dialog.setContentView(view);
        AVLoadingIndicatorView loadingIndicatorView = (AVLoadingIndicatorView) dialog.findViewById(R.id.loading_indicator);
        loadingIndicatorView.smoothToShow();

        return dialog;
    }

}
