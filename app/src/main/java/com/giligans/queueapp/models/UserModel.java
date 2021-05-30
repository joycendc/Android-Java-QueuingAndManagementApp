package com.giligans.queueapp.models;

public class UserModel {
    String id;
    String fname;
    String lname;
    String mobile;
    String email;

    public UserModel(String id, String fname, String lname, String mobile, String email) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.mobile = mobile;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}