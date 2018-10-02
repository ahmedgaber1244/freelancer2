package com.example.gaber.freelancer2;

/**
 * Created by gaber on 12/08/2018.
 */

public class user_data_model {


    public String name;
    public String token;
    public String pass;
    public String image_url;
    public String status;



    // Create table SQL query

    public user_data_model(){

    }

    public user_data_model(String name, String token,String pass,String image_url,String status) {
        this.name = name;
        this.token = token;
        this.pass = pass;
        this.image_url=image_url;
        this.status=status;
    }

}
