package co.com.imagenybelleza.imagenybelleza.main;

import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.UserLocation;

/*
* Actividad que permite al administrador ver la localizacion de los empleados
* Se incluye filtro por empleado. Se usa la API de Google Maps V2
* */

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Context context;
    private DatabaseHelper database;
    private List<UserLocation> locations;
    private AppCompatSpinner sellersSpinner;
    private List<Bitmap> markers;
    private String username = "";
    private String date = "";
    private EditText dateInput;

    private boolean isOpen = false;
    private LinearLayout searchView;
    private float height = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        context = LocationActivity.this;
        database = new DatabaseHelper(context);
        markers = getListFiles(new File(database.getDirectory(), "markers"));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        searchView = (LinearLayout) findViewById(R.id.search_view);
        height = searchView.getHeight();

        sellersSpinner = (AppCompatSpinner) findViewById(R.id.sellers_spinner);
        sellersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                username = sellersSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dateInput = (EditText) findViewById(R.id.date_input);
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar today = Calendar.getInstance();
                int year = today.get(Calendar.YEAR);
                int month = today.get(Calendar.MONTH);
                int day = today.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(LocationActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        date = String.valueOf(selectedyear) + "-" + String.valueOf(selectedmonth + 1) + "-" + String.valueOf(selectedday);
                        dateInput.setText(date);
                    }
                }, year, month, day);
                mDatePicker.setTitle("Dia de trabajo");
                mDatePicker.show();
            }
        });

        ImageView clearButton = (ImageView) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date = "";
                dateInput.setText(date);
            }
        });

        Button searchButton = (Button) findViewById(R.id.search_location_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                searchView.setVisibility(View.GONE);
                isOpen = false;
                if (username.equals("Todos")) {
                    new SetAllMarkersAsync().execute();
                } else {
                    new SetUserMarkersAsync().execute(username);
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_location, menu);
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

    private List<Bitmap> getListFiles(File parentDir) {
        ArrayList<Bitmap> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    inFiles.addAll(getListFiles(file));
                } else {
                    if (file.getName().endsWith(".png")) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        inFiles.add(bitmap);
                    }
                }
            }
            return inFiles;
        } else {
            return null;
        }
    }

    private void setUpAllMarkers() throws ParseException {
        LatLng userLocationMarker = null;
        List<LatLng> latLngs = new ArrayList<>();
        List<String> users = database.getDistinctLocationSellers();
        List<String> colors = getColors();
        if (markers != null) {
            for (int j = 0; j < users.size(); j++) {
                if (!users.get(j).equals("Todos")) {
                    latLngs.clear();
                    for (int i = 0; i < locations.size(); i++) {
                        if (date.equals("")) {
                            if (users.get(j).equals(locations.get(i).getSeller().getContact())) {
                                userLocationMarker = new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude());
                                final MarkerOptions options = new MarkerOptions().position(userLocationMarker)
                                        .icon(BitmapDescriptorFactory.fromBitmap(markers.get(j)))
                                        .title(locations.get(i).getSeller().getContact())
                                        .snippet(locations.get(i).getCreated());
                                userLocationMarker = new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude());
                                this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMap.addMarker(options);
                                    }
                                });
                                latLngs.add(userLocationMarker);
                            }
                        } else {
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date selectedDate = format.parse(date);
                            Date locationDate = format.parse(locations.get(i).getCreated());
                            System.out.println("Fecha 1: " + selectedDate.toString() + " Fecha 2: " + locationDate.toString());
                            if (users.get(j).equals(locations.get(i).getSeller().getContact()) && (selectedDate.compareTo(locationDate) == 0)) {
                                userLocationMarker = new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude());
                                final MarkerOptions options = new MarkerOptions().position(userLocationMarker)
                                        .icon(BitmapDescriptorFactory.fromBitmap(markers.get(j)))
                                        .title(locations.get(i).getSeller().getContact())
                                        .snippet(locations.get(i).getCreated());
                                this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMap.addMarker(options);
                                    }
                                });
                                latLngs.add(userLocationMarker);
                            }
                        }
                    }
                    final PolylineOptions polyLine = new PolylineOptions().addAll(latLngs).width(5).color(Color.parseColor(colors.get(j)));
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMap.addPolyline(polyLine);
                        }
                    });

                }
            }
            if (latLngs.size() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showSnackbar("No hay ubicaciones disponibles para la fecha seleccionada", LocationActivity.this, R.id.activity_location);
                    }
                });
            }
            if (userLocationMarker != null) {
                final LatLng finalUserLocationMarker = userLocationMarker;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(finalUserLocationMarker));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                    }
                });
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showSnackbar("La carpeta de marcadores no esta configurada", LocationActivity.this, R.id.activity_location);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.deleteTempLocations();
    }

    private void setUpMarkers(List<UserLocation> locations) throws ParseException {
        LatLng userLocationMarker = null;
        if (markers != null) {
            int index = 0;
            List<String> users = database.getDistinctLocationSellers();
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).equals(locations.get(0).getSeller().getContact())) {
                    index = i;
                }
            }
            List<LatLng> latLngs = new ArrayList<>();
            for (final UserLocation location : locations) {
                if (date.equals("")) {
                    userLocationMarker = new LatLng(location.getLatitude(), location.getLongitude());
                    final LatLng finalUserLocationMarker = userLocationMarker;
                    final int finalIndex = index;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMap.addMarker(new MarkerOptions().position(finalUserLocationMarker)
                                    .icon(BitmapDescriptorFactory.fromBitmap(markers.get(finalIndex)))
                                    .title(location.getSeller().getContact())
                                    .snippet(location.getCreated()));
                        }
                    });
                    latLngs.add(userLocationMarker);
                } else {
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date selectedDate = format.parse(date);
                    Date locationDate = format.parse(location.getCreated());
                    System.out.println("Fecha 1: " + selectedDate.toString() + " Fecha 2: " + locationDate.toString());
                    if (locationDate.compareTo(selectedDate) == 0) {
                        userLocationMarker = new LatLng(location.getLatitude(), location.getLongitude());
                        final LatLng finalUserLocationMarker = userLocationMarker;
                        final int finalIndex = index;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMap.addMarker(new MarkerOptions().position(finalUserLocationMarker)
                                        .icon(BitmapDescriptorFactory.fromBitmap(markers.get(finalIndex)))
                                        .title(location.getSeller().getContact())
                                        .snippet(location.getCreated()));
                            }
                        });
                        latLngs.add(userLocationMarker);
                    }
                }
            }
            if (latLngs.size() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showSnackbar("No hay ubicaciones disponibles para la fecha seleccionada", LocationActivity.this, R.id.activity_location);
                    }
                });
            }
            if (userLocationMarker != null) {
                final LatLng finalUserLocationMarker1 = userLocationMarker;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(finalUserLocationMarker1));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                    }
                });
            }
            final PolylineOptions polyLine = new PolylineOptions().addAll(latLngs).width(5).color(Color.parseColor(getColors().get(index)));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.addPolyline(polyLine);
                }
            });
        }
    }

    private List<UserLocation> getLocationsByUsername(String username) {
        List<UserLocation> locations = new ArrayList<>();
        for (UserLocation location : this.locations) {
            if (username.equals(location.getSeller().getContact())) {
                locations.add(location);
            }
        }

        return locations;
    }

    private boolean detectOpenGLES20() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x20000);
    }

    private List<String> getColors() {
        List<String> colors = new ArrayList<>();
        colors.add("#00D994"); //aguamarina
        colors.add("#91D900"); //limon
        colors.add("#BABABA"); //gris
        colors.add("#F70552"); //magenta
        colors.add("#943A3A"); //vinotinto
        colors.add("#421717"); //cafe
        colors.add("#003154"); //azul rey
        colors.add("#91DC5A"); //verde
        colors.add("#006DF0"); //azul
        colors.add("#933EC5"); //morado
        colors.add("#FFDA44"); //amarillo
        colors.add("#D80027"); //rojo
        colors.add("#FFFFFF"); //blanco
        colors.add("#000000"); //negro
        colors.add("#D900C3"); //rosado
        colors.add("#D98200"); //naranja
        return colors;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        new GetLocationsAsync().execute();
    }

    private void setSpinners() {
        List<String> names = database.getDistinctLocationSellers();
        locations = database.getTempLocations();
        System.out.println("names size: " + names.size());
        if (names.size() > 0) {
            sellersSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_spinner_item, names));
        }
    }

    private class SetAllMarkersAsync extends AsyncTask<Void, Void, Void> {

        private Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                setUpAllMarkers();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    private class SetUserMarkersAsync extends AsyncTask<String, Void, Void> {

        private Dialog dialog;
        private String username = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            username = params[0];
            try {
                setUpMarkers(getLocationsByUsername(username));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    private class GetLocationsAsync extends AsyncTask<Void, Void, Void> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.GET, database.getIpAdress() + Url.GET_LOCATION_SERVICE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dialog.dismiss();
                    System.out.println(response);
                    try {
                        List<UserLocation> locations = new ArrayList<>();
                        JSONArray array = new JSONArray(response);
                        if (array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject location = array.getJSONObject(i);
                                UserLocation userLocation = new UserLocation();
                                userLocation.setLongitude(location.getDouble("longitude"));
                                userLocation.setLatitude(location.getDouble("latitude"));
                                userLocation.setCreated(location.getString("created"));
                                userLocation.setSeller(database.getUser(location.getInt("seller_id")));
                                locations.add(userLocation);
                            }
                            System.out.println("Locations size: " + locations.size());
                            database.insertTempLocations(locations);
                            setSpinners();
                        } else {
                            Utils.showSnackbar("No hay ubicaciones para mostrar", LocationActivity.this, R.id.activity_location);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.showSnackbar("Error en la respuesta del servidor", LocationActivity.this, R.id.activity_location);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismiss();
                    Utils.showSnackbar("Error en la respuesta del servidor", LocationActivity.this, R.id.activity_location);
                }
            }) {
                @Override
                public Priority getPriority() {
                    return Priority.IMMEDIATE;
                }
            };
            queue.add(request);
            return null;
        }
    }

}
