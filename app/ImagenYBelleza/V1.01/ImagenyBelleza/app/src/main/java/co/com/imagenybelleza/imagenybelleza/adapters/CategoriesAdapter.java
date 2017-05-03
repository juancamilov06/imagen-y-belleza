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
import co.com.imagenybelleza.imagenybelleza.models.Category;

/**
 * Created by Juan Camilo Villa Amaya on 13/12/2016.
 * <p>
 * Usado para crear la vista personalizada en el la lista de categorias
 * <p>
 * Params: Contexto de donde se esta usando, id del recurso de la vista,
 * Lista de categorias y lista de archivos de imagen
 */
public class CategoriesAdapter extends ArrayAdapter<Category> {

    private List<File> files;
    private List<Category> categories;
    private int resource;
    private Context context;

    public CategoriesAdapter(Context context, int resource, List<Category> categories, List<File> files) {
        super(context, resource, categories);

        this.context = context;
        this.resource = resource;
        this.categories = categories;
        this.files = files;

    }

    @NonNull
    @Override
    //Obtiene la vista y asigna los valores para cada item de las categorias
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, resource, null);
            holder = new ViewHolder();
            holder.name = (RobotoRegularTextView) convertView.findViewById(R.id.category_label);
            holder.placeHolderImage = (ImageView) convertView.findViewById(R.id.category_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Category category = categories.get(position);
        if (category != null) {
            holder.name.setText(category.getName());
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName().toLowerCase().replace(".jpg", "");
                    if (fileName.equals(category.getName().toLowerCase())) {
                        Picasso.with(context).load(file).placeholder(R.drawable.logo_black).into(holder.placeHolderImage);
                    }
                }
            }
        }

        return convertView;
    }

    //Almacena los controles de cada item del grid de categorias
    private class ViewHolder {
        RobotoRegularTextView name;
        ImageView placeHolderImage;
    }

}
