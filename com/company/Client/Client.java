package com.company.Client;

import com.company.Tools.MessageAnswer;
import com.company.Tools.MessageRequest;
import com.company.Tools.SerialFactory;
import com.company.lab5.Human;
import com.company.lab5.HumanMaker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client {
    private static String LOGIN;
    private static String PASSWORD="";
    private static String IP;
    private static MessageRequest messageRequest;
    private static int serverPort = 1707;
    private static String answer="";
    public static void main(String[] args) throws Exception{
        BufferedReader readerLogin = new BufferedReader(new InputStreamReader(System.in));
        boolean readyMessege;
        String command="";
        String argument="";
        Human human = null;
        DatagramChannel channel = DatagramChannel.open ();
        try {
            IP = args[0].trim();
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            IP=InetAddress.getLocalHost().toString().substring(InetAddress.getLocalHost().toString().indexOf("/")+1);
        }
        System.out.println("Любая команда заканчивается \";\"");
        DatagramSocket serverSocket = new DatagramSocket(0);
        String newData;
        logining:
        while(true)
        {
            System.out.println("Залогиньтесь с помощью команду \"login\" или зарегистируйтесь с помощью команды" +
                    " \"registration\"");
            newData = CommandReader().trim();
            switch (newData){
                case "login":
                    System.out.print("Ввидете логин: ");
                    LOGIN = readerLogin.readLine();
                    System.out.print("Ввидете пароль: ");
                    PASSWORD = readerLogin.readLine();
                    command = newData.trim();
                    answer = sendMessegeForServer(true, command, null, serverSocket, channel);
                    System.out.println(answer);
                    if (answer.equals("Вы успешно вошли"))
                        break logining;
                    break;
                case "registration":
                    System.out.print("Ввидете логин: ");
                    LOGIN = readerLogin.readLine();
                    command = newData;
                    answer = sendMessegeForServer(true, command, null, serverSocket, channel);
                    System.out.println(answer);
                    break;
                default:
                    System.out.println("Вы где-то ошиблись, попроубйте снова");
            }

        }
        while (true) {
            readyMessege = false;
            System.out.print("--->");
            newData = CommandReader().trim();
            if (newData == null)
                System.exit(0);
            int spaceIndex = newData.indexOf(" ");
            if (spaceIndex == -1){
                command = newData;
            }else
            {
                command=newData.substring(0, spaceIndex);
                argument=newData.substring(spaceIndex+1).trim();
            }
            switch (command){
                case "exit":
                    System.exit(0);
                case "setport":
                    System.out.println("Порт сервера сменён на "+argument);
                    serverPort = Integer.parseInt(argument);
                    break;
                case "port":
                    System.out.println("port=" + serverSocket.getLocalPort());
                    break;
                case "add_if_max":
                case "add":
                case "remove":
                    if (spaceIndex==-1){
                        System.out.println("Вы не ввели аргумент");
                    }else{
                        try{
                            human = HumanMaker.makeHuman(argument, LOGIN);
                            readyMessege= true;
                        }
                        catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                    break;
                case "insert":
                    if (spaceIndex==-1){
                        System.out.println("Вы не ввели аргумент");
                    }else{
                        try{
                            int secondSpaceIndex = Integer.parseInt(argument.substring(0, argument.indexOf(' ')));
                            if (secondSpaceIndex!=-1) {
                                command +=" "+argument.substring(0, secondSpaceIndex);
                                argument = argument.substring(secondSpaceIndex+1).trim();
                                human = HumanMaker.makeHuman(argument, LOGIN);
                                readyMessege= true;
                            }
                        }
                        catch (NumberFormatException e){
                            System.out.println("Вы ввели не число");
                        }
                        catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                    break;
                case "show":
                case "info":
                case "help":
                    human=null;
                    readyMessege= true;
                    break;
                case "save":
                    command=newData;
                    human=null;
                    readyMessege= true;
                    break;
                default:
                    System.out.println("Нет такой команды: \""+command+"\"\nПопробуйте снова или восользуйтесь командой \"help;\".");
            }
            System.out.print(sendMessegeForServer(readyMessege, command, human, serverSocket, channel)+"\n");
        }

    }

    private static String sendMessegeForServer(boolean readyMessege, String command, Human human, DatagramSocket serverSocket, DatagramChannel channel) throws Exception {
        if (readyMessege) {
            Thread tr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(9800);
                        System.err.print("Возникли технические проблемы. Сервер временно не доступен.");
                        System.exit(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            tr.start();
            ByteBuffer buf = ByteBuffer.wrap(new byte[1024]);
            buf.clear();
            messageRequest = new MessageRequest(command, human,
                    InetAddress.getLocalHost().toString().substring(InetAddress.getLocalHost().toString().indexOf("/") + 1),
                    serverSocket.getLocalPort(), LOGIN, SerialFactory.md5Custom(PASSWORD));
            buf.put(SerialFactory.serialize(messageRequest));
            buf.flip();
            channel.send(buf, new InetSocketAddress(IP, serverPort));
            String answer = poluchatel(serverSocket);
            tr.stop();
            return answer;
        }
        return "";
    }

    public static String poluchatel(DatagramSocket socket) throws Exception{
        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
        socket.receive(packet);
        int count = ((MessageAnswer) SerialFactory.unserialize(packet.getData())).getCount();
        byte[] anserByte = new byte[0];
        for (int i=0;count>i;i++){
            socket.receive(packet);
            anserByte=SerialFactory.ArrayComination(anserByte, packet.getData());
        }
        return ((MessageAnswer)SerialFactory.unserialize(anserByte)).getInformation();
    }


    public static String CommandReader(){
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        char nextChar='j';
        int intChar;
        boolean kov = false;
        String strangeString="";
        while (nextChar!=';' || kov) {
            try {
                intChar= reader.read();
                if (intChar==-1)
                    System.exit(0);
                else
                    nextChar=(char) intChar;
                if (nextChar=='"')
                    kov=!kov;
                if (nextChar == ';' && !kov)
                    return strangeString;
                strangeString += nextChar;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
