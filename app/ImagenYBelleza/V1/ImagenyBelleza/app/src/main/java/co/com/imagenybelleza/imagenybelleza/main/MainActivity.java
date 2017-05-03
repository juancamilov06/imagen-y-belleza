package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.application.CombellezaApp;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Roles;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.CircleView;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.GPSReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.User;
import co.com.imagenybelleza.imagenybelleza.services.LocationService;

/*
* Actividad que muestra el menu principal
* Aqui se realizan las restricciones para usuarios dado el rol
* que desempeñen. Se puede acceder a los modulos de venta, administracion, bodega y facturacion
* Tambien al modulo de configuracion
* */

public class MainActivity extends AppCompatActivity implements GPSReceiver.LocationReceiverListener {

    private Context context;
    private User user;
    private Dialog dialog;
    private DatabaseHelper database;
    private CircleView notificationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;
        database = new DatabaseHelper(context);

        dialog = Utils.getAlertDialog(context);
        dialog.show();
        getUser();

        notificationView = (CircleView) findViewById(R.id.notification_view);

        LinearLayout ordersView = (LinearLayout) findViewById(R.id.orders_menu);
        ordersView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getRole().equals(Roles.ROLE_ADMIN) || user.getRole().equals(Roles.ROLE_SELLER)) {
                    startActivity(new Intent(MainActivity.this, OrderActivity.class));
                } else {
                    Utils.showSnackbar("No tienes acceso a este modulo", MainActivity.this, R.id.activity_main);
                }
            }
        });

        LinearLayout storageView = (LinearLayout) findViewById(R.id.storage_menu);
        storageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getRole().equals(Roles.ROLE_PACKER) || user.getRole().equals(Roles.ROLE_ADMIN)) {
                    startActivity(new Intent(MainActivity.this, StorageActivity.class));
                } else {
                    Utils.showSnackbar("No tienes acceso a este modulo", MainActivity.this, R.id.activity_main);
                }
            }
        });

        LinearLayout billsView = (LinearLayout) findViewById(R.id.bill_menu);
        billsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getRole().equals(Roles.ROLE_BILLER) || user.getRole().equals(Roles.ROLE_ADMIN)) {
                    startActivity(new Intent(MainActivity.this, BillActivity.class));
                } else {
                    Utils.showSnackbar("No tienes acceso a este modulo", MainActivity.this, R.id.activity_main);
                }
            }
        });

        LinearLayout managementView = (LinearLayout) findViewById(R.id.management_menu);
        managementView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getRole().equals(Roles.ROLE_ADMIN)) {
                    startActivity(new Intent(MainActivity.this, AdminActivity.class));
                    finish();
                } else {
                    Utils.showSnackbar("No tienes acceso a este modulo", MainActivity.this, R.id.activity_main);
                }
            }
        });


        if (user.getRole().equals(Roles.ROLE_ADMIN)) {
            if (ConnectivityReceiver.isConnected()) {
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest request = new StringRequest(Request.Method.GET, database.getIpAdress() + Url.GET_MESSAGES_SERVICE_URL + "?unread=true", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            System.out.println("Respuesta: " + response);
                            JSONObject objectResponse = new JSONObject(response);
                            JSONArray messagesResponse = objectResponse.getJSONArray("messages");
                            if (messagesResponse.length() > 0) {
                                notificationView.setCircleColor(ContextCompat.getColor(context, R.color.colorRed));
                                notificationView.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                queue.add(request);
            } else {
                Utils.showSnackbar("Activa el internet para obtener los mensajes sin leer", MainActivity.this, R.id.activity_main);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CombellezaApp.getInstance().setGPSListener(this);
        Utils.isGpsEnabled(context);
    }

    public void getUser() {
        user = database.getCurrentUser();
        dialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sign_out:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Terminar sesión");
                builder.setMessage("¿Deseas cerrar sesión?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.closeSession();
                        stopService(new Intent(MainActivity.this, LocationService.class));
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();

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

                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onGpsStateChangedListener(boolean isConnected) {
        if (!isConnected) {

        }
    }
}
