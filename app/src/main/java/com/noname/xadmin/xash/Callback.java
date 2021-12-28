package com.noname.xadmin.xash;

import android.widget.ArrayAdapter;

import com.noname.xadmin.ui.MessageAdapter;
import com.noname.xadmin.ui.data.Message;

public class Callback implements XashCallback {

    private MessageAdapter adapter;
    private ArrayAdapter<String> arrayAdapter;

    public Callback(MessageAdapter adapter, ArrayAdapter<String> arrayAdapter){
        this.adapter = adapter;
        this.arrayAdapter = arrayAdapter;
    }

    @Override
    public void receive(String input) {

        String[] spl = (input.substring(4)).split("\n");

        if(spl.length>0)
            if(spl[0].substring(0, 4).equals("maps")){

                int part = Integer.parseInt(spl[0].substring(5, 6));

                if(part==0){
                    arrayAdapter.clear();
                }

                for(int i = 1; i < spl.length; i++){
                    String map = spl[i];
                    String[] sp = map.split("\\.");

                    if(sp.length>1)
                        if(sp[1].equals("bsp"))
                            arrayAdapter.add(sp[0]);
                }

                arrayAdapter.notifyDataSetChanged();
                return;
            }

        adapter.messageList.clear();
        int idx = 1;

        for (String s : spl) {
            adapter.messageList.add(new Message(idx++, (idx == 2) ? "Server map: " + s : s , (idx == 2) ? "player" : "server"));
        }
        adapter.notifyDataSetChanged();
    }
}
