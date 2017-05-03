package co.com.imagenybelleza.imagenybelleza.main;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.City;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.ClientType;
import co.com.imagenybelleza.imagenybelleza.services.LocationService;

public class CreateClientActivity extends AppCompatActivity {

    private TextInputEditText contactInput, companyInput, addressInput, mailInput,
            phoneOneInput, phoneTwoInput, phoneThreeInput, extOneInput, extTwoInput,
            extThreeInput, nitInput, neighborhoodInput;
    private Context context;
    private DatabaseHelper database;
    private LocationManager mLocationManager = null;
    private Dialog dialog;
    private AppCompatSpinner typeSpinner, citiesSpinner;
    private List<ClientType> types;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_client);

        context = CreateClientActivity.this;
        database = new DatabaseHelper(context);
        initializeLocationManager();

        dialog = Utils.getAlertDialog(context);

        typeSpinner = (AppCompatSpinner) findViewById(R.id.type_spinner);
        citiesSpinner = (AppCompatSpinner) findViewById(R.id.city_spinner);

        contactInput = (TextInputEditText) findViewById(R.id.contact_input);
        phoneOneInput = (TextInputEditText) findViewById(R.id.phone_one_input);
        phoneTwoInput = (TextInputEditText) findViewById(R.id.phone_two_input);
        phoneThreeInput = (TextInputEditText) findViewById(R.id.phone_three_input);
        extOneInput = (TextInputEditText) findViewById(R.id.ext_one_input);
        extTwoInput = (TextInputEditText) findViewById(R.id.ext_two_input);
        extThreeInput = (TextInputEditText) findViewById(R.id.ext_three_input);
        companyInput = (TextInputEditText) findViewById(R.id.company_input);
        addressInput = (TextInputEditText) findViewById(R.id.address_input);
        mailInput = (TextInputEditText) findViewById(R.id.mail_input);
        nitInput = (TextInputEditText) findViewById(R.id.nit_input);
        neighborhoodInput = (TextInputEditText) findViewById(R.id.neighborhood_input);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String contact = contactInput.getText().toString();
                String company = companyInput.getText().toString();
                String phoneOne = phoneOneInput.getText().toString();
                String phoneTwo = phoneTwoInput.getText().toString();
                String phoneThree = phoneThreeInput.getText().toString();
                String extOne = extOneInput.getText().toString();
                String extTwo = extTwoInput.getText().toString();
                String extThree = extThreeInput.getText().toString();
                String address = addressInput.getText().toString();
                String mail = mailInput.getText().toString();
                String nit = nitInput.getText().toString();
                String neighborhood = neighborhoodInput.getText().toString();

                if (TextUtils.isEmpty(contact) || TextUtils.isEmpty(company) || TextUtils.isEmpty(address)
                        || TextUtils.isEmpty(mail) || TextUtils.isEmpty(nit) || TextUtils.isEmpty(neighborhood)
                        || TextUtils.isEmpty(phoneOne)) {

                    Utils.showSnackbar("Debes ingresar todos los campos", CreateClientActivity.this, R.id.create_client_layout);
                    return;

                }

                if (!Utils.isValidEmail(mail)) {
                    Utils.showSnackbar("Correo electronico invalido", CreateClientActivity.this, R.id.create_client_layout);
                    return;
                }

                if ((TextUtils.isEmpty(phoneTwo) && !TextUtils.isEmpty(extTwo)) || (TextUtils.isEmpty(phoneThree) && !TextUtils.isEmpty(extThree))) {
                    Utils.showSnackbar("No deben haber numeros vacios con extension", CreateClientActivity.this, R.id.create_client_layout);
                    return;
                }

                if (!TextUtils.isEmpty(phoneTwo) && !TextUtils.isEmpty(extTwo)) {
                    phoneTwo = phoneTwo.concat("-" + extTwo);
                }

                if (!TextUtils.isEmpty(phoneThree) && !TextUtils.isEmpty(extThree)) {
                    phoneThree = phoneThree.concat("-" + extThree);
                }

                if (!TextUtils.isEmpty(extOne)) {
                    phoneOne = phoneOne.concat("-" + extOne);
                }

                Client client = new Client();
                client.setId(Integer.valueOf(String.valueOf(Utils.getUserId())));
                client.setCode(0);
                client.setContact(contact);
                client.setCompany(company);
                client.setAddress(address);
                client.setNit(nit);
                client.setMail(mail);
                client.setCity(database.getCity(citiesSpinner.getSelectedItemPosition() + 1));
                client.setSent(false);
                client.setClientType(types.get(typeSpinner.getSelectedItemPosition()));
                client.setNeighborhood(neighborhood);
                client.setPhoneOne(phoneOne);
                client.setPhoneTwo(phoneTwo);
                client.setPhoneThree(phoneThree);
                client.setActive(true);
                client.setUser(database.getCurrentUser());

                Location location = getLastKnownLocation();
                if (location != null){
                    client.setLongitude(location.getLongitude());
                    client.setLatitude(location.getLatitude());
                } else {
                    client.setLongitude(6.2518400);
                    client.setLatitude(-75.5635900);
                }

                new AsyncCreateClient().execute(client);

            }
        });

        setSpinners();

    }

    private void setSpinners() {
        types = database.getClientTypes();
        List<String> names = new ArrayList<>();
        for (ClientType clientType : types) {
            names.add(clientType.getName());
        }
        typeSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_spinner_item, names));

        List<City> cities = database.getCities();
        List<String> citiesNames = new ArrayList<>();

        for (City city : cities) {
            citiesNames.add(city.getCity());
        }
        citiesSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_spinner_item, citiesNames));
    }

    //Metodo que obtiene la ultima ubicacion conocida por si el dispositivo no
    //puede obtener la ubicacion temporalmente
    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(CreateClientActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CreateClientActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateClientActivity.this);
        builder.setTitle("Ir atras");
        builder.setMessage("Si sales ahora, el cliente que estabas creando sera eliminado, ¿Deseas continuar?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(CreateClientActivity.this, ClientActivity.class));
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

    }

    private void sendData(final Client client) {
        if (ConnectivityReceiver.isConnected()) {
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
                            Utils.showSnackbar("El cliente fue enviado correctamente al servidor", CreateClientActivity.this, R.id.create_client_layout);
                            database.updateClient(true, client.getId());
                            startActivity(new Intent(CreateClientActivity.this, CreateOrderActivity.class).putExtra("client_id", client.getId()).putExtra("note", "***Nuevo Cliente***"));
                            finish();
                        } else {
                            Utils.showSnackbar("Error enviando el cliente, intenta mas tarde", CreateClientActivity.this, R.id.create_client_layout);
                            startActivity(new Intent(CreateClientActivity.this, CreateOrderActivity.class).putExtra("client_id", client.getId()).putExtra("note", "***Nuevo Cliente***"));
                            finish();
                        }
                    } catch (JSONException ignored) {
                        database.updateClient(false, client.getId());
                        Utils.showSnackbar("Error enviando cliente, puedes enviarlo luego", CreateClientActivity.this, R.id.create_client_layout);
                        startActivity(new Intent(CreateClientActivity.this, CreateOrderActivity.class).putExtra("client_id", client.getId()).putExtra("note", "***Nuevo Cliente***"));
                        finish();
                        ignored.printStackTrace();
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
                    params.put("latitude", String.valueOf(client.getLatitude()));
                    params.put("longitude", String.valueOf(client.getLongitude()));
                    return params;
                }

                @Override
                public Priority getPriority() {
                    return Priority.IMMEDIATE;
                }
            };
            queue.add(request);
        } else {
            dialog.dismiss();
            Utils.showSnackbar("El cliente sera enviado al servidor cuando haya red", CreateClientActivity.this, R.id.create_client_layout);
            startActivity(new Intent(CreateClientActivity.this, CreateOrderActivity.class).putExtra("client_id", client.getId()).putExtra("note", "***Nuevo Cliente***"));
            finish();

        }
    }

    private class AsyncCreateClient extends AsyncTask<Client, Void, Void> {

        boolean success = false;
        long id;
        private Client client;

        @Override
        protected void onPreExecute() {
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Client... params) {

            client = params[0];
            success = database.insertClient(params[0]);
            id = client.getId();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (success) {
                Utils.showSnackbar("Cliente creado con éxito", CreateClientActivity.this, R.id.create_client_layout);
                sendData(database.getClient(client.getId()));
            } else {
                dialog.dismiss();
                Utils.showSnackbar("Hubo un error creando el cliente, intenta luego.", CreateClientActivity.this, R.id.create_client_layout);
            }
        }
    }

}
