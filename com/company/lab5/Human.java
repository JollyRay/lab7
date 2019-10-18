package com.company.lab5;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

public abstract class Human implements HumanOpportunities, Comparable<Human>, Serializable {
    private String name = "Безымянный";
    private int clothes;
    private ZonedDateTime timeCreate;
    private String creater;

    Human(String name, int clothes, ZonedDateTime timeCreate, String creater) {
        this.name = name;
        this.clothes = clothes;
        this.timeCreate = timeCreate;
        this.creater = creater;
    }

//    Human(String name) {
//        this(name, 0);
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String see(String what){
        return "я видел " + what;
    }

    @Override
    public void takeOffClothes(int i){
        clothes -= i;
    }

    @Override
    public void putOnClothes(int i){
        clothes += i;
    }

    @Override
    public String degreeOfDress() throws ClothingException {
        if (clothes < 0)
            throw new ClothingException();
        return "я одето на " + clothes + "%";
    }

    @Override
    public String swear(){
        return "Совершишь богохульство.";
    }

    public abstract String Go();
    public abstract String Go(String str);

    @Override
    public String toString() {
        String data=timeCreate.toString();
        String[] s= data.substring(0, data.indexOf(":")+2).split("T");

        return String.format("Человечек, имя = %s, одежда = %s, был создан = %s в %s", name, clothes, creater, s[0]+" "+s[1]);
    }

    public int getClothes() {
        return clothes;
    }

    public void setClothes(int clothes) {
        this.clothes = clothes;
    }

    @Override
    public int compareTo(Human o) {
        return getClothes() - o.getClothes();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass()) return false;
        if (this == o) return true;
        Human c = (Human) o;

        return  c.getName().equals(getName()) &&
                c.getClothes() == getClothes() &&
                c.creater.equals(getCreater());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, clothes);
    }
    public ZonedDateTime getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(ZonedDateTime timeCreate) {
        this.timeCreate = timeCreate;
    }

    public String getCreater() {
        return creater;
    }

    public void setCreater(String creater) {
        this.creater = creater;
    }
}
