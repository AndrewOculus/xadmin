package com.noname.xadmin.xash;


import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Utils {

    private static final String TAG = Utils.class.getName();

    public static void send(String hex, DatagramSocket ds){
        if(ds.isClosed())
            return;
        DatagramPacket datagramPacket = new DatagramPacket(hexStringToByteArray(hex), hexStringToByteArray(hex).length);
        try {
            ds.send(datagramPacket);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void send(byte[] hex, DatagramSocket ds){
        DatagramPacket datagramPacket = new DatagramPacket(hex, hex.length);
        try {
            ds.send(datagramPacket);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void send(DatagramPacket hex, DatagramSocket ds){
        try {
            ds.send(hex);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static DatagramPacket receive(DatagramSocket ds) throws IOException {
        byte[] array = new byte[4096];
        DatagramPacket inputDatagramPacket = new DatagramPacket(array, array.length);
        ds.receive(inputDatagramPacket);
        return inputDatagramPacket;
    }

    public static String receiveString(DatagramSocket ds) throws IOException {
        DatagramPacket inputDatagramPacket = receive(ds);
        return new String(inputDatagramPacket.getData(), 0 , inputDatagramPacket.getLength());
    }

}
