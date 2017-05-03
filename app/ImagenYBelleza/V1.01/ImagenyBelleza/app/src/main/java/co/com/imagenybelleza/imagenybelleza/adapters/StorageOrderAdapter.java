package co.com.imagenybelleza.imagenybelleza.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.ItemStates;
import co.com.imagenybelleza.imagenybelleza.helpers.CircleView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;

/**
 * Created by Juan Camilo Villa Amaya on 19/01/2017.
 * Clase usada en el modulo de bodega, permite ver los pedidos y su progreso de empacado actual
 * Params: contexto de donde es llamada, Lista de pedidos, contexto de donde se llama
 */

public class StorageOrderAdapter extends ArrayAdapter<Order> {

    private Context context;
    private int resource;
    private List<Order> orders;
    private DatabaseHelper database;

    //Constructor de la clase
    public StorageOrderAdapter(Context context, int resource, List<Order> orders) {
        super(context, resource, orders);

        this.database = new DatabaseHelper(context);
        this.context = context;
        this.resource = resource;
        this.orders = orders;

    }

    @NonNull
    @Override
    //Obtiene la vista personalizada de cada item y le setea los atributos
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {

            holder = new ViewHolder();
            convertView = View.inflate(context, resource, null);

            holder.dateLabel = (RobotoRegularTextView) convertView.findViewById(R.id.date_label);
            holder.separatedItemCountLabel = (RobotoLightTextView) convertView.findViewById(R.id.separed_items_count_label);
            holder.separatedItemPercentageLabel = (RobotoLightTextView) convertView.findViewById(R.id.separed_items_percentage_label);
            holder.itemsLabel = (RobotoLightTextView) convertView.findViewById(R.id.items_label);
            holder.dayCountLabel = (RobotoLightTextView) convertView.findViewById(R.id.days_label);
            holder.clientCompanyLabel = (RobotoLightTextView) convertView.findViewById(R.id.client_company_label);
            holder.stateLabel = (RobotoRegularTextView) convertView.findViewById(R.id.state_label);
            holder.stateView = (CircleView) convertView.findViewById(R.id.state_view);
            holder.codeLabel = (RobotoLightTextView) convertView.findViewById(R.id.code_label);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Order order = orders.get(position);
        if (order != null) {
            int itemsCount = getItemCount(order);
            holder.dateLabel.setText(order.getMade() + " - Creado hace " + String.valueOf(getDaysFromCreation(order) + " dias"));
            holder.itemsLabel.setText("Cantidad de productos: " + String.valueOf(itemsCount));
            holder.separatedItemCountLabel.setText("Productos separados: " + String.valueOf(getSeparatedCount(order)));
            holder.separatedItemPercentageLabel.setText(String.valueOf(getPercentage(order)) + " % ");
            holder.dayCountLabel.setText("Ultima modificacion: hace " + String.valueOf(getDays(order)) + " dias");
            holder.clientCompanyLabel.setText(order.getClient().getContact() + " - " + order.getClient().getCompany());
            holder.codeLabel.setText("Pedido: " + order.getId());
            holder.stateLabel.setText(order.getState().getState());
            if (order.getState().getHexColor() == null) {
                holder.stateView.setCircleColor(Color.parseColor("#FFFFFF"));
            } else {
                holder.stateView.setCircleColor(Color.parseColor(order.getState().getHexColor()));
            }
        }

        return convertView;
    }

    //Obtiene el procentaje de productos separados
    private double getPercentage(Order order) {
        double separated = getSeparatedCount(order);
        double items = getItemCount(order);
        return Math.round((separated / items) * 100);
    }

    //Obtiene la cantidad de productos separados
    private int getSeparatedCount(Order order) {
        int separedCount = 0;
        List<OrderItem> orderItems = database.getOrderItems(order.getId());
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getSubItemId() == 0) {
                if (orderItem.getOrderItemsState().getId() != ItemStates.ITEM_STATE_CANCELED) {
                    List<OrderItem> subOrderItems = database.getSubItemsFromItem(orderItem.getOrder().getId(), orderItem.getItem().getId());
                    if (subOrderItems.size() > 0) {
                        for (OrderItem subOrderItem : subOrderItems) {
                            if (subOrderItem.getOrderItemsState().getId() != ItemStates.ITEM_STATE_CANCELED) {
                                separedCount = separedCount + subOrderItem.getStorageUnits();
                            }
                        }
                    } else {
                        separedCount = separedCount + orderItem.getStorageUnits();
                    }
                }
            }
        }
        return separedCount;
    }

    //Obtiene el numero de dias desde que se creo el pedido
    private int getDaysFromCreation(Order order) {
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat currentFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String todayDate = currentFormatter.format(new Date());
        DateTime currentDate = format.parseDateTime(todayDate);
        DateTime orderDate = format.parseDateTime(order.getMade());

        return Days.daysBetween(orderDate, currentDate).getDays();
    }

    private int getDays(Order order) {

        /*DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat currentFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String todayDate = currentFormatter.format(new Date());
        DateTime currentDate = format.parseDateTime(todayDate);
        DateTime orderDate = format.parseDateTime(order.getModifiedDate());*/

        return 0;
    }

    private int getItemCount(Order order) {
        int totalCount = 0;
        List<OrderItem> orderItems = database.getOrderItems(order.getId());
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getSubItemId() == 0) {
                if (orderItem.getOrderItemsState().getId() != ItemStates.ITEM_STATE_CANCELED) {
                    List<OrderItem> subOrderItems = database.getSubItemsFromItem(orderItem.getOrder().getId(), orderItem.getItem().getId());
                    if (subOrderItems.size() > 0) {
                        for (OrderItem subOrderItem : subOrderItems) {
                            if (subOrderItem.getOrderItemsState().getId() != ItemStates.ITEM_STATE_CANCELED) {
                                totalCount = totalCount + subOrderItem.getFreeUnits() + subOrderItem.getUnits();
                            }
                        }
                    } else {
                        totalCount = totalCount + orderItem.getFreeUnits() + orderItem.getUnits();
                    }
                }
            }
        }
        return totalCount;
    }

    private class ViewHolder {
        RobotoRegularTextView dateLabel, stateLabel;
        RobotoLightTextView clientCompanyLabel, codeLabel, separatedItemCountLabel,
                separatedItemPercentageLabel, dayCountLabel, itemsLabel;
        CircleView stateView;
    }

}
