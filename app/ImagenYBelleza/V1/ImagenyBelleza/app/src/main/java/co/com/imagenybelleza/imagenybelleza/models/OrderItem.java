package co.com.imagenybelleza.imagenybelleza.models;

/**
 * Created by Juan Camilo Villa Amaya on 2/12/2016.
 * <p>
 * <p>
 * POJO para almacenar los atributos de un producto incluido en un pedido
 */

public class OrderItem {

    private double unitPrice;
    private int units;
    private int freeUnits;
    private String notes;
    private double iva;
    private double discount;
    private double total;
    private double value;
    private String subItemName;
    private int storageUnits;
    private int storageFreeUnits;
    private double eqValue;
    private String storageNotes;

    private Order order;
    private User packer;
    private OrderItemsState orderItemsState;
    private Item item;
    private int subItemId;

    private String modified;

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public int getFreeUnits() {
        return freeUnits;
    }

    public void setFreeUnits(int freeUnits) {
        this.freeUnits = freeUnits;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public OrderItemsState getOrderItemsState() {
        return orderItemsState;
    }

    public void setOrderItemsState(OrderItemsState orderItemsState) {
        this.orderItemsState = orderItemsState;
    }

    public User getPacker() {
        return packer;
    }

    public void setPacker(User packer) {
        this.packer = packer;
    }

    public int getSubItemId() {
        return subItemId;
    }

    public void setSubItemId(int subItemId) {
        this.subItemId = subItemId;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getSubItemName() {
        return subItemName;
    }

    public void setSubItemName(String subItemName) {
        this.subItemName = subItemName;
    }

    public int getStorageUnits() {
        return storageUnits;
    }

    public void setStorageUnits(int storageUnits) {
        this.storageUnits = storageUnits;
    }

    public String getStorageNotes() {
        return storageNotes;
    }

    public void setStorageNotes(String storageNotes) {
        this.storageNotes = storageNotes;
    }

    public int getStorageFreeUnits() {
        return storageFreeUnits;
    }

    public void setStorageFreeUnits(int storageFreeUnits) {
        this.storageFreeUnits = storageFreeUnits;
    }

    public double getEqValue() {
        return eqValue;
    }

    public void setEqValue(double eqValue) {
        this.eqValue = eqValue;
    }
}
