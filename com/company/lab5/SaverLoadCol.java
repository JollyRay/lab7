package com.company.lab5;

import com.company.Server.Server;

import java.io.*;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

public class SaverLoadCol {

    private static LinkedList<String> arrayOfWay= new LinkedList<>();
    /**
     * Сохраняет коллекции в файл
     * @param way путь к файлу
     * @param cave сама коллекция
     * @throws Exception если при сохранение возникли ошибки. Смотри сообщения.
     */
    public static boolean save(String way, Cave cave) throws Exception{
        if (addInList(way)) {
            File file = new File(way);

            if (file.isDirectory()) {
                throw new IllegalArgumentException("Это директория, а не файл");
            }
            file.createNewFile();
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.append((cave.getName())).append(", ").append(cave.getWeather().toString()).append(", ").append(Long.toString(cave.getDate().getTime())).append('\n');
            for (Human human : cave.getList()) {
                writer.append(human.getName()).append(", ").append(Integer.toString(human.getClothes()));
                if (human instanceof Forwarder) {
                    writer.append(", ").append(((Forwarder) human).getFind().getHeightWidth());
                }
                writer.append(", ").append(human.getTimeCreate().toString()).append(", ").append(human.getCreater()).append("\n");
            }
            cave.setRemarkMark(false);
            writer.close();
            removeInList(way);
            return true;
        }
        return false;
    }

    /**
     * Загрузка коллекции из файла
     * @param way путь к файлу
     * @return возвращает коллецию
     * @throws Exception выкидывает исключение, если что-то идёт не так. Смотри сообщения.
     */
    public static Cave load(String way) throws Exception{
        File file = new File(way);
        file.createNewFile();
        Scanner scan = new Scanner(file);
        String hat;
        if (scan.hasNextLine()) {
            hat = scan.nextLine();
            if (!hat.contains(","))
                throw new Exception("Шапка указана не верно");
            String name = hat.substring(0, hat.indexOf(",")).trim();
            Weather weather;
            Date date;
            hat = hat.substring(hat.indexOf(",") + 1);
            if (!hat.contains(","))
                throw new Exception("Шапка указана не верно");
            try {
                weather = Weather.valueOf((hat.substring(0, hat.indexOf(",")).trim()));
                date = new Date(Long.parseLong(hat.substring(hat.indexOf(",") + 1).trim()));
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Загрузка файла не удалась, погода в шапке указана не верно");
            } catch (Exception e) {
                throw new Exception("Загрузка файла не удалась, дата в шапке указана неверно");
            }
            Cave cave = new Cave(weather, name);
            cave.setDate(date);
            int clothes, hieght, width;
            ZonedDateTime time;
            while (scan.hasNextLine()) {
                hat = scan.nextLine().trim();
                if (hat.isEmpty())
                    continue;
                String[] masStats = hat.split(",");
                if (masStats.length == 4) {
                    name = masStats[0].trim();
                    try {
                        clothes = Integer.parseInt(masStats[1].trim());
                    } catch (Exception e) {
                        throw new Exception("Неверно задана одетость");
                    }
                    try {
                        time = ZonedDateTime.parse(masStats[2].trim());
                    } catch (Exception e) {
                        throw new Exception("Неверно задана время");
                    }
                    String login = masStats[3].trim();


                    cave.add(HumanMaker.makeHuman("{\"name\": \""+name+"\", \"clothes\": "+clothes+",\"time\": \""+time+
                            "\"}", login));
                } else if (masStats.length == 6) {
                    name = masStats[0].trim();
                    String login = masStats[5].trim();
                    try {
                        clothes = Integer.parseInt(masStats[1].trim());
                        time = ZonedDateTime.parse(masStats[4].trim());
                        width = Integer.parseInt(masStats[3].trim());
                        hieght = Integer.parseInt(masStats[2].trim());
                    } catch (Exception e) {
                        throw new Exception("Неверно заданы характеристики экспедитора");
                    }
                    Forwarder forw = new Forwarder(name, clothes, time, login);
                    forw.setFind(new Picture(hieght, width));
                    cave.add(forw);
                } else
                    throw new Exception("Просто всё не правельно, укажите нормальный файл, где нормальные объекты");

            }
            scan.close();
            cave.setRemarkMark(false);
            return cave;
        }
        return new Cave(Weather.NORMAL, "Desperation");
    }

    public static void checkColAndBD(Cave cave){
        try(Connection c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/studs", "postgres", "root");  Statement stmt = c.createStatement()){
        c.setAutoCommit(false);
        CopyOnWriteArrayList<Human> arrayList = (CopyOnWriteArrayList)cave.getList().clone();
        HashMap<Integer, Picture> mapPicture = new HashMap();
        ResultSet rs = stmt.executeQuery( "SELECT * FROM picture;");
        while (rs.next()){
            int id = rs.getInt("id_picture");
            int hieght = rs.getInt("hieght");
            int wiedth = rs.getInt("wiedth");
            mapPicture.put(id, new Picture(hieght, wiedth));
        }

        rs = stmt.executeQuery( "SELECT * FROM sacrifices;" );
        while ( rs.next() ) {
            String  name = rs.getString("name");
            int clothes  = rs.getInt("clothes");
            int picture_id = rs.getInt("picture_id");
            String login = rs.getString("creator");
            ZonedDateTime time = ZonedDateTime.of(rs.getTimestamp("data").toLocalDateTime().toLocalDate(),
                    rs.getTimestamp("data").toLocalDateTime().toLocalTime(), ZoneId.of("Europe/Moscow"));
            if (picture_id==0){
                Human hum = HumanMaker.makeHuman("{\"name\": \""+name+"\", \"clothes\": "+clothes+",\"time\": \""+time+
                        "\"}", login);
                if (arrayList.contains(hum))
                    arrayList.remove(hum);
                else
                    cave.add(hum);
                    SaverLoadCol.save(Server.autoSave, cave);
                    cave.setRemarkMark(true);
            }
            else
            {
                Forwarder forw = new Forwarder(name, clothes, time, login);
                forw.setFind(mapPicture.get(picture_id));
                if (arrayList.contains(forw))
                    arrayList.remove(forw);
                else
                    cave.add(forw);
                    SaverLoadCol.save(Server.autoSave, cave);
                    cave.setRemarkMark(true);
            }
        }
        rs = stmt.executeQuery("SELECT MAX(id) FROM SACRIFICES;");
        rs.next();
        int countSacrifices = rs.getInt(1);
        rs.next();
        rs = stmt.executeQuery("SELECT MAX(picture_id) FROM SACRIFICES;");
        rs.next();
        int countPicture = rs.getInt(1)+1;
        rs.next();


        while(arrayList.size()>0){
            Human hum = arrayList.get(0);
            String name = hum.getName();;
            int clothes = hum.getClothes();;
            String login = hum.getCreater();;
            Timestamp time= Timestamp.from(hum.getTimeCreate().toInstant());
            if (hum.getClass() == Forwarder.class){
                Picture pic = ((Forwarder)hum).getFind();
                stmt.executeUpdate(String.format
                        ("INSERT INTO SACRIFICES (ID,NAME,CLOTHES,DATA,CREATOR, picture_id) VALUES (%s, '%s', %s, '%s', '%s', %s );",
                                ++countSacrifices, name, clothes, time, login, countPicture));
                c.commit();
                stmt.executeUpdate(String.format
                        ("INSERT INTO PICTURE (ID_PICTURE,HIEGHT, WIEDTH) VALUES (%s, %s, %s);",
                                countPicture++, pic.getHeight(), pic.getWidth()));
                c.commit();
            }else{
                stmt.executeUpdate(String.format
                        ("INSERT INTO SACRIFICES (ID,NAME,CLOTHES,DATA,CREATOR) VALUES (%s, '%s', %s, '%s', '%s' );",
                                ++countSacrifices, name, clothes, time, login));
                c.commit();
            }
            arrayList.remove(0);
        }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    static String createheAutoSavePath(String autoSave){
        int number = 2;
        if (new File(autoSave).exists() || new File(autoSave).isDirectory()) {
            while (new File(autoSave + number).exists() || new File(autoSave + number).isDirectory())
                number++;
            autoSave = autoSave + number + ".csv";
        }
        else
            autoSave = autoSave+".csv";
        try {
            File autoSaveFile = new File(autoSave);
            autoSaveFile.createNewFile();
            if (!autoSaveFile.canRead()) {
                System.out.println("Нет доступа к дирекктории, я отказываюсь рабоать в таких условиях");
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return autoSave;
    }

    static String createInfoFilePath(String infoFile){
        String nodoTak;
        if (infoFile.lastIndexOf(".csv")!=4){
            System.out.println("Ваш файл не csv, но мы это сейчас подправим");
            if (infoFile.lastIndexOf(".")==-1)
                nodoTak=infoFile+infoFile+".csv";
            else
                nodoTak=infoFile.substring(0, infoFile.lastIndexOf("."))+".csv";
            if (new File(infoFile).exists())
            try{
                new File(nodoTak).createNewFile();
                FileReader fileReader = new FileReader(infoFile);
                FileWriter fileWriter = new FileWriter(nodoTak);
                int ch = fileReader.read();
                while(ch != -1) {
                    fileWriter.append((char) ch);
                    ch = fileReader.read();
                }
                fileWriter.close();
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            infoFile=nodoTak;
        }
        return infoFile;
    }
    static synchronized boolean addInList(String way){
        if (arrayOfWay.contains(way))
            return false;
            arrayOfWay.add(way);
            return true;
    }
    static synchronized void removeInList(String way){
        arrayOfWay.remove(way);
    }

}
