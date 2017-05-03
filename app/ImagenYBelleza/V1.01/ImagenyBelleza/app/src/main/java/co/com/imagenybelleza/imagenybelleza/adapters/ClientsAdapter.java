package co.com.imagenybelleza.imagenybelleza.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.models.Client;

/**
 * Created by Juan Camilo Villa Amaya on 13/12/2016.
 * <p>
 * Usado para crear la vista personalizada en el la lista de clientes
 * <p>
 * Params: Contexto de donde se esta usando, id del recurso de la vista y
 * Lista de clientes
 */
public class ClientsAdapter extends ArrayAdapter<Client> {

    private Context context;
    private int resource;
    private List<Client> clients;

    public ClientsAdapter(Context context, int resource, List<Client> clients) {
        super(context, resource, clients);
        this.context = context;
        this.resource = resource;
        this.clients = clients;
    }

    @NonNull
    @Override
    //Obtiene la vista y le asigna los valores
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = View.inflate(context, resource, null);
            holder.cityNeighborhoodLabel = (RobotoLightTextView) convertView.findViewById(R.id.city_neighborhood_label);
            holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.client_item_layout);
            holder.clientCompanyLabel = (RobotoRegularTextView) convertView.findViewById(R.id.client_company_label);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Client client = clients.get(position);
        if (client != null) {
            if (!client.isSent()) {
                holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRed));
            }
            holder.clientCompanyLabel.setText(client.getCompany() + " - " + client.getContact());
            holder.cityNeighborhoodLabel.setText(client.getCity().getCity() + " - " + client.getNeighborhood());
        }

        return convertView;
    }

    //Almacena los controles de la vista
    private class ViewHolder {
        RobotoRegularTextView clientCompanyLabel;
        RobotoLightTextView cityNeighborhoodLabel;
        LinearLayout itemLayout;
    }

}
