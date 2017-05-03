package co.com.imagenybelleza.imagenybelleza.models;

/**
 * Created by Juan Camilo Villa Amaya on 2/12/2016.
 * <p>
 * <p>
 * POJO para almacenar los atributos de un producto
 */

public class Item {

    private int id;
    private String name;
    private boolean active;
    private double price;
    private boolean newItem;
    private int subItemId;
    private double discountOne;
    private double discountTwo;
    private double discountThree;
    private double discountFour;
    private double discountFive;
    private double paymentOne;
    private double paymentTwo;
    private double paymentThree;
    private double paymentFour;
    private double iva;

    private Brand brand;
    private Category category;

    private String modified;

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isNewItem() {
        return newItem;
    }

    public void setNewItem(boolean newItem) {
        this.newItem = newItem;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubItemId() {
        return subItemId;
    }

    public void setSubItemId(int subItemId) {
        this.subItemId = subItemId;
    }

    public double getDiscountOne() {
        return discountOne;
    }

    public void setDiscountOne(double discountOne) {
        this.discountOne = discountOne;
    }

    public double getDiscountTwo() {
        return discountTwo;
    }

    public void setDiscountTwo(double discountTwo) {
        this.discountTwo = discountTwo;
    }

    public double getDiscountThree() {
        return discountThree;
    }

    public void setDiscountThree(double discountThree) {
        this.discountThree = discountThree;
    }

    public double getDiscountFour() {
        return discountFour;
    }

    public void setDiscountFour(double discountFour) {
        this.discountFour = discountFour;
    }

    public double getDiscountFive() {
        return discountFive;
    }

    public void setDiscountFive(double discountFive) {
        this.discountFive = discountFive;
    }

    public double getPaymentOne() {
        return paymentOne;
    }

    public void setPaymentOne(double paymentOne) {
        this.paymentOne = paymentOne;
    }

    public double getPaymentTwo() {
        return paymentTwo;
    }

    public void setPaymentTwo(double paymentTwo) {
        this.paymentTwo = paymentTwo;
    }

    public double getPaymentThree() {
        return paymentThree;
    }

    public void setPaymentThree(double paymentThree) {
        this.paymentThree = paymentThree;
    }

    public double getPaymentFour() {
        return paymentFour;
    }

    public void setPaymentFour(double paymentFour) {
        this.paymentFour = paymentFour;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }
}
