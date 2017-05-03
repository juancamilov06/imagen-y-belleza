package co.com.imagenybelleza.imagenybelleza.models;

/**
 * Created by Juan Camilo Villa Amaya on 19/12/2016.
 * <p>
 * POJO para almacenar los atributos de los tipos de cliente
 */

public class ClientType {

    private int id;
    private String name;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
