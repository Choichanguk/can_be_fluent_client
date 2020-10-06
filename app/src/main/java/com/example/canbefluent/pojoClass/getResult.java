package com.example.canbefluent.pojoClass;

import com.google.gson.annotations.SerializedName;

public class getResult {
    @SerializedName("result")
    private String result;

    @Override
    public String toString() {
        return  result;
    }
}
