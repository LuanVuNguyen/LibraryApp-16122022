package com.example.libraryapp.common;

public class Config {
    public static String HTTP_SERVER_SHOP = "http://192.168.1.246:8069";   // Connect API server TONY THINH
   // private static final String HTTP_SERVER_SHOP = "http://192.168.1.244:8027";   // Connect API server TONY THINH
    /**
     * Method POST
     */
    public static final String METHOD_POST = "POST";

    /**
     * Property key
     */
    public static final String PROPERTY_KEY = "Content-Type";

    /**
     * Property value
     */
    //static final String PROPERTY_VALUE = "application/x-www-form-urlencoded";
    public static final String PROPERTY_VALUE = "application/json";

    /**
     * Property value post file
     */
    public static final String PROPERTY_VALUE_POST_FILE = "multipart/form-data";

    /**
     * Api key
     */
    public static final String API_KEY = "api_key";
    /**
     * Api key value
     */
    public static final String API_KEY_VALUE = "aip_rtsa_20220516_1";
    /**
     * API code login
     */
    public static final String CODE_LOGIN = "1";

    /**
     * #HUYNHQUANGVINH API RFID TO JAN
     */
    //public static final String API_RFID_TO_JAN = HTTP_SERVER_SHOP + "/api/v2/rfids_to_jans";
    public static final String API_ODOO_GETBOOK = "/library_controller/get_book";
    public static final String API_ODOO_GETMEMBER = "/library_controller/get_member";
    public static final String API_ODOO_ISSUEBOOK = "/library_controller/issue";
    public static final String API_ODOO_GETMULTIPLEPRODUCT = "/inventory_controller/get_quant";
    public static final String API_ODOO_CREATEINVENTORY = "/inventory_controller/create_fileinventory";
    public static final String API_ODOO_GETINFOPRODUCT = "/inventory_controller/get_info_product";
    public static final String API_ODOO_REGISTERRFID = "/inventory_controller/register_product";
    public static final String API_ODOO_CREATENEWPRODUCT = "/inventory_controller/creat_new_product";




}
