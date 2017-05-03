package co.com.imagenybelleza.imagenybelleza.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.fragments.MainImageFragment;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;

/**
 * Created by Juan Camilo Villa Amaya on 19/01/2017.
 * Clase usada en el modulo del catalogo, permite ver los productos usando el gesto swipe - Hereda de FragmentPagerAdapter
 * Params: FragmentManager usado para llamar a los FragmentDialog, lista de archivos, contexto de donde es llamada,
 * Lista de items y pedido
 */

public class MainPagerAdapter extends FragmentPagerAdapter {
    private final List<File> files;
    private final Context context;
    private final Order order;
    private final FragmentManager fragmentManager;
    private List<Fragment> fragments;
    private List<Item> items;

    //Constructor de la clase
    public MainPagerAdapter(List<File> files, Context context, List<Item> items, Order order, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;
        this.files = files;
        this.order = order;
        this.items = items;
        this.context = context;
        setFragmentList(files, items);
    }

    //Para cada elemento de la lista, instancia al FragmentDialog MainImageFragment
    // y añade el archivo de imagen correspondiente a ese producto
    private void setFragmentList(List<File> files, List<Item> items) {
        List<Fragment> fragments = new ArrayList<>();
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MainImageFragment fragment = new MainImageFragment();
                fragment.setFile(files.get(i), context, items.get(i), order, fragmentManager);
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
