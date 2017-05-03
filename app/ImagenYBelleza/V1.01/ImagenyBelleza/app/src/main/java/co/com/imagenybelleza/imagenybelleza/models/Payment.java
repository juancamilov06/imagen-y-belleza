package co.com.imagenybelleza.imagenybelleza.models;

/**
 * Created by Juan Camilo Villa Amaya on 19/12/2016.
 * <p>
 * <p>
 * POJO para almacenar los atributos de un tipo de pago
 */

public class Payment {

    private int id;
    private String name;
    private int term;
    private String modified;

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
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
