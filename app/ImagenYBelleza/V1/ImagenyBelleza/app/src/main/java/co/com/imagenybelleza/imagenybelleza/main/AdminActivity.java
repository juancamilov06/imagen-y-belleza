package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
import co.com.imagenybelleza.imagenybelleza.adapters.ItemCountAdapter;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Database;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Count;

/*
* Actividad que maneja todos los eventos de Administracion
* Reinicio de datos, Visualizacion de ubicaciones de vendedores
* y Version de datos
*/

public class AdminActivity extends AppCompatActivity {

    private Context context;
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        context = AdminActivity.this;
        database = new DatabaseHelper(context);

        Button locationButton = (Button) findViewById(R.id.location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminActivity.this, LocationActivity.class));
            }
        });

        Button reloadData = (Button) findViewById(R.id.clear_button);
        reloadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.clearAll();
                setFirstTime(true);
                startActivity(new Intent(AdminActivity.this, SplashActivity.class));
                finish();
            }
        });

        RobotoLightTextView versionLabel = (RobotoLightTextView) findViewById(R.id.version_label);
        versionLabel.setText(database.getLastModifiedDate());

        Button messagesButton = (Button) findViewById(R.id.messages_button);
        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminActivity.this, MessagesActivity.class));
            }
        });

        Button dataCountButton = (Button) findViewById(R.id.data_count_button);
        dataCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ConnectivityReceiver.isConnected()) {
                    final Dialog loadingDialog = Utils.getAlertDialog(context);
                    loadingDialog.setCancelable(true);

                    final Dialog dialog = new Dialog(context, R.style.FullDialog);
                    View view = View.inflate(context, R.layout.dialog_count, null);
                    dialog.setContentView(view);

                    dialog.show();
                    loadingDialog.show();

                    final ListView countListView = (ListView) dialog.findViewById(R.id.count_list_view);
                    final List<Count> counts = new ArrayList<>();
                    RequestQueue queue = Volley.newRequestQueue(context);
                    StringRequest request = new StringRequest(Request.Method.GET, database.getIpAdress() + Url.COUNT_SERVICE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("Response: " + response);
                            try {
                                loadingDialog.dismiss();
                                JSONObject object = new JSONObject(response);
                                JSONArray countsObject = object.getJSONArray("results");
                                if (countsObject.length() > 0) {
                                    for (int i = 0; i < countsObject.length(); i++) {
                                        JSONObject currentObject = countsObject.getJSONObject(i);
                                        Count count = new Count();
                                        count.setCdb(currentObject.getInt("itemCount"));
                                        String table = currentObject.getString("table");
                                        if (table.equals("order_")) {
                                            count.setLocal(database.count(Database.TABLE_ORDER));
                                        } else {
                                            count.setLocal(database.count(currentObject.getString("table")));
                                        }
                                        count.setTable(table);
                                        counts.add(count);
                                    }
                                }
                                if (counts.size() > 0) {
                                    countListView.setAdapter(new ItemCountAdapter(context, R.layout.item_count, counts));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                dialog.dismiss();
                                Utils.showSnackbar("Error recuperando los datos", AdminActivity.this, R.id.activity_admin);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loadingDialog.dismiss();
                            dialog.dismiss();
                            Utils.showSnackbar("Error recuperando los datos", AdminActivity.this, R.id.activity_admin);
                        }
                    });
                    queue.add(request);

                } else {
                    Utils.showSnackbar("Debes conectarte a la red para ver los resultados", AdminActivity.this, R.id.activity_admin);
                }
            }
        });
    }

    private void setFirstTime(boolean firstTime) {
        SharedPreferences prefs = getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        prefs.edit().putBoolean("firstrun", firstTime).apply();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AdminActivity.this, MainActivity.class));
        finish();
    }
}
