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
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.models.Brand;

/**
 * Created by Juan Camilo Villa Amaya on 13/12/2016.
 * <p>
 * Usado para crear la vista personalizada en el la lista de marcas
 * <p>
 * Params: Contexto de donde se esta usando, id del recurso de la vista,
 * Lista de marcas y lista de archivos de imagen
 */

public class BrandsAdapter extends ArrayAdapter<Brand> {

    private final List<File> files;
    private List<Brand> brands;
    private int resource;
    private Context context;

    public BrandsAdapter(Context context, int resource, List<Brand> brands, List<File> files) {
        super(context, resource, brands);

        this.files = files;
        this.context = context;
        this.resource = resource;
        this.brands = brands;

    }

    @NonNull
    @Override
    //Obtiene la vista y le asigna los valores
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, resource, null);
            holder = new ViewHolder();
            holder.name = (RobotoRegularTextView) convertView.findViewById(R.id.brand_label);
            holder.placeHolderImage = (ImageView) convertView.findViewById(R.id.brand_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Brand brand = brands.get(position);
        if (brand != null) {
            holder.name.setText(brand.getName());
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName().toLowerCase().replace(".jpg", "");
                    if (fileName.equals(brand.getName().toLowerCase())) {
                        Picasso.with(context).load(file).placeholder(R.drawable.logo_black).into(holder.placeHolderImage);
                    }
                }
            }
        }

        return convertView;
    }

    //Alamacena los controles de la vista
    private class ViewHolder {
        RobotoRegularTextView name;
        ImageView placeHolderImage;
    }

}
