package com.company.Tools;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SerialFactory {
    static byte byteMass[];
    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        byteMass = baos.toByteArray();
        oos.close();
        return byteMass;
    }
    public static Object unserialize(byte[] bytes) throws IOException, ClassNotFoundException{
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object object = ois.readObject();
        ois.close();
        return object;
    }
    public static byte[] ArrayComination(final byte[] ...arrays ) {
        int size = 0;
        for ( byte[] a: arrays )
            size += a.length;

        byte[] res = new byte[size];

        int destPos = 0;
        for ( int i = 0; i < arrays.length; i++ ) {
            if ( i > 0 ) destPos += arrays[i-1].length;
            int length = arrays[i].length;
            System.arraycopy(arrays[i], 0, res, destPos, length);
        }

        return res;
    }
    public static void readmass(byte[] b)
    {   int i = 0;
        for (byte byt:b) {
            if (byt==1)
                i++;
        }
        System.out.println(i);
    }
    public static byte[] getByteInfo1024(byte[] byteMass, int number){
        int start, stop;
        if (byteMass.length>(number+1)*1024){
            start=number*1024;
            stop=(number+1)*1024;
        }
        else{
            start=number*1024;
            stop=byteMass.length;
        }
        byte[] bytes=new byte[stop-start];
        for (int counter=start;counter<stop;counter++){
            bytes[counter-start]=byteMass[counter];
        }
        return bytes;
    }
    public static String md5Custom(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // тут можно обработать ошибку
            // возникает она если в передаваемый алгоритм в getInstance(,,,) не существует
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }
}
