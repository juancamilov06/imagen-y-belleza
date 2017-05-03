package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.ArraySwipeAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Roles;
import co.com.imagenybelleza.imagenybelleza.enums.States;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;
import co.com.imagenybelleza.imagenybelleza.models.Payment;

/*
Actividad que permite ver el detalle de un pedido y aprobarlo
rechazarlo o facturarlo. A la vez, permite ver precios, info. del cliente
y unidades
* */

public class BillDetailActivity extends AppCompatActivity {

    private Context context;
    private DatabaseHelper database;
    private Order order;
    private TextInputEditText notesInput, deliverInput, paymentNotesInput;
    private List<OrderItem> orderItems = new ArrayList<>();
    private TextView codeLabel, totalPriceLabel, subTotalLabel, ivaLabel, totalDiscountLabel;
    private int orderId, clientId;
    private Spinner termsSpinner, paymentSpinner;
    private String paymentName = null;
    private RobotoRegularTextView companyLabel, clientLabel;
    private ListView currentItemsListView;
    private LinearLayout notFoundView;
    private boolean settingSpinnerFromCode = false;
    private LinearLayout currentItemsView, billFinishView, billOptionsView;
    private Button disapproveOrderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        clientId = getIntent().getIntExtra("client_id", 0);
        orderId = getIntent().getIntExtra("order_id", 0);

        context = BillDetailActivity.this;
        database = new DatabaseHelper(context);

        currentItemsListView = (ListView) findViewById(R.id.current_items_list_view);
        currentItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                OrderItem orderItem = orderItems.get(position);

                final Dialog dialog = new Dialog(context, R.style.StyledDialog);
                View dialogView = View.inflate(context, R.layout.dialog_biller_info, null);
                dialog.setContentView(dialogView);

                RobotoLightTextView orderedUnitsLabel = (RobotoLightTextView) dialog.findViewById(R.id.ordered_units_label);
                RobotoLightTextView storageUnitsLabel = (RobotoLightTextView) dialog.findViewById(R.id.storage_units_label);
                RobotoLightTextView eqPriceLabel = (RobotoLightTextView) dialog.findViewById(R.id.eq_price_label);
                Button closeButton = (Button) dialog.findViewById(R.id.close_button);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                orderedUnitsLabel.setText(String.valueOf(orderItem.getUnits() + " + " + orderItem.getFreeUnits() + " = " + (orderItem.getUnits() + orderItem.getFreeUnits())));
                storageUnitsLabel.setText(String.valueOf(orderItem.getStorageUnits()));

                if (orderItem.getSubItemId() == 0) {
                    eqPriceLabel.setText(String.valueOf(orderItem.getEqValue()));
                } else {
                    for (OrderItem item : orderItems) {
                        if (item.getItem().getId() == orderItem.getItem().getId() && item.getSubItemId() == 0) {
                            eqPriceLabel.setText(String.valueOf(item.getEqValue()));
                        }
                    }
                }

                dialog.show();
            }
        });

        notFoundView = (LinearLayout) findViewById(R.id.not_found_view);
        billFinishView = (LinearLayout) findViewById(R.id.bill_finish_view);
        billFinishView.setVisibility(View.GONE);
        billOptionsView = (LinearLayout) findViewById(R.id.bill_options_view);
        billOptionsView.setVisibility(View.GONE);
        currentItemsView = (LinearLayout) findViewById(R.id.current_items_view);

        ivaLabel = (TextView) findViewById(R.id.iva_label);
        totalDiscountLabel = (TextView) findViewById(R.id.total_discount_label);
        subTotalLabel = (TextView) findViewById(R.id.subtotal_label);

        Button finishButton = (Button) findViewById(R.id.finish_order_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectivityReceiver.isConnected()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BillDetailActivity.this);
                    builder.setTitle("Facturar");
                    builder.setMessage("¿Estas seguro finalizar y facturar el pedido actual?");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ConnectivityReceiver.isConnected()) {
                                order.setState(database.getOrderState(States.ORDER_STATE_FINISHED));
                                new SendFinishedOrderAsync().execute(order);
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
            }
        });

        Button approveOrderButton = (Button) findViewById(R.id.approve_order_button);
        approveOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order.getState().getId() == States.ORDER_STATE_SENT) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BillDetailActivity.this);
                    builder.setTitle("Facturar");
                    builder.setMessage("¿Estas seguro de aprobar el pedido actual?");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (order.getState().getId() == States.ORDER_STATE_SENT) {
                                if (ConnectivityReceiver.isConnected()) {
                                    Utils.lockOrientation(BillDetailActivity.this);
                                    order.setState(database.getOrderState(States.ORDER_STATE_APPROVED));
                                    order.setSent(false);
                                    new SendBilledOrderAsync().execute(order);
                                } else {
                                    order.setState(database.getOrderState(States.ORDER_STATE_APPROVED));
                                    order.setSent(false);
                                    order.setBiller(database.getCurrentUser());
                                    database.updateOrderBiller(order);
                                    database.updateOrderStateAndSent(order);
                                    Utils.showSnackbar("No se detecto conexion a internet, \n active la conexion para enviar \n automaticamente las facturaciones pendientes", BillDetailActivity.this, R.id.activity_bill_detail);
                                    startActivity(new Intent(BillDetailActivity.this, BillActivity.class));
                                }
                            } else {
                                Utils.showSnackbar("La orden ya se aprobo pero esta pendiente de envio \n Active la conexion para enviarla automaticamente", BillDetailActivity.this, R.id.activity_bill_detail);
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
                if (order.getState().getId() == States.ORDER_STATE_DISAPPROVED && database.getCurrentUser().getRole().equals(Roles.ROLE_ADMIN)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BillDetailActivity.this);
                    builder.setTitle("Facturar");
                    builder.setMessage("¿Estas seguro de pasar la orden rechazada a aprobada?");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (order.getState().getId() == States.ORDER_STATE_DISAPPROVED) {
                                if (ConnectivityReceiver.isConnected()) {
                                    Utils.lockOrientation(BillDetailActivity.this);
                                    order.setState(database.getOrderState(States.ORDER_STATE_APPROVED));
                                    order.setSent(false);
                                    new SendBilledOrderAsync().execute(order);
                                } else {
                                    order.setState(database.getOrderState(States.ORDER_STATE_APPROVED));
                                    order.setSent(false);
                                    order.setBiller(database.getCurrentUser());
                                    database.updateOrderBiller(order);
                                    database.updateOrderStateAndSent(order);
                                    Utils.showSnackbar("No se detecto conexion a internet, \n active la conexion para enviar \n automaticamente las facturaciones pendientes", BillDetailActivity.this, R.id.activity_bill_detail);
                                    startActivity(new Intent(BillDetailActivity.this, BillActivity.class));
                                }
                            } else {
                                Utils.showSnackbar("La orden ya se facturo pero esta pendiente de envio \n Active la conexion para enviarla automaticamente", BillDetailActivity.this, R.id.activity_bill_detail);
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
                if (order.getState().getId() == States.ORDER_STATE_APPROVED) {
                    Utils.showSnackbar("El pedido ya ha sido enviado", BillDetailActivity.this, R.id.activity_bill_detail);
                }
            }
        });

        disapproveOrderButton = (Button) findViewById(R.id.disapprove_order_button);
        disapproveOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order.getState().getId() == States.ORDER_STATE_SENT) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BillDetailActivity.this);
                    builder.setTitle("Facturar");
                    builder.setMessage("¿Estas seguro de rechazar el pedido actual");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (order.getState().getId() == States.ORDER_STATE_SENT) {
                                if (ConnectivityReceiver.isConnected()) {
                                    order.setState(database.getOrderState(States.ORDER_STATE_DISAPPROVED));
                                    order.setSent(false);
                                    new SendBilledOrderAsync().execute(order);
                                } else {
                                    order.setState(database.getOrderState(States.ORDER_STATE_DISAPPROVED));
                                    order.setSent(false);
                                    order.setBiller(database.getCurrentUser());
                                    database.updateOrderBiller(order);
                                    database.updateOrderStateAndSent(order);
                                    Utils.showSnackbar("No se detecto conexion a internet, \n active la conexion para enviar \n automaticamente las facturaciones pendientes", BillDetailActivity.this, R.id.activity_bill_detail);
                                    startActivity(new Intent(BillDetailActivity.this, BillActivity.class));
                                }
                            } else {
                                Utils.showSnackbar("La orden ya se desaprobo pero esta pendiente de envio \n Active la conexion para enviarla automaticamente", BillDetailActivity.this, R.id.activity_bill_detail);
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
                if (order.getState().getId() == States.ORDER_STATE_DISAPPROVED) {
                    Utils.showSnackbar("No puedes rechazar una orden ya rechazada", BillDetailActivity.this, R.id.activity_bill_detail);
                }
                if (order.getState().getId() == States.ORDER_STATE_APPROVED) {
                    Utils.showSnackbar("No puedes rechazar una orden ya aprobada", BillDetailActivity.this, R.id.activity_bill_detail);
                }

            }
        });

        clientLabel = (RobotoRegularTextView) findViewById(R.id.client_label);
        clientLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClientInfo();
            }
        });

        companyLabel = (RobotoRegularTextView) findViewById(R.id.company_label);
        companyLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClientInfo();
            }
        });

        codeLabel = (TextView) findViewById(R.id.code_label);
        totalPriceLabel = (TextView) findViewById(R.id.total_label);
        notesInput = (TextInputEditText) findViewById(R.id.notes_input);
        paymentNotesInput = (TextInputEditText) findViewById(R.id.payment_notes_input);

        paymentSpinner = (Spinner) findViewById(R.id.payment_spinner);
        paymentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!settingSpinnerFromCode) {
                    List<Payment> payments = database.getPayments(order.getPayment().getName());

                    List<String> termsNames = new ArrayList<>();
                    for (Payment payment : payments) {
                        termsNames.add(String.valueOf(payment.getTerm()));
                    }
                    termsSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_spinner_item, termsNames));
                    if (order.getPayment() != null) {
                        switch (order.getPayment().getName()) {
                            case "Credito":
                                if (order.getPayment().getTerm() == 8) {
                                    termsSpinner.setSelection(0);
                                }
                                if (order.getPayment().getTerm() == 15) {
                                    termsSpinner.setSelection(1);
                                }
                                if (order.getPayment().getTerm() == 30) {
                                    termsSpinner.setSelection(2);
                                }
                                if (order.getPayment().getTerm() == 45) {
                                    termsSpinner.setSelection(3);
                                }
                                if (order.getPayment().getTerm() == 60) {
                                    termsSpinner.setSelection(4);
                                }
                                if (order.getPayment().getTerm() == 90) {
                                    termsSpinner.setSelection(5);
                                }
                                break;
                            case "Credito pronto pago":
                                if (order.getPayment().getTerm() == 0) {
                                    termsSpinner.setSelection(0);
                                }
                                if (order.getPayment().getTerm() == 8) {
                                    termsSpinner.setSelection(1);
                                }
                                if (order.getPayment().getTerm() == 15) {
                                    termsSpinner.setSelection(2);
                                }
                                if (order.getPayment().getTerm() == 30) {
                                    termsSpinner.setSelection(3);
                                }
                                break;
                            default:
                                termsSpinner.setSelection(0);
                                break;
                        }
                    }
                    settingSpinnerFromCode = true;
                } else {
                    paymentName = paymentSpinner.getSelectedItem().toString();
                    List<Payment> payments = database.getPayments(paymentName);
                    List<String> termsNames = new ArrayList<>();
                    for (Payment payment : payments) {
                        termsNames.add(String.valueOf(payment.getTerm()));
                    }
                    termsSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_spinner_item, termsNames));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        termsSpinner = (Spinner) findViewById(R.id.term_spinner);
        termsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int term = Integer.valueOf(termsSpinner.getSelectedItem().toString());
                order.setPayment(database.getPayment(term, paymentSpinner.getSelectedItem().toString()));
                database.insertOrder(order);

                double paymentDiscount = 0.0;
                orderItems = database.getOrderItems(orderId);
                for (OrderItem orderItem : orderItems) {
                    if (orderItem.getSubItemId() == 0) {
                        if (order.getPayment().getName().equals("Credito pronto pago")) {
                            double bg = getBg(orderItem);
                            if (order.getPayment().getTerm() == 0) {
                                paymentDiscount += bg * (orderItem.getItem().getPaymentOne() / 100);
                            }
                            if (order.getPayment().getTerm() == 8) {
                                paymentDiscount += bg * (orderItem.getItem().getPaymentTwo() / 100);
                            }
                            if (order.getPayment().getTerm() == 15) {
                                paymentDiscount += bg * (orderItem.getItem().getPaymentThree() / 100);
                            }
                            if (order.getPayment().getTerm() == 30) {
                                paymentDiscount += bg * (orderItem.getItem().getPaymentFour() / 100);
                            }
                        }
                    }
                }

                if (order.getPayment().getName().equals("Credito pronto pago")) {
                    String paymentNotes = "Al pagar a " + order.getPayment().getTerm() + " dias, usted tendra un descuento adicional de " + String.format("%,.2f", paymentDiscount)
                            + ", para un total de " + String.format("%,.0f", (getTotal() - paymentDiscount));
                    paymentNotesInput.setText(paymentNotes);
                } else {
                    paymentNotesInput.setText("No aplica descuento de pronto pago");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        deliverInput = (TextInputEditText) findViewById(R.id.deliver_input);

        if (orderId != 0) {
            if (database.getCurrentOrder(orderId) != null) {
                setStartingOrder(database.getCurrentOrder(orderId));
                disableAll();
                setButtons();
                new OrderPropertiesAsync().execute();
            } else {
                Utils.showSnackbar("La orden es invalida", BillDetailActivity.this, R.id.activity_bill_detail);
            }
        }
    }

    private void disableAll() {
        paymentSpinner.setEnabled(false);
        termsSpinner.setEnabled(false);
        deliverInput.setEnabled(false);
        notesInput.setEnabled(false);
        if (database.getCurrentUser().getRole().equals(Roles.ROLE_ADMIN) && order.getState().getId() == States.ORDER_STATE_DISAPPROVED) {
            disapproveOrderButton.setVisibility(View.GONE);
        }
    }

    private void setButtons() {
        if (order.getState().getId() == States.ORDER_STATE_SEPARATION_FINISHED) {
            billFinishView.setVisibility(View.VISIBLE);
            billOptionsView.setVisibility(View.GONE);
            return;
        }
        if (order.getState().getId() == States.ORDER_STATE_SENT) {
            billFinishView.setVisibility(View.GONE);
            billOptionsView.setVisibility(View.VISIBLE);
            return;
        }
        billFinishView.setVisibility(View.GONE);
        billOptionsView.setVisibility(View.GONE);
    }

    private void showClientInfo() {

        Dialog dialog = new Dialog(context, R.style.StyledDialog);
        View view = View.inflate(context, R.layout.dialog_client_detail, null);
        dialog.setContentView(view);

        Client client = database.getClient(clientId);

        RobotoLightTextView contactLabel = (RobotoLightTextView) dialog.findViewById(R.id.contact_label);
        contactLabel.setText(client.getContact());
        RobotoLightTextView companyLabel = (RobotoLightTextView) dialog.findViewById(R.id.company_label);
        companyLabel.setText(client.getCompany());
        RobotoLightTextView nitLabel = (RobotoLightTextView) dialog.findViewById(R.id.nit_label);
        nitLabel.setText(client.getNit());
        RobotoLightTextView typeLabel = (RobotoLightTextView) dialog.findViewById(R.id.client_type_label);
        typeLabel.setText(client.getClientType().getName());
        RobotoLightTextView addressLabel = (RobotoLightTextView) dialog.findViewById(R.id.address_label);
        addressLabel.setText(client.getAddress());
        RobotoLightTextView cityLabel = (RobotoLightTextView) dialog.findViewById(R.id.city_label);
        cityLabel.setText(client.getCity().getCity());
        RobotoLightTextView neighborhoodLabel = (RobotoLightTextView) dialog.findViewById(R.id.neighborhood_label);
        neighborhoodLabel.setText(client.getNeighborhood());
        RobotoLightTextView phoneLabel = (RobotoLightTextView) dialog.findViewById(R.id.phone_one_label);
        phoneLabel.setText(client.getPhoneOne());
        RobotoLightTextView phoneTwoLabel = (RobotoLightTextView) dialog.findViewById(R.id.phone_two_label);

        if (TextUtils.isEmpty(client.getPhoneTwo())) {
            phoneTwoLabel.setText(client.getPhoneTwo());
        } else {
            phoneTwoLabel.setVisibility(View.GONE);
        }

        RobotoLightTextView phoneThreeLabel = (RobotoLightTextView) dialog.findViewById(R.id.phone_three_label);

        if (TextUtils.isEmpty(client.getPhoneThree())) {
            phoneThreeLabel.setText(client.getPhoneThree());
        } else {
            phoneThreeLabel.setVisibility(View.GONE);
        }
        RobotoLightTextView mailLabel = (RobotoLightTextView) dialog.findViewById(R.id.mail_label);
        mailLabel.setText(client.getMail());

        dialog.show();
    }

    private double getTotal() {
        double totalPrice = 0.0;
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getSubItemId() == 0) {
                double orderItemPrice = orderItem.getTotal();
                totalPrice += orderItemPrice;
            }
        }

        return totalPrice;
    }

    private double getBg(OrderItem orderItem) {
        orderItems = database.getOrderItems(order.getId());
        if (orderItem.getSubItemId() == 0) {
            double orderItemPrice = orderItem.getTotal();
            return orderItemPrice / (1 + (orderItem.getIva() / 100));
        }

        return 0.0;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(BillDetailActivity.this, BillActivity.class));
        finish();
    }

    private void setStartingOrder(Order order) {
        this.order = order;
    }

    private void setUpSpinners() {
        List<String> paymentsNames = database.getPaymentsNames();
        paymentSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.item_spinner_item, paymentsNames));
        if (order.getPayment() != null) {
            switch (order.getPayment().getName()) {
                case "Efectivo":
                    paymentSpinner.setSelection(0);
                    break;
                case "Credito":
                    paymentSpinner.setSelection(1);
                    break;
                default:
                    paymentSpinner.setSelection(2);
                    break;
            }
        }

        if (orderItems.size() > 0) {
            notFoundView.setVisibility(View.GONE);
            currentItemsView.setVisibility(View.VISIBLE);
        } else {
            notFoundView.setVisibility(View.VISIBLE);
            currentItemsView.setVisibility(View.GONE);
        }
    }

    public void calculateListHeight(ListView list) {
        int height = 50;
        for (int i = 0; i < list.getCount(); i++) {
            View childView = list.getAdapter().getView(i, null, list);
            childView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            height += childView.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = list.getLayoutParams();
        height += list.getDividerHeight() * list.getCount();
        params.height = height;
        currentItemsListView.setLayoutParams(params);
        currentItemsListView.requestLayout();
    }

    private JSONObject getAllOrderItems() throws JSONException {
        JSONObject mainObject = new JSONObject();
        JSONArray array = new JSONArray();
        List<OrderItem> orderItems = database.getOrderItems(order.getId());
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getSubItemId() == 0) {
                JSONObject object = new JSONObject();
                object.put("order_id", order.getId());
                object.put("item_id", orderItem.getItem().getId());
                object.put("subitem_id", orderItem.getSubItemId());
                object.put("units", orderItem.getUnits());
                object.put("unit_price", orderItem.getUnitPrice());
                object.put("free_units", orderItem.getFreeUnits());
                object.put("storage_units", orderItem.getStorageUnits());
                object.put("storage_notes", orderItem.getStorageNotes());
                object.put("notes", orderItem.getNotes());
                object.put("order_items_state_id", orderItem.getOrderItemsState().getId());
                object.put("iva", orderItem.getIva());
                object.put("discount", orderItem.getDiscount());
                object.put("total", orderItem.getTotal());
                object.put("value", orderItem.getValue());
                object.put("subitem_name", orderItem.getSubItemName());
                object.put("eq_value", orderItem.getEqValue());
                array.put(object);
            }
        }
        mainObject.put("allOrdersItems", array);
        System.out.println("Main all: " + mainObject.toString());
        return mainObject;
    }

    private class OrderPropertiesAsync extends AsyncTask<Void, Void, Void> {

        private double totalPrice = 0.0;
        private String date = "";
        private String comments = "";
        private double iva = 0.0;
        private double bg = 0.0;
        private double discount = 0.0;
        private double subTotalPrice = 0.0;
        private String paymentNotes = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (order.getState().getId() != States.ORDER_STATE_SEPARATION_FINISHED) {
                    orderItems = database.getOrderItems(order.getId());
                } else {
                    orderItems = database.getFinishedOrderItems(order.getId());
                }

                for (OrderItem orderItem : orderItems) {
                    if (orderItem.getSubItemId() == 0) {
                        double orderItemPrice = orderItem.getTotal();
                        totalPrice += orderItemPrice;
                        bg = orderItemPrice / (1 + (orderItem.getIva() / 100));
                        subTotalPrice += bg;
                    }
                }
                for (OrderItem orderItem : orderItems) {
                    if (orderItem.getSubItemId() == 0) {
                        discount += (database.getParentPrice(orderItem.getItem().getId(), database.getClient(clientId).getClientType().getId()) - orderItem.getValue()) * orderItem.getUnits();
                    }
                }
                iva = totalPrice - subTotalPrice;
                if (order.getDeliver() != null) {
                    date = order.getDeliver();
                }
                if (order.getNotes() != null) {
                    comments = order.getNotes();
                }
                if (order.getPayment() != null) {
                    Payment payment = order.getPayment();
                    double paymentDiscount = 0.0;
                    for (OrderItem orderItem : orderItems) {
                        if (orderItem.getSubItemId() == 0) {
                            if (payment.getTerm() == 0) {
                                paymentDiscount += bg * (orderItem.getItem().getPaymentOne() / 100);
                            }
                            if (payment.getTerm() == 8) {
                                paymentDiscount += bg * (orderItem.getItem().getPaymentTwo() / 100);
                            }
                            if (payment.getTerm() == 15) {
                                paymentDiscount += bg * (orderItem.getItem().getPaymentThree() / 100);
                            }
                            if (payment.getTerm() == 30) {
                                paymentDiscount += bg * (orderItem.getItem().getPaymentFour() / 100);
                            }
                        }
                    }
                    paymentNotes = "Al pagar a " + payment.getTerm() + " dias, usted tendra un descuento adicional de " + String.format("%,.2f", paymentDiscount)
                            + ", para un total de " + (totalPrice - paymentDiscount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            totalPriceLabel.setText("Total del pedido: $" + String.format("%,.2f", totalPrice));
            codeLabel.setText("Codigo de pedido: " + String.valueOf(order.getId()));
            clientLabel.setText("Cliente: " + database.getClient(clientId).getContact());
            companyLabel.setText("Empresa: " + database.getClient(clientId).getCompany());
            ivaLabel.setText("IVA: $" + String.format("%,.2f", iva));
            subTotalLabel.setText("Subtotal: $" + String.format("%,.2f", subTotalPrice));
            totalDiscountLabel.setText("Su descuento fue: $" + String.format("%,.2f", discount));
            paymentNotesInput.setText(paymentNotes);
            deliverInput.setText(date);
            notesInput.setText(comments);
            currentItemsListView.setAdapter(new CurrentItemsSwipeAdapter(context, R.layout.item_current_item, orderItems));
            setUpSpinners();
            calculateListHeight(currentItemsListView);
        }
    }

    public class CurrentItemsSwipeAdapter extends ArraySwipeAdapter<OrderItem> {

        private Context context;
        private int resource;
        private List<OrderItem> orderItems;

        CurrentItemsSwipeAdapter(Context context, int resource, List<OrderItem> orderItems) {
            super(context, resource, orderItems);
            this.context = context;
            this.resource = resource;
            this.orderItems = orderItems;
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return 0;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(context, resource, null);
                holder.layout = (SwipeLayout) convertView.findViewById(R.id.swipe_layout);
                holder.codeLabel = (RobotoLightTextView) convertView.findViewById(R.id.code_label);
                holder.deleteButton = (LinearLayout) convertView.findViewById(R.id.delete_button);
                holder.priceLabel = (RobotoLightTextView) convertView.findViewById(R.id.price_label);
                holder.unitsLabel = (RobotoLightTextView) convertView.findViewById(R.id.units_label);
                holder.nameLabel = (RobotoLightTextView) convertView.findViewById(R.id.name_label);
                holder.containerLayout = (LinearLayout) convertView.findViewById(R.id.container_layout);
                convertView.setTag(convertView);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            OrderItem orderItem = orderItems.get(position);
            if (order.getState().getId() != States.ORDER_STATE_SEPARATION_FINISHED) {
                if (orderItem != null) {
                    holder.layout.setSwipeEnabled(false);
                    if (orderItem.getSubItemId() == 0) {
                        holder.containerLayout.setBackgroundColor(Color.parseColor("#151515"));
                        Item item = orderItem.getItem();
                        holder.nameLabel.setText(item.getName());
                        holder.codeLabel.setText(String.valueOf(item.getId()));
                        if (orderItem.getFreeUnits() == 0) {
                            holder.unitsLabel.setText(String.valueOf(orderItem.getUnits()));
                        } else {
                            holder.unitsLabel.setText(String.valueOf(orderItem.getUnits()) + " + " + String.valueOf(orderItem.getFreeUnits()));
                        }
                        holder.priceLabel.setText(String.format("%,.0f", orderItem.getUnitPrice()));
                    } else {
                        Item item = orderItem.getItem();
                        holder.nameLabel.setText(orderItem.getSubItemName());
                        holder.codeLabel.setText(String.valueOf(item.getId()));
                        if (orderItem.getFreeUnits() == 0) {
                            holder.unitsLabel.setText(String.valueOf(orderItem.getUnits()));
                        } else {
                            holder.unitsLabel.setText(String.valueOf(orderItem.getUnits()) + " + " + String.valueOf(orderItem.getFreeUnits()));
                        }
                    }
                }
            } else {
                if (orderItem != null) {
                    holder.layout.setSwipeEnabled(false);
                    if (orderItem.getSubItemId() == 0) {
                        holder.containerLayout.setBackgroundColor(Color.parseColor("#151515"));
                        Item item = orderItem.getItem();
                        holder.nameLabel.setText(item.getName());
                        holder.codeLabel.setText(String.valueOf(item.getId()));
                        holder.unitsLabel.setText(String.valueOf(orderItem.getStorageUnits()));
                        holder.priceLabel.setText(String.format("%,.0f", orderItem.getEqValue()));
                    } else {
                        Item item = orderItem.getItem();
                        holder.nameLabel.setText(orderItem.getSubItemName());
                        holder.codeLabel.setText(String.valueOf(item.getId()));
                        holder.unitsLabel.setText(String.valueOf(orderItem.getStorageUnits()));
                    }
                }
            }
            return convertView;
        }
    }

    private class ViewHolder {
        RobotoLightTextView codeLabel, priceLabel, unitsLabel, nameLabel;
        SwipeLayout layout;
        LinearLayout deleteButton, containerLayout;
    }

    private class SendFinishedOrderAsync extends AsyncTask<Order, Void, Void> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Order... params) {
            final Order order = params[0];
            final RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.FINISH_BILL_SERVICE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Utils.unlockOrientation(BillDetailActivity.this);
                    System.out.println("Respuesta: " + response);
                    try {
                        JSONObject object = new JSONObject(response);
                        String message = object.getString("mensaje");
                        if (message.equals("Creacion exitosa")) {
                            order.setSent(true);
                            order.setBiller(database.getCurrentUser());
                            if (database.updateOrderStateAndSent(order) && database.updateOrderBiller(order)) {
                                dialog.dismiss();
                                Utils.showSnackbar("Aprobacion exitosa", BillDetailActivity.this, R.id.activity_bill_detail);
                                startActivity(new Intent(BillDetailActivity.this, BillActivity.class));
                                finish();
                            } else {
                                dialog.dismiss();
                                Utils.showSnackbar("Actualizacion correcta en base de datos pero no en dispositivo, elimine los datos de la aplicacion y reiniciela por favor", BillDetailActivity.this, R.id.activity_bill_detail);
                            }
                        } else {
                            dialog.dismiss();
                            Utils.showSnackbar("Error en el servidor, intente de nuevo", BillDetailActivity.this, R.id.activity_bill_detail);
                        }
                    } catch (JSONException e) {
                        dialog.dismiss();
                        Utils.showSnackbar("No se pudo enviar la factura al servidor, intente de nuevo", BillDetailActivity.this, R.id.activity_bill_detail);
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Utils.unlockOrientation(BillDetailActivity.this);
                    dialog.dismiss();
                    Utils.showSnackbar("Error en el servidor, intente de nuevo", BillDetailActivity.this, R.id.activity_bill_detail);
                }
            }) {
                @Override
                public Priority getPriority() {
                    return Priority.IMMEDIATE;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    try {
                        params.put("id", String.valueOf(order.getId()));
                        params.put("biller_id", String.valueOf(database.getCurrentUser().getId()));
                        params.put("state_id", String.valueOf(order.getState().getId()));
                        params.put("order_items", getAllOrderItems().get("allOrdersItems").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return params;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
            return null;
        }
    }

    private class SendBilledOrderAsync extends AsyncTask<Order, Void, Void> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = Utils.getAlertDialog(context);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Order... params) {
            final Order order = params[0];
            final RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.UPDATE_ORDER_SERVICE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Utils.unlockOrientation(BillDetailActivity.this);
                    System.out.println("Respuesta: " + response);
                    try {
                        JSONObject object = new JSONObject(response);
                        String message = object.getString("mensaje");
                        if (message.equals("Creacion exitosa")) {
                            order.setSent(true);
                            order.setBiller(database.getCurrentUser());
                            if (database.updateOrderStateAndSent(order) && database.updateOrderBiller(order)) {
                                dialog.dismiss();
                                Utils.showSnackbar("Aprobacion exitosa", BillDetailActivity.this, R.id.activity_bill_detail);
                                startActivity(new Intent(BillDetailActivity.this, BillActivity.class));
                                finish();
                            } else {
                                dialog.dismiss();
                                Utils.showSnackbar("Actualizacion correcta en base de datos pero no en dispositivo, elimine los datos de la aplicacion y reiniciela por favor", BillDetailActivity.this, R.id.activity_bill_detail);
                            }
                        } else {
                            dialog.dismiss();
                            Utils.showSnackbar("Error en el servidor, intente de nuevo", BillDetailActivity.this, R.id.activity_bill_detail);
                        }
                    } catch (JSONException e) {
                        dialog.dismiss();
                        Utils.showSnackbar("No se pudo enviar la factura al servidor, intente de nuevo", BillDetailActivity.this, R.id.activity_bill_detail);
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Utils.unlockOrientation(BillDetailActivity.this);
                    dialog.dismiss();
                    Utils.showSnackbar("Error en el servidor, intente de nuevo", BillDetailActivity.this, R.id.activity_bill_detail);
                }
            }) {
                @Override
                public Priority getPriority() {
                    return Priority.IMMEDIATE;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", String.valueOf(order.getId()));
                    params.put("biller_id", String.valueOf(database.getCurrentUser().getId()));
                    params.put("state_id", String.valueOf(order.getState().getId()));
                    return params;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
            return null;
        }
    }

}