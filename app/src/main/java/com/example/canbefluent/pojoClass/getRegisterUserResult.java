package com.example.canbefluent.pojoClass;

import com.google.gson.annotations.SerializedName;

public class getRegisterUserResult {
    @SerializedName("result")
    private String result;

    @Override
    public String toString() {
        return  result;

    }
}
