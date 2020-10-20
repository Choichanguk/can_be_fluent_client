package com.example.canbefluent.pojoClass;

import java.io.Serializable;

public class getChatList implements Serializable {
    String msg;
    String user_id;
    String time;
    String result;
    String room_index;
    String profile_img;

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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
