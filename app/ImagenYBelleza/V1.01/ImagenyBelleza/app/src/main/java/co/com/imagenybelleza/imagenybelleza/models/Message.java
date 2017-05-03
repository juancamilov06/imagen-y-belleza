package co.com.imagenybelleza.imagenybelleza.models;

/**
 * Created by Manuela Duque M on 22/03/2017.
 */

public class Message {

    private int id;
    private String message;
    private boolean isRead;
    private String date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
