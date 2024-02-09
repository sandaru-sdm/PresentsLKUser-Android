package com.sdm.presentslk.model;

public class ReadWriteUserDetails {
    public String email, doB, mobile, address, userType;

    //Constructor
    public ReadWriteUserDetails(){}

    public ReadWriteUserDetails(String textEmail, String textDoB, String textMobile, String textAddress, String textUserType){
        this.email = textEmail;
        this.doB = textDoB;
        this.mobile = textMobile;
        this.address = textAddress;
        this.userType = textUserType;
    }
}
