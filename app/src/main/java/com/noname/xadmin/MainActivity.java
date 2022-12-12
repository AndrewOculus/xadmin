package com.noname.xadmin;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.noname.xadmin.ui.MessageAdapter;
import com.noname.xadmin.xash.Callback;
import com.noname.xadmin.xash.Xash;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static Xash xash = null;
    public static String selectedMap = "";
    public static TextView commandToClient;

    private RecyclerView mRecyclerView;
    private MessageAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayAdapter<String> mMapListAdapter;

    private Button restart, changeMap;
    private TextView chat;
    private TextView connectionStatus;

    private TimerTask checkNetworkTask;
    private Timer schedule;

    private Handler mUiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mUiHandler = new Handler(Looper.getMainLooper());

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MessageAdapter(getBaseContext(), xash);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.notifyDataSetChanged();

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        mMapListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        mMapListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mMapListAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setText("Select map: "+ ((TextView) parent.getChildAt(0)).getText());

                selectedMap = (String)parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setText("Select map: "+ ((TextView) parent.getChildAt(0)).getText());
            }
        });

        changeMap = (Button)findViewById(R.id.change_map);
        changeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xash.changeServerMap(MainActivity.selectedMap);
                Log.d(TAG, "Change Map " + selectedMap);
            }
        });

        restart = (Button)findViewById(R.id.restart_server);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xash.restartServer();
                Log.d(TAG, "Restart Server");
            }
        });

        chat = (TextView) findViewById(R.id.chat);
        connectionStatus = (TextView) findViewById(R.id.connection_status);

        commandToClient =  (TextView) findViewById(R.id.client_cmd);
        
        checkNetworkTask = new TimerTask() {
            @Override
            public void run() {
                if(isConnected()) {
                    if(xash == null) {
                        connect();
                    }
                }else {
                    if(xash != null) {
                        xash.close();
                        xash = null;

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                connectionStatus.setText("Offline");
                            }
                        });
                    }
                }
            }
        };

        if(schedule != null)
            schedule.cancel();

        schedule = new Timer();
        schedule.schedule(checkNetworkTask, 0 , com.noname.xadmin.Settings.NETWORK_CONNECTION_TIMEOUT);

//        if (isConnected()) {
//            Toast.makeText(getApplicationContext(), "Internet Connected", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
//        }
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

    private void connect(){
        final String android_id = Settings.Secure.getString( getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        final String address = "185.252.146.194";
        final int port = 27022;

        Log.e(TAG, android_id);

        if(xash != null)
            xash.close();

        xash = new Xash(address, port, android_id, new Callback(mAdapter, mMapListAdapter, chat), connectionStatus);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("CMD", commandToClient.getText().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkNetworkTask = new TimerTask() {
            @Override
            public void run() {
                if(isConnected()) {
                    if(xash == null) {
                        connect();
                    }
                }else {
                    if(xash != null) {
                        xash.close();
                        xash = null;

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                connectionStatus.setText("Offline");
                            }
                        });
                    }
                }
            }
        };

        if(schedule != null)
            schedule.cancel();

        schedule = new Timer();
        schedule.schedule(checkNetworkTask, 0 , com.noname.xadmin.Settings.NETWORK_CONNECTION_TIMEOUT);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(xash != null) {
            xash.close();
            xash = null;
        }

        if(checkNetworkTask != null) {
            checkNetworkTask.cancel();
            checkNetworkTask = null;
        }

        if(schedule != null){
            schedule.cancel();
            schedule = null;
        }
    }
}