package com.example.canbefluent.pojoClass;

import com.google.gson.annotations.SerializedName;

public class getLanguageNameResult {

    @SerializedName("lang_name")
    private String name;

    @Override
    public String toString() {
        return  name;
    }

}
