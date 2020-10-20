package com.example.canbefluent.pojoClass;

import java.io.Serializable;

public class getRoomList implements Serializable {
    String first_name;
    String profile_img;
    String room_index;
    String last_message;
    String time;
    String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public void setProfile_img(String profile_img) {
        this.profile_img = profile_img;
    }

    public String getRoom_index() {
        return room_index;
    }

    public void setRoom_index(String room_index) {
        this.room_index = room_index;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
