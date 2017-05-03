package co.com.imagenybelleza.imagenybelleza.models;

/**
 * Created by Juan Camilo Villa Amaya on 2/12/2016.
 * <p>
 * <p>
 * POJO para almacenar los atributos del estado de un pedido
 */

public class OrderState {

    private int id;
    private String state;
    private String hexColor;
    private String modified;

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
