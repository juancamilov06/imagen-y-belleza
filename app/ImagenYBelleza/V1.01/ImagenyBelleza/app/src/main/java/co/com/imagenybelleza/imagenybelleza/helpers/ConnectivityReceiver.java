package co.com.imagenybelleza.imagenybelleza.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import co.com.imagenybelleza.imagenybelleza.application.CombellezaApp;

/**
 * Created by Juan Camilo Villa Amayaon 2/12/2016.
 * <p>
 * Clase encargada de recibir el estado de la conexion, implementa la interfaz
 * OnConnectivityReceiverListener
 * <p>
 * Se ejecuta cada vez que se detecta un cambio en la red, o se desactiva algun dispositivo de red
 * (Celular o Wi-Fi)
 * <p>
 * Hereda de BroadcastReceiver -- debe referenciarse en el manifest de la aplicacion para que sea
 * reconocido por la misma
 */

public class ConnectivityReceiver extends BroadcastReceiver {

    public static ConnectivityReceiverListener connectivityReceiverListener;

    public ConnectivityReceiver() {
        super();
    }

    public static boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) CombellezaApp.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }

}
