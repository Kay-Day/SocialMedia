package com.example.socialchat.Utills;

public class Users {
    private String username,city,country,profession,profileImage,status;

    public Users(){

    }

    public Users(String username, String city, String country, String profession, String profileImage, String status) {
        this.username = username;
        this.city = city;
        this.country = country;
        this.profession = profession;
        this.profileImage = profileImage;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
