package co.com.imagenybelleza.imagenybelleza.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.ItemStates;
import co.com.imagenybelleza.imagenybelleza.helpers.CircleView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;

/**
 * Created by Juan Camilo Villa Amaya on 3/12/2016.
 * <p>
 * Adaptador que permite obtener la vista personalizada en la lista de pedidos
 * Params: Contexto de donde se usa, recurso de la vista, lista de pedidos
 */

public class OrderAdapter extends ArrayAdapter<Order> {

    private Context context;
    private int resource;
    private List<Order> orders;
    private DatabaseHelper database;

    //Constructor de la clase
    public OrderAdapter(Context context, int resource, List<Order> orders) {
        super(context, resource, orders);

        this.database = new DatabaseHelper(context);
        this.context = context;
        this.resource = resource;
        this.orders = orders;

    }

    @NonNull
    @Override
    //Obtiene la vista y le asigna los valores
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {

            holder = new ViewHolder();
            convertView = View.inflate(context, resource, null);

            holder.datePriceLabel = (RobotoRegularTextView) convertView.findViewById(R.id.date_price_label);
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
            holder.datePriceLabel.setText(order.getModifiedDate() + " - $" + String.format("%,.0f", getOrderTotal(order.getId())));
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

    //Metodo que obtiene el valor total del pedido
    private double getOrderTotal(int id) {
        double totalOrder = 0;
        List<OrderItem> orderItems = database.getOrderItems(id);
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getSubItemId() == 0 && orderItem.getOrderItemsState().getId() != ItemStates.ITEM_STATE_CANCELED) {
                totalOrder += orderItem.getTotal();
            }
        }
        return totalOrder;
    }

    //Almacena los controles de la vista
    private class ViewHolder {
        RobotoRegularTextView datePriceLabel, stateLabel;
        RobotoLightTextView clientCompanyLabel, codeLabel;
        CircleView stateView;
    }
}
