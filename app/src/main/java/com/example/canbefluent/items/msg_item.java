package com.example.canbefluent.items;

import java.io.Serializable;
import java.util.ArrayList;

public class msg_item implements Serializable {
    String message;
    String type;
    String token;
    String room_index;
    String user_index;
    String first_name;
    String status;
    String profile_img;
    ArrayList<String> img_list;
    long time;

    public ArrayList<String> getImg_list() {
        return img_list;
    }

    public void setImg_list(ArrayList<String> img_list) {
        this.img_list = img_list;
    }

    private static final long serialVersionUID = 1L;

    public String getProfile_img() {
        return profile_img;
    }

    public void setProfile_img(String profile_img) {
        this.profile_img = profile_img;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRoom_index() {
        return room_index;
    }

    public void setRoom_index(String room_index) {
        this.room_index = room_index;
    }

    public String getUser_index() {
        return user_index;
    }

    public void setUser_index(String user_index) {
        this.user_index = user_index;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
