package com.example.gaber.freelancer2;

/**
 * Created by gaber on 12/08/2018.
 */

public class friend_data_model {


    public String name;
    public String image;
    public String token;



    // Create table SQL query

    public friend_data_model(){

    }

    public friend_data_model(String name, String image,String token) {
        this.name = name;
        this.image = image;
        this.token = token;
    }

}
