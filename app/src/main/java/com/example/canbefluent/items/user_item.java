package com.example.canbefluent.items;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class user_item implements Serializable {
    String user_id;
    String user_pw;
    String year, month, day;
    String first_name, last_name;
    String sex;
    String profile_img;
    String native_lang1, native_lang2, practice_lang1, practice_lang2;
    String practice_lang1_level, practice_lang2_level;
    String result;
    double distance;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getNative_lang1() {
        return native_lang1;
    }

    public void setNative_lang1(String native_lang1) {
        this.native_lang1 = native_lang1;
    }

    public String getNative_lang2() {
        return native_lang2;
    }

    public void setNative_lang2(String native_lang2) {
        this.native_lang2 = native_lang2;
    }

    public String getPractice_lang1() {
        return practice_lang1;
    }

    public void setPractice_lang1(String practice_lang1) {
        this.practice_lang1 = practice_lang1;
    }

    public String getPractice_lang2() {
        return practice_lang2;
    }

    public void setPractice_lang2(String practice1_lang2) {
        this.practice_lang2 = practice1_lang2;
    }

    public String getPractice_lang1_level() {
        return practice_lang1_level;
    }

    public void setPractice_lang1_level(String practice_lang1_level) {
        this.practice_lang1_level = practice_lang1_level;
    }

    public String getPractice_lang2_level() {
        return practice_lang2_level;
    }

    public void setPractice_lang2_level(String practice_lang2_level) {
        this.practice_lang2_level = practice_lang2_level;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_pw() {
        return user_pw;
    }

    public void setUser_pw(String user_pw) {
        this.user_pw = user_pw;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public void setProfile_img(String profile_img) {
        this.profile_img = profile_img;
    }

}
