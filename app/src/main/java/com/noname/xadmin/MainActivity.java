package com.noname.xadmin;

import android.graphics.Color;
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

import com.noname.xadmin.ui.MessageAdapter;
import com.noname.xadmin.xash.Callback;
import com.noname.xadmin.xash.Xash;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static Xash xash = null;
    public static String selectedMap = "";

    private RecyclerView mRecyclerView;
    private MessageAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayAdapter<String> mMapListAdapter;

    private Button restart, changeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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

        connect();
    }

    private void connect(){
        final String android_id = Settings.Secure.getString( getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        final String address = "185.252.146.194";//"81.5.99.9";//
        final int port = 27022;//27035;//

        Log.d(TAG, android_id);

        if(xash != null)
            xash.close();

        xash = new Xash(address, port, android_id, new Callback(mAdapter, mMapListAdapter));
    }

    @Override
    protected void onResume() {
        super.onResume();
        connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(xash != null)
            xash.close();
    }
}