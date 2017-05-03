package co.com.imagenybelleza.imagenybelleza.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Item;

/**
 * Created by Juan Camilo Villa Amaya on 5/12/2016.
 * Clase usada para obtener la lista personalizada de los subproductos
 * Params: Contexto de donde se llama, id del recurso de la vista, lista de subproductos, cliente actual
 */

public class SubItemsAdapter extends ArrayAdapter<Item> {


    private Client client;
    private Context context;
    private int resource;
    private List<Item> items;

    //Constructor de la clase
    public SubItemsAdapter(Context context, int resource, List<Item> items, Client client) {
        super(context, resource, items);

        this.client = client;
        this.context = context;
        this.resource = resource;
        this.items = items;

    }

    @NonNull
    @Override
    //Obtiene la vista personalizada y asigna los valores a ella
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, resource, null);
            holder.nameLabel = (RobotoLightTextView) convertView.findViewById(R.id.name_label);
            holder.priceLabel = (RobotoLightTextView) convertView.findViewById(R.id.price_label);
            holder.codeLabel = (RobotoLightTextView) convertView.findViewById(R.id.code_label);
            holder.descLabel = (RobotoLightTextView) convertView.findViewById(R.id.desc_label);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = items.get(position);
        if (item != null) {
            holder.nameLabel.setText(item.getName());
            holder.priceLabel.setText(String.format("%,.0f", item.getPrice()));
            holder.codeLabel.setText(String.valueOf(item.getId()));
            holder.descLabel.setText(getDiscount(item));
        }

        return convertView;
    }

    //Obtiene el descuento por tipo de cliente formateado de un producto
    private String getDiscount(Item item) {
        double discount = 0;
        if (client.getClientType().getId() == 1) {
            discount = item.getDiscountOne();
        }
        if (client.getClientType().getId() == 2) {
            discount = item.getDiscountTwo();
        }
        if (client.getClientType().getId() == 3) {
            discount = item.getDiscountThree();
        }
        if (client.getClientType().getId() == 4) {
            discount = item.getDiscountFour();
        }
        if (client.getClientType().getId() == 5) {
            discount = item.getDiscountFive();
        }
        return String.valueOf(discount) + "%";
    }

    //Almacena las referencias de los controles en la vista
    private class ViewHolder {
        RobotoLightTextView priceLabel, nameLabel, codeLabel, descLabel;
    }
}