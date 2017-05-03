package co.com.imagenybelleza.imagenybelleza.enums;

/**
 * Created by Juan Camilo Villa Amaya on 6/12/2016.
 * <p>
 * Contantes de las URI's de los servicios
 */

public class Url {

    public static final String HOST = "http://192.168.1.7/admin-hub/v1.1/";

    //Update password RESTAPI Url
    public static final String UPDATE_PASSWORD_SERVICE_URL = "users/update_pass";

    //OrderItems RESTAPI Urls
    public static final String UPDATE_ORDER_ITEMS_SERVICE_URL = "orders/items/update";

    //Client RESTAPI Urls
    public static final String GET_CLIENTS_SERVICE_URL = "clients/all";
    public static final String CREATE_CLIENTS_SERVICE_URL = "clients/add";
    public static final String CREATE_ALL_CLIENTS_SERVICE_URL = "clients/add_all";
    public static final String UPDATE_CLIENT_TYPE_SERVICE_URL = "clients/types/update";

    //Order RESTAPI Urls
    public static final String CREATE_ORDERS_SERVICE_URL = "orders/create";
    public static final String GET_STORAGE_ORDERS_SERVICE_URL = "orders/storage/all";

    //Basic dataset RESTAPI Urls
    public static final String GET_SERVICE_URL = "basic";
    public static final String CHECK_UPDATES_SERVICE_URL = "basic/update_check";
    public static final String UPDATE_SERVICE_URL = "basic/update";
    public static final String VERIFY_SERVICE_URL = "basic/verify";
    public static final String GET_MESSAGES_SERVICE_URL = "basic/messages";
    public static final String SET_READ_MESSAGES_URL = "basic/messages";

    //Count CDB records RESTAPI Url
    public static final String COUNT_SERVICE_URL = "count";

    //Bill RESTAPI Url
    public static final String FINISH_BILL_SERVICE_URL = "bill/finish";
    public static final String GET_UNBILLED_ORDERS_SERVICE_URL = "bill/unbilled";
    public static final String UPDATE_ORDERS_SERVICE_URL = "bill/update_all";
    public static final String UPDATE_ORDER_SERVICE_URL = "bill/update";

    //Location RESTAPI Urls
    public static final String GET_LOCATION_SERVICE_URL = "location/all";
    public static final String CREATE_LOCATION_SERVICE_URL = "location/create";
}
