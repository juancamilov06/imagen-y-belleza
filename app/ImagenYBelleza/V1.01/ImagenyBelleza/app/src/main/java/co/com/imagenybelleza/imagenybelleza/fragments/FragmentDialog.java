package co.com.imagenybelleza.imagenybelleza.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.adapters.PagerAdapter;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;

/**
 * Created by danim_000 on 2/01/2017.
 */

public class FragmentDialog extends DialogFragment {

    private List<File> files;
    private int position;
    private Context context;
    private List<Item> subItems;
    private Order order;

    public void setArgs(List<File> files, int position, Context context, List<Item> subItems, Order order) {
        this.files = files;
        this.order = order;
        this.context = context;
        this.position = position;
        this.subItems = subItems;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FilePickerTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_pager, null);
        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager(), files, context, subItems, order);
        if (adapter.getCount() != 0) {
            pager.setAdapter(adapter);
            pager.setCurrentItem(position);
        }
        return view;
    }
}
