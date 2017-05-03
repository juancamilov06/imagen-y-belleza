package co.com.imagenybelleza.imagenybelleza.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;

/**
 * Created by danim_000 on 2/01/2017.
 */

public class ImageFragment extends Fragment {

    private File file;
    private Item subItem;
    private Context context;
    private DatabaseHelper database;
    private Order order;

    public void setFile(File file, Context context, Item subItem, Order order) {
        this.file = file;
        this.context = context;
        this.subItem = subItem;
        this.order = order;
        database = new DatabaseHelper(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_full_image, null);
        ImageView fullImage = (ImageView) view.findViewById(R.id.full_item_image);
        fullImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showAddSubItemItemDialog(context, subItem, database, database.getItem(subItem.getId()), order.getClient().getId(), order.getId());
            }
        });
        RobotoLightTextView nameLabel = (RobotoLightTextView) view.findViewById(R.id.name_label);
        nameLabel.setText(file.getName().replace(".jpg", ""));
        RobotoLightTextView codeLabel = (RobotoLightTextView) view.findViewById(R.id.code_label);
        codeLabel.setText(String.valueOf(subItem.getId()));
        RobotoLightTextView priceLabel = (RobotoLightTextView) view.findViewById(R.id.price_label);
        priceLabel.setText("$" + String.format("%,.0f", database.getParentPrice(subItem.getId(), order.getClient().getClientType().getId()) * (1 - getDiscountNumber(subItem, order.getClient()))));
        Picasso.with(context).load(file).into(fullImage);
        return view;
    }

    private double getDiscountNumber(Item item, Client client) {
        double discount = 0;
        if (client.getClientType().getId() == 1) {
            discount = item.getPriceOne() / 100;
        }
        if (client.getClientType().getId() == 2) {
            discount = item.getPriceTwo() / 100;
        }
        if (client.getClientType().getId() == 3) {
            discount = item.getPriceThree() / 100;
        }
        if (client.getClientType().getId() == 4) {
            discount = item.getPriceFour() / 100;
        }
        if (client.getClientType().getId() == 5) {
            discount = item.getPriceFive() / 100;
        }
        return discount;
    }
}
