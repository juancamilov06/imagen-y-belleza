package co.com.imagenybelleza.imagenybelleza.models;

/**
 * Created by Manuela Duque M on 14/03/2017.
 */

public class Count {

    private String table;
    private long local;
    private long cdb;

    public long getCdb() {
        return cdb;
    }

    public void setCdb(long cdb) {
        this.cdb = cdb;
    }

    public long getLocal() {
        return local;
    }

    public void setLocal(long local) {
        this.local = local;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
