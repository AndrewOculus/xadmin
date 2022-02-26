package com.noname.xadmin.xash;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

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
    private TextView connectionStatus;

    private Timer chatSchedule, playersSchedule;

    private long lastUpdateTime = 0;

    public Xash(String address, int port, String deviceId, XashCallback xashCallback, TextView connectionStatus){
        this.deviceId = deviceId;
        this.address = address;
        this.port = port;

        this.logPort = Settings.LOG_PORT;
        this.isWork = true;

        this.xashCallback = xashCallback;

        this.connectionStatus = connectionStatus;

        mUiHandler = new Handler(Looper.getMainLooper());

        thread = new Thread(this);
        thread.start();
    }

    boolean isConnected = false;

    public void connect(){
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

//        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void run() {

        connect();

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

        requestChat();
        updateChatTimer = new TimerTask() {
            @Override
            public void run() {
                requestChat();
            }
        };

        chatSchedule = new Timer();
        chatSchedule.schedule(updateChatTimer, 0 , Settings.CHAT_SCHEDULE_TIME);

        requestPlayersList();
        updatePlayersTimer = new TimerTask() {
            @Override
            public void run() {
                requestPlayersList();

                String status = "Offline";

                long delta = (System.currentTimeMillis() - lastUpdateTime);
                Log.e("ERRR",  delta + " " );
                if(System.currentTimeMillis() - lastUpdateTime < Settings.NETWORK_CONNECTION_STATUS_OFFLINE){
                    status = "Online";
                }

                final String finalStatus = status;
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        connectionStatus.setText(finalStatus);
                    }
                });
            }
        };

        playersSchedule = new Timer();
        playersSchedule.schedule(updatePlayersTimer, 0 , Settings.PLAYERS_SCHEDULE_TIME);

        while (isWork){
            try{
                String input = Utils.receiveString(datagramSocket);
                inputStringParser(input);
                lastUpdateTime = System.currentTimeMillis();
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

    public void executeClientCommand(int id, String command){
        try{
            String cm = "ffffffff"+Utils.bytesToHex(String.format("admin_execute_client_command %s %d %s", deviceId, id, command).getBytes());
            Utils.send(cm, datagramSocket);
            Utils.send(cm, logDatagramSocket);
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

        if(updateChatTimer != null)
            updateChatTimer.cancel();

        if(playersSchedule != null)
        playersSchedule.cancel();

        if(chatSchedule != null)
            chatSchedule.cancel();

        if(datagramSocket!= null)
            datagramSocket.close();
    }

}

interface XashCallback {
    void receive(String input);
}
