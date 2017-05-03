package co.com.imagenybelleza.imagenybelleza.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.text.TextUtils;

import com.google.common.hash.Hashing;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import co.com.imagenybelleza.imagenybelleza.enums.ClientTypes;
import co.com.imagenybelleza.imagenybelleza.enums.Database;
import co.com.imagenybelleza.imagenybelleza.enums.ItemStates;
import co.com.imagenybelleza.imagenybelleza.enums.States;
import co.com.imagenybelleza.imagenybelleza.models.Brand;
import co.com.imagenybelleza.imagenybelleza.models.Category;
import co.com.imagenybelleza.imagenybelleza.models.City;
import co.com.imagenybelleza.imagenybelleza.models.Client;
import co.com.imagenybelleza.imagenybelleza.models.ClientType;
import co.com.imagenybelleza.imagenybelleza.models.Filter;
import co.com.imagenybelleza.imagenybelleza.models.Item;
import co.com.imagenybelleza.imagenybelleza.models.Order;
import co.com.imagenybelleza.imagenybelleza.models.OrderItem;
import co.com.imagenybelleza.imagenybelleza.models.OrderItemsState;
import co.com.imagenybelleza.imagenybelleza.models.OrderState;
import co.com.imagenybelleza.imagenybelleza.models.Payment;
import co.com.imagenybelleza.imagenybelleza.models.User;
import co.com.imagenybelleza.imagenybelleza.models.UserLocation;

/**
 * Created by danim_000 on 3/12/2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Database.CREATE_BRAND);
        db.execSQL(Database.CREATE_CATEGORY);
        db.execSQL(Database.CREATE_CLIENT);
        db.execSQL(Database.CREATE_ITEM);
        db.execSQL(Database.CREATE_LOCATION);
        db.execSQL(Database.CREATE_ORDER);
        db.execSQL(Database.CREATE_ORDER_ITEMS);
        db.execSQL(Database.CREATE_ORDER_STATE);
        db.execSQL(Database.CREATE_ORDER_ITEMS_STATE);
        db.execSQL(Database.CREATE_USER);
        db.execSQL(Database.CREATE_SESSION);
        db.execSQL(Database.CREATE_CLIENT_TYPE);
        db.execSQL(Database.CREATE_IP);
        db.execSQL(Database.CREATE_DIRECTORY);
        db.execSQL(Database.CREATE_PAYMENT);
        db.execSQL(Database.CREATE_VERSION);
        db.execSQL(Database.CREATE_CITY);
        db.execSQL(Database.CREATE_FILTERS);
        db.execSQL(Database.CREATE_TEMP_LOCATION);
        db.execSQL(Database.CREATE_TEMP_ORDER_ITEMS);
        db.execSQL(Database.CREATE_TEMP_ORDER_STATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void clearAll() {

        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + Database.TABLE_ITEM);
        database.execSQL("DELETE FROM " + Database.TABLE_SESSION);
        database.execSQL("DELETE FROM " + Database.TABLE_CATEGORY);
        database.execSQL("DELETE FROM " + Database.TABLE_BRAND);
        database.execSQL("DELETE FROM " + Database.TABLE_CLIENT);
        database.execSQL("DELETE FROM " + Database.TABLE_LOCATION);
        database.execSQL("DELETE FROM " + Database.TABLE_ORDER_ITEMS_STATE);
        database.execSQL("DELETE FROM " + Database.TABLE_USER);
        database.execSQL("DELETE FROM " + Database.TABLE_ORDER);
        database.execSQL("DELETE FROM " + Database.TABLE_ORDER_ITEMS);
        database.execSQL("DELETE FROM " + Database.TABLE_ORDER_STATE);
        database.execSQL("DELETE FROM " + Database.TABLE_CLIENT_TYPE);
        database.execSQL("DELETE FROM " + Database.TABLE_PAYMENT);
        database.execSQL("DELETE FROM " + Database.TABLE_VERSION);
        database.execSQL("DELETE FROM " + Database.TABLE_CITY);
        database.execSQL("DELETE FROM " + Database.TABLE_TEMP_LOCATION);
        database.execSQL("DELETE FROM " + Database.TABLE_TEMP_ORDER_ITEMS);
        database.execSQL("DELETE FROM " + Database.TABLE_TEMP_ORDER_STATE);
    }

    public long count(String table) {
        SQLiteDatabase database = this.getWritableDatabase();
        SQLiteStatement statement = database.compileStatement("SELECT count(*) FROM " + table);
        return statement.simpleQueryForLong();
    }

    public void deleteTempOrderStates() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + Database.TABLE_TEMP_ORDER_STATE);
    }

    public Filter getCurrentFilter() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_FILTERS + " WHERE id = 1", null);
        Filter filter = null;
        if (cursor.moveToFirst()) {
            filter = new Filter();
            filter.setId(1);
            if (cursor.getInt(1) == 800) {
                Brand all = new Brand();
                all.setId(800);
                all.setName("Todas");
                filter.setBrand(all);
            } else if (cursor.getInt(1) == 801) {
                Brand newBrand = new Brand();
                newBrand.setId(801);
                newBrand.setName("Nuevos");
                filter.setBrand(newBrand);
            } else {
                filter.setBrand(getBrand(cursor.getInt(1)));
            }
            if (cursor.getInt(2) == 900) {
                Category allCategories = new Category();
                allCategories.setId(900);
                allCategories.setName("Todas");
                filter.setCategory(allCategories);
            } else {
                filter.setCategory(getCategory(cursor.getInt(2)));
            }
            filter.setName(cursor.getString(3));
        }
        cursor.close();
        return filter;
    }

    public void deleteCurrentFilter() {
        SQLiteDatabase database = this.getWritableDatabase();
        Filter filter = getCurrentFilter();
        if (filter != null) {
            database.execSQL("DELETE FROM " + Database.TABLE_FILTERS);
        }
    }


    public boolean insertFilter(Filter filter) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ID, filter.getId());
            values.put(Database.KEY_CATEGORY_ID, filter.getCategory().getId());
            values.put(Database.KEY_BRAND_ID, filter.getBrand().getId());
            if (TextUtils.isEmpty(filter.getName())) {
                values.put(Database.KEY_NAME, "");
            } else {
                values.put(Database.KEY_NAME, filter.getName());
            }
            database.replace(Database.TABLE_FILTERS, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertCities(List<City> cities) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (City city : cities) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, city.getId());
                values.put(Database.KEY_CITY, city.getCity());
                database.replace(Database.TABLE_CITY, null, values);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<City> getCities() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CITY, null);
        List<City> cities = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(0));
                city.setCity(cursor.getString(1));
                cities.add(city);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cities;
    }

    public City getCity(int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CITY + " WHERE " + Database.KEY_ID + " = " + id, null);
        City city = null;
        if (cursor.moveToFirst()) {
            city = new City();
            city.setId(cursor.getInt(0));
            city.setCity(cursor.getString(1));
        }

        cursor.close();
        return city;
    }

    public boolean insertVersion(String date) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ID, 1);
            values.put(Database.KEY_DATE, date);
            database.replace(Database.TABLE_VERSION, null, values);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean insertDirectory(String path) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("id", 1);
            values.put("path", path);
            database.replace(Database.TABLE_DIRECTORY, null, values);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getLastModifiedDate() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_VERSION, null);
        String date = null;
        if (cursor.moveToFirst()) {
            date = cursor.getString(1);
        }
        cursor.close();
        return date;
    }

    public boolean hasParentInOrder(int orderId, int itemId) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS + " WHERE "
                + Database.KEY_ORDER_ID + " = " + orderId + " AND " + Database.KEY_ITEM_ID + " = " + itemId
                + " AND " + Database.KEY_SUBITEM_ID + " = 0", null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }

        cursor.close();
        return false;
    }

    public OrderItem getParent(int orderId, int itemId) {
        SQLiteDatabase database = this.getWritableDatabase();
        OrderItem orderItem = null;
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS + " WHERE "
                + Database.KEY_ORDER_ID + " = " + orderId + " AND " + Database.KEY_ITEM_ID + " = " + itemId
                + " AND " + Database.KEY_SUBITEM_ID + " = 0", null);
        if (cursor.moveToFirst()) {
            orderItem = new OrderItem();
            orderItem.setOrder(getOrder(cursor.getInt(0)));
            orderItem.setItem(getItem(cursor.getInt(1)));
            orderItem.setSubItemId(cursor.getInt(2));
            orderItem.setUnitPrice(cursor.getDouble(3));
            orderItem.setUnits(cursor.getInt(4));
            orderItem.setFreeUnits(cursor.getInt(5));
            orderItem.setNotes(cursor.getString(6));
            if (cursor.getInt(7) == 0) {
                orderItem.setPacker(null);
            } else {
                orderItem.setPacker(getUser(cursor.getInt(7)));
            }
            orderItem.setOrderItemsState(getOrderItemState(cursor.getInt(8)));
            orderItem.setIva(cursor.getDouble(9));
            orderItem.setDiscount(cursor.getDouble(10));
            orderItem.setTotal(cursor.getDouble(11));
            orderItem.setValue(cursor.getDouble(12));
            orderItem.setStorageUnits(cursor.getInt(14));
            orderItem.setStorageNotes(cursor.getString(15));
            orderItem.setEqValue(cursor.getDouble(16));
        }
        cursor.close();

        return orderItem;
    }

    public String getDirectory() {

        SQLiteDatabase database = this.getWritableDatabase();
        String path = null;
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_DIRECTORY, null);
        if (cursor.moveToFirst()) {
            path = cursor.getString(1);
        }

        cursor.close();
        return path;
    }

    public String getIpAdress() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_IP, null);
        String ip = null;
        if (cursor.moveToFirst()) {
            ip = cursor.getString(1);
        }

        cursor.close();
        return ip;
    }

    public boolean insertIp(String ip) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ID, 1);
            values.put(Database.KEY_IP_ADDRESS, ip);
            database.replace(Database.TABLE_IP, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getTempOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_TEMP_ORDER_STATE, null);
        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(0));
                order.setMade(cursor.getString(1));
                order.setDeliver(cursor.getString(2));
                order.setModifiedDate(cursor.getString(3));
                order.setSent(Boolean.valueOf(cursor.getString(4)));
                order.setNotes(cursor.getString(5));
                order.setPayment(getPayment(cursor.getInt(6)));
                order.setClient(getClient(cursor.getInt(7)));
                order.setSeller(getCurrentUser());
                order.setState(getOrderState(cursor.getInt(9)));
                order.setInProgress(Boolean.valueOf(cursor.getString(11)));
                orders.add(order);
            } while (cursor.moveToNext());
        }
        return orders;
    }

    public List<Order> getSentOrders() {
        List<Order> allOrders = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER, null);

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(0));
                order.setMade(cursor.getString(1));
                order.setDeliver(cursor.getString(2));
                order.setModifiedDate(cursor.getString(3));
                order.setSent(Boolean.valueOf(cursor.getString(4)));
                order.setNotes(cursor.getString(5));
                order.setPayment(getPayment(cursor.getInt(6)));
                order.setClient(getClient(cursor.getInt(7)));
                order.setSeller(getCurrentUser());
                order.setState(getOrderState(cursor.getInt(9)));
                order.setInProgress(Boolean.valueOf(cursor.getString(11)));
                allOrders.add(order);
            } while (cursor.moveToNext());
        }

        List<Order> orders = new ArrayList<>();
        for (Order order : allOrders) {
            if (order.getState().getId() == States.ORDER_STATE_APPROVED || order.getState().getId() == States.ORDER_STATE_SENT
                    || order.getState().getId() == States.ORDER_STATE_DISAPPROVED || order.getState().getId() == States.ORDER_STATE_SEPARATION_FINISHED
                    || order.getState().getId() == States.ORDER_STATE_FINISHED) {
                orders.add(order);
            }
        }

        /*List<Order> ordersByDate = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat currentFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String todayDate = currentFormatter.format(new Date());

        for (Order order : orders){
            DateTime currentDate = format.parseDateTime(todayDate);
            DateTime orderDate = format.parseDateTime(order.getModifiedDate());
            System.out.println("Fechas: " + currentDate.toString() + " - " + orderDate.toString());
            System.out.println("Dias: " + String.valueOf(Days.daysBetween(orderDate, currentDate).getDays()));
            if (Days.daysBetween(currentDate, orderDate).getDays() <= 15){
                ordersByDate.add(order);
            }
        }
*/

        cursor.close();

        return orders;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER
                + " ORDER BY " + Database.KEY_ORDER_STATE_ID + ", datetime(modified) ASC", null);
        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(0));
                order.setMade(cursor.getString(1));
                order.setDeliver(cursor.getString(2));
                order.setModifiedDate(cursor.getString(3));
                order.setSent(Boolean.valueOf(cursor.getString(4)));
                order.setNotes(cursor.getString(5));
                order.setPayment(getPayment(cursor.getInt(6)));
                order.setClient(getClient(cursor.getInt(7)));
                order.setSeller(getCurrentUser());
                order.setState(getOrderState(cursor.getInt(9)));
                order.setInProgress(Boolean.valueOf(cursor.getString(11)));
                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return orders;

    }

    public List<Order> getStorageOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER + " WHERE (" + Database.KEY_ORDER_STATE_ID + " = "
                + States.ORDER_STATE_APPROVED + " OR " + Database.KEY_ORDER_STATE_ID + " = " + States.ORDER_STATE_SEPARATION_PROCESS
                + " OR " + Database.KEY_ORDER_STATE_ID + " = " + States.ORDER_STATE_PRODUCT_PENDING + " OR " + Database.KEY_ORDER_STATE_ID + " = " + States.ORDER_STATE_PROMOTER_PENDING
                + " OR " + Database.KEY_ORDER_STATE_ID + " = " + States.ORDER_STATE_PRODUCT_AND_PROMOTER_PENDING + " OR " + Database.KEY_ORDER_STATE_ID + " = " + States.ORDER_STATE_SEPARATION_FINISHED
                + " OR (" + Database.KEY_ORDER_STATE_ID + " = " + States.ORDER_STATE_CANCELLED + " AND " + Database.KEY_IS_SENT + " = 1)) ORDER BY " + Database.KEY_ORDER_STATE_ID + ", datetime(modified) ASC", null);

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(0));
                order.setMade(cursor.getString(1));
                order.setDeliver(cursor.getString(2));
                order.setModifiedDate(cursor.getString(3));
                order.setSent(Boolean.valueOf(cursor.getString(4)));
                order.setNotes(cursor.getString(5));
                order.setPayment(getPayment(cursor.getInt(6)));
                order.setClient(getClient(cursor.getInt(7)));
                order.setSeller(getCurrentUser());
                order.setState(getOrderState(cursor.getInt(9)));
                order.setInProgress(Boolean.valueOf(cursor.getString(11)));
                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return orders;

    }

    public List<Order> getMyOrders() throws ParseException {

        List<Order> orders = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER + " WHERE " + Database.KEY_SELLER_ID
                + " = " + getCurrentUser().getId() + " ORDER BY " + Database.KEY_ORDER_STATE_ID + ", datetime(modified) ASC", null);
        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(0));
                order.setMade(cursor.getString(1));
                order.setDeliver(cursor.getString(2));
                order.setModifiedDate(cursor.getString(3));
                order.setSent(Boolean.valueOf(cursor.getString(4)));
                order.setNotes(cursor.getString(5));
                order.setPayment(getPayment(cursor.getInt(6)));
                order.setClient(getClient(cursor.getInt(7)));
                order.setSeller(getCurrentUser());
                order.setState(getOrderState(cursor.getInt(9)));
                order.setInProgress(Boolean.valueOf(cursor.getString(10)));
                orders.add(order);
            } while (cursor.moveToNext());
        }

        List<Order> ordersByDate = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat currentFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String todayDate = currentFormatter.format(new Date());

        for (Order order : orders) {
            DateTime currentDate = format.parseDateTime(todayDate);
            DateTime orderDate = format.parseDateTime(order.getModifiedDate());
            System.out.println("Fechas: " + currentDate.toString() + " - " + orderDate.toString());
            System.out.println("Dias: " + String.valueOf(Days.daysBetween(orderDate, currentDate).getDays()));
            if (Days.daysBetween(currentDate, orderDate).getDays() <= 15) {
                ordersByDate.add(order);
            }
        }

        cursor.close();

        return ordersByDate;

    }

    public List<Client> getMyClients() {
        List<Client> clients = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CLIENT + " WHERE is_active = 1 AND " + Database.KEY_USER_ID + " = "
                + getCurrentUser().getId(), null);

        if (cursor.moveToFirst()) {
            do {
                Client client = new Client();
                client.setId(cursor.getInt(0));
                client.setCode(cursor.getInt(1));
                client.setCompany(cursor.getString(2));
                client.setAddress(cursor.getString(3));
                client.setCity(getCity(cursor.getInt(4)));
                client.setPhoneOne(cursor.getString(5));
                client.setPhoneTwo(cursor.getString(6));
                client.setPhoneThree(cursor.getString(7));
                client.setNit(cursor.getString(8));
                client.setMail(cursor.getString(9));
                client.setContact(cursor.getString(10));
                client.setSent(cursor.getInt(11) > 0);
                client.setClientType(getClientType(cursor.getInt(12)));
                client.setNeighborhood(cursor.getString(13));
                client.setUser(getUser(cursor.getInt(14)));
                clients.add(client);
            } while (cursor.moveToNext());
        }

        cursor.close();


        return clients;
    }

    public boolean updatePendingClients(List<Client> clients) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (Client client : clients) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_IS_SENT, true);
                database.update(Database.TABLE_CLIENT, values, Database.KEY_ID + " = " + client.getId(), null);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public List<Client> getPendingClients() {
        List<Client> clients = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CLIENT + " WHERE " + Database.KEY_IS_SENT + " = 0 ", null);

        if (cursor.moveToFirst()) {
            do {
                Client client = new Client();
                client.setId(cursor.getInt(0));
                client.setCode(cursor.getInt(1));
                client.setCompany(cursor.getString(2));
                client.setAddress(cursor.getString(3));
                client.setCity(getCity(cursor.getInt(4)));
                client.setPhoneOne(cursor.getString(5));
                client.setPhoneTwo(cursor.getString(6));
                client.setPhoneThree(cursor.getString(7));
                client.setNit(cursor.getString(8));
                client.setMail(cursor.getString(9));
                client.setContact(cursor.getString(10));
                client.setSent(cursor.getInt(11) > 0);
                client.setClientType(getClientType(cursor.getInt(12)));
                client.setNeighborhood(cursor.getString(13));
                client.setUser(getUser(cursor.getInt(14)));
                clients.add(client);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return clients;
    }

    public List<Client> getClients() {
        List<Client> clients = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CLIENT + " WHERE is_active = 1 ", null);

        if (cursor.moveToFirst()) {
            do {
                Client client = new Client();
                client.setId(cursor.getInt(0));
                client.setCode(cursor.getInt(1));
                client.setCompany(cursor.getString(2));
                client.setAddress(cursor.getString(3));
                client.setCity(getCity(cursor.getInt(4)));
                client.setPhoneOne(cursor.getString(5));
                client.setPhoneTwo(cursor.getString(6));
                client.setPhoneThree(cursor.getString(7));
                client.setNit(cursor.getString(8));
                client.setMail(cursor.getString(9));
                client.setContact(cursor.getString(10));
                client.setSent(cursor.getInt(11) > 0);
                client.setClientType(getClientType(cursor.getInt(12)));
                client.setNeighborhood(cursor.getString(13));
                client.setUser(getUser(cursor.getInt(14)));
                clients.add(client);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return clients;
    }

    public List<ClientType> getClientTypes() {
        List<ClientType> clientTypes = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CLIENT_TYPE, null);

        if (cursor.moveToFirst()) {
            do {
                ClientType clientType = new ClientType();
                clientType.setId(cursor.getInt(0));
                clientType.setName(cursor.getString(1));
                clientTypes.add(clientType);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return clientTypes;
    }

    public ClientType getClientType(int id) {
        ClientType clientType = null;
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CLIENT_TYPE + " WHERE " +
                Database.KEY_ID + " = " + id, null);

        if (cursor.moveToFirst()) {
            clientType = new ClientType();
            clientType.setId(id);
            clientType.setName(cursor.getString(1));
        }

        cursor.close();


        return clientType;
    }

    public Client getClient(int id) {

        Client client = null;
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CLIENT + " WHERE " +
                Database.KEY_ID + " = " + id, null);

        if (cursor.moveToFirst()) {
            client = new Client();
            client.setId(cursor.getInt(0));
            client.setCode(cursor.getInt(1));
            client.setCompany(cursor.getString(2));
            client.setAddress(cursor.getString(3));
            client.setCity(getCity(cursor.getInt(4)));
            client.setPhoneOne(cursor.getString(5));
            client.setPhoneTwo(cursor.getString(6));
            client.setPhoneThree(cursor.getString(7));
            client.setNit(cursor.getString(8));
            client.setMail(cursor.getString(9));
            client.setContact(cursor.getString(10));
            client.setSent(cursor.getInt(11) > 0);
            client.setClientType(getClientType(cursor.getInt(12)));
            client.setNeighborhood(cursor.getString(13));
            client.setUser(getUser(cursor.getInt(14)));
        }

        cursor.close();
        return client;

    }

    public OrderItem getOrderItemInfo(int orderId, int itemId, int subItemId) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS + " WHERE " + Database.KEY_ORDER_ID + " = " + orderId
                + " AND " + Database.KEY_ITEM_ID + " = " + itemId + " AND " + Database.KEY_SUBITEM_ID + " = " + subItemId, null);
        OrderItem orderItem = null;
        if (cursor.moveToFirst()) {
            orderItem = new OrderItem();
            orderItem.setOrder(getOrder(cursor.getInt(0)));
            orderItem.setItem(getItem(cursor.getInt(1)));
            orderItem.setSubItemId(cursor.getInt(2));
            orderItem.setUnitPrice(cursor.getDouble(3));
            orderItem.setUnits(cursor.getInt(4));
            orderItem.setFreeUnits(cursor.getInt(5));
            orderItem.setNotes(cursor.getString(6));
            if (cursor.getInt(7) == 0) {
                orderItem.setPacker(null);
            } else {
                orderItem.setPacker(getUser(cursor.getInt(7)));
            }
            orderItem.setOrderItemsState(getOrderItemState(cursor.getInt(8)));
            orderItem.setIva(cursor.getDouble(9));
            orderItem.setDiscount(cursor.getDouble(10));
            orderItem.setTotal(cursor.getDouble(11));
            orderItem.setValue(cursor.getDouble(12));
            orderItem.setStorageUnits(cursor.getInt(14));
            orderItem.setStorageNotes(cursor.getString(15));
            orderItem.setEqValue(cursor.getDouble(16));
        }
        cursor.close();
        return orderItem;
    }

    public boolean hasChildren(int orderId, int itemId) {

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS + " WHERE " + Database.KEY_ORDER_ID + " = " + orderId
                + " AND " + Database.KEY_ITEM_ID + " = " + itemId + " AND " + Database.KEY_SUBITEM_ID + " != 0", null);
        boolean hasChildren;
        hasChildren = cursor.moveToFirst();
        cursor.close();
        return hasChildren;

    }

    public List<OrderItem> getSeparatedOrderItems(int orderId) {

        SQLiteDatabase database = this.getWritableDatabase();
        List<OrderItem> orderItems = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS + " WHERE " + Database.KEY_ORDER_ID + " = " + orderId
                + " AND " + Database.KEY_ORDER_ITEMS_STATE_ID + " = " + ItemStates.ITEM_STATE_SEPARED + " AND "
                + Database.KEY_SUBITEM_ID + " = 0 " + " ORDER BY " + Database.KEY_ITEM_ID + " ASC, " + Database.KEY_SUBITEM_ID + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(getOrder(cursor.getInt(0)));
                orderItem.setItem(getItem(cursor.getInt(1)));
                orderItem.setSubItemId(cursor.getInt(2));
                orderItem.setUnitPrice(cursor.getDouble(3));
                orderItem.setUnits(cursor.getInt(4));
                orderItem.setFreeUnits(cursor.getInt(5));
                orderItem.setNotes(cursor.getString(6));
                if (cursor.getInt(7) == 0) {
                    orderItem.setPacker(null);
                } else {
                    orderItem.setPacker(getUser(cursor.getInt(7)));
                }
                orderItem.setOrderItemsState(getOrderItemState(cursor.getInt(8)));
                orderItem.setIva(cursor.getDouble(9));
                orderItem.setDiscount(cursor.getDouble(10));
                orderItem.setTotal(cursor.getDouble(11));
                orderItem.setValue(cursor.getDouble(12));
                if (orderItem.getSubItemId() == 0) {
                    orderItem.setSubItemName("");
                } else {
                    orderItem.setSubItemName(cursor.getString(13));
                }
                orderItems.add(orderItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderItems;

    }

    public List<OrderItem> getOrderItems(int orderId) {

        SQLiteDatabase database = this.getWritableDatabase();
        List<OrderItem> orderItems = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS + " WHERE " + Database.KEY_ORDER_ID + " = " + orderId
                + " ORDER BY " + Database.KEY_ITEM_ID + " ASC, " + Database.KEY_SUBITEM_ID + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(getOrder(cursor.getInt(0)));
                orderItem.setItem(getItem(cursor.getInt(1)));
                orderItem.setSubItemId(cursor.getInt(2));
                orderItem.setUnitPrice(cursor.getDouble(3));
                orderItem.setUnits(cursor.getInt(4));
                orderItem.setFreeUnits(cursor.getInt(5));
                orderItem.setNotes(cursor.getString(6));
                if (cursor.getInt(7) == 0) {
                    orderItem.setPacker(null);
                } else {
                    orderItem.setPacker(getUser(cursor.getInt(7)));
                }
                orderItem.setOrderItemsState(getOrderItemState(cursor.getInt(8)));
                orderItem.setIva(cursor.getDouble(9));
                orderItem.setDiscount(cursor.getDouble(10));
                orderItem.setTotal(cursor.getDouble(11));
                orderItem.setValue(cursor.getDouble(12));
                if (orderItem.getSubItemId() == 0) {
                    orderItem.setSubItemName("");
                } else {
                    orderItem.setSubItemName(cursor.getString(13));
                }
                orderItem.setStorageUnits(cursor.getInt(14));
                orderItem.setStorageNotes(cursor.getString(15));
                orderItem.setEqValue(cursor.getDouble(16));
                orderItems.add(orderItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderItems;

    }

    public List<OrderItem> getFinishedOrderItems(int orderId) {

        SQLiteDatabase database = this.getWritableDatabase();
        List<OrderItem> allOrderItems = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS + " WHERE " + Database.KEY_ORDER_ID + " = " + orderId + " ORDER BY " + Database.KEY_ITEM_ID + " ASC, " + Database.KEY_SUBITEM_ID + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(getOrder(cursor.getInt(0)));
                orderItem.setItem(getItem(cursor.getInt(1)));
                orderItem.setSubItemId(cursor.getInt(2));
                orderItem.setUnitPrice(cursor.getDouble(3));
                orderItem.setUnits(cursor.getInt(4));
                orderItem.setFreeUnits(cursor.getInt(5));
                orderItem.setNotes(cursor.getString(6));
                if (cursor.getInt(7) == 0) {
                    orderItem.setPacker(null);
                } else {
                    orderItem.setPacker(getUser(cursor.getInt(7)));
                }
                orderItem.setOrderItemsState(getOrderItemState(cursor.getInt(8)));
                orderItem.setIva(cursor.getDouble(9));
                orderItem.setDiscount(cursor.getDouble(10));
                orderItem.setTotal(cursor.getDouble(11));
                orderItem.setValue(cursor.getDouble(12));
                if (orderItem.getSubItemId() == 0) {
                    orderItem.setSubItemName("");
                } else {
                    orderItem.setSubItemName(cursor.getString(13));
                }
                orderItem.setStorageUnits(cursor.getInt(14));
                orderItem.setStorageNotes(cursor.getString(15));
                orderItem.setEqValue(cursor.getDouble(16));
                allOrderItems.add(orderItem);
            } while (cursor.moveToNext());
        }

        cursor.close();

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItem orderItem : allOrderItems) {
            if (orderItem.getOrderItemsState().getId() != ItemStates.ITEM_STATE_CANCELED) {
                orderItems.add(orderItem);
            }
        }

        return orderItems;

    }

    public void deleteOrder(int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        String orderWhereClause = Database.KEY_ID + " = ?";
        String orderItemsWhereClause = Database.KEY_ORDER_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        database.delete(Database.TABLE_ORDER, orderWhereClause, whereArgs);
        database.delete(Database.TABLE_ORDER_ITEMS, orderItemsWhereClause, whereArgs);
    }

    public boolean updateUserPassword(User user) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_IDENTIFICATOR, user.getIdentificator());
            database.update(Database.TABLE_USER, values, "id = " + user.getId(), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateParentStorageUnits(OrderItem orderItem) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {

            ContentValues values = new ContentValues();
            values.put(Database.KEY_STORAGE_UNITS, orderItem.getStorageUnits());
            database.update(Database.TABLE_ORDER_ITEMS, values, Database.KEY_ORDER_ID + " = " + orderItem.getOrder().getId() + " AND "
                    + Database.KEY_ITEM_ID + " = " + orderItem.getItem().getId() + " AND " + Database.KEY_SUBITEM_ID + " = 0", null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateItemStorageUnits(OrderItem orderItem) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {

            ContentValues values = new ContentValues();
            values.put(Database.KEY_STORAGE_UNITS, orderItem.getStorageUnits());
            values.put(Database.KEY_UNITS, orderItem.getUnits());
            values.put(Database.KEY_FREE_UNITS, orderItem.getFreeUnits());
            values.put(Database.KEY_TOTAL, orderItem.getTotal());
            database.update(Database.TABLE_ORDER_ITEMS, values, Database.KEY_ORDER_ID + " = " + orderItem.getOrder().getId() + " AND "
                    + Database.KEY_ITEM_ID + " = " + orderItem.getItem().getId() + " AND " + Database.KEY_SUBITEM_ID + " = " + orderItem.getSubItemId(), null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateProductPendingItem(OrderItem orderItem) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {

            ContentValues values = new ContentValues();
            values.put(Database.KEY_ORDER_ITEMS_STATE_ID, orderItem.getOrderItemsState().getId());
            values.put(Database.KEY_STORAGE_UNITS, orderItem.getStorageUnits());
            values.put(Database.KEY_STORAGE_NOTES, orderItem.getStorageNotes());
            database.update(Database.TABLE_ORDER_ITEMS, values, Database.KEY_ORDER_ID + " = " + orderItem.getOrder().getId() + " AND "
                    + Database.KEY_ITEM_ID + " = " + orderItem.getItem().getId() + " AND " + Database.KEY_SUBITEM_ID + " = " + orderItem.getSubItemId(), null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateOrderItemsStates(List<OrderItem> orderItems) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (OrderItem orderItem : orderItems) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ORDER_ITEMS_STATE_ID, orderItem.getOrderItemsState().getId());
                database.update(Database.TABLE_ORDER_ITEMS, values, Database.KEY_ORDER_ID + " = " + orderItem.getOrder().getId() + " AND "
                        + Database.KEY_ITEM_ID + " = " + orderItem.getItem().getId() + " AND " + Database.KEY_SUBITEM_ID + " = " + orderItem.getSubItemId(), null);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateOrderItemState(OrderItem orderItem) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ORDER_ITEMS_STATE_ID, orderItem.getOrderItemsState().getId());
            database.update(Database.TABLE_ORDER_ITEMS, values, Database.KEY_ORDER_ID + " = " + orderItem.getOrder().getId() + " AND "
                    + Database.KEY_ITEM_ID + " = " + orderItem.getItem().getId() + " AND " + Database.KEY_SUBITEM_ID + " = " + orderItem.getSubItemId(), null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateOrderItemNotes(OrderItem orderItem) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_NOTES, orderItem.getNotes());
            database.update(Database.TABLE_ORDER, values, Database.KEY_ORDER_ID + " = " + orderItem.getOrder().getId() + " AND "
                    + Database.KEY_ITEM_ID + " = " + orderItem.getItem().getId() + " AND " + Database.KEY_SUBITEM_ID + " = " + orderItem.getSubItemId(), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getBilledButUnsentOrders() {

        SQLiteDatabase database = this.getWritableDatabase();
        List<Order> orders = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER + " WHERE " + Database.KEY_ORDER_STATE_ID + " = " + States.ORDER_STATE_APPROVED
                + " AND " + Database.KEY_IS_SENT + " = 0", null);

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(0));
                order.setMade(cursor.getString(1));
                order.setDeliver(cursor.getString(2));
                order.setModifiedDate(cursor.getString(3));
                order.setSent(Boolean.valueOf(cursor.getString(4)));
                order.setNotes(cursor.getString(5));
                order.setPayment(getPayment(cursor.getInt(6)));
                order.setClient(getClient(cursor.getInt(7)));
                order.setSeller(getCurrentUser());
                order.setState(getOrderState(cursor.getInt(9)));
                order.setInProgress(Boolean.valueOf(cursor.getString(11)));
                orders.add(order);
            } while (cursor.moveToNext());
        }

        return orders;

    }

    public boolean updateOrderBiller(Order order) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_BILLER_ID, order.getBiller().getId());
            database.update(Database.TABLE_ORDER, values, "id = " + order.getId(), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrderStateAndSent(Order order) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ORDER_STATE_ID, order.getState().getId());
            values.put(Database.KEY_IS_SENT, order.isSent());
            database.update(Database.TABLE_ORDER, values, "id = " + order.getId(), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrderModifiedDate(Order order) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_MODIFIED, order.getModifiedDate());
            database.update(Database.TABLE_ORDER, values, "id = " + order.getId(), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrderState(Order order) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ORDER_STATE_ID, order.getState().getId());
            database.update(Database.TABLE_ORDER, values, "id = " + order.getId(), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrder(Order order) {
        SQLiteDatabase database = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_PAYMENT_ID, order.getPayment().getId());
            values.put(Database.KEY_NOTES, order.getNotes());
            values.put(Database.KEY_DELIVER, order.getDeliver());
            values.put(Database.KEY_PROGRESS, order.isInProgress());
            values.put(Database.KEY_ORDER_STATE_ID, order.getState().getId());
            values.put(Database.KEY_PAYMENT_NOTES, order.getPaymentNotes());
            database.update(Database.TABLE_ORDER, values, "id = " + order.getId(), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public OrderState getOrderState(int id) {
        OrderState orderState = null;
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_STATE + " WHERE " + Database.KEY_ID + " = " + id, null);
        if (cursor.moveToFirst()) {
            orderState = new OrderState();
            orderState.setId(cursor.getInt(0));
            orderState.setState(cursor.getString(1));
            orderState.setHexColor(cursor.getString(2));
        }

        cursor.close();

        return orderState;
    }

    public List<OrderItemsState> getOrderItemStates() {
        List<OrderItemsState> orderItemStates = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS_STATE, null);
        if (cursor.moveToFirst()) {
            do {
                OrderItemsState orderItemState = new OrderItemsState();
                orderItemState.setId(cursor.getInt(0));
                orderItemState.setState(cursor.getString(1));
                orderItemState.setHexColor(cursor.getString(2));
                orderItemStates.add(orderItemState);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return orderItemStates;
    }

    public OrderItemsState getOrderItemState(int id) {
        OrderItemsState orderItemState = null;
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS_STATE + " WHERE " + Database.KEY_ID + " = " + id, null);
        if (cursor.moveToFirst()) {
            orderItemState = new OrderItemsState();
            orderItemState.setId(cursor.getInt(0));
            orderItemState.setState(cursor.getString(1));
            orderItemState.setHexColor(cursor.getString(2));
        }

        cursor.close();

        return orderItemState;
    }

    public boolean isLastOrderItem(int orderId, int itemId) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS + " WHERE "
                + Database.KEY_ORDER_ID + " = " + orderId + " AND " + Database.KEY_ITEM_ID + " = " + itemId
                + " AND " + Database.KEY_SUBITEM_ID + " != 0", null);

        if (cursor.getCount() > 1) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }
    }

    public User getCurrentUser() {

        User user = null;
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_SESSION, null);
        if (cursor.moveToFirst()) {
            user = getUser(cursor.getInt(1));
        }

        cursor.close();

        return user;
    }

    public void closeSession() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + Database.TABLE_SESSION);
    }

    public boolean startSession(String username, String password) {

        String hashedPassword = Hashing.sha256().hashString(password, Charset.forName("UTF-8")).toString();

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_USER + " WHERE "
                + Database.KEY_USER_NAME + " = '" + username
                + "' AND " + Database.KEY_IDENTIFICATOR + " = '" + hashedPassword + "'", null);

        if (cursor.moveToFirst()) {
            boolean active = cursor.getInt(4) > 0;
            if (active) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_USER_ID, cursor.getInt(0));
                database.replace(Database.TABLE_SESSION, null, values);
                cursor.close();
                return true;
            } else {
                cursor.close();
                return false;
            }
        } else {
            cursor.close();
            return false;
        }

    }

    public void deleteLocations() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + Database.TABLE_LOCATION);
    }

    public void deleteTempLocations() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + Database.TABLE_TEMP_LOCATION);
    }

    public List<UserLocation> getTempLocations() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_TEMP_LOCATION + " ORDER BY seller_id, datetime(created)", null);
        List<UserLocation> locations = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                UserLocation location = new UserLocation();
                location.setLatitude(cursor.getDouble(1));
                location.setLongitude(cursor.getDouble(2));
                location.setSeller(getUser(cursor.getInt(3)));
                location.setCreated(cursor.getString(4));
                locations.add(location);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return locations;
    }

    public List<UserLocation> getLocations() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_LOCATION, null);
        List<UserLocation> locations = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                UserLocation location = new UserLocation();
                location.setLatitude(cursor.getDouble(1));
                location.setLongitude(cursor.getDouble(2));
                location.setSeller(getUser(cursor.getInt(3)));
                location.setCreated(cursor.getString(4));
                locations.add(location);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return locations;
    }

    public User getUser(int id) {

        User user = null;
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_USER + " WHERE "
                + Database.KEY_ID + " = " + id, null);

        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setUsername(cursor.getString(1));
            user.setIdentificator(cursor.getString(2));
            user.setContact(cursor.getString(3));
            user.setActive(cursor.getInt(4) > 0);
            user.setRole(cursor.getString(5));
        }

        cursor.close();

        return user;

    }

    public Brand getBrand(int id) {

        Brand brand = null;
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_BRAND
                + " WHERE " + Database.KEY_ID + " = " + id, null);

        if (cursor.moveToFirst()) {
            brand = new Brand();
            brand.setId(cursor.getInt(0));
            brand.setName(cursor.getString(1));
        }

        cursor.close();


        return brand;
    }

    public boolean deleteAllFromOrderItem(int orderId, int itemId) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            String orderItemsWhereClause = Database.KEY_ORDER_ID + " = ? AND " + Database.KEY_ITEM_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(orderId), String.valueOf(itemId)};
            database.delete(Database.TABLE_ORDER_ITEMS, orderItemsWhereClause, whereArgs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<OrderItem> getSubItemsFromItem(int orderId, int itemId) {

        SQLiteDatabase database = this.getWritableDatabase();
        List<OrderItem> orderItems = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER_ITEMS + " WHERE " + Database.KEY_ORDER_ID
                + " = " + orderId + " AND " + Database.KEY_ITEM_ID + " = " + itemId
                + " AND " + Database.KEY_SUBITEM_ID + " != 0", null);

        if (cursor.moveToFirst()) {
            do {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(getOrder(cursor.getInt(0)));
                orderItem.setItem(getItem(cursor.getInt(1)));
                orderItem.setSubItemId(cursor.getInt(2));
                orderItem.setUnitPrice(cursor.getDouble(3));
                orderItem.setUnits(cursor.getInt(4));
                orderItem.setFreeUnits(cursor.getInt(5));
                orderItem.setNotes(cursor.getString(6));
                if (cursor.getInt(7) == 0) {
                    orderItem.setPacker(null);
                } else {
                    orderItem.setPacker(getUser(cursor.getInt(7)));
                }
                orderItem.setOrderItemsState(getOrderItemState(cursor.getInt(8)));
                orderItem.setIva(cursor.getDouble(9));
                orderItem.setDiscount(cursor.getDouble(10));
                orderItem.setTotal(cursor.getDouble(11));
                orderItem.setValue(cursor.getDouble(12));
                if (orderItem.getSubItemId() == 0) {
                    orderItem.setSubItemName("");
                } else {
                    orderItem.setSubItemName(cursor.getString(13));
                }
                orderItem.setStorageUnits(cursor.getInt(14));
                orderItem.setStorageNotes(cursor.getString(15));
                orderItem.setEqValue(cursor.getDouble(16));
                orderItems.add(orderItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return orderItems;

    }

    public boolean deleteItem(int orderId, int itemId, int subItemId) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {

            OrderItem baseOrderItem = getOrderItemInfo(orderId, itemId, 0);
            OrderItem deletedOrderItem = getOrderItemInfo(orderId, itemId, subItemId);

            String orderItemsWhereClause = Database.KEY_ORDER_ID + " = ? AND " + Database.KEY_ITEM_ID + " = ? AND " + Database.KEY_SUBITEM_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(orderId), String.valueOf(itemId), String.valueOf(subItemId)};
            database.delete(Database.TABLE_ORDER_ITEMS, orderItemsWhereClause, whereArgs);

            ContentValues values = new ContentValues();
            values.put(Database.KEY_UNITS, baseOrderItem.getUnits() - deletedOrderItem.getUnits());
            values.put(Database.KEY_FREE_UNITS, baseOrderItem.getFreeUnits() - deletedOrderItem.getFreeUnits());
            database.update(Database.TABLE_ORDER_ITEMS, values, Database.KEY_ORDER_ID + " = " + orderId
                    + " AND " + Database.KEY_ITEM_ID + " = " + itemId + " AND " + Database.KEY_SUBITEM_ID + " = " + 0, null);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Brand> getBrands() {

        List<Brand> brands = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_BRAND + " ORDER BY " + Database.KEY_NAME + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                Brand brand = new Brand();
                brand.setId(cursor.getInt(0));
                brand.setName(cursor.getString(1));
                brands.add(brand);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return brands;

    }

    public Category getCategory(int id) {

        Category category = null;
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CATEGORY
                + " WHERE " + Database.KEY_ID + " = " + id, null);

        if (cursor.moveToFirst()) {
            category = new Category();
            category.setId(cursor.getInt(0));
            category.setName(cursor.getString(1));
        }

        cursor.close();
        return category;
    }

    public Category getCategoryByName(String name) {

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CATEGORY + " WHERE " + Database.KEY_NAME
                + " = '" + name + "'", null);

        Category category = null;

        if (cursor.moveToFirst()) {
            category = new Category();
            category.setId(cursor.getInt(0));
            category.setName(cursor.getString(1));
        }

        cursor.close();

        return category;
    }

    public List<Category> getNewItemsCategories() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT DISTINCT category_id FROM " + Database.TABLE_ITEM + " WHERE " + Database.KEY_IS_NEW
                + " = 1", null);
        List<Category> categories = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Category category = getCategory(cursor.getInt(0));
                categories.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return categories;
    }

    public List<Category> getCategoriesByBrand(int brandId) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT DISTINCT category_id FROM " + Database.TABLE_ITEM + " WHERE " + Database.KEY_BRAND_ID
                + " = " + brandId, null);
        List<Category> categories = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Category category = getCategory(cursor.getInt(0));
                categories.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return categories;
    }

    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_CATEGORY, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getInt(0));
                category.setName(cursor.getString(1));
                categories.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return categories;
    }

    public boolean insertTempLocations(List<UserLocation> locations) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (UserLocation location : locations) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_LATITUDE, location.getLatitude());
                values.put(Database.KEY_LONGITUDE, location.getLongitude());
                values.put(Database.KEY_SELLER_ID, location.getSeller().getId());
                values.put(Database.KEY_CREATED, location.getCreated());
                database.replace(Database.TABLE_TEMP_LOCATION, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getDistinctLocationSellers() {
        SQLiteDatabase database = this.getWritableDatabase();
        List<String> sellerNames = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT DISTINCT " + Database.KEY_SELLER_ID + " AS " + Database.KEY_SELLER_ID + " FROM " + Database.TABLE_TEMP_LOCATION, null);
        if (cursor.moveToFirst()) {
            sellerNames.add("Todos");
            do {
                String sellerName = getUser(cursor.getInt(0)).getContact();
                sellerNames.add(sellerName);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return sellerNames;
    }

    public boolean insertLocation(Location location, User user) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_LATITUDE, location.getLatitude());
            values.put(Database.KEY_LONGITUDE, location.getLongitude());
            values.put(Database.KEY_SELLER_ID, user.getId());
            values.put(Database.KEY_CREATED, new Timestamp(System.currentTimeMillis()).toString());
            database.replace(Database.TABLE_LOCATION, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertOrderItemStates(List<OrderItemsState> states) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (OrderItemsState state : states) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, state.getId());
                values.put(Database.KEY_STATE, state.getState());
                values.put(Database.KEY_HEX_COLOR, state.getHexColor());
                database.replace(Database.TABLE_ORDER_ITEMS_STATE, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertOrderItems(List<OrderItem> orderItems) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (OrderItem orderItem : orderItems) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ORDER_ID, orderItem.getOrder().getId());
                values.put(Database.KEY_ITEM_ID, orderItem.getItem().getId());
                values.put(Database.KEY_SUBITEM_ID, orderItem.getSubItemId());
                values.put(Database.KEY_UNIT_PRICE, orderItem.getUnitPrice());
                values.put(Database.KEY_UNITS, orderItem.getUnits());
                values.put(Database.KEY_FREE_UNITS, orderItem.getFreeUnits());
                values.put(Database.KEY_NOTES, orderItem.getNotes());
                if (orderItem.getPacker() != null) {
                    values.put(Database.KEY_PACKER_ID, orderItem.getPacker().getId());
                } else {
                    values.put(Database.KEY_PACKER_ID, 0);
                }
                values.put(Database.KEY_ORDER_ITEMS_STATE_ID, orderItem.getOrderItemsState().getId());
                values.put(Database.KEY_IVA, orderItem.getIva());
                values.put(Database.KEY_STORAGE_UNITS, orderItem.getStorageUnits());
                values.put(Database.KEY_STORAGE_NOTES, orderItem.getStorageNotes());
                values.put(Database.KEY_DISCOUNT, orderItem.getDiscount());
                values.put(Database.KEY_TOTAL, orderItem.getTotal());
                values.put(Database.KEY_VALUE, orderItem.getValue());
                values.put(Database.KEY_SUBITEM_NAME, orderItem.getSubItemName());
                values.put(Database.KEY_EQ_VALUE, orderItem.getEqValue());
                database.replace(Database.TABLE_ORDER_ITEMS, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTempOrderItems(int orderId) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            database.execSQL("DELETE FROM " + Database.TABLE_TEMP_ORDER_ITEMS + " WHERE " + Database.KEY_ORDER_ID + " = " + orderId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTempOrderItems() {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            database.execSQL("DELETE FROM " + Database.TABLE_TEMP_ORDER_ITEMS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertTempOrderItems(List<OrderItem> orderItems) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (OrderItem orderItem : orderItems) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ORDER_ID, orderItem.getOrder().getId());
                values.put(Database.KEY_ITEM_ID, orderItem.getItem().getId());
                values.put(Database.KEY_SUBITEM_ID, orderItem.getSubItemId());
                values.put(Database.KEY_STORAGE_UNITS, orderItem.getStorageUnits());
                values.put(Database.KEY_STORAGE_NOTES, orderItem.getStorageNotes());
                values.put(Database.KEY_UNIT_PRICE, orderItem.getUnitPrice());
                values.put(Database.KEY_UNITS, orderItem.getUnits());
                values.put(Database.KEY_FREE_UNITS, orderItem.getFreeUnits());
                values.put(Database.KEY_NOTES, orderItem.getNotes());
                if (orderItem.getPacker() != null) {
                    values.put(Database.KEY_PACKER_ID, orderItem.getPacker().getId());
                } else {
                    values.put(Database.KEY_PACKER_ID, 0);
                }
                values.put(Database.KEY_ORDER_ITEMS_STATE_ID, orderItem.getOrderItemsState().getId());
                values.put(Database.KEY_IVA, orderItem.getIva());
                values.put(Database.KEY_DISCOUNT, orderItem.getDiscount());
                values.put(Database.KEY_TOTAL, orderItem.getTotal());
                values.put(Database.KEY_VALUE, orderItem.getValue());
                values.put(Database.KEY_SUBITEM_NAME, orderItem.getSubItemName());
                values.put(Database.KEY_EQ_VALUE, orderItem.getEqValue());
                database.replace(Database.TABLE_TEMP_ORDER_ITEMS, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertTempOrder(Order order) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ID, order.getId());
            values.put(Database.KEY_MADE, order.getMade());
            values.put(Database.KEY_DELIVER, order.getDeliver());
            values.put(Database.KEY_MODIFIED, order.getModifiedDate());
            values.put(Database.KEY_IS_SENT, order.isSent());
            values.put(Database.KEY_NOTES, order.getNotes());
            if (order.getPayment() != null) {
                values.put(Database.KEY_PAYMENT_ID, order.getPayment().getId());
            } else {
                values.put(Database.KEY_PAYMENT_ID, 0);
            }
            values.put(Database.KEY_CLIENT_ID, order.getClient().getId());
            values.put(Database.KEY_SELLER_ID, order.getSeller().getId());
            values.put(Database.KEY_ORDER_STATE_ID, order.getState().getId());
            values.put(Database.KEY_PROGRESS, order.isInProgress());
            if (order.getBiller() != null) {
                values.put(Database.KEY_BILLER_ID, order.getBiller().getId());
            } else {
                values.put(Database.KEY_BILLER_ID, 0);
            }

            database.replace(Database.TABLE_TEMP_ORDER_STATE, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertTempOrderItem(OrderItem orderItem) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ORDER_ID, orderItem.getOrder().getId());
            values.put(Database.KEY_ITEM_ID, orderItem.getItem().getId());
            values.put(Database.KEY_SUBITEM_ID, orderItem.getSubItemId());
            values.put(Database.KEY_STORAGE_UNITS, orderItem.getStorageUnits());
            values.put(Database.KEY_STORAGE_NOTES, orderItem.getStorageNotes());
            values.put(Database.KEY_UNIT_PRICE, orderItem.getUnitPrice());
            values.put(Database.KEY_UNITS, orderItem.getUnits());
            values.put(Database.KEY_FREE_UNITS, orderItem.getFreeUnits());
            values.put(Database.KEY_NOTES, orderItem.getNotes());
            if (orderItem.getPacker() != null) {
                values.put(Database.KEY_PACKER_ID, orderItem.getPacker().getId());
            } else {
                values.put(Database.KEY_PACKER_ID, 0);
            }
            values.put(Database.KEY_ORDER_ITEMS_STATE_ID, orderItem.getOrderItemsState().getId());
            values.put(Database.KEY_IVA, orderItem.getIva());
            values.put(Database.KEY_DISCOUNT, orderItem.getDiscount());
            values.put(Database.KEY_TOTAL, orderItem.getTotal());
            values.put(Database.KEY_VALUE, orderItem.getValue());
            values.put(Database.KEY_SUBITEM_NAME, orderItem.getSubItemName());
            values.put(Database.KEY_EQ_VALUE, orderItem.getEqValue());
            database.replace(Database.TABLE_TEMP_ORDER_ITEMS, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<OrderItem> getTempOrderItems(int orderId) {

        SQLiteDatabase database = this.getWritableDatabase();
        List<OrderItem> orderItems = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_TEMP_ORDER_ITEMS
                + " WHERE " + Database.KEY_ORDER_ID + " = " + orderId + " ORDER BY " + Database.KEY_ITEM_ID + " ASC, " + Database.KEY_SUBITEM_ID + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(getOrder(cursor.getInt(0)));
                orderItem.setItem(getItem(cursor.getInt(1)));
                orderItem.setSubItemId(cursor.getInt(2));
                orderItem.setUnitPrice(cursor.getDouble(3));
                orderItem.setUnits(cursor.getInt(4));
                orderItem.setFreeUnits(cursor.getInt(5));
                orderItem.setNotes(cursor.getString(6));
                if (cursor.getInt(7) == 0) {
                    orderItem.setPacker(null);
                } else {
                    orderItem.setPacker(getUser(cursor.getInt(7)));
                }
                orderItem.setOrderItemsState(getOrderItemState(cursor.getInt(8)));
                orderItem.setIva(cursor.getDouble(9));
                orderItem.setDiscount(cursor.getDouble(10));
                orderItem.setTotal(cursor.getDouble(11));
                orderItem.setValue(cursor.getDouble(12));
                if (orderItem.getSubItemId() == 0) {
                    orderItem.setSubItemName("");
                } else {
                    orderItem.setSubItemName(cursor.getString(13));
                }
                orderItem.setStorageUnits(cursor.getInt(14));
                orderItem.setStorageNotes(cursor.getString(15));
                orderItem.setEqValue(cursor.getDouble(16));
                orderItems.add(orderItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderItems;

    }

    public List<OrderItem> getTempOrderItems() {

        SQLiteDatabase database = this.getWritableDatabase();
        List<OrderItem> orderItems = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_TEMP_ORDER_ITEMS
                + " ORDER BY " + Database.KEY_ITEM_ID + " ASC, " + Database.KEY_SUBITEM_ID + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(getOrder(cursor.getInt(0)));
                orderItem.setItem(getItem(cursor.getInt(1)));
                orderItem.setSubItemId(cursor.getInt(2));
                orderItem.setUnitPrice(cursor.getDouble(3));
                orderItem.setUnits(cursor.getInt(4));
                orderItem.setFreeUnits(cursor.getInt(5));
                orderItem.setNotes(cursor.getString(6));
                if (cursor.getInt(7) == 0) {
                    orderItem.setPacker(null);
                } else {
                    orderItem.setPacker(getUser(cursor.getInt(7)));
                }
                orderItem.setOrderItemsState(getOrderItemState(cursor.getInt(8)));
                orderItem.setIva(cursor.getDouble(9));
                orderItem.setDiscount(cursor.getDouble(10));
                orderItem.setTotal(cursor.getDouble(11));
                orderItem.setValue(cursor.getDouble(12));
                if (orderItem.getSubItemId() == 0) {
                    orderItem.setSubItemName("");
                } else {
                    orderItem.setSubItemName(cursor.getString(13));
                }
                orderItem.setStorageUnits(cursor.getInt(14));
                orderItem.setStorageNotes(cursor.getString(15));
                orderItem.setEqValue(cursor.getDouble(16));
                orderItems.add(orderItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderItems;

    }

    public boolean insertOrderItem(OrderItem orderItem) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ORDER_ID, orderItem.getOrder().getId());
            values.put(Database.KEY_ITEM_ID, orderItem.getItem().getId());
            values.put(Database.KEY_SUBITEM_ID, orderItem.getSubItemId());
            values.put(Database.KEY_STORAGE_UNITS, orderItem.getStorageUnits());
            values.put(Database.KEY_STORAGE_NOTES, orderItem.getStorageNotes());
            values.put(Database.KEY_UNIT_PRICE, orderItem.getUnitPrice());
            values.put(Database.KEY_UNITS, orderItem.getUnits());
            values.put(Database.KEY_FREE_UNITS, orderItem.getFreeUnits());
            values.put(Database.KEY_NOTES, orderItem.getNotes());
            if (orderItem.getPacker() != null) {
                values.put(Database.KEY_PACKER_ID, orderItem.getPacker().getId());
            } else {
                values.put(Database.KEY_PACKER_ID, 0);
            }
            values.put(Database.KEY_ORDER_ITEMS_STATE_ID, orderItem.getOrderItemsState().getId());
            values.put(Database.KEY_IVA, orderItem.getIva());
            values.put(Database.KEY_DISCOUNT, orderItem.getDiscount());
            values.put(Database.KEY_TOTAL, orderItem.getTotal());
            values.put(Database.KEY_VALUE, orderItem.getValue());
            values.put(Database.KEY_SUBITEM_NAME, orderItem.getSubItemName());
            values.put(Database.KEY_EQ_VALUE, orderItem.getEqValue());
            database.replace(Database.TABLE_ORDER_ITEMS, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertUsers(List<User> users) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (User user : users) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, user.getId());
                values.put(Database.KEY_USER_NAME, user.getUsername());
                values.put(Database.KEY_IDENTIFICATOR, user.getIdentificator());
                values.put(Database.KEY_CONTACT, user.getContact());
                values.put(Database.KEY_IS_ACTIVE, user.isActive());
                values.put(Database.KEY_ROLE, user.getRole());
                database.replace(Database.TABLE_USER, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertOrderStates(List<OrderState> states) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (OrderState state : states) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, state.getId());
                values.put(Database.KEY_STATE, state.getState());
                values.put(Database.KEY_HEX_COLOR, state.getHexColor());
                database.replace(Database.TABLE_ORDER_STATE, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Order getOrder(int id) {

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER + " WHERE " + Database.KEY_ID + " = " + id, null);
        Order order = null;
        if (cursor.moveToFirst()) {
            order = new Order();
            order.setId(cursor.getInt(0));
            order.setMade(cursor.getString(1));
            order.setDeliver(cursor.getString(2));
            order.setModifiedDate(cursor.getString(3));
            order.setSent(cursor.getInt(4) > 0);
            order.setNotes(cursor.getString(5));
            order.setPayment(getPayment(cursor.getInt(6)));
            order.setClient(getClient(cursor.getInt(7)));
            order.setSeller(getCurrentUser());
            order.setState(getOrderState(cursor.getInt(9)));
            order.setInProgress(cursor.getInt(11) > 0);
            order.setBiller(getUser(cursor.getInt(10)));
        }

        cursor.close();
        return order;

    }

    public Order getCurrentOrder(int id) {

        SQLiteDatabase database = this.getWritableDatabase();
        Order order = null;
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ORDER + " WHERE " + Database.KEY_ID + " = " + id, null);
        if (cursor.moveToFirst()) {
            order = new Order();
            order.setId(cursor.getInt(0));
            order.setMade(cursor.getString(1));
            order.setDeliver(cursor.getString(2));
            order.setModifiedDate(cursor.getString(3));
            order.setSent(cursor.getInt(4) > 0);
            order.setNotes(cursor.getString(5));
            order.setPayment(getPayment(cursor.getInt(6)));
            order.setClient(getClient(cursor.getInt(7)));
            order.setSeller(getCurrentUser());
            order.setState(getOrderState(cursor.getInt(9)));
            order.setBiller(getUser(cursor.getInt(10)));
            order.setInProgress(cursor.getInt(11) > 0);
        }

        cursor.close();

        return order;
    }

    public boolean insertOrder(Order order) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ID, order.getId());
            values.put(Database.KEY_MADE, order.getMade());
            values.put(Database.KEY_DELIVER, order.getDeliver());
            values.put(Database.KEY_MODIFIED, order.getModifiedDate());
            values.put(Database.KEY_IS_SENT, order.isSent());
            values.put(Database.KEY_NOTES, order.getNotes());
            if (order.getPayment() != null) {
                values.put(Database.KEY_PAYMENT_ID, order.getPayment().getId());
            } else {
                values.put(Database.KEY_PAYMENT_ID, 0);
            }
            values.put(Database.KEY_CLIENT_ID, order.getClient().getId());
            values.put(Database.KEY_SELLER_ID, order.getSeller().getId());
            values.put(Database.KEY_ORDER_STATE_ID, order.getState().getId());
            values.put(Database.KEY_PROGRESS, order.isInProgress());
            if (order.getBiller() != null) {
                values.put(Database.KEY_BILLER_ID, order.getBiller().getId());
            } else {
                values.put(Database.KEY_BILLER_ID, 0);
            }

            database.replace(Database.TABLE_ORDER, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Excepcion: " + e.getMessage());
            return false;
        }
    }

    public boolean insertOrders(List<Order> orders) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (Order order : orders) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, order.getId());
                values.put(Database.KEY_MADE, order.getMade());
                values.put(Database.KEY_DELIVER, order.getDeliver());
                values.put(Database.KEY_MODIFIED, order.getModifiedDate());
                values.put(Database.KEY_IS_SENT, order.isSent());
                values.put(Database.KEY_NOTES, order.getNotes());
                if (order.getPayment() != null) {
                    values.put(Database.KEY_PAYMENT_ID, order.getPayment().getId());
                } else {
                    values.put(Database.KEY_PAYMENT_ID, 0);
                }
                values.put(Database.KEY_CLIENT_ID, order.getClient().getId());
                values.put(Database.KEY_SELLER_ID, order.getSeller().getId());
                values.put(Database.KEY_ORDER_STATE_ID, order.getState().getId());
                values.put(Database.KEY_PROGRESS, order.isInProgress());
                if (order.getBiller() != null) {
                    values.put(Database.KEY_BILLER_ID, order.getBiller().getId());
                } else {
                    values.put(Database.KEY_BILLER_ID, 0);
                }
                database.replace(Database.TABLE_ORDER, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertItems(List<Item> items) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (Item item : items) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, item.getId());
                values.put(Database.KEY_NAME, item.getName());
                values.put(Database.KEY_IS_ACTIVE, item.isActive());
                values.put(Database.KEY_IS_NEW, item.isNewItem());
                values.put(Database.KEY_BRAND_ID, item.getBrand().getId());
                values.put(Database.KEY_CATEGORY_ID, item.getCategory().getId());
                values.put(Database.KEY_SUBITEM_ID, item.getSubItemId());
                values.put(Database.KEY_PRICE_ONE, item.getPriceOne());
                values.put(Database.KEY_PRICE_TWO, item.getPriceTwo());
                values.put(Database.KEY_PRICE_THREE, item.getPriceThree());
                values.put(Database.KEY_PRICE_FOUR, item.getPriceFour());
                values.put(Database.KEY_PRICE_FIVE, item.getPriceFive());
                values.put(Database.KEY_PAYMENT_ONE, item.getPaymentOne());
                values.put(Database.KEY_PAYMENT_TWO, item.getPaymentTwo());
                values.put(Database.KEY_PAYMENT_THREE, item.getPaymentThree());
                values.put(Database.KEY_PAYMENT_FOUR, item.getPaymentFour());
                values.put(Database.KEY_IVA, item.getIva());
                database.replace(Database.TABLE_ITEM, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertClientTypes(List<ClientType> clientTypes) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (ClientType clientType : clientTypes) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, clientType.getId());
                values.put(Database.KEY_NAME, clientType.getName());
                database.replace(Database.TABLE_CLIENT_TYPE, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public Payment getPayment(int term, String name) {
        SQLiteDatabase database = this.getWritableDatabase();
        Payment payment = null;
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_PAYMENT + " WHERE " + Database.KEY_TERM + " = " + term
                + " AND " + Database.KEY_NAME + " = " + "'" + name + "'", null);
        if (cursor.moveToFirst()) {
            payment = new Payment();
            payment.setId(cursor.getInt(0));
            payment.setName(cursor.getString(1));
            payment.setTerm(cursor.getInt(2));
        }

        cursor.close();

        return payment;
    }

    public List<Payment> getPayments(String name) {
        SQLiteDatabase database = this.getWritableDatabase();
        List<Payment> payments = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_PAYMENT + " WHERE " + Database.KEY_NAME + " = " + "'" + name + "'", null);
        if (cursor.moveToFirst()) {
            do {
                Payment payment = new Payment();
                payment.setId(cursor.getInt(0));
                payment.setName(cursor.getString(1));
                payment.setTerm(cursor.getInt(2));
                payments.add(payment);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return payments;
    }

    public List<String> getPaymentsNames() {
        SQLiteDatabase database = this.getWritableDatabase();
        List<String> payments = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_PAYMENT, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(1);
                payments.add(name);
            } while (cursor.moveToNext());
        }

        HashSet<String> uniqueValues = new HashSet<>(payments);
        List<String> distinctPayments = new ArrayList<>();
        String[] paymentsArray = new String[3];
        for (String value : uniqueValues) {
            switch (value) {
                case "Efectivo":
                    paymentsArray[0] = value;
                    break;
                case "Credito":
                    paymentsArray[1] = value;
                    break;
                default:
                    paymentsArray[2] = value;
                    break;
            }
        }

        distinctPayments = Arrays.asList(paymentsArray);

        cursor.close();
        return distinctPayments;
    }

    public List<Payment> getPayments() {
        SQLiteDatabase database = this.getWritableDatabase();
        List<Payment> payments = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_PAYMENT, null);
        if (cursor.moveToFirst()) {
            do {
                Payment payment = new Payment();
                payment.setId(cursor.getInt(0));
                payment.setName(cursor.getString(1));
                payment.setTerm(cursor.getInt(2));
                payments.add(payment);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return payments;
    }

    public Payment getPayment(int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        Payment payment = null;
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_PAYMENT + " WHERE " + Database.KEY_ID + " = " + id, null);
        if (cursor.moveToFirst()) {
            payment = new Payment();
            payment.setId(cursor.getInt(0));
            payment.setName(cursor.getString(1));
            payment.setTerm(cursor.getInt(2));
        }

        cursor.close();

        return payment;
    }

    public boolean insertPayments(List<Payment> payments) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (Payment payment : payments) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, payment.getId());
                values.put(Database.KEY_NAME, payment.getName());
                values.put(Database.KEY_TERM, payment.getTerm());
                database.replace(Database.TABLE_PAYMENT, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertClient(Client client) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_ID, client.getId());
            values.put(Database.KEY_CODE, client.getCode());
            values.put(Database.KEY_COMPANY, client.getCompany());
            values.put(Database.KEY_ADDRESS, client.getAddress());
            values.put(Database.KEY_CITY_ID, client.getCity().getId());
            values.put(Database.KEY_PHONE_ONE, client.getPhoneOne());
            values.put(Database.KEY_PHONE_TWO, client.getPhoneTwo());
            values.put(Database.KEY_PHONE_THREE, client.getPhoneThree());
            values.put(Database.KEY_NIT, client.getNit());
            values.put(Database.KEY_MAIL, client.getMail());
            values.put(Database.KEY_CONTACT, client.getContact());
            values.put(Database.KEY_NEIGHBORHOOD, client.getNeighborhood());
            values.put(Database.KEY_IS_SENT, client.isSent());
            values.put(Database.KEY_IS_ACTIVE, client.isActive());
            values.put(Database.KEY_CLIENT_TYPE_ID, client.getClientType().getId());
            values.put(Database.KEY_USER_ID, client.getUser().getId());
            values.put(Database.KEY_LATITUDE, client.getLatitude());
            values.put(Database.KEY_LONGITUDE, client.getLongitude());
            database.replace(Database.TABLE_CLIENT, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Excepiom: " + e.getMessage());
            return false;
        }
    }

    public boolean updateClient(boolean sent, int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_IS_SENT, sent);
            database.update(Database.TABLE_CLIENT, values, "id = " + id, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertClients(List<Client> clients) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (Client client : clients) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, client.getId());
                values.put(Database.KEY_CODE, client.getCode());
                values.put(Database.KEY_COMPANY, client.getCompany());
                values.put(Database.KEY_ADDRESS, client.getAddress());
                values.put(Database.KEY_CITY_ID, client.getCity().getId());
                values.put(Database.KEY_PHONE_ONE, client.getPhoneOne());
                values.put(Database.KEY_PHONE_TWO, client.getPhoneTwo());
                values.put(Database.KEY_PHONE_THREE, client.getPhoneThree());
                values.put(Database.KEY_NIT, client.getNit());
                values.put(Database.KEY_MAIL, client.getMail());
                values.put(Database.KEY_CONTACT, client.getContact());
                values.put(Database.KEY_NEIGHBORHOOD, client.getNeighborhood());
                values.put(Database.KEY_IS_SENT, client.isSent());
                values.put(Database.KEY_CLIENT_TYPE_ID, client.getClientType().getId());
                values.put(Database.KEY_IS_ACTIVE, client.isActive());
                values.put(Database.KEY_USER_ID, client.getUser().getId());
                values.put(Database.KEY_LATITUDE, client.getLatitude());
                values.put(Database.KEY_LONGITUDE, client.getLongitude());
                database.replace(Database.TABLE_CLIENT, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertCategories(List<Category> categories) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (Category category : categories) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, category.getId());
                values.put(Database.KEY_NAME, category.getName());
                database.replace(Database.TABLE_CATEGORY, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertBrands(List<Brand> brands) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            for (Brand brand : brands) {
                ContentValues values = new ContentValues();
                values.put(Database.KEY_ID, brand.getId());
                values.put(Database.KEY_NAME, brand.getName());
                database.replace(Database.TABLE_BRAND, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getParentPrice(int id, int clientType) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " WHERE id = " + id
                + " AND " + Database.KEY_SUBITEM_ID + " = 0", null);
        double price = 0;
        try {
            if (cursor.moveToFirst()) {
                if (clientType == ClientTypes.TYPE_CENTRO) {
                    price = cursor.getDouble(7);
                }
                if (clientType == ClientTypes.TYPE_NACIONAL) {
                    price = cursor.getDouble(8);
                }
                if (clientType == ClientTypes.TYPE_PERIFERIAS) {
                    price = cursor.getDouble(9);
                }
                if (clientType == ClientTypes.TYPE_PELUQUERIAS) {
                    price = cursor.getDouble(10);
                }
                if (clientType == ClientTypes.TYPE_USUARIO_FINAL) {
                    price = cursor.getDouble(11);
                }
            }
            cursor.close();
            return price;
        } catch (Exception e) {
            cursor.close();
            return 0;
        }
    }

    public Item getItem(int id) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " WHERE id = " + id, null);
        Item item = null;
        try {
            if (cursor.moveToFirst()) {
                item = new Item();
                item.setId(cursor.getInt(0));
                item.setName(cursor.getString(1));
                item.setActive(cursor.getInt(2) > 0);
                item.setNewItem(cursor.getInt(3) > 0);
                item.setBrand(getBrand(cursor.getInt(4)));
                item.setCategory(getCategory(cursor.getInt(5)));
                item.setSubItemId(cursor.getInt(6));
                item.setPriceOne(cursor.getDouble(7));
                item.setPriceTwo(cursor.getDouble(8));
                item.setPriceThree(cursor.getDouble(9));
                item.setPriceFour(cursor.getDouble(10));
                item.setPriceFive(cursor.getDouble(11));
                item.setPaymentOne(cursor.getDouble(12));
                item.setPaymentTwo(cursor.getDouble(13));
                item.setPaymentThree(cursor.getDouble(14));
                item.setPaymentFour(cursor.getDouble(15));
                item.setIva(cursor.getDouble(16));
            }
            cursor.close();
            return item;
        } catch (Exception e) {
            cursor.close();
            return null;
        }
    }

    public List<Item> getNewItems() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " WHERE "
                + Database.KEY_IS_NEW + " = 1", null);
        List<Item> items = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setId(cursor.getInt(0));
                    item.setName(cursor.getString(1));
                    item.setActive(cursor.getInt(2) > 0);
                    item.setNewItem(cursor.getInt(3) > 0);
                    item.setBrand(getBrand(cursor.getInt(4)));
                    item.setCategory(getCategory(cursor.getInt(5)));
                    item.setSubItemId(cursor.getInt(6));
                    item.setPriceOne(cursor.getDouble(7));
                    item.setPriceTwo(cursor.getDouble(8));
                    item.setPriceThree(cursor.getDouble(9));
                    item.setPriceFour(cursor.getDouble(10));
                    item.setPriceFive(cursor.getDouble(11));
                    item.setPaymentOne(cursor.getDouble(12));
                    item.setPaymentTwo(cursor.getDouble(13));
                    item.setPaymentThree(cursor.getDouble(14));
                    item.setPaymentFour(cursor.getDouble(15));
                    item.setIva(cursor.getDouble(16));
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return items;
        } catch (Exception e) {
            cursor.close();
            return null;
        }
    }

    public List<Item> getItemsByCategory(Category category) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " WHERE "
                + Database.KEY_CATEGORY_ID + " = " + category.getId() + " AND " + Database.KEY_IS_ACTIVE + " = 1", null);
        List<Item> items = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setId(cursor.getInt(0));
                    item.setName(cursor.getString(1));
                    item.setActive(cursor.getInt(2) > 0);
                    item.setNewItem(cursor.getInt(3) > 0);
                    item.setBrand(getBrand(cursor.getInt(4)));
                    item.setCategory(getCategory(cursor.getInt(5)));
                    item.setSubItemId(cursor.getInt(6));
                    item.setPriceOne(cursor.getDouble(7));
                    item.setPriceTwo(cursor.getDouble(8));
                    item.setPriceThree(cursor.getDouble(9));
                    item.setPriceFour(cursor.getDouble(10));
                    item.setPriceFive(cursor.getDouble(11));
                    item.setPaymentOne(cursor.getDouble(12));
                    item.setPaymentTwo(cursor.getDouble(13));
                    item.setPaymentThree(cursor.getDouble(14));
                    item.setPaymentFour(cursor.getDouble(15));
                    item.setIva(cursor.getDouble(16));
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return items;
        } catch (Exception e) {
            cursor.close();
            return null;
        }
    }

    public void deleteFilters() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM " + Database.TABLE_FILTERS);
    }

    public List<Item> getNewItemsByCategory(Category category) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " WHERE "
                + Database.KEY_CATEGORY_ID + " = " + category.getId() + " AND " + Database.KEY_IS_NEW + " = 1", null);
        List<Item> items = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setId(cursor.getInt(0));
                    item.setName(cursor.getString(1));
                    item.setActive(cursor.getInt(2) > 0);
                    item.setNewItem(cursor.getInt(3) > 0);
                    item.setBrand(getBrand(cursor.getInt(4)));
                    item.setCategory(getCategory(cursor.getInt(5)));
                    item.setSubItemId(cursor.getInt(6));
                    item.setPriceOne(cursor.getDouble(7));
                    item.setPriceTwo(cursor.getDouble(8));
                    item.setPriceThree(cursor.getDouble(9));
                    item.setPriceFour(cursor.getDouble(10));
                    item.setPriceFive(cursor.getDouble(11));
                    item.setPaymentOne(cursor.getDouble(12));
                    item.setPaymentTwo(cursor.getDouble(13));
                    item.setPaymentThree(cursor.getDouble(14));
                    item.setPaymentFour(cursor.getDouble(15));
                    item.setIva(cursor.getDouble(16));
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return items;
        } catch (Exception e) {
            cursor.close();
            return null;
        }
    }

    public List<Item> getItemsByBrand(Brand brand) {

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " WHERE "
                + Database.KEY_BRAND_ID + " = " + brand.getId() + " AND " + Database.KEY_IS_ACTIVE + " = 1", null);
        List<Item> items = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setId(cursor.getInt(0));
                    item.setName(cursor.getString(1));
                    item.setActive(cursor.getInt(2) > 0);
                    item.setNewItem(cursor.getInt(3) > 0);
                    item.setBrand(getBrand(cursor.getInt(4)));
                    item.setCategory(getCategory(cursor.getInt(5)));
                    item.setSubItemId(cursor.getInt(6));
                    item.setPriceOne(cursor.getDouble(7));
                    item.setPriceTwo(cursor.getDouble(8));
                    item.setPriceThree(cursor.getDouble(9));
                    item.setPriceFour(cursor.getDouble(10));
                    item.setPriceFive(cursor.getDouble(11));
                    item.setPaymentOne(cursor.getDouble(12));
                    item.setPaymentTwo(cursor.getDouble(13));
                    item.setPaymentThree(cursor.getDouble(14));
                    item.setPaymentFour(cursor.getDouble(15));
                    item.setIva(cursor.getDouble(16));
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return items;
        } catch (Exception e) {
            cursor.close();
            return null;
        }

    }

    public List<Item> getItemByCategoryAndBrand(Category category, Brand brand) {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " WHERE " + Database.KEY_CATEGORY_ID + " = "
                + category.getId() + " AND " + Database.KEY_BRAND_ID + " = " + brand.getId(), null);
        List<Item> items = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setId(cursor.getInt(0));
                    item.setName(cursor.getString(1));
                    item.setActive(cursor.getInt(2) > 0);
                    item.setNewItem(cursor.getInt(3) > 0);
                    item.setBrand(getBrand(cursor.getInt(4)));
                    item.setCategory(getCategory(cursor.getInt(5)));
                    item.setSubItemId(cursor.getInt(6));
                    item.setPriceOne(cursor.getDouble(7));
                    item.setPriceTwo(cursor.getDouble(8));
                    item.setPriceThree(cursor.getDouble(9));
                    item.setPriceFour(cursor.getDouble(10));
                    item.setPriceFive(cursor.getDouble(11));
                    item.setPaymentOne(cursor.getDouble(12));
                    item.setPaymentTwo(cursor.getDouble(13));
                    item.setPaymentThree(cursor.getDouble(14));
                    item.setPaymentFour(cursor.getDouble(15));
                    item.setIva(cursor.getDouble(16));
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return items;
        } catch (Exception e) {
            cursor.close();
            return null;
        }
    }

    public List<Item> getItemsSeq(int currentRows) {
        if (currentRows == 0) {
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " WHERE " + Database.KEY_IS_ACTIVE + " = 1 LIMIT 30", null);
            List<Item> items = new ArrayList<>();
            try {
                if (cursor.moveToFirst()) {
                    do {
                        Item item = new Item();
                        item.setId(cursor.getInt(0));
                        item.setName(cursor.getString(1));
                        item.setActive(cursor.getInt(2) > 0);
                        item.setNewItem(cursor.getInt(3) > 0);
                        item.setBrand(getBrand(cursor.getInt(4)));
                        item.setCategory(getCategory(cursor.getInt(5)));
                        item.setSubItemId(cursor.getInt(6));
                        item.setPriceOne(cursor.getDouble(7));
                        item.setPriceTwo(cursor.getDouble(8));
                        item.setPriceThree(cursor.getDouble(9));
                        item.setPriceFour(cursor.getDouble(10));
                        item.setPriceFive(cursor.getDouble(11));
                        item.setPaymentOne(cursor.getDouble(12));
                        item.setPaymentTwo(cursor.getDouble(13));
                        item.setPaymentThree(cursor.getDouble(14));
                        item.setPaymentFour(cursor.getDouble(15));
                        item.setIva(cursor.getDouble(16));
                        items.add(item);
                    } while (cursor.moveToNext());
                }
                cursor.close();
                return items;
            } catch (Exception e) {
                cursor.close();
                return null;
            }
        } else {
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " LIMIT " + currentRows + ", " + (currentRows + 30), null);
            List<Item> items = new ArrayList<>();
            try {
                if (cursor.moveToFirst()) {
                    do {
                        Item item = new Item();
                        item.setId(cursor.getInt(0));
                        item.setName(cursor.getString(1));
                        item.setActive(cursor.getInt(2) > 0);
                        item.setNewItem(cursor.getInt(3) > 0);
                        item.setBrand(getBrand(cursor.getInt(4)));
                        item.setCategory(getCategory(cursor.getInt(5)));
                        item.setSubItemId(cursor.getInt(6));
                        item.setPriceOne(cursor.getDouble(7));
                        item.setPriceTwo(cursor.getDouble(8));
                        item.setPriceThree(cursor.getDouble(9));
                        item.setPriceFour(cursor.getDouble(10));
                        item.setPriceFive(cursor.getDouble(11));
                        item.setPaymentOne(cursor.getDouble(12));
                        item.setPaymentTwo(cursor.getDouble(13));
                        item.setPaymentThree(cursor.getDouble(14));
                        item.setPaymentFour(cursor.getDouble(15));
                        item.setIva(cursor.getDouble(16));
                        items.add(item);
                    } while (cursor.moveToNext());
                }
                cursor.close();
                return items;
            } catch (Exception e) {
                cursor.close();
                return null;
            }
        }
    }

    public List<Item> getItems() {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + Database.TABLE_ITEM + " WHERE " + Database.KEY_IS_ACTIVE + " = 1", null);
        List<Item> items = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setId(cursor.getInt(0));
                    item.setName(cursor.getString(1));
                    item.setActive(cursor.getInt(2) > 0);
                    item.setNewItem(cursor.getInt(3) > 0);
                    item.setBrand(getBrand(cursor.getInt(4)));
                    item.setCategory(getCategory(cursor.getInt(5)));
                    item.setSubItemId(cursor.getInt(6));
                    item.setPriceOne(cursor.getDouble(7));
                    item.setPriceTwo(cursor.getDouble(8));
                    item.setPriceThree(cursor.getDouble(9));
                    item.setPriceFour(cursor.getDouble(10));
                    item.setPriceFive(cursor.getDouble(11));
                    item.setPaymentOne(cursor.getDouble(12));
                    item.setPaymentTwo(cursor.getDouble(13));
                    item.setPaymentThree(cursor.getDouble(14));
                    item.setPaymentFour(cursor.getDouble(15));
                    item.setIva(cursor.getDouble(16));
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return items;
        } catch (Exception e) {
            cursor.close();
            return null;
        }
    }

    public boolean updateClientTypeAndLocation(Client client) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(Database.KEY_CLIENT_TYPE_ID, client.getClientType().getId());
            values.put(Database.KEY_LONGITUDE, client.getLongitude());
            values.put(Database.KEY_LATITUDE, client.getLatitude());
            database.update(Database.TABLE_CLIENT, values, Database.KEY_ID + " = " + client.getId(), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
