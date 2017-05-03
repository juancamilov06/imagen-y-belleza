package co.com.imagenybelleza.imagenybelleza.main;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.adapters.ClientsAdapter;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.ClientTypes;
import co.com.imagenybelleza.imagenybelleza.enums.Roles;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.ClientType;

/*
Actividad que permite al vendedor crear un cliente nuevo en la
Aplicacion
* */

public class ClientActivity extends AppCompatActivity {

    private FloatingActionButton newClientButton;
    private ListView clientsListView;
    private Context context;
    private Dialog dialog;
    private List<Client> clientList;
    private List<Client> originalClients;
    private DatabaseHelper database;
    private LinearLayout searchView, notFoundView;
    private boolean isOpen = false;
    private float height;
    private EditText cityQueryInput, companyQueryInput, contactQueryInput, neighborhoodQueryInput;
    private ClientsAdapter adapter;
    private Button searchButton;
    private ImageView clearCityButton, clearCompanyButton, clearContactButton, clearNeighborhoodButton;
    private Button deleteButton;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        context = ClientActivity.this;
        database = new DatabaseHelper(context);
        initializeLocationManager();

        searchView = (LinearLayout) findViewById(R.id.search_view);
        height = searchView.getHeight();

        notFoundView = (LinearLayout) findViewById(R.id.not_found_view);

        contactQueryInput = (EditText) findViewById(R.id.contact_input);
        clearContactButton = (ImageView) findViewById(R.id.clear_contact_button);
        clearContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactQueryInput.setText("");
            }
        });

        companyQueryInput = (EditText) findViewById(R.id.company_input);
        clearCompanyButton = (ImageView) findViewById(R.id.clear_company_button);
        clearCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                companyQueryInput.setText("");
            }
        });

        cityQueryInput = (EditText) findViewById(R.id.city_input);
        clearCityButton = (ImageView) findViewById(R.id.clear_city_button);
        clearCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityQueryInput.setText("");
            }
        });

        neighborhoodQueryInput = (EditText) findViewById(R.id.neighborhood_input);
        clearNeighborhoodButton = (ImageView) findViewById(R.id.clear_neighborhood_button);
        clearNeighborhoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                neighborhoodQueryInput.setText("");
            }
        });

        deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityQueryInput.setText("");
                neighborhoodQueryInput.setText("");
                companyQueryInput.setText("");
                contactQueryInput.setText("");
            }
        });

        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.hideKeyboard(context, ClientActivity.this);

                if (originalClients != null) {
                    String contact = contactQueryInput.getText().toString();
                    String company = companyQueryInput.getText().toString();
                    String city = cityQueryInput.getText().toString();
                    String neighborhood = neighborhoodQueryInput.getText().toString();

                    if (city.equals("") && company.equals("") && contact.equals("") && neighborhood.equals("")) {
                        clientList.clear();
                        clientList.addAll(originalClients);
                    } else {
                        clientList.clear();
                        List<Client> clientsFiltered = new ArrayList<>();
                        clientsFiltered.addAll(originalClients);
                        for (Client client : clientsFiltered) {
                            if (client.getContact().toLowerCase().contains(contact.toLowerCase()) && client.getCity().getCity().toLowerCase().contains(city.toLowerCase())
                                    && client.getNeighborhood().toLowerCase().contains(neighborhood.toLowerCase()) && client.getCompany().toLowerCase().contains(company.toLowerCase())) {
                                clientList.add(client);
                            }
                        }
                    }

                    if (clientList.size() == 0) {
                        searchView.setVisibility(View.GONE);
                        clientsListView.setVisibility(View.GONE);
                        notFoundView.setVisibility(View.VISIBLE);
                    } else {
                        notFoundView.setVisibility(View.GONE);
                        searchView.setVisibility(View.GONE);
                        clientsListView.setVisibility(View.VISIBLE);
                    }

                    adapter = new ClientsAdapter(context, R.layout.item_client, clientList);
                    clientsListView.setAdapter(adapter);
                } else {
                    Utils.showSnackbar("No tienes clientes asociados", ClientActivity.this, R.id.clients_layout);
                }
            }
        });

        newClientButton = (FloatingActionButton) findViewById(R.id.fab);
        newClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ClientActivity.this, CreateClientActivity.class));
                finish();
            }
        });

        clientsListView = (ListView) findViewById(R.id.clients_list);
        clientsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Client client = clientList.get(position);
                if (client.getClientType().getId() == ClientTypes.TYPE_INDEFINIDO) {

                    final Dialog dialog = new Dialog(context, R.style.StyledDialog);
                    View dialogView = View.inflate(context, R.layout.dialog_client_type, null);
                    dialog.setContentView(dialogView);

                    final List<ClientType> types = database.getClientTypes();
                    List<String> names = new ArrayList<>();
                    for (ClientType clientType : types) {
                        if (clientType.getId() != ClientTypes.TYPE_INDEFINIDO) {
                            names.add(clientType.getName());
                        }
                    }

                    final AppCompatSpinner typeSpinner = (AppCompatSpinner) dialogView.findViewById(R.id.type_spinner);
                    typeSpinner.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
                    typeSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_spinner_item, names));

                    Button confirmButton = (Button) dialogView.findViewById(R.id.confirm_button);
                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            final Dialog loadingDialog = Utils.getAlertDialog(context);
                            loadingDialog.show();

                            final ClientType clientType = types.get(typeSpinner.getSelectedItemPosition());

                            RequestQueue queue = Volley.newRequestQueue(context);
                            StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.UPDATE_CLIENT_TYPE_SERVICE_URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    loadingDialog.dismiss();
                                    System.out.println("Response: " + response);
                                    try {
                                        JSONObject responseObject = new JSONObject(response);
                                        String message = responseObject.getString("mensaje");
                                        if (message.equals("Creacion exitosa")){
                                            database.updateClientTypeAndLocation(client);
                                            Toast.makeText(context, "Cliente actualizado con exito", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(ClientActivity.this, CreateOrderActivity.class).putExtra("client_id", client.getId()));
                                            finish();
                                        }
                                    } catch (JSONException e) {
                                        Toast.makeText(context, "Error actualizando el cliente, puede seguir con el pedido", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ClientActivity.this, CreateOrderActivity.class).putExtra("client_id", client.getId()));
                                        finish();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    loadingDialog.dismiss();
                                    Toast.makeText(context, "Error actualizando el cliente, puede seguir con el pedido", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ClientActivity.this, CreateOrderActivity.class).putExtra("client_id", client.getId()));
                                    finish();
                                }
                            }){
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    Location location = getLastKnownLocation();

                                    params.put("client_id", String.valueOf(client.getId()));
                                    params.put("type_id", String.valueOf(clientType.getId()));
                                    if (location != null){

                                        client.setClientType(clientType);
                                        client.setLongitude(location.getLongitude());
                                        client.setLatitude(location.getLatitude());

                                        params.put("latitude", String.valueOf(location.getLatitude()));
                                        params.put("longitude", String.valueOf(location.getLongitude()));
                                    } else {

                                        client.setClientType(clientType);
                                        client.setLongitude(0);
                                        client.setLatitude(0);

                                        params.put("latitude", String.valueOf(0));
                                        params.put("longitude", String.valueOf(0));
                                    }
                                    return params;
                                }
                            };

                            queue.add(request);
                        }
                    });

                    dialog.show();

                } else {
                    startActivity(new Intent(ClientActivity.this, CreateOrderActivity.class).putExtra("client_id", clientList.get(position).getId()));
                    finish();
                }
            }
        });

        clientsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!clientList.get(position).isSent()) {
                    final Client client = clientList.get(position);
                    if (ConnectivityReceiver.isConnected()) {
                        dialog = Utils.getAlertDialog(context);
                        dialog.show();
                        RequestQueue queue = Volley.newRequestQueue(context);
                        StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.CREATE_CLIENTS_SERVICE_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                dialog.dismiss();
                                String mensaje;
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    mensaje = jsonObject.getString("mensaje");
                                    System.out.println("Mensaje: " + mensaje);
                                    if (mensaje.equals("Creacion exitosa")) {
                                        Utils.showSnackbar("El cliente fue enviado correctamente al servidor", ClientActivity.this, R.id.clients_layout);
                                        database.updateClient(true, client.getId());
                                        new AsyncClients().execute();
                                    } else {
                                        Utils.showSnackbar("Error enviando el cliente", ClientActivity.this, R.id.clients_layout);
                                    }
                                } catch (JSONException ignored) {
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                dialog.dismiss();
                                Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                                if (volleyError instanceof TimeoutError) {
                                    Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }) {
                            @Override
                            public Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("op", String.valueOf(1));
                                params.put("id", String.valueOf(client.getId()));
                                params.put("code", String.valueOf(client.getCode()));
                                params.put("company", client.getCompany());
                                params.put("address", client.getAddress());
                                params.put("city_id", String.valueOf(client.getCity().getId()));
                                params.put("phone_one", client.getPhoneOne());
                                params.put("phone_two", client.getPhoneTwo());
                                params.put("phone_three", client.getPhoneThree());
                                params.put("nit", client.getNit());
                                params.put("mail", client.getMail());
                                params.put("contact", client.getContact());
                                params.put("client_type_id", String.valueOf(client.getClientType().getId()));
                                params.put("neighborhood", client.getNeighborhood());
                                params.put("user_id", String.valueOf(client.getUser().getId()));
                                return params;
                            }

                            @Override
                            public Priority getPriority() {
                                return Priority.IMMEDIATE;
                            }
                        };
                        request.setRetryPolicy(new DefaultRetryPolicy(
                                15000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(request);
                    } else {
                        Utils.showSnackbar("No detectamos conexion a internet, verifica tu red", ClientActivity.this, R.id.clients_layout);
                    }
                } else {
                    Utils.showSnackbar("El cliente ya ha sido enviado al servidor", ClientActivity.this, R.id.clients_layout);
                }
                return true;
            }
        });

        new AsyncClients().execute();
    }

    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(ClientActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ClientActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            Log.d("Location found: ", "nope");
            return null;
        }
        Log.d("Location found: ", bestLocation.toString());
        return bestLocation;
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.isGpsEnabled(context);
    }

    private void setOriginalList(List<Client> list) {
        this.originalClients = new ArrayList<>();
        originalClients.addAll(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                if (!isOpen) {
                    searchView.setVisibility(View.VISIBLE);
                    searchView.animate()
                            .translationY(height)
                            .alpha(1.0f);
                    isOpen = true;
                } else {
                    searchView.animate()
                            .translationY(0)
                            .alpha(0.0f);
                    searchView.setVisibility(View.GONE);
                    isOpen = false;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ClientActivity.this, OrderActivity.class));
        finish();
    }

    private class AsyncClients extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            dialog = Utils.getAlertDialog(context);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (database.getCurrentUser().getRole().equals(Roles.ROLE_ADMIN)) {
                clientList = database.getClients();
            } else {
                clientList = database.getMyClients();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (clientList.size() > 0) {
                clientsListView.setVisibility(View.VISIBLE);
                setOriginalList(clientList);
            }
        }
    }

}
