package co.com.imagenybelleza.imagenybelleza.main;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.ArraySwipeAdapter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.ItemStates;
import co.com.imagenybelleza.imagenybelleza.enums.States;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;
import co.com.imagenybelleza.imagenybelleza.models.Payment;

/*
* Actividad que permite crear un pedido nuevo
* Desde aqui se puede ingresar al modulo de productos (lista, catalogo)
* ver cantidades y precios, a la vez ver info.del cliente, comentarios del pedido
* */

public class CreateOrderActivity extends AppCompatActivity {

    private Context context;
    private DatabaseHelper database;
    private Order order;
    private TextInputEditText notesInput, deliverInput, paymentNotesInput;
    private List<OrderItem> orderItems = new ArrayList<>();
    private TextView codeLabel, totalPriceLabel, subTotalLabel, ivaLabel, totalDiscountLabel;
    private FloatingActionButton fab;
    private int orderId;
    private int clientId;
    private boolean fromBillActivity;
    private AppCompatSpinner termsSpinner, paymentSpinner;
    private String paymentName = null;
    private RobotoRegularTextView companyLabel, clientLabel;
    private ListView currentItemsListView;
    private LinearLayout notFoundView;
    private boolean settingSpinnerFromCode = false;
    private LinearLayout currentItemsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        clientId = getIntent().getIntExtra("client_id", 0);
        orderId = getIntent().getIntExtra("order_id", 0);

        fromBillActivity = getIntent().getBooleanExtra("from_bill_activity", false);

        context = CreateOrderActivity.this;
        database = new DatabaseHelper(context);

        currentItemsListView = (ListView) findViewById(R.id.current_items_list_view);
        currentItemsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                OrderItem orderItem = orderItems.get(position);
                if (orderItem.getOrder().getState().getId() == States.ORDER_STATE_CAPTURE) {
                    if (orderItem.getSubItemId() != 0) {
                        if (!database.isLastOrderItem(orderItem.getOrder().getId(), orderItem.getItem().getId())) {
                            if (database.deleteItem(orderItem.getOrder().getId(), orderItem.getItem().getId(), orderItem.getSubItemId())) {
                                new OrderPropertiesAsync().execute();
                                settingSpinnerFromCode = false;
                            } else {
                                Toast.makeText(context, "Error borrando los productos, intenta mas tarde", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (database.deleteAllFromOrderItem(orderItem.getOrder().getId(), orderItem.getItem().getId())) {
                                new OrderPropertiesAsync().execute();
                                settingSpinnerFromCode = false;
                            } else {
                                Toast.makeText(context, "Error borrando los productos, intenta mas tarde", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        if (database.deleteAllFromOrderItem(orderItem.getOrder().getId(), orderItem.getItem().getId())) {
                            new OrderPropertiesAsync().execute();
                            settingSpinnerFromCode = false;
                        } else {
                            Toast.makeText(context, "Error borrando los productos, intenta mas tarde", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Utils.showSnackbar("No puedes editar una orden enviada", CreateOrderActivity.this, R.id.create_order_activity);
                }
                return true;
            }
        });

        currentItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (order.getState().getId() == States.ORDER_STATE_CAPTURE) {
                    if (orderItems.get(position).getSubItemId() != 0) {
                        OrderItem orderItem = orderItems.get(position);
                        final Item subItem = createSubItem(orderItem, orderItem.getItem(), database.getClient(clientId));
                        final Item item = database.getParent(orderItem.getOrder().getId(), orderItem.getItem().getId()).getItem();
                        showAddSubItemItemDialog(context, subItem, database, item, clientId, orderId, orderItem);
                    } else {
                        if (!database.hasChildren(orderId, orderItems.get(position).getItem().getId())) {
                            showAddItemDialog(context, clientId, orderItems.get(position).getItem(), database, orderId, CreateOrderActivity.this);
                        } else {
                            Utils.showSnackbar("Para editar la cantidad de este item, debes modificar la cantidad de sus subitems", CreateOrderActivity.this, R.id.create_order_activity);
                        }
                    }
                } else {
                    Utils.showSnackbar("No puedes editar el pedido enviado", CreateOrderActivity.this, R.id.create_order_activity);
                }
            }
        });

        notFoundView = (LinearLayout) findViewById(R.id.not_found_view);
        currentItemsView = (LinearLayout) findViewById(R.id.current_items_view);

        ivaLabel = (TextView) findViewById(R.id.iva_label);
        totalDiscountLabel = (TextView) findViewById(R.id.total_discount_label);
        subTotalLabel = (TextView) findViewById(R.id.subtotal_label);

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

        paymentSpinner = (AppCompatSpinner) findViewById(R.id.payment_spinner);
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
                        if (order.getPayment().getName().equals("Credito")) {
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
                        } else if (order.getPayment().getName().equals("Credito pronto pago")) {
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
                        } else {
                            termsSpinner.setSelection(0);
                        }
                    } else {
                        termsSpinner.setSelection(2);
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

        termsSpinner = (AppCompatSpinner) findViewById(R.id.term_spinner);
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

                if (order.getPayment().getName().equals("Credito pronto pago") && paymentDiscount != 0) {
                    String paymentNotes = "Al pagar a " + order.getPayment().getTerm() + " dias, usted tendra un descuento adicional de " + String.format("%,.0f", paymentDiscount)
                            + ", para un total de " + String.format("%,.0f", (getTotal() - paymentDiscount));
                    paymentNotesInput.setText(paymentNotes);
                    order.setPaymentNotes(paymentNotes);
                } else {
                    paymentNotesInput.setText("No aplica descuento de pronto pago");
                    order.setPaymentNotes("No aplica descuento de pronto pago");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        deliverInput = (TextInputEditText) findViewById(R.id.deliver_input);
        deliverInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar today = Calendar.getInstance();
                int year = today.get(Calendar.YEAR);
                int month = today.get(Calendar.MONTH);
                int day = today.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(CreateOrderActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        String date = String.valueOf(selectedyear) + "-" + String.valueOf(selectedmonth + 1) + "-" + String.valueOf(selectedday);
                        long value = System.currentTimeMillis() - Date.valueOf(date).getTime();
                        if (value <= 0) {
                            deliverInput.setText(date);
                            order.setDeliver(date);
                            database.insertOrder(order);
                        } else {
                            Utils.showSnackbar("La fecha ingresada es menor a la actual", CreateOrderActivity.this, R.id.create_order_activity);
                        }
                    }
                }, year, month, day);
                mDatePicker.setTitle("Fecha de entrega");
                mDatePicker.show();
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.add_items_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateOrderActivity.this, ItemsListActivity.class).putExtra("order_id", order.getId()).putExtra("client_id", clientId));
                finish();
            }
        });

        if (orderId != 0) {
            if (database.getCurrentOrder(orderId) != null) {
                setStartingOrder(database.getCurrentOrder(orderId));
                if (order.getState().getId() == States.ORDER_STATE_CANCELLED || order.getState().getId() == States.ORDER_STATE_PENDING) {
                    order.setState(database.getOrderState(States.ORDER_STATE_CAPTURE));
                    database.updateOrderState(order);
                }
                if (order.getState().getId() != States.ORDER_STATE_CAPTURE && order.getState().getId() != States.ORDER_STATE_PENDING) {
                    disableAll();
                }
                new OrderPropertiesAsync().execute();
            } else {
                Utils.showSnackbar("La orden es invalida", CreateOrderActivity.this, R.id.create_order_activity);
            }
        } else {
            new CreateOrderAsync().execute();
        }
    }

    private Item createSubItem(OrderItem subOrderItem, Item item, Client client) {
        double price = 0;
        Item subItem = new Item();
        subItem.setId(item.getId());
        subItem.setName(subOrderItem.getSubItemName());
        subItem.setIva(item.getIva());
        subItem.setBrand(item.getBrand());
        subItem.setCategory(item.getCategory());
        subItem.setActive(item.isActive());
        subItem.setSubItemId(subOrderItem.getSubItemId());
        subItem.setPriceFive(item.getPriceFive());
        subItem.setPriceFour(item.getPriceFour());
        subItem.setPriceThree(item.getPriceThree());
        subItem.setPriceTwo(item.getPriceTwo());
        subItem.setPriceOne(item.getPriceOne());
        subItem.setPaymentOne(item.getPaymentOne());
        subItem.setPaymentTwo(item.getPaymentTwo());
        subItem.setPaymentThree(item.getPaymentThree());
        subItem.setPaymentFour(item.getPaymentFour());
        return subItem;
    }

    private void disableAll() {
        paymentSpinner.setEnabled(false);
        termsSpinner.setEnabled(false);
        currentItemsListView.setEnabled(false);
        deliverInput.setEnabled(false);
        notesInput.setEnabled(false);
        fab.setEnabled(false);
        fab.setVisibility(View.GONE);
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
            RobotoRegularTextView titleTwo = (RobotoRegularTextView) dialog.findViewById(R.id.title_two_label);
            titleTwo.setVisibility(View.GONE);
        }

        RobotoLightTextView phoneThreeLabel = (RobotoLightTextView) dialog.findViewById(R.id.phone_three_label);

        if (TextUtils.isEmpty(client.getPhoneThree())) {
            phoneThreeLabel.setText(client.getPhoneThree());
        } else {
            phoneThreeLabel.setVisibility(View.GONE);
            RobotoRegularTextView titleThree = (RobotoRegularTextView) dialog.findViewById(R.id.title_three_label);
            titleThree.setVisibility(View.GONE);
        }

        RobotoLightTextView mailLabel = (RobotoLightTextView) dialog.findViewById(R.id.mail_label);
        mailLabel.setText(client.getMail());
        RobotoLightTextView codeLabel = (RobotoLightTextView) dialog.findViewById(R.id.code_label);
        codeLabel.setText(String.valueOf(client.getCode()));

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_order, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_cancel:
                if (order.getState().getId() == States.ORDER_STATE_CAPTURE || order.getState().getId() == States.ORDER_STATE_PENDING) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateOrderActivity.this);
                    builder.setTitle("Cancelar");
                    builder.setMessage("¿Estas seguro de cancelar el pedido actual?");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            order.setState(database.getOrderState(States.ORDER_STATE_CANCELLED));
                            if (database.updateOrderState(order)) {
                                Toast.makeText(context, "Pedido cancelado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Error cambiando de estado, intenta mas tarde", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                            startActivity(new Intent(CreateOrderActivity.this, OrderActivity.class));
                            finish();
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
                } else {
                    Utils.showSnackbar("No puedes cancelar una orden enviada", CreateOrderActivity.this, R.id.create_order_activity);
                }
                return true;

            case R.id.action_save:
                if (order.getState().getId() == States.ORDER_STATE_CAPTURE || order.getState().getId() == States.ORDER_STATE_PENDING) {
                    if (orderItems.size() > 0) {

                        String notes = notesInput.getText().toString();
                        String deliver = deliverInput.getText().toString();
                        String payment = paymentNotesInput.getText().toString();

                        if (deliver.equals("")) {
                            Utils.showSnackbar("Debes ingresar la fecha estimada de entrega", CreateOrderActivity.this, R.id.create_order_activity);
                            return true;
                        }

                        if (order.getPayment() != null) {
                            new SendOrderAsync(order.getPayment(), notes, deliver, payment).execute();
                        } else {
                            Utils.showSnackbar("Debes escoger primero un metodo de pago", CreateOrderActivity.this, R.id.create_order_activity);
                        }

                    } else {
                        Utils.showSnackbar("El pedido no tiene productos aun, debe agregar por lo menos uno", CreateOrderActivity.this, R.id.create_order_activity);
                    }
                } else {
                    startActivity(new Intent(CreateOrderActivity.this, OrderActivity.class));
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void showAddItemDialog(final Context context, final int clientId, final Item it
            , final DatabaseHelper database, final int orderId, final AppCompatActivity currentActivity) {

        final Dialog dialog = new Dialog(context, R.style.StyledDialog);
        View dialogView = View.inflate(context, R.layout.dialog_add_item, null);
        dialog.setContentView(dialogView);

        final RobotoLightTextView eqPriceLabel = (RobotoLightTextView) dialog.findViewById(R.id.eq_price_label);
        eqPriceLabel.setText(String.valueOf(0));

        final TextInputEditText priceInput = (TextInputEditText) dialog.findViewById(R.id.price_input);
        priceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!priceInput.isFocusable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cambio de precio");
                    builder.setMessage("¿Estas seguro de cambiar el precio del producto?, necesitas autorizacion previa de un administrador");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            priceInput.setFocusableInTouchMode(true);
                            priceInput.requestFocus();
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
        Client client = database.getClient(clientId);
        priceInput.setText(String.valueOf(database.getParentPrice(it.getId(), client.getClientType().getId())));
        final TextInputEditText unitsInput = (TextInputEditText) dialog.findViewById(R.id.units_input);
        final TextInputEditText freeUnitsInput = (TextInputEditText) dialog.findViewById(R.id.free_units_input);
        freeUnitsInput.setText(String.valueOf(0));
        unitsInput.setText(String.valueOf(0));
        final TextInputEditText notesInput = (TextInputEditText) dialog.findViewById(R.id.notes_input);

        unitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Utils.isInteger(s.toString()) && Utils.isInteger(freeUnitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        int price = Double.valueOf(priceInput.getText().toString()).intValue();
                        double eqValue = ((price) * Integer.valueOf(unitsInput.getText().toString())) / (Integer.valueOf(unitsInput.getText().toString()) + Integer.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    unitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        freeUnitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Utils.isInteger(s.toString()) && Utils.isInteger(unitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        int price = Double.valueOf(priceInput.getText().toString()).intValue();
                        double eqValue = (price * Integer.valueOf(unitsInput.getText().toString())) / (Integer.valueOf(unitsInput.getText().toString()) + Integer.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    freeUnitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        freeUnitsInput.setText(String.valueOf(0));


        Button twelvePlusButton = (Button) dialog.findViewById(R.id.twelve_plus_button);
        twelvePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusButton = (Button) dialog.findViewById(R.id.five_plus_button);
        fivePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusButton = (Button) dialog.findViewById(R.id.two_plus_button);
        twoPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusButton = (Button) dialog.findViewById(R.id.one_plus_button);
        onePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessButton = (Button) dialog.findViewById(R.id.twelve_less_button);
        twelveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessButton = (Button) dialog.findViewById(R.id.five_less_button);
        fiveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessButton = (Button) dialog.findViewById(R.id.two_less_button);
        twoLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessButton = (Button) dialog.findViewById(R.id.one_less_button);
        oneLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 1));
                }
            }
        });

        Button twelvePlusFreeButton = (Button) dialog.findViewById(R.id.twelve_plus_free_button);
        twelvePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusFreeButton = (Button) dialog.findViewById(R.id.five_plus_free_button);
        fivePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusFreeButton = (Button) dialog.findViewById(R.id.two_plus_free_button);
        twoPlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusFreeButton = (Button) dialog.findViewById(R.id.one_plus_free_button);
        onePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessFreeButton = (Button) dialog.findViewById(R.id.twelve_less_free_button);
        twelveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessFreeButton = (Button) dialog.findViewById(R.id.five_less_free_button);
        fiveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessFreeButton = (Button) dialog.findViewById(R.id.two_less_free_button);
        twoLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessFreeButton = (Button) dialog.findViewById(R.id.one_less_free_button);
        oneLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 1));
                }
            }
        });

        Button addButon = (Button) dialog.findViewById(R.id.add_button);
        addButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = priceInput.getText().toString();
                String units = unitsInput.getText().toString();
                String freeUnits = freeUnitsInput.getText().toString();
                int freeUnitsData;
                String notes = notesInput.getText().toString();

                if (price.equals("") || units.equals("") || Double.valueOf(price) <= 0) {
                    Toast.makeText(context, "Cantidad invalida", Toast.LENGTH_LONG).show();
                    return;
                }

                if (freeUnits.equals("")) {
                    freeUnitsData = 0;
                } else {
                    freeUnitsData = Integer.valueOf(freeUnits);
                }

                if (Integer.valueOf(units) <= 0 && Integer.valueOf(freeUnits) <= 0) {
                    Toast.makeText(context, "Alguna entre unidades y unidades regalo debe ser mayor a 0", Toast.LENGTH_LONG).show();
                    return;
                }

                double unitPrice = Double.valueOf(price);
                double discount = 0;
                OrderItem orderItems = new OrderItem();
                orderItems.setItem(it);
                orderItems.setNotes(notes);
                orderItems.setFreeUnits(freeUnitsData);
                orderItems.setIva(it.getIva());
                orderItems.setOrder(database.getOrder(orderId));
                orderItems.setSubItemId(it.getSubItemId());
                orderItems.setUnits(Integer.valueOf(units));
                orderItems.setUnitPrice(unitPrice);
                orderItems.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                orderItems.setPacker(null);
                orderItems.setStorageUnits(0);
                orderItems.setStorageNotes("");
                orderItems.setDiscount(discount * 100);
                orderItems.setValue(unitPrice);
                orderItems.setTotal(orderItems.getValue() * orderItems.getUnits());
                orderItems.setEqValue(Math.round((orderItems.getUnitPrice() * orderItems.getUnits()) / (orderItems.getUnits() + orderItems.getFreeUnits())));

                System.out.println("Descuento: " + orderItems.getDiscount());
                System.out.println("Total" + orderItems.getTotal());

                if (database.insertOrderItem(orderItems)) {
                    new OrderPropertiesAsync().execute();
                    dialog.dismiss();
                    Toast.makeText(context, "Añadido al pedido", Toast.LENGTH_LONG).show();
                    context.startActivity(new Intent(context, CreateOrderActivity.class).putExtra("client_id", clientId).putExtra("order_id", orderId));
                    currentActivity.finish();
                } else {
                    dialog.dismiss();
                    Toast.makeText(context, "Lo sentimos, intentalo de nuevo", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        OrderItem orderItem = database.getOrderItemInfo(orderId, it.getId(), it.getSubItemId());
        if (orderItem != null) {
            priceInput.setText(String.valueOf(orderItem.getUnitPrice()));
            unitsInput.setText(String.valueOf(orderItem.getUnits()));
            freeUnitsInput.setText(String.valueOf(orderItem.getFreeUnits()));
            notesInput.setText(String.valueOf(orderItem.getNotes()));
        }

        dialog.show();
    }

    private void showAddSubItemItemDialog(final Context context, final Item sub, final DatabaseHelper database
            , final Item it, final int clientId, final int orderId, final OrderItem clickedOrderItem) {

        final Dialog dialog = new Dialog(context, R.style.StyledDialog);
        View dialogView = View.inflate(context, R.layout.dialog_add_item, null);
        dialog.setContentView(dialogView);

        final RobotoLightTextView eqPriceLabel = (RobotoLightTextView) dialog.findViewById(R.id.eq_price_label);
        eqPriceLabel.setText(String.valueOf(0));

        final TextInputEditText priceInput = (TextInputEditText) dialog.findViewById(R.id.price_input);
        priceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!priceInput.isFocusable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cambio de precio");
                    builder.setMessage("¿Estas seguro de cambiar el precio del producto?, necesitas autorizacion previa de un administrador");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            priceInput.setFocusableInTouchMode(true);
                            priceInput.requestFocus();
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
        Client client = database.getClient(clientId);
        priceInput.setText(String.valueOf(database.getParentPrice(it.getId(), client.getClientType().getId())));
        final TextInputEditText unitsInput = (TextInputEditText) dialog.findViewById(R.id.units_input);
        unitsInput.setText(String.valueOf(0));
        final TextInputEditText notesInput = (TextInputEditText) dialog.findViewById(R.id.notes_input);
        final TextInputEditText freeUnitsInput = (TextInputEditText) dialog.findViewById(R.id.free_units_input);
        freeUnitsInput.setText(String.valueOf(0));

        unitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Utils.isInteger(s.toString()) && Utils.isInteger(freeUnitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        int price = Double.valueOf(priceInput.getText().toString()).intValue();
                        double eqValue = ((price) * Integer.valueOf(unitsInput.getText().toString())) / (Integer.valueOf(unitsInput.getText().toString()) + Integer.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    unitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        freeUnitsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Utils.isInteger(s.toString()) && Utils.isInteger(unitsInput.getText().toString())) {
                    if (Integer.valueOf(unitsInput.getText().toString()) != 0 || Integer.valueOf(freeUnitsInput.getText().toString()) != 0) {
                        int price = Double.valueOf(priceInput.getText().toString()).intValue();
                        double eqValue = (price * Integer.valueOf(unitsInput.getText().toString())) / (Integer.valueOf(unitsInput.getText().toString()) + Integer.valueOf(freeUnitsInput.getText().toString()));
                        eqPriceLabel.setText(String.valueOf(Math.round(eqValue)));
                    } else {
                        eqPriceLabel.setText(String.valueOf(0));
                    }
                } else {
                    eqPriceLabel.setText(String.valueOf(0));
                }

                if (s.toString().equals("")) {
                    freeUnitsInput.setText(String.valueOf(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button twelvePlusButton = (Button) dialog.findViewById(R.id.twelve_plus_button);
        twelvePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusButton = (Button) dialog.findViewById(R.id.five_plus_button);
        fivePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusButton = (Button) dialog.findViewById(R.id.two_plus_button);
        twoPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusButton = (Button) dialog.findViewById(R.id.one_plus_button);
        onePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessButton = (Button) dialog.findViewById(R.id.twelve_less_button);
        twelveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessButton = (Button) dialog.findViewById(R.id.five_less_button);
        fiveLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessButton = (Button) dialog.findViewById(R.id.two_less_button);
        twoLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessButton = (Button) dialog.findViewById(R.id.one_less_button);
        oneLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(unitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    unitsInput.setText(String.valueOf(Integer.valueOf(unitsInput.getText().toString()) - 1));
                }
            }
        });

        Button twelvePlusFreeButton = (Button) dialog.findViewById(R.id.twelve_plus_free_button);
        twelvePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 12));
            }
        });
        Button fivePlusFreeButton = (Button) dialog.findViewById(R.id.five_plus_free_button);
        fivePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 5));
            }
        });
        Button twoPlusFreeButton = (Button) dialog.findViewById(R.id.two_plus_free_button);
        twoPlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 2));
            }
        });
        Button onePlusFreeButton = (Button) dialog.findViewById(R.id.one_plus_free_button);
        onePlusFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) + 1));
            }
        });

        Button twelveLessFreeButton = (Button) dialog.findViewById(R.id.twelve_less_free_button);
        twelveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 12) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 12));
                }
            }
        });
        Button fiveLessFreeButton = (Button) dialog.findViewById(R.id.five_less_free_button);
        fiveLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 5) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 5));
                }
            }
        });
        Button twoLessFreeButton = (Button) dialog.findViewById(R.id.two_less_free_button);
        twoLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 2) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 2));
                }
            }
        });

        Button oneLessFreeButton = (Button) dialog.findViewById(R.id.one_less_free_button);
        oneLessFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = Integer.valueOf(freeUnitsInput.getText().toString());
                if ((value - 1) < 0) {
                    Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                } else {
                    freeUnitsInput.setText(String.valueOf(Integer.valueOf(freeUnitsInput.getText().toString()) - 1));
                }
            }
        });

        Button addButon = (Button) dialog.findViewById(R.id.add_button);
        addButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = priceInput.getText().toString();
                String units = unitsInput.getText().toString();
                String freeUnits = freeUnitsInput.getText().toString();
                int freeUnitsData;
                String notes = notesInput.getText().toString();

                if (price.equals("") || units.equals("") || Double.valueOf(price) <= 0) {
                    Toast.makeText(context, "Cantidad invalida, debe ser mayor a 0", Toast.LENGTH_LONG).show();
                    return;
                }
                if (freeUnits.equals("")) {
                    freeUnitsData = 0;
                } else {
                    freeUnitsData = Integer.valueOf(freeUnits);
                }

                if (Integer.valueOf(units) <= 0 && Integer.valueOf(freeUnits) <= 0) {
                    Toast.makeText(context, "Alguna entre unidades y unidades regalo debe ser mayor a 0", Toast.LENGTH_LONG).show();
                    return;
                }

                double unitPrice = Double.valueOf(price);
                double discount = 0;

                if (database.hasParentInOrder(orderId, it.getId())) {

                    OrderItem parent = database.getParent(orderId, it.getId());
                    int unitsData = Integer.valueOf(units);

                    if (clickedOrderItem.getFreeUnits() > freeUnitsData) {
                        parent.setFreeUnits(parent.getFreeUnits() - (clickedOrderItem.getFreeUnits() - freeUnitsData));
                    }
                    if (clickedOrderItem.getFreeUnits() < freeUnitsData) {
                        parent.setFreeUnits(parent.getFreeUnits() + (freeUnitsData - clickedOrderItem.getFreeUnits()));
                    }
                    if (clickedOrderItem.getUnits() > unitsData) {
                        parent.setUnits(parent.getUnits() - (clickedOrderItem.getUnits() - unitsData));
                        parent.setStorageUnits(0);
                    }
                    if (clickedOrderItem.getUnits() < unitsData) {
                        parent.setUnits(parent.getUnits() + (unitsData - clickedOrderItem.getUnits()));
                        parent.setStorageUnits(0);
                    }

                    parent.setStorageNotes("");
                    parent.setUnitPrice(unitPrice);
                    parent.setDiscount(discount * 100);
                    parent.setValue(unitPrice);
                    parent.setTotal(parent.getUnits() * unitPrice);
                    parent.setEqValue(Math.round((parent.getUnitPrice() * parent.getUnits()) / (parent.getUnits() + parent.getFreeUnits())));

                    OrderItem orderItems = new OrderItem();
                    orderItems.setItem(sub);
                    orderItems.setNotes(notes);
                    orderItems.setSubItemName(sub.getName());
                    orderItems.setFreeUnits(freeUnitsData);
                    orderItems.setIva(sub.getIva());
                    orderItems.setOrder(database.getOrder(orderId));
                    orderItems.setSubItemId(sub.getSubItemId());
                    orderItems.setUnits(Integer.valueOf(units));
                    orderItems.setStorageNotes("");
                    orderItems.setStorageUnits(0);
                    orderItems.setUnitPrice(unitPrice);
                    orderItems.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                    orderItems.setPacker(null);
                    orderItems.setDiscount(discount * 100);
                    orderItems.setValue(0);
                    orderItems.setTotal(0);
                    orderItems.setEqValue(0);

                    if (database.insertOrderItem(orderItems) && database.insertOrderItem(parent)) {
                        dialog.dismiss();
                        Toast.makeText(context, "Añadido al pedido", Toast.LENGTH_LONG).show();
                        new OrderPropertiesAsync().execute();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(context, "Lo sentimos, intentalo de nuevo", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    OrderItem orderItems = new OrderItem();
                    orderItems.setItem(it);
                    orderItems.setNotes(notes);
                    orderItems.setStorageNotes("");
                    orderItems.setStorageUnits(0);
                    orderItems.setFreeUnits(freeUnitsData);
                    orderItems.setIva(it.getIva());
                    orderItems.setOrder(database.getOrder(orderId));
                    orderItems.setSubItemId(0);
                    orderItems.setUnits(Integer.valueOf(units));
                    orderItems.setUnitPrice(unitPrice);
                    orderItems.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                    orderItems.setPacker(null);
                    orderItems.setDiscount(discount * 100);
                    orderItems.setValue(unitPrice);
                    orderItems.setTotal(orderItems.getValue() * orderItems.getUnits());
                    orderItems.setEqValue(Math.round((orderItems.getUnitPrice() * orderItems.getUnits()) / (orderItems.getUnits() + orderItems.getFreeUnits())));

                    OrderItem secondaryItem = new OrderItem();
                    secondaryItem.setItem(sub);
                    secondaryItem.setNotes(notes);
                    secondaryItem.setFreeUnits(freeUnitsData);
                    secondaryItem.setSubItemName(sub.getName());
                    secondaryItem.setIva(sub.getIva());
                    secondaryItem.setOrder(database.getOrder(orderId));
                    secondaryItem.setSubItemId(sub.getSubItemId());
                    secondaryItem.setUnits(Integer.valueOf(units));
                    secondaryItem.setStorageNotes("");
                    secondaryItem.setStorageUnits(0);
                    secondaryItem.setUnitPrice(unitPrice);
                    secondaryItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                    secondaryItem.setPacker(null);
                    secondaryItem.setDiscount(discount * 100);
                    secondaryItem.setValue(0);
                    secondaryItem.setTotal(0);
                    secondaryItem.setEqValue(0);

                    if (database.insertOrderItem(secondaryItem) && database.insertOrderItem(orderItems)) {
                        dialog.dismiss();
                        Toast.makeText(context, "Añadido al pedido", Toast.LENGTH_LONG).show();
                        new OrderPropertiesAsync().execute();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(context, "Lo sentimos, intentalo de nuevo", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        OrderItem orderItem = database.getOrderItemInfo(orderId, sub.getId(), sub.getSubItemId());
        if (orderItem != null) {
            priceInput.setText(String.valueOf(orderItem.getUnitPrice()));
            unitsInput.setText(String.valueOf(orderItem.getUnits()));
            freeUnitsInput.setText(String.valueOf(orderItem.getFreeUnits()));
            notesInput.setText(String.valueOf(orderItem.getNotes()));
        }

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.isGpsEnabled(context);
    }

    @Override
    public void onBackPressed() {
        if (!fromBillActivity) {
            startActivity(new Intent(CreateOrderActivity.this, OrderActivity.class));
            finish();
        } else {
            startActivity(new Intent(CreateOrderActivity.this, BillActivity.class));
            finish();
        }
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
        } else {
            paymentSpinner.setSelection(2);
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

    private class SendOrderAsync extends AsyncTask<Void, Void, Void> {

        String deliver, notes, paymentNotes;
        Payment payment;
        boolean updated = false;

        private SendOrderAsync(Payment payment, String notes, String deliver, String paymentNotes) {
            this.payment = payment;
            this.notes = notes;
            this.deliver = deliver;
            this.paymentNotes = paymentNotes;
        }

        @Override
        protected Void doInBackground(Void... params) {
            double totalOrder = 0;

            order.setPayment(payment);
            order.setNotes(notes);
            order.setPaymentNotes(paymentNotes);
            order.setDeliver(deliver);
            order.setState(database.getOrderState(States.ORDER_STATE_PENDING));
            order.setInProgress(false);

            List<OrderItem> orderItems = database.getOrderItems(order.getId());
            for (OrderItem orderItem : orderItems) {
                totalOrder = totalOrder + ((orderItem.getUnitPrice() * orderItem.getUnits()) - orderItem.getDiscount());
            }

            order.setTotal(totalOrder);
            updated = database.updateOrder(order);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (updated) {
                Utils.showSnackbar("Se actualizo con exito", CreateOrderActivity.this, R.id.create_order_activity);
                startActivity(new Intent(CreateOrderActivity.this, OrderActivity.class));
                finish();
            } else {
                Utils.showSnackbar("Error, intenta de nuevo", CreateOrderActivity.this, R.id.create_order_activity);
            }
        }
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
                orderItems = database.getOrderItems(order.getId());
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

                    if (paymentDiscount != 0) {
                        paymentNotes = "Al pagar a " + payment.getTerm() + " dias, usted tendra un descuento adicional de " + String.format("%,.0f", paymentDiscount)
                                + ", para un total de " + (totalPrice - paymentDiscount);
                    } else {
                        paymentNotes = "No aplica descuento de pronto pago";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            totalPriceLabel.setText("Total del pedido: $" + String.format("%,.0f", totalPrice));
            codeLabel.setText("Codigo de pedido: " + String.valueOf(order.getId()));
            clientLabel.setText("Cliente: " + database.getClient(clientId).getContact());
            companyLabel.setText("Empresa: " + database.getClient(clientId).getCompany());
            ivaLabel.setText("IVA: $" + String.format("%,.0f", iva));
            subTotalLabel.setText("Subtotal: $" + String.format("%,.0f", subTotalPrice));
            totalDiscountLabel.setText("Su descuento fue: $" + String.format("%,.0f", discount));
            paymentNotesInput.setText(paymentNotes);
            deliverInput.setText(date);
            notesInput.setText(comments);
            currentItemsListView.setAdapter(new CurrentItemsSwipeAdapter(context, R.layout.item_current_item, orderItems));
            System.out.println("Altura: " + currentItemsListView.getHeight());
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
            if (orderItem != null) {
                holder.layout.setShowMode(SwipeLayout.ShowMode.PullOut);
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
                holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OrderItem orderItem = orderItems.get(position);
                        if (orderItem.getOrder().getState().getId() == States.ORDER_STATE_CAPTURE) {
                            if (orderItem.getSubItemId() != 0) {
                                if (!database.isLastOrderItem(orderItem.getOrder().getId(), orderItem.getItem().getId())) {
                                    if (database.deleteItem(orderItem.getOrder().getId(), orderItem.getItem().getId(), orderItem.getSubItemId())) {
                                        new OrderPropertiesAsync().execute();
                                        settingSpinnerFromCode = false;
                                    } else {
                                        Toast.makeText(context, "Error borrando los productos, intenta mas tarde", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (database.deleteAllFromOrderItem(orderItem.getOrder().getId(), orderItem.getItem().getId())) {
                                        new OrderPropertiesAsync().execute();
                                        settingSpinnerFromCode = false;
                                    } else {
                                        Toast.makeText(context, "Error borrando los productos, intenta mas tarde", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                if (database.deleteAllFromOrderItem(orderItem.getOrder().getId(), orderItem.getItem().getId())) {
                                    new OrderPropertiesAsync().execute();
                                    settingSpinnerFromCode = false;
                                } else {
                                    Toast.makeText(context, "Error borrando los productos, intenta mas tarde", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Utils.showSnackbar("No puedes editar una orden enviada", CreateOrderActivity.this, R.id.create_order_activity);
                        }
                    }
                });
            }
            return convertView;
        }
    }

    private class ViewHolder {
        RobotoLightTextView codeLabel, priceLabel, unitsLabel, nameLabel;
        SwipeLayout layout;
        LinearLayout deleteButton, containerLayout;
    }

    private class CreateOrderAsync extends AsyncTask<Void, Void, Order> {

        private double totalPrice = 0.0;
        private int id;
        private String notes;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Order doInBackground(Void... params) {

            Order order = new Order();
            order.setId(Utils.getOrderId());
            id = order.getId();
            order.setMade(Utils.getCurrentDate());
            order.setSeller(database.getCurrentUser());
            order.setClient(database.getClient(clientId));
            order.setState(database.getOrderState(States.ORDER_STATE_CAPTURE));
            order.setModifiedDate(Utils.getCurrentDate());
            order.setSent(false);
            order.setBiller(null);
            order.setPayment(database.getPayment(4));
            notes = getIntent().getStringExtra("note");
            if (notes != null) {
                order.setNotes(notes);
            } else {
                order.setNotes(null);
            }
            order.setTotal(0);
            order.setInProgress(true);

            if (database.insertOrder(order)) {
                return order;
            } else {
                return null;
            }

        }

        @Override
        protected void onPostExecute(Order order) {
            super.onPostExecute(order);
            if (order != null) {
                setStartingOrder(order);
                totalPriceLabel.setText("Total del pedido: $" + String.format("%,.0f", totalPrice));
                clientLabel.setText("Cliente: " + database.getClient(clientId).getContact());
                companyLabel.setText("Empresa: " + database.getClient(clientId).getCompany());
                subTotalLabel.setText(subTotalLabel.getText() + " $" + String.format("%,.0f", Double.valueOf(0)));
                ivaLabel.setText(ivaLabel.getText() + " " + String.valueOf(0));
                codeLabel.setText("Codigo de pedido: " + String.valueOf(id));
                if (notes != null) {
                    notesInput.setText(notes);
                }
                notFoundView.setVisibility(View.VISIBLE);
                currentItemsView.setVisibility(View.INVISIBLE);
                setUpSpinners();
            } else {
                Utils.showSnackbar("Error creando orden, intenta de nuevo", CreateOrderActivity.this, R.id.create_order_activity);
                finish();
            }
        }
    }


}
