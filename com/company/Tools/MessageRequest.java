package com.company.Tools;

import java.io.Serializable;
import com.company.lab5.*;

public class MessageRequest implements Serializable {

    public MessageRequest(String command, String address, int port, String LOGIN, String PASSWORD) {
        this.command = command;
        this.address = address;
        this.port = port;
        this.human = null;
        this.LOGIN = LOGIN;
        this.PASSWORD = PASSWORD;
    }
    public MessageRequest(String command, Human human, String address, int port, String LOGIN, String PASSWORD) {
        this.command = command;
        this.address = address;
        this.port = port;
        this.human = human;
        this.LOGIN = LOGIN;
        this.PASSWORD = PASSWORD;
    }

    private static final long serialVersionUID=1234567876;
    private String command;
    private Human human;
    private String address;
    private int port;
    private String LOGIN;
    private String PASSWORD;


    public String getCommand() {return command;}

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void setcommand(String command) {
        this.command = command;
    }

    public String getLOGIN() {
        return LOGIN;
    }

    public void setLOGIN(String LOGIN) {
        this.LOGIN = LOGIN;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public void setPASSWORD(String PASSWORD) {
        this.PASSWORD = PASSWORD;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Human getHuman() {return human;}

    public void setHuman(Human human) {this.human = human;}
}


