package com.company.Tools;

import java.io.Serializable;

public class ClassFromString implements Serializable {
    ClassFromString(String s){

        this.string=s;
    }
    String string;

    public String getString() {

        return string;
    }

    public void setString(String string) {

        this.string = string;
    }
}
