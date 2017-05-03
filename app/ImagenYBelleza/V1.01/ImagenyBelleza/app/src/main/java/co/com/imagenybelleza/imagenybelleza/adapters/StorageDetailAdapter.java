package co.com.imagenybelleza.imagenybelleza.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.ItemStates;
import co.com.imagenybelleza.imagenybelleza.helpers.CircleView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.main.StorageDetailActivity;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;

/**
 * Created by Juan Camilo Villa Amaya on 26/01/2017.
 * <p>
 * Usado para crear la vista personalizada en la lista de productos
 * en el modulo de bodega
 * <p>
 * Params: Contexto de donde se esta usando, id del recurso de la vista,
 * Lista de items y Activity desde donde se llama
 */

public class StorageDetailAdapter extends ArrayAdapter<OrderItem> {

    private StorageDetailActivity storageDetailActivity;
    private Context context;
    private int resource;
    private List<OrderItem> items;
    private DatabaseHelper database;

    //Constructor de la clase
    public StorageDetailAdapter(Context context, int resource, List<OrderItem> items, StorageDetailActivity storageDetailActivity) {
        super(context, resource, items);

        this.database = new DatabaseHelper(context);
        this.context = context;
        this.resource = resource;
        this.storageDetailActivity = storageDetailActivity;
        this.items = items;

    }

    @NonNull
    @Override
    //Obtiene la vista y le asigna los valores
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = View.inflate(context, resource, null);
            holder.containerLayout = (LinearLayout) convertView.findViewById(R.id.container_layout);
            holder.onlySubItemLayout = (LinearLayout) convertView.findViewById(R.id.only_subitem_layout);
            holder.notesLayout = (LinearLayout) convertView.findViewById(R.id.notes_layout);
            holder.stateView = (CircleView) convertView.findViewById(R.id.state_view);
            holder.codeLabel = (RobotoRegularTextView) convertView.findViewById(R.id.code_label);
            holder.nameLabel = (RobotoRegularTextView) convertView.findViewById(R.id.name_label);
            holder.notesLabel = (RobotoLightTextView) convertView.findViewById(R.id.notes_label);
            holder.orderUnitsLabel = (RobotoLightTextView) convertView.findViewById(R.id.product_count_label);
            holder.packButton = (ImageButton) convertView.findViewById(R.id.pack_button);
            holder.cancelButton = (ImageButton) convertView.findViewById(R.id.cancel_button);
            holder.markableLayout = (LinearLayout) convertView.findViewById(R.id.markable_layout);
            holder.promoterPendingButton = (ImageButton) convertView.findViewById(R.id.promoter_pending_button);
            holder.productPendingButton = (ImageButton) convertView.findViewById(R.id.product_pending_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final OrderItem item = items.get(position);
        if (item != null) {
            holder.stateView.setCircleColor(Color.parseColor(item.getOrderItemsState().getHexColor()));
            holder.orderUnitsLabel.setText(String.valueOf(item.getStorageUnits() + item.getStorageFreeUnits()) + " / " + String.valueOf(item.getUnits() + item.getFreeUnits()));
            if (TextUtils.isEmpty(item.getNotes())) {
                holder.notesLayout.setVisibility(View.GONE);
            } else {
                holder.notesLabel.setText(item.getNotes());
            }
            if (item.getSubItemId() == 0) {
                holder.containerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground));
                holder.codeLabel.setText(String.valueOf("Codigo: " + item.getItem().getId()));
                holder.nameLabel.setText(item.getItem().getName());
                if (database.hasChildren(item.getOrder().getId(), item.getItem().getId())) {
                    holder.onlySubItemLayout.setVisibility(View.GONE);
                }
            } else {
                holder.codeLabel.setText("Subproducto");
                holder.nameLabel.setText(item.getSubItemName());
                holder.containerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            }
            //Setea el listener al boton de empaque
            holder.packButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Verifica si el item es padre o hijo
                    if (item.getSubItemId() != 0) {
                        //Cambia el estado del item a separado
                        item.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_SEPARED));
                        //Calcula las unidades de bodega
                        item.setStorageUnits(item.getUnits() + item.getFreeUnits());
                        OrderItem parent = database.getParent(item.getOrder().getId(), item.getItem().getId());
                        //Calcula las unidades de bodega para el padre
                        parent.setStorageUnits(getParentCount(parent, item.getStorageUnits(), item));
                        //Guarda los cambios
                        if (database.updateOrderItemState(item) && database.updateItemStorageUnits(item) && database.updateParentStorageUnits(parent)) {
                            //Actualiza el valor del producto en el pedido
                            parent.setTotal(parent.getUnits() * item.getUnitPrice());
                            database.updateItemStorageUnits(parent);
                            //Envia los datos al servidor
                            Utils.updateOrderItem(item, context);
                            //Recalcula el estado del producto padre
                            checkParentItemState(database.getParent(item.getOrder().getId(), item.getItem().getId()));
                            //Recarga la actividad
                            storageDetailActivity.reload();
                        }
                    } else {
                        //Si es producto padre, sin hijos
                        //Cambia el estado del producto
                        item.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_SEPARED));
                        //Asigna las unidades de bodega
                        item.setStorageUnits(item.getUnits() + item.getFreeUnits());
                        if (database.updateOrderItemState(item) && database.updateItemStorageUnits(item)) {
                            //Asigna el valor del producto recalculado
                            item.setTotal(item.getUnits() * item.getUnitPrice());
                            database.updateItemStorageUnits(item);
                            Utils.updateOrderItem(item, context);
                            storageDetailActivity.reload();
                        }
                    }
                }
            });

            //Setea el listener al boton de cancelar
            holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Crea el dialogo de confirmacion
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cancelar producto");
                    builder.setMessage("Este producto no se entregará al cliente. Se eliminará de la factura. ¿desea eliminarlo del pedido?");
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            item.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_CANCELED));
                            int units = item.getStorageUnits();
                            //Verifica si es padre o hijo
                            if (item.getSubItemId() != 0) {
                                //Lleva las unidades de bodega a 0
                                item.setStorageUnits(0);
                                OrderItem parent = database.getParent(item.getOrder().getId(), item.getItem().getId());
                                //Recalcula las unidades de bodega del padre
                                parent.setStorageUnits(parent.getStorageUnits() - units);
                                if (database.updateOrderItemState(item) && database.updateItemStorageUnits(item) && database.updateItemStorageUnits(parent)) {
                                    //Recalcula el total del producto
                                    parent.setTotal(parent.getTotal() - item.getTotal());
                                    database.updateItemStorageUnits(parent);
                                    Utils.updateOrderItem(item, context);
                                    //Recalcula el estado del padre
                                    checkParentItemState(parent);
                                    //Recarga la actividad
                                    storageDetailActivity.reload();
                                }
                            } else {
                                //Asigna al padre unidades de bodega 0
                                item.setStorageUnits(0);
                                //Obtiene todos sus subproductos
                                List<OrderItem> orderItems = database.getSubItemsFromItem(item.getOrder().getId(), item.getItem().getId());
                                //Verifica si tiene subproductos
                                if (orderItems.size() > 0) {
                                    //Le añade al padre para poder enviarlo
                                    orderItems.add(item);
                                    for (OrderItem orderItem : orderItems) {
                                        //A cada subitem le lleva unidades 0 y cambia su estado a cancelado
                                        orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_CANCELED));
                                        orderItem.setStorageUnits(0);
                                        database.updateItemStorageUnits(orderItem);
                                    }
                                    database.updateOrderItemsStates(orderItems);
                                    item.setTotal(0);
                                    database.updateItemStorageUnits(item);
                                    Utils.updateOrderItems(orderItems, item.getOrder().getId(), context);
                                } else {
                                    //Guarda el estado
                                    database.updateOrderItemState(item);
                                    //Lleva el total a 0
                                    item.setTotal(0);
                                    database.updateItemStorageUnits(item);
                                    //Envia los datos al servidor
                                    Utils.updateOrderItem(item, context);
                                }
                            }
                            //Recarga la actividad
                            storageDetailActivity.reload();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Cancela el dialogo
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            //Setea el listener al boton de pendiente producto
            holder.productPendingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(context, R.style.StyledDialog);
                    View dialogView = View.inflate(context, R.layout.dialog_storage_detail, null);
                    dialog.setContentView(dialogView);

                    //Crea el dialogo y asigna los controles de suma y resta de unidades y tambien el input de unidades de bodega
                    RobotoLightTextView orderUnitsLabel = (RobotoLightTextView) dialogView.findViewById(R.id.order_units);
                    orderUnitsLabel.setText(String.valueOf(item.getUnits() + item.getFreeUnits()));
                    final RobotoLightTextView pendingUnitsLabel = (RobotoLightTextView) dialogView.findViewById(R.id.pending_units);
                    pendingUnitsLabel.setText(String.valueOf((item.getUnits() + item.getFreeUnits()) - item.getStorageUnits()));
                    final TextInputEditText storageNotesInput = (TextInputEditText) dialogView.findViewById(R.id.notes_input);
                    storageNotesInput.setText(item.getStorageNotes());
                    final TextInputEditText storageUnitsInput = (TextInputEditText) dialogView.findViewById(R.id.units_input);
                    if (item.getStorageUnits() == 0) {
                        storageUnitsInput.setText(String.valueOf(1));
                    } else {
                        storageUnitsInput.setText(String.valueOf(item.getStorageUnits()));
                    }
                    storageUnitsInput.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (Utils.isInteger(s.toString())) {
                                int value = Integer.valueOf(s.toString());
                                if (value >= item.getUnits() + item.getFreeUnits() || value == 0) {
                                    storageUnitsInput.setText(String.valueOf(1));
                                    pendingUnitsLabel.setText(String.valueOf((item.getUnits() + item.getFreeUnits()) - (1)));
                                } else {
                                    pendingUnitsLabel.setText(String.valueOf((item.getUnits() + item.getFreeUnits()) - (Integer.valueOf(s.toString()))));
                                }
                            } else {
                                storageUnitsInput.setText(String.valueOf(1));
                            }

                            if (s.toString().equals("")) {
                                storageUnitsInput.setText(String.valueOf(1));
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                    //Botones de aumento y decremento
                    Button fivePlusButton = (Button) dialogView.findViewById(R.id.five_plus_button);
                    fivePlusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int value = 1;
                            if (!storageUnitsInput.getText().toString().equals("")) {
                                value = Integer.valueOf(storageUnitsInput.getText().toString());
                            }
                            if ((value + 5) > (item.getFreeUnits() + item.getUnits()) - 1) {
                                Toast.makeText(context, "La cantidad de bodega no puede excederla cantidad de pedido", Toast.LENGTH_SHORT).show();
                            } else {
                                storageUnitsInput.setText(String.valueOf(value + 5));
                                pendingUnitsLabel.setText(String.valueOf((item.getUnits() + item.getFreeUnits()) - (value + 5)));
                            }
                        }
                    });
                    Button twoPlusButton = (Button) dialogView.findViewById(R.id.two_plus_button);
                    twoPlusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int value = 1;
                            if (!storageUnitsInput.getText().toString().equals("")) {
                                value = Integer.valueOf(storageUnitsInput.getText().toString());
                            }
                            if ((value + 2) > (item.getFreeUnits() + item.getUnits()) - 1) {
                                Toast.makeText(context, "La cantidad de bodega no puede excederla cantidad de pedido", Toast.LENGTH_SHORT).show();
                            } else {
                                storageUnitsInput.setText(String.valueOf(value + 2));
                                pendingUnitsLabel.setText(String.valueOf((item.getUnits() + item.getFreeUnits()) - (value + 2)));
                            }
                        }
                    });
                    Button onePlusButton = (Button) dialogView.findViewById(R.id.one_plus_button);
                    onePlusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int value = 1;
                            if (!storageUnitsInput.getText().toString().equals("")) {
                                value = Integer.valueOf(storageUnitsInput.getText().toString());
                            }
                            if ((value + 1) > (item.getFreeUnits() + item.getUnits()) - 1) {
                                Toast.makeText(context, "La cantidad de bodega no puede excederla cantidad de pedido", Toast.LENGTH_SHORT).show();
                            } else {
                                storageUnitsInput.setText(String.valueOf(value + 1));
                                pendingUnitsLabel.setText(String.valueOf((item.getUnits() + item.getFreeUnits()) - (value + 1)));
                            }
                        }
                    });
                    Button fiveLessButton = (Button) dialog.findViewById(R.id.five_less_button);
                    fiveLessButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int value = 1;
                            if (!storageUnitsInput.getText().toString().equals("")) {
                                value = Integer.valueOf(storageUnitsInput.getText().toString());
                            }
                            if ((value - 5) < 1) {
                                Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                            } else {
                                storageUnitsInput.setText(String.valueOf(value - 5));
                                pendingUnitsLabel.setText(String.valueOf((item.getUnits() + item.getFreeUnits()) - (value - 5)));
                            }
                        }
                    });
                    Button twoLessButton = (Button) dialog.findViewById(R.id.two_less_button);
                    twoLessButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int value = 1;
                            if (!storageUnitsInput.getText().toString().equals("")) {
                                value = Integer.valueOf(storageUnitsInput.getText().toString());
                            }
                            if ((value - 2) < 1) {
                                Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                            } else {
                                storageUnitsInput.setText(String.valueOf(value - 2));
                                pendingUnitsLabel.setText(String.valueOf((item.getUnits() + item.getFreeUnits()) - (value - 2)));
                            }
                        }
                    });

                    Button oneLessButton = (Button) dialog.findViewById(R.id.one_less_button);
                    oneLessButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int value = 1;
                            if (!storageUnitsInput.getText().toString().equals("")) {
                                value = Integer.valueOf(storageUnitsInput.getText().toString());
                            }
                            if ((value - 1) < 1) {
                                Toast.makeText(context, "Valor invalido", Toast.LENGTH_SHORT).show();
                            } else {
                                storageUnitsInput.setText(String.valueOf(value - 1));
                                pendingUnitsLabel.setText(String.valueOf((item.getUnits() + item.getFreeUnits()) - (value - 1)));
                            }
                        }
                    });

                    //Cancela el dialogo
                    Button cancelButton = (Button) dialogView.findViewById(R.id.cancel_button);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    //Al aceptar se empieza el proceso de guardado y envio al servidor
                    Button acceptButton = (Button) dialogView.findViewById(R.id.accept_button);
                    acceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Verifica si el campo de unidades no es vacio
                            if (!storageUnitsInput.getText().toString().equals("")) {
                                int value = Integer.valueOf(storageUnitsInput.getText().toString());
                                if (value > 0) {
                                    //Verifica si es padre o hijo
                                    if (item.getSubItemId() != 0) {
                                        //Asigna las unidades de bodega que hay en el input de unidades
                                        item.setStorageUnits(value);
                                        //Asighna las notas de bodega
                                        item.setStorageNotes(storageNotesInput.getText().toString());
                                        //Cambia el estado
                                        item.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_PRODUCT_PENDING));
                                        OrderItem parent = database.getParent(item.getOrder().getId(), item.getItem().getId());
                                        //Recalcula las unidades de bodega para el padre
                                        parent.setStorageUnits(getParentCount(parent, value, item));
                                        dialog.dismiss();
                                        //Guarda los datos
                                        if (database.updateProductPendingItem(item) && database.updateParentStorageUnits(parent)) {
                                            //Calcula el total con el valor equivalente
                                            parent.setTotal(parent.getStorageUnits() * parent.getEqValue());
                                            database.updateItemStorageUnits(parent);
                                            //Envia todo al servidor
                                            Utils.updateOrderItem(item, context);
                                            //Cambia el estado del padre
                                            checkParentItemState(database.getParent(item.getOrder().getId(), item.getItem().getId()));
                                            //Recarga la actividad
                                            storageDetailActivity.reload();
                                        }
                                    } else {
                                        item.setStorageUnits(value);
                                        item.setStorageNotes(storageNotesInput.getText().toString());
                                        item.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_PRODUCT_PENDING));
                                        if (database.updateProductPendingItem(item)) {
                                            item.setTotal(item.getStorageUnits() * item.getEqValue());
                                            database.updateItemStorageUnits(item);
                                            Utils.updateOrderItem(item, context);
                                            storageDetailActivity.reload();
                                        }
                                        dialog.dismiss();
                                    }
                                } else {
                                    Toast.makeText(context, "Ingrese un valor mayor a 0", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "Ingrese un valor valido", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.show();
                }
            });
            //Setea el listener al boton pendiente mercaderista
            holder.promoterPendingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Verifica si el producto es padre o hijo
                    if (item.getSubItemId() == 0) {
                        //Cambia el estado
                        item.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_PROMOTER_PENDING));
                        //Asigna unidades de bodega = 0
                        item.setStorageUnits(0);
                        //Obtiene todos los hijos
                        List<OrderItem> orderItems = database.getSubItemsFromItem(item.getOrder().getId(), item.getItem().getId());
                        if (orderItems.size() > 0) {
                            orderItems.add(item);
                            for (OrderItem orderItem : orderItems) {
                                //A cada item le cambia el estado y le pone las unidades de bodega iguales a 0
                                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_PROMOTER_PENDING));
                                orderItem.setStorageUnits(0);
                                database.updateItemStorageUnits(orderItem);
                            }
                            database.updateOrderItemsStates(orderItems);
                            item.setTotal(0);
                            database.updateItemStorageUnits(item);
                            //Envvia todos los datos
                            Utils.updateOrderItems(orderItems, item.getOrder().getId(), context);
                        } else {
                            //Asigna unidades de bodega a 0 para el item sin hijos
                            database.updateOrderItemState(item);
                            item.setTotal(0);
                            database.updateItemStorageUnits(item);
                            //Envia todos los datos
                            Utils.updateOrderItem(item, context);
                        }
                    } else {
                        //Cambia el estado
                        item.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_PROMOTER_PENDING));
                        int units = item.getStorageUnits();
                        OrderItem parent = database.getParent(item.getOrder().getId(), item.getItem().getId());
                        //recalcula las unidades de bodega del padre
                        parent.setStorageUnits(parent.getStorageUnits() - units);
                        //Pone las unidades del subproducto en 0
                        item.setStorageUnits(0);
                        database.updateItemStorageUnits(item);
                        if (database.updateOrderItemState(item) && database.updateItemStorageUnits(item) && database.updateParentStorageUnits(parent)) {
                            //Pone el total del subproducto en
                            item.setTotal(0);
                            database.updateItemStorageUnits(item);
                            //Aqui no se recalcula el total para el padre, ya que pendiente mercaderista puede ser modificado por la mercaderista y ponerse en cualquiera de los estados previos
                            //Envia todo
                            Utils.updateOrderItem(item, context);
                            //Recalcula el estado del padre
                            checkParentItemState(database.getParent(item.getOrder().getId(), item.getItem().getId()));
                        }
                    }
                    //Recarga la actividad
                    storageDetailActivity.reload();
                }
            });
        }
        return convertView;
    }

    //Metodo para recalcular el conteo total del item padre
    private int getParentCount(OrderItem parent, int value, OrderItem item) {
        List<OrderItem> orderItems = database.getSubItemsFromItem(parent.getOrder().getId(), parent.getItem().getId());
        int currentValue = 0;
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getSubItemId() != item.getSubItemId()) {
                currentValue = currentValue + orderItem.getStorageUnits();
            }
        }
        return currentValue + value;
    }

    //Metodo que obtiene el estado actual de un producto padre basado en sus subproductos
    private void checkParentItemState(OrderItem orderItem) {
        List<OrderItem> subItems = database.getSubItemsFromItem(orderItem.getOrder().getId(), orderItem.getItem().getId());
        if (orderItem.getSubItemId() == 0 && subItems.size() > 0) {

            boolean hasSepared = false;
            boolean hasProductPending = false;
            boolean hasPromoterPending = false;
            boolean hasCanceled = false;
            boolean hasUnsepared = false;
            boolean hasProductAndPromoterPending = false;

            for (OrderItem subItem : subItems) {
                if (subItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_UNSEPARED) {
                    hasUnsepared = true;
                }
                if (subItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_SEPARED) {
                    hasSepared = true;
                }
                if (subItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_PROMOTER_PENDING) {
                    hasPromoterPending = true;
                }
                if (subItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_PRODUCT_PENDING) {
                    hasProductPending = true;
                }
                if (subItem.getOrderItemsState().getId() == ItemStates.ITEM_STATE_CANCELED) {
                    hasCanceled = true;
                }
            }

            if (hasUnsepared && !hasProductPending && !hasPromoterPending && !hasCanceled && hasSepared) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
                return;
            }

            if (hasUnsepared && !hasProductPending && !hasPromoterPending && !hasCanceled && !hasSepared) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_UNSEPARED));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
                return;
            }

            if (hasSepared && !hasProductPending && !hasPromoterPending && !hasCanceled && !hasProductAndPromoterPending) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_SEPARED));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
                return;
            }

            if (hasSepared && hasProductPending && !hasPromoterPending) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_PRODUCT_PENDING));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
                return;
            }

            if (hasPromoterPending && hasProductPending) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_PRODUCT_PROMOTER_PENDING));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
                return;
            }

            if (hasPromoterPending && !hasProductPending) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_PROMOTER_PENDING));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
                return;
            }

            if (hasCanceled && !hasProductPending && !hasPromoterPending && !hasSepared) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_CANCELED));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
                return;
            }

            if (hasProductPending && !hasCanceled && !hasPromoterPending && !hasSepared) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_PRODUCT_PENDING));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
                return;
            }

            if (hasPromoterPending && !hasProductPending && !hasCanceled && !hasSepared) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_PROMOTER_PENDING));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
                return;
            }

            if (hasSepared && hasCanceled && !hasProductPending && !hasPromoterPending) {
                orderItem.setOrderItemsState(database.getOrderItemState(ItemStates.ITEM_STATE_SEPARED));
                database.updateOrderItemState(orderItem);
                Utils.updateOrderItem(orderItem, context);
            }

        }
    }

    //Mantiene la referecncia de los controles de la vista
    private class ViewHolder {
        RobotoRegularTextView codeLabel, nameLabel;
        RobotoLightTextView notesLabel, orderUnitsLabel;
        ImageButton packButton, cancelButton, promoterPendingButton, productPendingButton;
        LinearLayout notesLayout, onlySubItemLayout, containerLayout, markableLayout;
        CircleView stateView;
    }
}
