package co.com.imagenybelleza.imagenybelleza.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.adapters.SubItemsAdapter;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;

/**
 * Created by Manuela Duque M on 19/01/2017.
 */

public class MainImageFragment extends Fragment {

    private File file;
    private Item item;
    private Context context;
    private DatabaseHelper database;
    private Order order;
    private List<Item> subItems;
    private AlertDialog alertDialog;
    private FragmentManager fragmentManager;

    public void setFile(File file, Context context, Item item, Order order, FragmentManager fragmentManager) {
        this.file = file;
        this.context = context;
        this.item = item;
        this.order = order;
        this.fragmentManager = fragmentManager;
        database = new DatabaseHelper(context);
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    inFiles.addAll(getListFiles(file));
                } else {
                    if (file.getName().endsWith(".jpg")) {
                        inFiles.add(file);
                    }
                }
            }
            return inFiles;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_full_image, null);
        ImageView fullImage = (ImageView) view.findViewById(R.id.full_item_image);
        fullImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(database.getDirectory());
                file = new File(file, "items");
                file = new File(file, String.valueOf(item.getId()));
                if (file.isDirectory()) {
                    final List<File> files = getListFiles(file);
                    Client client = order.getClient();
                    subItems = new ArrayList<>();
                    File mydir = new File(file.getPath());
                    File lister = mydir.getAbsoluteFile();
                    int index = 0;

                    for (String list : lister.list()) {
                        Item subItem = new Item();
                        list = list.replace(".jpg", "");
                        index++;
                        double discount = 0;
                        subItem.setId(item.getId());
                        subItem.setName(list);
                        subItem.setIva(item.getIva());
                        subItem.setBrand(item.getBrand());
                        subItem.setCategory(item.getCategory());
                        subItem.setActive(item.isActive());
                        subItem.setSubItemId(index);
                        subItem.setPriceFive(item.getPriceFive());
                        subItem.setPriceFour(item.getPriceFour());
                        subItem.setPriceThree(item.getPriceThree());
                        subItem.setPriceTwo(item.getPriceTwo());
                        subItem.setPriceOne(item.getPriceOne());
                        subItem.setPaymentOne(item.getPaymentOne());
                        subItem.setPaymentTwo(item.getPaymentTwo());
                        subItem.setPaymentThree(item.getPaymentThree());
                        subItem.setPaymentFour(item.getPaymentFour());
                    }

                    AlertDialog.Builder dialog = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
                    View dialogView = View.inflate(context, R.layout.dialog_sub_items, null);
                    dialog.setView(dialogView);
                    ListView subItemListView = (ListView) dialogView.findViewById(R.id.subitem_list_view);
                    subItemListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            Utils.showAddSubItemItemDialog(context, subItems.get(position), database, item, order.getClient().getId(), order.getId());
                            return true;
                        }
                    });
                    subItemListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(android.widget.AdapterView<?> parent, View view, final int position, final long id) {
                            if (files != null) {
                                FragmentDialog fragmentDialog = new FragmentDialog();
                                fragmentDialog.setArgs(files, position, context, subItems, database.getOrder(order.getId()));
                                fragmentDialog.show(fragmentManager, "FragmentDialog");
                            }
                        }
                    });
                    subItemListView.setAdapter(new SubItemsAdapter(context, R.layout.item_item, subItems, client));
                    alertDialog = dialog.create();
                    alertDialog.show();
                } else {
                    Utils.showAddItemCatalogDialog(context, order.getClient().getId(), item, database, order.getId());
                }
            }
        });
        RobotoLightTextView nameLabel = (RobotoLightTextView) view.findViewById(R.id.name_label);
        nameLabel.setText(item.getName());
        RobotoLightTextView codeLabel = (RobotoLightTextView) view.findViewById(R.id.code_label);
        if (hasSubItems(item.getId())) {
            codeLabel.setText(String.valueOf(item.getId()) + " (+)");
        } else {
            codeLabel.setText(String.valueOf(item.getId()));
        }
        RobotoLightTextView priceLabel = (RobotoLightTextView) view.findViewById(R.id.price_label);
        priceLabel.setText(String.format("%,.0f", database.getParentPrice(item.getId(), order.getClient().getClientType().getId())));
        Picasso.with(context).load(file).into(fullImage);
        return view;
    }

    private boolean hasSubItems(int id) {
        File file = new File(database.getDirectory());
        file = new File(file, "items");
        file = new File(file, String.valueOf(id));
        return file.isDirectory();
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
