package com.noname.xadmin.xash;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.noname.xadmin.Settings;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Xash implements Runnable {

    private static final String TAG = Xash.class.getName();

    private Thread thread;
    private TimerTask updatePlayersTimer, updateMapListTimer, updateChatTimer;

    private boolean isWork;
    private DatagramSocket datagramSocket;
    private InetAddress inetAddress;
    private String address;
    private int port;

    private DatagramSocket logDatagramSocket;
    private InetAddress logInetAddress;
    private int logPort;

    private String deviceId;
    private XashCallback xashCallback;
    private Handler mUiHandler;

    public Xash(String address, int port, String deviceId, XashCallback xashCallback){
        this.deviceId = deviceId;
        this.address = address;
        this.port = port;

        this.logPort = Settings.LOG_PORT;
        this.isWork = true;

        this.xashCallback = xashCallback;

        mUiHandler = new Handler(Looper.getMainLooper());

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        boolean isConnected = false;
        do {
            isConnected = createConnetion();
            if(isConnected == false) {
                try {
                    Thread.sleep(Settings.CONNECTION_TRY_TIMEOUT);
                } catch (InterruptedException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }while (!isConnected && isWork);

        requestMapsList();

//        updateMapListTimer = new TimerTask() {
//            @Override
//            public void run() {
//                requestMapsList();
//            }
//        };

//        Timer mapSchedule = new Timer();
//        mapSchedule.schedule(updateMapListTimer, 0 , Settings.MAP_SCHEDULE_TIME);
//

        updateChatTimer = new TimerTask() {
            @Override
            public void run() {
                requestChat();
            }
        };

        Timer chatSchedule = new Timer();
        chatSchedule.schedule(updateChatTimer, 0 , Settings.CHAT_SCHEDULE_TIME);

        updatePlayersTimer = new TimerTask() {
            @Override
            public void run() {
                requestPlayersList();
            }
        };

        Timer playersSchedule = new Timer();
        playersSchedule.schedule(updatePlayersTimer, 0 , Settings.PLAYERS_SCHEDULE_TIME);

        while (isWork){
            try{
                String input = Utils.receiveString(datagramSocket);
                inputStringParser(input);
            }catch (IOException e){
//                Log.d(TAG, e.toString());
            }

        }
    }

    private void inputStringParser(final String input){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                xashCallback.receive(input);
            }
        });
    }

    private boolean createConnetion(){
        try {
            inetAddress = InetAddress.getByName(address);
            logInetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            Log.d(TAG, e.toString());
            return false;
        }

        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(Settings.TIMEOUT);
            datagramSocket.connect(inetAddress, port);

            logDatagramSocket = new DatagramSocket();
            logDatagramSocket.setSoTimeout(Settings.TIMEOUT);
            logDatagramSocket.connect(logInetAddress, logPort);
        } catch (SocketException e) {
            Log.d(TAG, e.toString());
            return false;
        }

        return true;
    }

    public void requestMapsList(){
        try{
            String cm = "ffffffff" + Utils.bytesToHex("maps".getBytes());
            Utils.send(cm, datagramSocket);
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    public void requestPlayersList(){
        try{
            String cm = "ffffffff" + Utils.bytesToHex(String.format("admin_player_statuses %s", deviceId).getBytes());
            Utils.send(cm, datagramSocket);
            //Utils.send(cm, logDatagramSocket);
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }

    }

    public void banPlayer(int id){
        try{
            String cm = "ffffffff"+Utils.bytesToHex( String.format("admin_ban_player %s #%d", deviceId, id).getBytes());
            Utils.send(cm, datagramSocket);
            Utils.send(cm, logDatagramSocket);
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }

    }

    public void kickPlayer(int id){
        try{
            String cm = "ffffffff"+Utils.bytesToHex(String.format("admin_kick_player %s %d", deviceId, id).getBytes());
            Utils.send(cm, datagramSocket);
            Utils.send(cm, logDatagramSocket);
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    public void killPlayer(int id){
        try{
            String cm = "ffffffff"+Utils.bytesToHex(String.format("admin_kill_player %s %d", deviceId, id).getBytes());
            Utils.send(cm, datagramSocket);
            Utils.send(cm, logDatagramSocket);
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    public void changeServerMap(String map){
        try{
            String cm = "ffffffff"+Utils.bytesToHex(String.format("admin_change_map %s %s", deviceId, map).getBytes());
            Utils.send(cm, datagramSocket);
            Utils.send(cm, logDatagramSocket);
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    public void restartServer(){
        try{
            String cm = "ffffffff"+Utils.bytesToHex(String.format("admin_restart_server %s", deviceId).getBytes());
            Utils.send(cm, datagramSocket);
            Utils.send(cm, logDatagramSocket);
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    public void requestChat(){
        try{
            String cm = "ffffffff"+Utils.bytesToHex(String.format("admin_chat_buffer %s", deviceId).getBytes());
            Utils.send(cm, datagramSocket);
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    public void close(){
        isWork = false;

        if(updatePlayersTimer != null )
            updatePlayersTimer.cancel();

        if(updateMapListTimer != null )
            updateMapListTimer.cancel();

        if(datagramSocket!= null)
            datagramSocket.close();
    }

}

interface XashCallback {
    void receive(String input);
}
