package co.com.imagenybelleza.imagenybelleza.enums;

/**
 * Created by Juan Camilo Villa Amaya on 4/12/2016.
 * <p>
 * Esta clase define las constantes de creacion de tablas, nombres de tablas, version, nombre de base de datos
 * y nombres de columnas
 */

public class Database {

    public static final String DATABASE_NAME = "combelleza_db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_TEMP_LOCATION = "temp_location";
    public static final String TABLE_TEMP_ORDER = "temp_order";
    public static final String TABLE_TEMP_ORDER_ITEMS = "temp_order_items";

    public static final String KEY_ID = "id";
    public static final String KEY_CREATED = "created";
    public static final String KEY_MODIFIED = "modified";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_NAME = "name";
    public static final String KEY_IS_ACTIVE = "is_active";
    public static final String KEY_SELLER_ID = "seller_id";
    public static final String KEY_STATE = "state";
    public static final String KEY_HEX_COLOR = "hex_color";

    public static final String TABLE_BRAND = "brand";
    public static final String TABLE_CATEGORY = "category";

    public static final String TABLE_CLIENT = "client";
    public static final String KEY_CODE = "code";
    public static final String KEY_COMPANY = "company";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_CITY_ID = "city";
    public static final String KEY_CLIENT_TYPE_ID = "client_type_id";
    public static final String KEY_PHONE_ONE = "phone_one";
    public static final String KEY_PHONE_TWO = "phone_two";
    public static final String KEY_PHONE_THREE = "phone_three";
    public static final String KEY_NIT = "nit";
    public static final String KEY_MAIL = "mail_address";
    public static final String KEY_CONTACT = "contact";
    public static final String KEY_NEIGHBORHOOD = "neighborhood";

    public static final String TABLE_ITEM = "item";
    public static final String KEY_IS_NEW = "is_new";
    public static final String KEY_PRICE = "price";
    public static final String KEY_BRAND_ID = "brand_id";
    public static final String KEY_CATEGORY_ID = "category_id";
    public static final String KEY_DISCOUNT_ONE = "discount_one";
    public static final String KEY_DISCOUNT_TWO = "discount_two";
    public static final String KEY_DISCOUNT_THREE = "discount_three";
    public static final String KEY_DISCOUNT_FOUR = "discount_four";
    public static final String KEY_DISCOUNT_FIVE = "discount_five";
    public static final String KEY_PAYMENT_ONE = "payment_one";
    public static final String KEY_PAYMENT_TWO = "payment_two";
    public static final String KEY_PAYMENT_THREE = "payment_three";
    public static final String KEY_PAYMENT_FOUR = "payment_four";
    public static final String KEY_IVA = "iva";

    public static final String TABLE_LOCATION = "location";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    public static final String TABLE_ORDER = "´order´";
    public static final String KEY_MADE = "made";
    public static final String KEY_DELIVER = "deliver";
    public static final String KEY_IS_SENT = "is_sent";
    public static final String KEY_PAYMENT_ID = "payment_id";
    public static final String KEY_CLIENT_ID = "client_id";
    public static final String KEY_ORDER_STATE_ID = "order_state_id";
    public static final String KEY_BILLER_ID = "biller_id";
    public static final String KEY_PROGRESS = "in_progress";

    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String KEY_EQ_VALUE = "eq_value";
    public static final String KEY_ORDER_ID = "order_id";
    public static final String KEY_SUBITEM_ID = "subitem_id";
    public static final String KEY_UNIT_PRICE = "unit_price";
    public static final String KEY_STORAGE_UNITS = "storage_units";
    public static final String KEY_STORAGE_NOTES = "storage_notes";
    public static final String KEY_UNITS = "units";
    public static final String KEY_FREE_UNITS = "free_units";
    public static final String KEY_PACKER_ID = "packer_id";
    public static final String KEY_ORDER_ITEMS_STATE_ID = "order_items_state_id";
    public static final String KEY_ITEM_ID = "item_id";
    public static final String KEY_DISCOUNT = "discount";
    public static final String KEY_TOTAL = "total";
    public static final String KEY_VALUE = "value";
    public static final String KEY_SUBITEM_NAME = "subitem_name";
    public static final String KEY_PAYMENT_NOTES = "payment_notes";

    public static final String TABLE_ORDER_ITEMS_STATE = "order_items_state";
    public static final String TABLE_ORDER_STATE = "order_state";

    public static final String TABLE_USER = "user";
    public static final String KEY_USER_NAME = "username";
    public static final String KEY_IDENTIFICATOR = "identificator";
    public static final String KEY_ROLE = "role";

    public static final String TABLE_SESSION = "session";
    public static final String KEY_USER_ID = "user_id";

    public static final String TABLE_PAYMENT = "payment";
    public static final String KEY_TERM = "term";

    public static final String TABLE_IP = "ip";
    public static final String KEY_IP_ADDRESS = "ip_address";

    public static final String TABLE_DIRECTORY = "directory";
    public static final String TABLE_VERSION = "version_t";
    public static final String KEY_DATE = "date";

    public static final String TABLE_FILTERS = "filters";

    public static final String TABLE_CLIENT_TYPE = "client_type";

    public static final String TABLE_CITY = "city";
    public static final String KEY_CITY = "city";

    public static final String TABLE_TEMP_ORDER_STATE = "temp_order_state";

    public static final String CREATE_TEMP_ORDER_ITEMS = "CREATE TABLE " + TABLE_TEMP_ORDER_ITEMS + " (" + KEY_ORDER_ID + " INTEGER, "
            + KEY_ITEM_ID + " INTEGER, " + KEY_SUBITEM_ID + " INTEGER, " + KEY_UNIT_PRICE + " DOUBLE NOT NULL, "
            + KEY_UNITS + " INTEGER NOT NULL, " + KEY_FREE_UNITS + " INTEGER NOT NULL, " + KEY_NOTES + " TEXT, "
            + KEY_PACKER_ID + " INTEGER, " + KEY_ORDER_ITEMS_STATE_ID + " INTEGER NOT NULL, " + KEY_IVA + " DOUBLE NOT NULL, "
            + KEY_DISCOUNT + " DOUBLE, " + KEY_TOTAL + " DOUBLE NOT NULL, " + KEY_VALUE + " DOUBLE NOT NULL, " + KEY_SUBITEM_NAME
            + " TEXT, " + KEY_STORAGE_UNITS + " INTEGER NOT NULL, " + KEY_STORAGE_NOTES + " TEXT, " + KEY_EQ_VALUE + " DOUBLE, " + " PRIMARY KEY " +
            "(" + KEY_ORDER_ID + ", " + KEY_ITEM_ID + ", " + KEY_SUBITEM_ID + "))";

    public static final String CREATE_FILTERS = "CREATE TABLE " + TABLE_FILTERS + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_BRAND_ID + " INTEGER, " + KEY_CATEGORY_ID + " INTEGER, " + KEY_NAME + " TEXT)";

    private static final String KEY_PATH = "path";

    public static String CREATE_TEMP_LOCATION = "CREATE TABLE " + TABLE_TEMP_LOCATION + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_LATITUDE + " DOUBLE NOT NULL, " + KEY_LONGITUDE + " DOUBLE NOT NULL, " + KEY_SELLER_ID + " INTEGER NOT NULL, "
            + KEY_CREATED + " TEXT NOT NULL)";

    public static String CREATE_TEMP_ORDER_STATE = "CREATE TABLE " + TABLE_TEMP_ORDER_STATE + "(" + KEY_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + KEY_MADE + " TEXT NOT NULL, " + KEY_DELIVER + " TEXT, " + KEY_MODIFIED + " TEXT NOT NULL, "
            + KEY_IS_SENT + " BOOLEAN NOT NULL, " + KEY_NOTES + " TEXT, " + KEY_PAYMENT_ID + " TEXT, "
            + KEY_CLIENT_ID + " INTEGER NOT NULL, " + KEY_SELLER_ID + " INTEGER NOT NULL, " + KEY_ORDER_STATE_ID + " INTEGER NOT NULL, "
            + KEY_BILLER_ID + " INTEGER, " + KEY_PROGRESS + " BOOLEAN NOT NULL, " + KEY_PAYMENT_NOTES + " TEXT )";


    public static String CREATE_CITY = "CREATE TABLE " + TABLE_CITY + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_CITY + " TEXT NOT NULL)";

    public static String CREATE_VERSION = "CREATE TABLE " + TABLE_VERSION + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_DATE + " TEXT NOT NULL)";

    public static String CREATE_DIRECTORY = "CREATE TABLE " + TABLE_DIRECTORY + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_PATH + " TEXT NOT NULL)";

    public static String CREATE_PAYMENT = "CREATE TABLE " + TABLE_PAYMENT + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_NAME + " TEXT NOT NULL, " + KEY_TERM + " INTEGER NOT NULL)";

    public static String CREATE_CLIENT_TYPE = "CREATE TABLE " + TABLE_CLIENT_TYPE + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_NAME + " TEXT NOT NULL)";

    public static String CREATE_BRAND = "CREATE TABLE " + TABLE_BRAND + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_NAME + " TEXT NOT NULL)";

    public static String CREATE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_NAME + " TEXT NOT NULL)";

    public static String CREATE_CLIENT = "CREATE TABLE " + TABLE_CLIENT + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_CODE + " INTEGER NOT NULL, " + KEY_COMPANY + " TEXT NOT NULL, " + KEY_ADDRESS + " TEXT NOT NULL, "
            + KEY_CITY_ID + " INTEGER NOT NULL, " + KEY_PHONE_ONE + " TEXT NOT NULL, " + KEY_PHONE_TWO + " TEXT, "
            + KEY_PHONE_THREE + " TEXT, " + KEY_NIT + " TEXT NOT NULL, " + KEY_MAIL + " TEXT NOT NULL, " + KEY_CONTACT + " TEXT NOT NULL, "
            + KEY_IS_SENT + " BOOLEAN NOT NULL, " + KEY_CLIENT_TYPE_ID + " INTEGER NOT NULL, " + KEY_NEIGHBORHOOD + " TEXT NOT NULL, "
            + KEY_IS_ACTIVE + " BOOLEAN NOT NULL, " + KEY_USER_ID + " INTEGER NOT NULL, " + KEY_LATITUDE + " DOUBLE NOT NULL, "
            + KEY_LONGITUDE + " DOUBLE NOT NULL)";

    public static String CREATE_ITEM = "CREATE TABLE " + TABLE_ITEM + " (" + KEY_ID + " INTEGER NOT NULL, "
            + KEY_NAME + " TEXT NOT NULL, " + KEY_IS_ACTIVE + " BOOLEAN NOT NULL, " + KEY_IS_NEW + " BOOLEAN NOT NULL, "
            + KEY_PRICE + " DOUBLE NOT NULL, " + KEY_BRAND_ID + " INTEGER NOT NULL, " + KEY_CATEGORY_ID + " INTEGER NOT NULL, "
            + KEY_SUBITEM_ID + " INTEGER NOT NULL, " + KEY_DISCOUNT_ONE + " DOUBLE NOT NULL, "
            + KEY_DISCOUNT_TWO + " DOUBLE NOT NULL, " + KEY_DISCOUNT_THREE + " DOUBLE NOT NULL, " + KEY_DISCOUNT_FOUR + " DOUBLE NOT NULL, "
            + KEY_DISCOUNT_FIVE + " DOUBLE NOT NULL, " + KEY_PAYMENT_ONE + " DOUBLE NOT NULL, " + KEY_PAYMENT_TWO + " DOUBLE NOT NULL, "
            + KEY_PAYMENT_THREE + " DOUBLE NOT NULL, " + KEY_PAYMENT_FOUR + " DOUBLE NOT NULL, " + KEY_IVA + " DOUBLE NOT NULL, "
            + "PRIMARY KEY (" + KEY_ID + ", " + KEY_SUBITEM_ID + "))";

    public static String CREATE_LOCATION = "CREATE TABLE " + TABLE_LOCATION + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_LATITUDE + " DOUBLE NOT NULL, " + KEY_LONGITUDE + " DOUBLE NOT NULL, " + KEY_SELLER_ID + " INTEGER NOT NULL, "
            + KEY_CREATED + " TEXT NOT NULL)";

    public static String CREATE_ORDER = "CREATE TABLE " + TABLE_ORDER + "(" + KEY_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + KEY_MADE + " TEXT NOT NULL, " + KEY_DELIVER + " TEXT, " + KEY_MODIFIED + " TEXT NOT NULL, "
            + KEY_IS_SENT + " BOOLEAN NOT NULL, " + KEY_NOTES + " TEXT, " + KEY_PAYMENT_ID + " TEXT, "
            + KEY_CLIENT_ID + " INTEGER NOT NULL, " + KEY_SELLER_ID + " INTEGER NOT NULL, " + KEY_ORDER_STATE_ID + " INTEGER NOT NULL, "
            + KEY_BILLER_ID + " INTEGER, " + KEY_PROGRESS + " BOOLEAN NOT NULL, " + KEY_PAYMENT_NOTES + " TEXT )";

    public static String CREATE_ORDER_ITEMS = "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" + KEY_ORDER_ID + " INTEGER, "
            + KEY_ITEM_ID + " INTEGER, " + KEY_SUBITEM_ID + " INTEGER, " + KEY_UNIT_PRICE + " DOUBLE NOT NULL, "
            + KEY_UNITS + " INTEGER NOT NULL, " + KEY_FREE_UNITS + " INTEGER NOT NULL, " + KEY_NOTES + " TEXT, "
            + KEY_PACKER_ID + " INTEGER, " + KEY_ORDER_ITEMS_STATE_ID + " INTEGER NOT NULL, " + KEY_IVA + " DOUBLE NOT NULL, "
            + KEY_DISCOUNT + " DOUBLE, " + KEY_TOTAL + " DOUBLE NOT NULL, " + KEY_VALUE + " DOUBLE NOT NULL, " + KEY_SUBITEM_NAME
            + " TEXT, " + KEY_STORAGE_UNITS + " INTEGER NOT NULL, " + KEY_STORAGE_NOTES + " TEXT, " + KEY_EQ_VALUE + " DOUBLE, "
            + " PRIMARY KEY (" + KEY_ORDER_ID + ", " + KEY_ITEM_ID + ", " + KEY_SUBITEM_ID + "))";

    public static String CREATE_ORDER_ITEMS_STATE = "CREATE TABLE " + TABLE_ORDER_ITEMS_STATE + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_STATE + " TEXT NOT NULL, " + KEY_HEX_COLOR + " TEXT)";

    public static String CREATE_ORDER_STATE = "CREATE TABLE " + TABLE_ORDER_STATE + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_STATE + " TEXT NOT NULL, " + KEY_HEX_COLOR + " TEXT)";

    public static String CREATE_USER = "CREATE TABLE " + TABLE_USER + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_USER_NAME + " TEXT NOT NULL, " + KEY_IDENTIFICATOR + " TEXT NOT NULL, " + KEY_CONTACT + " TEXT NOT NULL, "
            + KEY_IS_ACTIVE + " BOOLEAN NOT NULL, " + KEY_ROLE + " TEXT NOT NULL)";

    public static String CREATE_SESSION = "CREATE TABLE " + TABLE_SESSION + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_USER_ID + " INTEGER NOT NULL)";

    public static String CREATE_IP = "CREATE TABLE " + TABLE_IP + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_IP_ADDRESS + " TEXT NOT NULL)";


}
