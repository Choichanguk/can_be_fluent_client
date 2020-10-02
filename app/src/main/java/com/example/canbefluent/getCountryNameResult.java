package com.example.canbefluent;

import com.google.gson.annotations.SerializedName;

public class getCountryNameResult {

    @SerializedName("name")
    private String name;

    @Override
    public String toString() {
        return  name;

    }

}
