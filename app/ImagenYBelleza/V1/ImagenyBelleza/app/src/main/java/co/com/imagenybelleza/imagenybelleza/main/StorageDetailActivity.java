package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.adapters.StorageDetailAdapter;
import co.com.imagenybelleza.imagenybelleza.application.CombellezaApp;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.ItemStates;
import co.com.imagenybelleza.imagenybelleza.enums.States;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;

/*
* Actividad que muestra la lista de productos de un pedido y permite actualizar
* el estado de cada uno en tiempo real si hay red
*
* */

public class StorageDetailActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private DatabaseHelper database;
    private Context context;
    private int orderId;
    private ListView itemsListView;

    private List<OrderItem> orderItems;
    private Button finishButton, finishCompletedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_detail);

        context = StorageDetailActivity.this;
        database = new DatabaseHelper(context);

        orderId = getIntent().getIntExtra("order_id", 0);

        finishCompletedButton = (Button) findViewById(R.id.finish_complete_button);
        finishCompletedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Finalizar empacado");
                builder.setMessage("¿Deseas terminar el proceso de empacado? el pedido pasara a proceso de facturación. ¿desea terminar?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Order order = database.getOrder(orderId);
                        order.setState(database.getOrderState(States.ORDER_STATE_SEPARATION_FINISHED));
                        if (database.updateOrderState(order) && database.insertTempOrder(order)) {
                            Utils.updateAll(context, orderId);
                            startActivity(new Intent(StorageDetailActivity.this, StorageActivity.class));
                            finish();
                        } else {
                            Utils.showSnackbar("Error actualizando pedido", StorageDetailActivity.this, R.id.content_storage_detail);
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        itemsListView = (ListView) findViewById(R.id.items_list);
        finishButton = (Button) findViewById(R.id.finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Entregar pedido incompleto");
                builder.setMessage("Este pedido se entregará incompleto. Los productos no empacados se eliminarán de la factura. ¿desea terminar?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Order order = database.getOrder(orderId);
                        order.setState(database.getOrderState(States.ORDER_STATE_SEPARATION_FINISHED));
                        if (database.updateOrderState(order) && database.insertTempOrder(order)) {
                            Utils.updateAll(context, orderId);
                            startActivity(new Intent(StorageDetailActivity.this, StorageActivity.class));
                            finish();
                        } else {
                            Utils.showSnackbar("Error actualizando pedido", StorageDetailActivity.this, R.id.content_storage_detail);
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        new AsyncGetItems().execute();
    }

    public void reload() {
        new AsyncGetItems().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_storage_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_reset:
                new AsyncReset().execute();
                return true;
            case R.id.action_info:
                Dialog dialog = new Dialog(context, R.style.StyledDialog);
                View dialogView = View.inflate(context, R.layout.dialog_order_info, null);
                dialog.setContentView(dialogView);

                Order order = database.getOrder(orderId);

                RobotoLightTextView sellerLabel = (RobotoLightTextView) dialogView.findViewById(R.id.seller_label);
                sellerLabel.setText(order.getSeller().getContact());
                RobotoLightTextView orderCodeLabel = (RobotoLightTextView) dialogView.findViewById(R.id.order_id_label);
                orderCodeLabel.setText(String.valueOf(order.getId()));
                RobotoLightTextView clientLabel = (RobotoLightTextView) dialogView.findViewById(R.id.client_company_label);
                clientLabel.setText(order.getClient().getContact() + " - " + order.getClient().getCompany());
                RobotoLightTextView createdLabel = (RobotoLightTextView) dialogView.findViewById(R.id.created_label);
                createdLabel.setText(order.getMade());
                RobotoLightTextView billerLabel = (RobotoLightTextView) dialogView.findViewById(R.id.biller_label);
                billerLabel.setText(order.getBiller().getContact());
                RobotoLightTextView stateLabel = (RobotoLightTextView) dialogView.findViewById(R.id.state_label);
                stateLabel.setText(order.getState().getState());
                RobotoLightTextView notesLabel = (RobotoLightTextView) dialogView.findViewById(R.id.notes_label);
                if (TextUtils.isEmpty(order.getNotes())) {
                    notesLabel.setText("No hay notas");
                } else {
                    notesLabel.setText(order.getNotes());
                }

                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setOrderState() {

        Order order = database.getOrder(orderId);
        List<OrderItem> parentItems = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            if (orderItem.getSubItemId() == 0) {
                parentItems.add(orderItem);
            }
        }

        int separedCount = 0;
        int canceledCount = 0;
        int unseparedCount = 0;

        for (OrderItem orderItem : parentItems) {
            if (orderItem.getSubItemId() == 0) {
                if (orderItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_SEPARED) {
                    separedCount++;
                }
                if (orderItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_CANCELED) {
                    canceledCount++;
                }
                if (orderItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_UNSEPARED) {
                    unseparedCount++;
                }
            }
        }

        boolean hasProductPending = false;
        boolean hasPromoterPending = false;
        boolean hasProductAndPromoterPending = false;

        for (OrderItem orderItem : parentItems) {
            if (orderItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_PRODUCT_PENDING) {
                hasProductPending = true;
            }
            if (orderItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_PROMOTER_PENDING) {
                hasPromoterPending = true;
            }

            if (orderItem.getOrderItemsState().getId() == ItemStates.ITEM_PRODUCT_PROMOTER_PENDING) {
                hasProductAndPromoterPending = true;
            }
        }

        if (separedCount == parentItems.size()) {
            finishCompletedButton.setVisibility(View.VISIBLE);
        } else {
            finishCompletedButton.setVisibility(View.GONE);
        }

        if (unseparedCount == parentItems.size() || unseparedCount > 0 || hasPromoterPending || hasProductAndPromoterPending || (canceledCount == parentItems.size()) || (separedCount == parentItems.size())) {
            finishButton.setVisibility(View.GONE);
        } else {
            finishButton.setVisibility(View.VISIBLE);
        }

        if (separedCount == parentItems.size()) {
            order.setState(database.getOrderState(States.ORDER_STATE_SEPARATION_FINISHED));
            database.updateOrderState(order);
            database.insertTempOrder(order);
            Utils.updateAll(context, orderId);
            return;
        }

        if (canceledCount == parentItems.size()) {
            order.setState(database.getOrderState(States.ORDER_STATE_CANCELLED));
            database.updateOrderState(order);
            database.insertTempOrder(order);
            Utils.updateAll(context, orderId);
            return;
        }

        boolean hasUnsepared = false;

        for (OrderItem orderItem : parentItems) {
            if (unseparedCount > 0) {
                if (orderItem.getOrderItemsState().getId() != ItemStates.ITEM_STATE_UNSEPARED) {
                    hasUnsepared = true;
                }
            }
        }

        if (hasUnsepared) {
            order.setState(database.getOrderState(States.ORDER_STATE_SEPARATION_PROCESS));
            database.updateOrderState(order);
            database.insertTempOrder(order);
            Utils.updateAll(context, orderId);
            return;
        }

        if (separedCount > 0 && canceledCount > 0 && unseparedCount > 0 && !hasProductPending && !hasPromoterPending) {
            order.setState(database.getOrderState(States.ORDER_STATE_SEPARATION_PROCESS));
            database.updateOrderState(order);
            database.insertTempOrder(order);
            Utils.updateAll(context, orderId);
        }

        if (hasProductAndPromoterPending) {
            order.setState(database.getOrderState(States.ORDER_STATE_PRODUCT_AND_PROMOTER_PENDING));
            database.updateOrderState(order);
            database.insertTempOrder(order);
            Utils.updateAll(context, orderId);
            return;
        }

        if (hasProductPending && hasPromoterPending) {
            order.setState(database.getOrderState(States.ORDER_STATE_PRODUCT_AND_PROMOTER_PENDING));
            database.updateOrderState(order);
            database.insertTempOrder(order);
            Utils.updateAll(context, orderId);
            return;
        }
        if (hasProductPending && !hasPromoterPending) {
            order.setState(database.getOrderState(States.ORDER_STATE_PRODUCT_PENDING));
            database.updateOrderState(order);
            database.insertTempOrder(order);
            Utils.updateAll(context, orderId);
            return;
        }
        if (!hasProductPending && hasPromoterPending) {
            order.setState(database.getOrderState(States.ORDER_STATE_PROMOTER_PENDING));
            database.updateOrderState(order);
            database.insertTempOrder(order);
            Utils.updateAll(context, orderId);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(StorageDetailActivity.this, StorageActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CombellezaApp.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            Utils.updateAll(context, orderId);
        }
    }

    private class AsyncReset extends AsyncTask<Void, Void, Void> {

        private Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utils.showSnackbar("Reiniciando pedido...", StorageDetailActivity.this, R.id.content_storage_detail);
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (orderItems != null) {
                Order order = database.getOrder(orderId);
                order.setState(database.getOrderState(States.ORDER_STATE_APPROVED));
                database.updateOrderState(order);
                database.insertTempOrder(order);
                if (orderItems.size() > 0) {
                    for (OrderItem orderItem : orderItems) {
                        orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                        orderItem.setStorageUnits(0);
                        database.updateOrderItemState(orderItem);
                        database.updateItemStorageUnits(orderItem);
                    }
                } else {
                    orderItems = database.getOrderItems(orderId);
                    for (OrderItem orderItem : orderItems) {
                        orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                        orderItem.setStorageUnits(0);
                        database.updateOrderItemState(orderItem);
                        database.updateItemStorageUnits(orderItem);
                    }
                }
                Utils.updateOrderItems(orderItems, orderId, context);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            reload();
        }
    }

    private class AsyncGetItems extends AsyncTask<Void, Void, Void> {

        private Dialog dialog;
        private StorageDetailAdapter adapter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            orderItems = database.getOrderItems(orderId);
            adapter = new StorageDetailAdapter(context, R.layout.item_storage_detail_item, orderItems, StorageDetailActivity.this);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setOrderState();
            dialog.dismiss();
            itemsListView.setAdapter(adapter);
        }
    }

}
