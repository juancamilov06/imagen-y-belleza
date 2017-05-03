package co.com.imagenybelleza.imagenybelleza.enums;

/**
 * Created by Juan Camilo Villa Amaya on 6/12/2016.
 * <p>
 * Contantes de las URI's de los servicios
 */

public class Url {

    public static final String HOST = "http://192.168.1.7";

    //Update password RESTAPI Url
    public static final String UPDATE_PASSWORD_SERVICE_URL = "/admin-hub/users/update_pass";

    //OrderItems RESTAPI Urls
    public static final String UPDATE_ORDER_ITEMS_SERVICE_URL = "/admin-hub/orders/items/update";

    //Client RESTAPI Urls
    public static final String GET_CLIENTS_SERVICE_URL = "/admin-hub/clients/all";
    public static final String CREATE_CLIENTS_SERVICE_URL = "/admin-hub/clients/add";
    public static final String CREATE_ALL_CLIENTS_SERVICE_URL = "/admin-hub/clients/add_all";
    public static final String UPDATE_CLIENT_TYPE_SERVICE_URL = "/admin-hub/clients/types/update";

    //Order RESTAPI Urls
    public static final String CREATE_ORDERS_SERVICE_URL = "/admin-hub/orders/create";
    public static final String GET_STORAGE_ORDERS_SERVICE_URL = "/admin-hub/orders/storage/all";

    //Basic dataset RESTAPI Urls
    public static final String GET_SERVICE_URL = "/admin-hub/basic";
    public static final String CHECK_UPDATES_SERVICE_URL = "/admin-hub/basic/update_check";
    public static final String UPDATE_SERVICE_URL = "/admin-hub/basic/update";
    public static final String VERIFY_SERVICE_URL = "/admin-hub/basic/verify";
    public static final String GET_MESSAGES_SERVICE_URL = "/admin-hub/basic/messages";
    public static final String SET_READ_MESSAGES_URL = "/admin-hub/basic/messages";

    //Count CDB records RESTAPI Url
    public static final String COUNT_SERVICE_URL = "/admin-hub/count";

    //Bill RESTAPI Url
    public static final String FINISH_BILL_SERVICE_URL = "/admin-hub/bill/finish";
    public static final String GET_UNBILLED_ORDERS_SERVICE_URL = "/admin-hub/bill/unbilled";
    public static final String UPDATE_ORDERS_SERVICE_URL = "/admin-hub/bill/update_all";
    public static final String UPDATE_ORDER_SERVICE_URL = "/admin-hub/bill/update";

    //Location RESTAPI Urls
    public static final String GET_LOCATION_SERVICE_URL = "/admin-hub/location/all";
    public static final String CREATE_LOCATION_SERVICE_URL = "/admin-hub/location/create";
}
