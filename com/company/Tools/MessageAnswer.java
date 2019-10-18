package com.company.Tools;

import java.io.Serializable;

public class MessageAnswer implements Serializable {
    public MessageAnswer(int count, String information){
        this.count=count;
        this.information = new ClassFromString(information);
    }
    int count;
    ClassFromString information;

    public int getCount() {
        return count;
    }

    public MessageAnswer setCount(int count) {
        this.count = count;
        return this;
    }

    public String getInformation() {
        return information.getString();
    }

    public MessageAnswer setInformation(String information) {
        this.information.setString(information);
        return this;
    }
}
