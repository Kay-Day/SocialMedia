package com.example.socialchat.Utills;

public class Friends {
    private String profession,profileImageUrl,username;

    public Friends(String profession, String profileImageUrl, String username) {
        this.profession = profession;
        this.profileImageUrl = profileImageUrl;
        this.username = username;
    }
    public Friends(){

    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
