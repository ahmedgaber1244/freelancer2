package com.example.gaber.freelancer2;

/**
 * Created by gaber on 12/08/2018.
 */

public class data_model {
    public static final String TABLE_NAME = "chat";

    public static final String to_sql = "to_sql";
    public static final String from_sql = "from_user";
    public static final String message_sql = "message";
    public static final String type_sql = "message_type";
    public static final String time_sql = "message_time";
    public static final String storage_url_sql = "storage_url";


    public String from;
    public String to;
    public String message;
    public String type;
    public String time;
    public String storage_url;



    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + from_sql + " TEXT ,"
                    + message_sql + " TEXT,"
                    + type_sql + " TEXT,"
                    + time_sql + " TEXT,"
                    + storage_url_sql + " TEXT,"
                    +to_sql+" TEXT"
                    + ")";



    public data_model(String from,String to,String message,String type,String time,String storage_url) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.type = type;
        this.time = time;
        this.storage_url = storage_url;
    }
    public data_model() {

    }

}
