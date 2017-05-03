package co.com.imagenybelleza.imagenybelleza.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import co.com.imagenybelleza.imagenybelleza.helpers.Utils;

/*
* Servicio que se ejecuta en un proceso background de la aplicacion
* Obtiene la ubicacion del usuario cada que se mueve en cierto rango
 * o cada 5 minutos
*/

public class LocationService extends Service {

    //Tag del servicio, sirve para ser identificado
    private static final String TAG = "GPService";
    //Intervalo de tiempo en ms
    private static final int LOCATION_INTERVAL = 300000;
    //Intervalo de distancia en metros
    private static final float LOCATION_DISTANCE = 20f;
    //Proveedores de ubicacion
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    //Temporizador para controlar el tiempo
    private Timer timer = null;
    //Manejador de la localizacion
    private LocationManager mLocationManager = null;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    //Se ejecuta al iniciar la app
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    //Se ejecuta luego del onStartCommand
    public void onCreate() {
        Log.e(TAG, "onCreate");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        receiveLocationUpdates();
    }

    //Metodo para obtener las actualizaciones
    private void receiveLocationUpdates() {
        if (timer == null) {
            timer = new Timer();
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Location location = getLastKnownLocation();
                if (location != null) {
                    Log.d("User Location Provided", String.valueOf(location.getLatitude()) + "      " + String.valueOf(location.getLongitude()));
                    Utils.sendLocation(location, LocationService.this);
                }
            }
        }, 0, 300000);
    }

    //Metodo que obtiene la ultima ubicacion conocida por si el dispositivo no
    //puede obtener la ubicacion temporalmente
    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    //Elige el mejor proveedor de ubicacion
    private String getBestProvider() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Criteria locationCritera = new Criteria();
        locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
        locationCritera.setAltitudeRequired(false);
        locationCritera.setBearingRequired(false);
        locationCritera.setCostAllowed(true);
        locationCritera.setPowerRequirement(Criteria.NO_REQUIREMENT);
        Log.d("Best provider", locationManager.getBestProvider(locationCritera, true));
        return locationManager.getBestProvider(locationCritera, true);
    }

    @Override
    //Se ejecuta al finalizar el proceso de Imagen y Belleza, para que no siga consumiendo bateria
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    //Inicializa el manejador de localizacion para obtener la ubicacion con base en
    // el proveedor
    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    //Clase embebida que obtiene actualizaciones por distancia
    private class LocationListener implements android.location.LocationListener {

        Location mLastLocation;

        LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
            Log.e(TAG, "location: " + mLastLocation.toString());
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.e(TAG, "onLocationChanged: " + location.toString());
                mLastLocation.set(location);
                Utils.sendLocation(location, LocationService.this);
            } else {
                if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                location = mLocationManager.getLastKnownLocation(getBestProvider());
                if (location != null) {
                    Utils.sendLocation(location, LocationService.this);
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
}

