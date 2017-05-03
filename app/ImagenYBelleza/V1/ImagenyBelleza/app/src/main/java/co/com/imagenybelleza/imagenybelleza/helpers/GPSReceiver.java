package co.com.imagenybelleza.imagenybelleza.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import co.com.imagenybelleza.imagenybelleza.application.CombellezaApp;

/**
 * Created by danim_000 on 2/01/2017.
 */
public class GPSReceiver extends BroadcastReceiver {

    public static LocationReceiverListener locationReceiverListener;

    public GPSReceiver() {
        super();
    }

    public static boolean isGpsEnabled() {
        LocationManager manager = (LocationManager) CombellezaApp.getInstance().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isConnected = manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        System.out.println("Esta conectado: " + String.valueOf(isConnected));

        if (locationReceiverListener != null) {
            locationReceiverListener.onGpsStateChangedListener(isConnected);
        }

    }

    public interface LocationReceiverListener {
        void onGpsStateChangedListener(boolean isConnected);
    }

}
