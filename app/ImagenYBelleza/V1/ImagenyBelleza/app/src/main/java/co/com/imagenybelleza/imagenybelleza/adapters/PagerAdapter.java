package co.com.imagenybelleza.imagenybelleza.adapters;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.fragments.ImageFragment;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;

/**
 * Created by Juan Camilo Villa Amaya on 19/01/2017.
 * Clase usada en el modulo del catalogo, permite ver los subproductos de un producto usando el gesto swipe - Hereda de FragmentPagerAdapter
 * Params: FragmentManager usado para llamar a los FragmentDialog, lista de archivos, contexto de donde es llamada,
 * Lista de subproductos y pedido
 */
public class PagerAdapter extends FragmentPagerAdapter {

    private final List<File> files;
    private final Context context;
    private final Order order;
    private List<Fragment> fragments;
    private List<Item> subItems;

    //Constructor de la clase
    public PagerAdapter(FragmentManager fm, List<File> files, Context context, List<Item> subItems, Order order) {
        super(fm);
        this.files = files;
        this.order = order;
        this.subItems = subItems;
        this.context = context;
        setFragmentList(files, subItems);
    }

    //Para cada elemento de la lista, instancia al FragmentDialog MainImageFragment
    // y añade el archivo de imagen correspondiente a ese subproducto
    private void setFragmentList(List<File> files, List<Item> subItems) {
        List<Fragment> fragments = new ArrayList<>();
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                ImageFragment fragment = new ImageFragment();
                fragment.setFile(files.get(i), context, subItems.get(i), order);
                fragments.add(fragment);
            }
            this.fragments = fragments;
        }
    }

    @Override
    //Metodo de la super clase, permite traer un Fragment dada la posicion en la lista
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    //Metodo de la super clase, permite contar cuantos archivos hay en la lista
    public int getCount() {
        if (files != null) {
            return files.size();
        }
        return 0;
    }

}
