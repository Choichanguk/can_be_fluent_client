package com.example.canbefluent.pojoClass;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class imgUploadResult {
    @SerializedName("result")
    @Expose
    private String result;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("path")
    @Expose
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
