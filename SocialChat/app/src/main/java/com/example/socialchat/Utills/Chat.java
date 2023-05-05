package com.example.socialchat.Utills;

public class Chat {
    private String sms, status, userID;
    public Chat(){

    }

    public Chat(String sms, String status, String userID) {
        this.sms = sms;
        this.status = status;
        this.userID = userID;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
