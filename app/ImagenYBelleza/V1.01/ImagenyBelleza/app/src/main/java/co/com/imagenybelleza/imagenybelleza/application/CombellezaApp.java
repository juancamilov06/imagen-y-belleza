package co.com.imagenybelleza.imagenybelleza.application;

import android.app.Application;

import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.GPSReceiver;

/**
 * Created by Juan Camilo Villa Amaya on 2/12/2016.
 * Almacena la referencia del Application global de la aplicacion
 * Se ejecuta al primero que cualquier actividad
 */

public class CombellezaApp extends Application {

    private static CombellezaApp app;

    //Obtiene la instancia de si misma
    public static synchronized CombellezaApp getInstance() {
        return app;
    }

    @Override
    //metodo que se ejecuta en primera instancia
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public void setGPSListener(GPSReceiver.LocationReceiverListener listener) {
        GPSReceiver.locationReceiverListener = listener;
    }

    //Agrega el listener de conectividad, se usa cuando hay cambios de estado en la red
    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
