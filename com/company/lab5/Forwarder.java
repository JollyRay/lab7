package com.company.lab5;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

public class Forwarder extends Human implements FaorwarderOpportunities, Serializable {
    private Picture lastFind;

    public Picture getLastFind() {
        return lastFind;
    }

    public void setLastFind(Picture lastFind) {
        this.lastFind = lastFind;
    }

//    public Forwarder(String name) {
//        super(name);
//    }
    public Forwarder(String name, int cloth, ZonedDateTime timeCreate, String creater){super(name,cloth,timeCreate,creater);}

    @Override
    public int hashCode() {
        return Objects.hash(lastFind); }
    public String toString() {
        return String.format("Экспедитор, имя = %s, одежда = %s, находил картину размером = %s, был создан = %s в %s",
                getName(), getClothes(), getFind().getHeightWidth(), getCreater() ,getTimeCreate());
    }

    @Override
    public String stockingUp() {
        return "Мы запаслись";
    }

    @Override
    public String stockingUp(DifferentItems item) {
        return "Мы взяли с собой " + item.name();
    }

    @Override
    public String hope() throws GodException {
        if (Math.random() > 0.95) {
            throw new GodException();
        } return "Надеются, что абстрактый бог номера 5(Братчиков) поможет им выжить";
    }

    @Override
    public String landing() {
        return "Самолёт успешно приземлилсь";
    }

    @Override
    public Picture toMakePhoto() {
        int width=(int)Math.round(Math.random()*10000);
        int height=(int)Math.round(Math.random()*10000);
        return new Picture(width, height);
    }

    @Override
    public Picture paint(){
        int width=(int)Math.round(Math.random()*2000);
        int height=(int)Math.round(Math.random()*2000);
        return new Picture(width, height);
    }

    @Override
    public Picture takeSamples() {
        return new Picture(5, 10);
    }


    @Override
    public Picture getFind(){
        return lastFind;
    }

    @Override
    public void setFind(Picture find){lastFind=find;}

    @Override
    public String Go(){return "Я иду";}

    @Override
    public String Go(String str){return "Я иду в направление";}
}
