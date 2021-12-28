package com.noname.xadmin.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.noname.xadmin.MainActivity;
import com.noname.xadmin.R;
import com.noname.xadmin.ui.data.Message;
import com.noname.xadmin.xash.Xash;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MessageAdapter extends RecyclerView.Adapter  {

    private static final String TAG = MessageAdapter.class.getName();

    public List<Message> messageList;
    public static final int SENDER = 0;
    public static final int RECEIVER = 1;
    private Xash xash;

    public MessageAdapter(Context context, Xash xash) {
        this.xash = xash;
        messageList = new ArrayList<Message>(){{new Message(1, "123 123 123", "123 123 123");}};
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public Button kickBtn;
        public Button banBtn;
        public Button killBtn;
        public Button chmpBtn;
        public EditText mapField;

        public ViewHolder(LinearLayout v) {
            super(v);
            banBtn = (Button) v.findViewById(R.id.ban);
            mTextView = (TextView) v.findViewById(R.id.text);
            kickBtn = (Button) v.findViewById(R.id.kick);
            chmpBtn = (Button) v.findViewById(R.id.changemap);
            mapField = (EditText) v.findViewById(R.id.map);
            killBtn = (Button) v.findViewById(R.id.kill);
        }
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.xash_map, parent, false);
            ViewHolder vh = new ViewHolder((LinearLayout) v);
            return vh;
        }else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.xash_players, parent, false);
            ViewHolder vh = new ViewHolder((LinearLayout) v);
            return vh;
        }
    }

    public void remove(int pos) {
        int position = pos;
        messageList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, messageList.size());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        ((MessageAdapter.ViewHolder)holder).mTextView.setText( messageList.get(position).getMessage());

        ((ViewHolder)holder).banBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = new Scanner(messageList.get(position).getMessage()).useDelimiter("\\D+").nextInt();
                MainActivity.xash.banPlayer(id);
                Log.d(TAG, "Ban Player id: " + id);
            }
        });

        ((ViewHolder)holder).kickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = new Scanner(messageList.get(position).getMessage()).useDelimiter("\\D+").nextInt();
                MainActivity.xash.kickPlayer(id);
                Log.d(TAG, "Kick Player id: " + id);
            }
        });

        ((ViewHolder)holder).killBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = new Scanner(messageList.get(position).getMessage()).useDelimiter("\\D+").nextInt();
                MainActivity.xash.killPlayer(id);
                Log.d(TAG, "Kill Player id: " + id);
            }
        });

        final EditText mapName = ((ViewHolder)holder).mapField;

        ((ViewHolder)holder).chmpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.xash.changeServerMap(MainActivity.selectedMap);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = (Message) messageList.get(position);

        if (message.getSenderName().equals("server")) {
            return SENDER;
        } else {
            return RECEIVER;
        }

    }
}
