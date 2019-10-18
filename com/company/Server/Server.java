package com.company.Server;

import com.company.lab5.*;
import com.company.Tools.MessageAnswer;
import com.company.Tools.MessageRequest;
import com.company.Tools.SerialFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.sql.*;
import java.util.Properties;
import java.util.stream.Stream;

public class Server {
    private static Cave cave = new Cave(Weather.NORMAL, "???");
    private static String infoFile = "test.csv";
    public static String autoSave = "test.csv";
    private static DatagramChannel channel;
    private static Connection c;
    private static Statement stmt;

    public static void main(String[] args) {
        try {
            createTables();
            System.out.println(InetAddress.getLocalHost().toString().substring(InetAddress.getLocalHost().toString().indexOf("/") + 1));
            cave = SaverLoadCol.load(infoFile);
            SaverLoadCol.checkColAndBD(cave);
            waitPacket();
        } catch (SQLException e) {
            System.out.println("БД упала, при подключение");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void waitPacket() throws Exception {
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(1707));
        } catch (Exception e) {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(0));
            System.out.println("Предпологаймый порт занят, так что наш новый: " + channel.socket().getLocalPort());
        }
        try {
            while (true) {
                ByteBuffer buf = ByteBuffer.wrap(new byte[1024]);
                buf.clear();
                channel.receive(buf);
                new Thread(() -> answers(buf)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void answers(ByteBuffer buffer) {
        try {
            MessageRequest messageRequest = (MessageRequest) SerialFactory.unserialize(buffer.array());
            String answer = doCommand(messageRequest.getCommand(), messageRequest.getHuman(), messageRequest.getLOGIN(), messageRequest.getPASSWORD());
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
            int count;
            MessageAnswer messageAnswer = new MessageAnswer(1, answer);
            if (SerialFactory.serialize(messageAnswer).length % 1024 == 0)
                count = SerialFactory.serialize(messageAnswer).length / 1024;
            else
                count = (SerialFactory.serialize(messageAnswer).length / 1024) + 1;
            messageAnswer.setCount(count).setInformation(null);
            datagramPacket = new DatagramPacket(SerialFactory.serialize(messageAnswer), SerialFactory.serialize(messageAnswer).length, InetAddress.getByName(messageRequest.getAddress()), messageRequest.getPort()); //datagramPacket = new DatagramPacket(SerialFactory.serialize(messageAnswer), SerialFactory.serialize(messageAnswer).length, InetAddress.getByName("localhost"), messageRequest.getPort());
            socket.send(datagramPacket);
            byte[] bytesAnswer = SerialFactory.serialize(new MessageAnswer(count, answer));
            for (int i = 0; count > i; i++) {

                datagramPacket = new DatagramPacket(SerialFactory.getByteInfo1024(bytesAnswer, i), SerialFactory.getByteInfo1024(bytesAnswer, i).length, InetAddress.getByName(messageRequest.getAddress()), messageRequest.getPort());//datagramPacket = new DatagramPacket(SerialFactory.serialize(messageAnswer), SerialFactory.serialize(messageAnswer).length, messageRequest.getAddress(), messageRequest.getPort());
                socket.send(datagramPacket);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("База данный R.I.P.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static String doCommand(String comman, Human human, String LOGIN, String PASSWORD) throws SQLException {
        ResultSet rs;
        stmt=c.createStatement();
        String commanda;
        String argum = null;
        if (comman == null)
            return "Пришёл null";
        rs =stmt.executeQuery(String.format("select count(*) from spectators where LOGIN='%s' AND PASSWORD='%s';", LOGIN, PASSWORD));
        rs.next();
        comman = comman.trim();
        if (rs.getInt(1)!=1 && !comman.equals("login") && !comman.equals("registration")) {
            return "Вы не залогинены";
        }
        int spaceIndex = comman.indexOf(' ');
        if (spaceIndex == -1) {
            commanda = comman;
            argum = null;
        } else {
            commanda = comman.substring(0, spaceIndex);
            argum = comman.substring(spaceIndex + 1).trim();
        }
        if (commanda.length() == 0)
            return "Введите команду";

        switch (commanda) {
//            case "exit":
//                if (!cave.isRemarkMark())
//                    new File(autoSave).delete();
//                System.exit(0);
            case "login":
                stmt = c.createStatement();
                rs = stmt.executeQuery(String.format("SELECT count(*) FROM SPECTATORS WHERE LOGIN='%s' AND PASSWORD='%s';", LOGIN, PASSWORD));
                c.commit();
                rs.next();
                String ans;
                if (rs.getInt(1) == 1)
                    ans = "Вы успешно вошли";
                else
                    ans = "Логин или пароль введены не верно, попробуйте ещё раз";
                stmt.close();
                return ans;

            case "registration":
                stmt = c.createStatement();
                String newPassword = geniratePassword();
                rs = stmt.executeQuery(String.format("SELECT count(*) FROM SPECTATORS WHERE LOGIN='%s';", LOGIN));
                rs.next();
                if (rs.getInt(1)==1)
                    return "Эта почта уже зарегистрирована";
                if (sendMail(LOGIN, newPassword)){
                    rs = stmt.executeQuery("SELECT max(ID) FROM SPECTATORS;");
                    rs.next();
                    rs.getInt(1);
                    stmt.executeUpdate(String.format("INSERT INTO SPECTATORS VALUES (%s, '%s', '%s');", rs.getInt(1) + 1, LOGIN, SerialFactory.md5Custom(newPassword)));
                    return "Вы зарегистрировались. Првоерьте почту с паролем.";
                }else
                {
                    return "Вы обманщик, это не почта";
                }

            case "add":
                if (human == null)
                    return "Не указали аргумент";

                try {
                    addDB(human);
                    cave.add(human);
                    SaverLoadCol.save(autoSave, cave);
                    cave.setRemarkMark(true);
                    return "Создан человечек";
                } catch (Exception e) {
                    return e.getMessage();
                }

            case "add_if_max":
                if (human == null)
                    return "Не указан аргумент";
                try {
                    boolean make = cave.add_if_max(human);
                    addDB(human);
                    if (make) {
                        SaverLoadCol.save(autoSave, cave);
                        cave.setRemarkMark(true);
                        return "Создан человечек";
                    }
                    return "Не создан человек";
                } catch (Exception e) {
                    return e.getMessage();
                }

            case "show":
                return cave.show();

            case "info":
                return cave.getCollectionInfo();

            case "remove":
                if (human == null) {
                    return "Нет аргумента";
                }
                try {
                    if (cave.remove(human)) {
                        removeDB(human);
                        SaverLoadCol.save(autoSave, cave);
                        cave.setRemarkMark(true);
                        return "Человек ликвидирован";
                    }
                    return "Человек, принадлежащий тебе, не найден, для ликвидации";
                } catch (Exception e) {
                    return e.getMessage();
                }

            case "insert":
                if (human == null)
                    return "Нет аргумента";
                if (argum != null) {
                    try {
                        cave.insert(Integer.parseInt(argum), human);
                        addDB(human);
                        SaverLoadCol.save(autoSave, cave);
                        cave.setRemarkMark(true);
                        return "Человек добавлен по индексу (функции не работает, из-за условий на сортировку)";
                    } catch (NumberFormatException e) {
                        return "Вы ввели не число";
                    } catch (Exception e) {
                        return e.getMessage();
                    }
                } else
                    return "Вы ввели неверный аргумент";
            case "save":
                try {
                    if (argum == null) {
                        return "Я не знаю куда сохранять Т.Т, введите нормальное имя файла";
                    }
                    if (SaverLoadCol.save(argum, cave)) {
                        cave.setRemarkMark(false);
                        return "Йа усё сохраниль";
                    }
                    return "По этому адресу происходит другое сохранение, выебрите другое место";
                } catch (Exception e) {
                    return e.getMessage();
                }
            case "help":
                return "Вы можете использовать команды:\n\n" +
                        "show - вывести все элементы коллекции\n" +
                        "insert {int index} {element} - добавить элемент по индексу\n" +
                        "add_if_max {element} - добавить элемент если его значение больше любого из имеющихся\n" +
                        "add {element} - добавить элемент\n" +
                        "remove {element} - удалить первое вхождение подобного элемента\n" +
                        "info - вывести информацию о коллекции\n" +
                        "save {link} - сохранить коллекцию\n" +
                        "port - вывод порта пользователя" +
                        "help - вывести спосок команд и подсказок\n\n" +
                        "Подсказки:\n" +
                        "Любая команда заканчивается на ';'\n" +
                        "{element} - это элемент записанный в формате JSON\n" +
                        "{int index} - индекс аргумента" +
                        "\n\nПримеры аргументов: {\"name\": \"Jack\", \"clothes\": 22}\n" +
                        "{\"name\": \"Gab\", \"clothes\": 75, \"width\": 100, \"height\": 200}";

            default:
                return "Нет такой команды: \"" + comman + "\"\nПопробуйте снова или восользуйтесь командой \"help;\".";
        }
    }

    private static void addDB(Human human) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM SACRIFICES;");
        rs.next();
        int countSacrifices =rs.getInt(1)+1;
        rs.next();
        if (human.getClass() == Forwarder.class){
            rs = stmt.executeQuery("SELECT MAX(picture_id) FROM SACRIFICES;");
            rs.next();
            int countPicture = rs.getInt(1)+1;
            rs.next();
            stmt.executeUpdate(String.format
                    ("INSERT INTO SACRIFICES (ID,NAME,CLOTHES,DATA,CREATOR, picture_id) VALUES (%s, '%s', %s, '%s', '%s', %s );",
                            countSacrifices, human.getName(), human.getClothes(), Timestamp.from(human.getTimeCreate().toInstant()), human.getCreater(), countPicture));
            c.commit();
            stmt.executeUpdate(String.format
                    ("INSERT INTO PICTURE (ID_PICTURE,HIEGHT, WIEDTH) VALUES (%s, %s, %s);",
                            countPicture++, ((Forwarder)human).getFind().getHeight(), ((Forwarder)human).getFind().getWidth()));
            c.commit();
        }else{
            stmt.executeUpdate(String.format
                    ("INSERT INTO SACRIFICES (ID,NAME,CLOTHES,DATA,CREATOR) VALUES (%s, '%s', %s, '%s', '%s' );",
                            ++countSacrifices, human.getName(), human.getClothes(), Timestamp.from(human.getTimeCreate().toInstant()), human.getCreater()));
            c.commit();
        }
        rs.close();
    }
    private static void removeDB(Human human) throws SQLException {
        try {
            ResultSet rs = stmt.executeQuery(String.format("SELECT id, picture_id FROM SACRIFICES WHERE name = '%s' AND clothes =%s AND creator='%s' LIMIT 1;", human.getName(), human.getClothes(), human.getCreater()));
            rs.next();
            int id = rs.getInt(1);
            if (human.getClass() == Forwarder.class) {
                int picture_id = rs.getInt(2);
                stmt.executeUpdate(String.format("DELETE from PICTURE where ID_picture=%s;", picture_id));
                c.commit();
            }
            stmt.executeUpdate(String.format("DELETE from SACRIFICES where ID=%s;", id));
            c.commit();
            rs.close();
        }catch (Exception e){e.printStackTrace();}
    }

    private static void createTables() throws SQLException {
        c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/studs", "postgres", "root");
        c.setAutoCommit(false);
        stmt = c.createStatement();
        String sql;
        sql = "CREATE TABLE IF NOT EXISTS SPECTATORS " +
                "(ID INT PRIMARY KEY     NOT NULL," +
                "LOGIN           VARCHAR(64)    NOT NULL, " +
                "PASSWORD           VARCHAR(32))";
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS SACRIFICES" +
                "(ID                INT PRIMARY KEY NOT NULL," +
                "NAME               VARCHAR(64) NOT NULL," +
                "CLOTHES            INT NOT NULL," +
                "ID_PICTURE         INT," +
                "ID_USER            INT NOT NULL," +
                "CREATOR            VARCHAR(64) NOT NULL,"+
                "DATA               timestamp NOT NULL)";
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS PICTURE " +
                "(ID_PICTURE         INT PRIMARY KEY NOT NULL," +
                "HIEGHT             INT NOT NULL," +
                "WIEDTH             INT NOT NULL)";
        stmt.executeUpdate(sql);
        c.commit();
        stmt.close();
    }

    private static String geniratePassword() {
        String start = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        String finishString = "";
        while (finishString.length() < 12) {
            finishString += start.charAt((int) Math.round(61 * Math.random()));
        }
        return finishString;
    }

    private static boolean sendMail(String email, String password) {
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream("mail.properties"));
            Session mailSession = Session.getDefaultInstance(properties);
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(properties.getProperty("mail.smtps.user")));

            message.addRecipients(Message.RecipientType.TO, email);
            message.setSubject("Я есть lab7");
            message.setText(password);
            Transport transport = mailSession.getTransport();
            transport.connect(null, properties.getProperty("password"));
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            return true;
        } catch (SendFailedException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
