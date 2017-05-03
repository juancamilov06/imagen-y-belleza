package co.com.imagenybelleza.imagenybelleza.models;

/**
 * Created by Juan Camilo Villa on 10/01/2017.
 * <p>
 * <p>
 * POJO para almacenar los atributos de un filtro en el modulo de busqueda de productos
 */

public class Filter {

    private int id;
    private Brand brand;
    private Category category;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
