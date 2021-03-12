package com.devdroid.hellochat.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String id;
    private String username;
    private String phone;
    private String email;
    private String status;
    private String profileUrl;





    public User(String id,String username,String phone,String email,String status,String profileUrl){
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.profileUrl = profileUrl;
    }
    public User(){

    }

    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        phone = in.readString();
        email = in.readString();
        status = in.readString();
        profileUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
    public String getEmail(){
        return email;
    }
    public String getID(){
        return id;
    }
    public  String getUsername(){
        return username;
    }
    public String getPhone(){
        return phone;
    }
    public void setID(String id){
        this.id = id;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(status);
        dest.writeString(profileUrl);
    }
}
