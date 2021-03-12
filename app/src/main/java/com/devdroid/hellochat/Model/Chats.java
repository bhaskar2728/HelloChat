package com.devdroid.hellochat.Model;

public class Chats {
    private String sender;
    private String receiver;
    private String message;
    private long time;
    private boolean isseen;

    public Chats(String sender,String receiver,String message,long time,Boolean isseen){
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.time = time;
        this.isseen = isseen;
    }
    public Chats(){

    }

    public long getTime(){

        return time;
    }
    public String getSender(){

        return  sender;
    }
    public String getReceiver(){

        return  receiver;
    }
    public String getMessage(){

        return message;
    }
    public  void setTime(long time){
        this.time = time;
    }
    public void setSender(String sender){
        this.sender = sender;
    }
    public void setReceiver(String receiver){
        this.receiver = receiver;
    }
    public void setAmount(String amount){
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
//test