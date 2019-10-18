package com.company.lab5;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cave {
    public Cave(Weather weather, String name){
        this.weather = weather;
        this.name = name;
    }
    private boolean RemarkMark = false;
    private Date date = new Date();
    private String name;
    private Weather weather;
    private CopyOnWriteArrayList<Human> list = new CopyOnWriteArrayList<>();

    /**
     *Добавить человек в список жертв
     * @param hum человек который попадёт в список
     */
    public void add(Human hum){
        list.add(hum);

        RemarkMark= true;
    }

    /**
     * Добавить человека в список жертв, если его значение больше остальных находящихся в списке
     * @param hum человек который попадёт в список
     * @return true, если вышло
     */
    public boolean add_if_max(Human hum){
        int maxim = Stream.concat(Stream.of(Integer.MIN_VALUE), list.stream().map(s -> s.getClothes())).max(Comparator.naturalOrder()).get();

        if (maxim < hum.getClothes()) {
            list.add(hum);
            RemarkMark= true;
            return true;
        }
        return false;
    }

    public void setDate(Date date) {
        this.date = date;
        RemarkMark = true;
    }

    public Date getDate() {
        return date;
    }

    /**
     * Дарует общую информацию о коллекции
     * @return строку, содержащую общую информацию о коллекции
     */
    public String getCollectionInfo() {
        return "Пещера, использует коллекцию типа " + list.getClass().getName() + ",\n" +
                "называется " + name + ",\n" +
                "создана " + date + ",\n" +
                "погода в пещере " + weather + ",\n" +
                "содержит " + list.size() + " жертв";
    }

    /**
     * Выводит информацию о людях, что уже в коллекции
     * @return возвращет нумерованный список людей, которые уже есть в коллеции с информацией о них
     */
    public String show(){
        StringBuilder builder = new StringBuilder();
        int i=0;
        Collection<Human> collect = list.stream().sorted((s1 , s2)-> String.CASE_INSENSITIVE_ORDER.compare(s1.getName(), s2.getName())).collect(Collectors.toList());
        if (collect.size()!=0){
            for (Iterator<Human> iter = collect.iterator(); iter.hasNext(); ) {
                builder.append(i++ + ". " + iter.next().toString()+";");
                if (iter.hasNext())
                    builder.append("\n");
            }
            return builder.toString();
        }
        return "Коллекция пуста";
    }

    /**
     * Добавляет элемент на место с указанным индексом
     * @param i сам индекс
     * @param human человек, которого мы хотим добавить
     * @throws IndexOutOfBoundsException ошибка вылезающая, если элемент с таким индексом нельзя создать
     */
    public void insert(int i, Human human) throws IndexOutOfBoundsException{
        if (i<=list.size() && i>-1){
            list.add(i, human);
            RemarkMark= true;}
        else
            throw new IndexOutOfBoundsException("Аргумент с таким индексом невозможнос создать");
    }

    /**
     * Удаляет человека из коллекции
     * @param human человек образец, которого хотим удалить
     * @return true, если вышло
     */
    public boolean remove(Human human){
        RemarkMark= true;
        return list.remove(human);
    }

    public CopyOnWriteArrayList<Human> getList() {
        return list;
    }
    public void setList(CopyOnWriteArrayList<Human> list) {
        this.list = list;
    }

    public String getName() {
        return name;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setRemarkMark(boolean remarkMark) {
        RemarkMark = remarkMark;
    }

    public boolean isRemarkMark() {
        return RemarkMark;
    }
}
