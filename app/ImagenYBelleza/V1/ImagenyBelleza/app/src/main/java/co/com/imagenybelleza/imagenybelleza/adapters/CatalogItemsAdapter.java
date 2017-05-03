package co.com.imagenybelleza.imagenybelleza.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Item;

/**
 * Created by Juan Camilo Villa Amaya on 13/12/2016.
 * <p>
 * Usado para crear la vista personalizada en el grid de productos
 * en el catalogo
 * <p>
 * Params: Contexto de donde se esta usando, id del recurso de la vista,
 * Lista de items, Cliente actual y lista de archivos de imagen
 */

public class CatalogItemsAdapter extends ArrayAdapter<Item> {


    private final List<File> files;
    private Context context;
    private int resource;
    private List<Item> items;
    private Client client;
    private DatabaseHelper database;

    //Constructor de la clase
    public CatalogItemsAdapter(Context context, int resource, List<Item> items, Client client, List<File> files) {
        super(context, resource, items);

        this.files = files;
        this.client = client;
        this.context = context;
        this.resource = resource;
        this.items = items;
        this.database = new DatabaseHelper(context);

    }

    @NonNull
    @Override
    //Obtiene la lista y le asigna los valores de cada item
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, resource, null);
            holder.productImage = (ImageView) convertView.findViewById(R.id.product_image);
            holder.nameLabel = (RobotoLightTextView) convertView.findViewById(R.id.name_label);
            holder.priceLabel = (RobotoLightTextView) convertView.findViewById(R.id.price_label);
            holder.codeLabel = (RobotoLightTextView) convertView.findViewById(R.id.code_label);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = items.get(position);
        if (item != null) {
            holder.priceLabel.setText(String.format("%,.0f", item.getPrice() * (1 - getDiscountNumber(item))));
            if (hasSubproducts(item.getId())) {
                holder.codeLabel.setText(String.valueOf(item.getId()) + "   (+)");
            } else {
                holder.codeLabel.setText(String.valueOf(item.getId()));
            }
            holder.nameLabel.setText(item.getName());
            if (files != null) {
                File file = getFile(item.getId());
                if (file != null) {
                    Picasso.with(context).load(file).into(holder.productImage);
                }
            }
        }

        return convertView;
    }

    //Calcula si el producto tiene subproductos
    private boolean hasSubproducts(int itemId) {

        File file = new File(database.getDirectory(), "items");
        file = new File(file, String.valueOf(itemId));

        return file.isDirectory();

    }

    //Obtiene el archivo de imagen de un producto
    private File getFile(int id) {
        for (File file : files) {
            String fileName = file.getName().toLowerCase().replace(".jpg", "");
            if (fileName.equals(String.valueOf(id))) {
                return file;
            }
        }

        return null;
    }

    //Obtiene el descuento de un producto
    private double getDiscountNumber(Item item) {
        double discount = 0;
        if (client.getClientType().getId() == 1) {
            discount = item.getDiscountOne() / 100;
        }
        if (client.getClientType().getId() == 2) {
            discount = item.getDiscountTwo() / 100;
        }
        if (client.getClientType().getId() == 3) {
            discount = item.getDiscountThree() / 100;
        }
        if (client.getClientType().getId() == 4) {
            discount = item.getDiscountFour() / 100;
        }
        if (client.getClientType().getId() == 5) {
            discount = item.getDiscountFive() / 100;
        }
        return discount;
    }

    //Almacena los controles de cada item del grid de productos
    private class ViewHolder {
        RobotoLightTextView priceLabel, nameLabel, codeLabel;
        ImageView productImage;
    }
}
