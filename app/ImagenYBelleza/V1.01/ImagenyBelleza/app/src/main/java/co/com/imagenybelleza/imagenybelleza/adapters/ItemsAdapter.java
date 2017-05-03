package co.com.imagenybelleza.imagenybelleza.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Item;

/**
 * Created by Juan Camilo Villa Amaya on 13/12/2016.
 * <p>
 * Usado para crear la vista personalizada en el la lista de productos
 * <p>
 * Params: Contexto de donde se esta usando, id del recurso de la vista,
 * Lista de productos, cliente actual
 */

public class ItemsAdapter extends ArrayAdapter<Item> {


    private Context context;
    private int resource;
    private List<Item> items;
    private Client client;
    private DatabaseHelper database;

    //Constructor de la clase
    public ItemsAdapter(Context context, int resource, List<Item> items, Client client) {
        super(context, resource, items);

        this.client = client;
        this.context = context;
        this.resource = resource;
        this.items = items;
        this.database = new DatabaseHelper(context);

    }

    //Obtiene el descuento en formato
    private String getDiscount(Item item) {
        double discount = 0;
        if (client.getClientType().getId() == 1) {
            discount = item.getPriceOne();
        }
        if (client.getClientType().getId() == 2) {
            discount = item.getPriceTwo();
        }
        if (client.getClientType().getId() == 3) {
            discount = item.getPriceThree();
        }
        if (client.getClientType().getId() == 4) {
            discount = item.getPriceFour();
        }
        if (client.getClientType().getId() == 5) {
            discount = item.getPriceFive();
        }
        return String.valueOf(discount) + "%";
    }

    @NonNull
    @Override
    //Obtiene la vista y asigna los valores
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
            holder.priceLabel.setText(String.format("%,.0f", database.getParentPrice(item.getId(), client.getClientType().getId())));
            holder.codeLabel.setText(String.valueOf(item.getId()));
            holder.descLabel.setText(getDiscount(item));
        }

        return convertView;
    }

    //Almacena los controles de la vista
    private class ViewHolder {
        RobotoLightTextView priceLabel, nameLabel, codeLabel, descLabel;
    }
}
