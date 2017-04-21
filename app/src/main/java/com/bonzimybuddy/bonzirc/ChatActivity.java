package com.bonzimybuddy.bonzirc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* Basically all the IRC code and chat window code crammed together. Another refactoring project.
 */

public class ChatActivity extends Activity {
    // networking stuff
    private IRCService mService; // network service. this is a Service, not an IBinder object. fwiw.
    private boolean mBound = false;

    // irc stuff
    private boolean registered = false;
    private String nick;
    private String channel;
    private String server;
    private String port;

    // display stuff
    private RecyclerView mChatView;
    private ChatScrollAdapter mChatScrollAdapter;
    private ArrayList<String> chatLog;

    /* This ServiceConnection must be provided when binding to a service (calling bindService()).
     * A call to bindService() returns immediately, and therefore does not return an IBinder for
     * the service. Instead, when the system succeeds in establishing a connection between the
     * calling client and the service, it runs the onServiceConnected() method of this
     * ServiceConnection and delivers the prepared IBinder as an argument.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            IRCService.NetworkBinder binder = (IRCService.NetworkBinder) service;

            if (binder != null) {
                mService = binder.getService();
                mBound = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //mService.unBind();
            //mBound = false;
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String command = extras.get("IRC_COMMAND").toString();
            String chatLine = null;

            if(command.equals(IRCService.IRC_COMMAND_PRIVMSG))
                chatLine = extras.get("IRC_SPEAKER").toString() + ": "
                        + extras.get("IRC_MESSAGE").toString();
            else if(command.equals(IRCService.IRC_COMMAND_JOIN)) {
                String speaker = extras.get("IRC_SPEAKER").toString();

                if(speaker.equals(nick)) { // marks a successful connection. sort of.
                    mChatScrollAdapter.clearLines(); // erase loading message etc
                    chatLine = "You have joined " + channel + ".";
                } else
                    chatLine = extras.get("IRC_SPEAKER").toString() + " has joined.";
            } else if(command.equals(IRCService.IRC_COMMAND_PART))
                chatLine = extras.get("IRC_SPEAKER").toString() + " has quit: "
                        + extras.get("IRC_MESSAGE").toString();
            else
                chatLine = extras.get("IRC_RAW").toString();

            if(chatLine != null)
                commitLine(chatLine);
        }
    };

    /* Not sure if bindService() should be called here or in onStart().
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        mChatView = (RecyclerView) findViewById(R.id.chatScroll);
        mChatScrollAdapter = new ChatScrollAdapter();
        mChatView.setAdapter(mChatScrollAdapter);
        mChatView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int a, int b, int c, int d,
                                       int e, int f, int g, int h) {
                ((RecyclerView) view).smoothScrollToPosition(mChatScrollAdapter.getItemCount() - 1);
            }
        });

        Bundle extras;
        if(savedInstanceState == null) { // first starting the app
            extras = getIntent().getExtras();
            chatLog = new ArrayList<String>();
            mChatScrollAdapter.addLine("Connecting...");
        } else { // re-orientation or system slaughter
            extras = savedInstanceState;
            setLines(extras.getStringArrayList("chatLog"));
        }


        nick = extras.get("nick").toString();
        server = extras.get("server").toString();
        channel = extras.get("channel").toString();
        port = extras.get("port").toString();

        Intent intent = new Intent(this, IRCService.class);
        intent.putExtra("nick", nick);
        intent.putExtra("server", server);
        intent.putExtra("port", port);
        intent.putExtra("channel", channel);

        // register for intent before binding to service, in case service has backlog of
        // messages to send
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("incomingMessage"));

        // call startService() first to disable default service lifecycle
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("nick", nick);
        outState.putString("channel", channel);
        outState.putString("server", server);
        outState.putString("port", port);
        outState.putStringArrayList("chatLog", chatLog);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // the question is... what happens to the service when the activity is
        // destroyed but the service not unbound? answer probably in the docs.

        if(mBound) {
            mService.deregisterClient();
            unbindService(mConnection);
            mBound = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);


        if(isFinishing())
            stopService(new Intent(this, IRCService.class));

        super.onDestroy();
    }

    public void onSendMessage(View view) {
        EditText inputBox = (EditText) findViewById(R.id.send_message);
        String message = inputBox.getText().toString();
        String chatLine = nick + ": " + message;

        if(message.equals(""))
            return;

        inputBox.setText("");

        commitLine(chatLine);
        mService.privateMessage(message);
    }

    // commit a line to the chat log. triggers update of chat display
    private void commitLine(String line) {
        chatLog.add(line);
        mChatScrollAdapter.addLine(line);
        mChatView.smoothScrollToPosition(mChatScrollAdapter.getItemCount());
    }

    // set chat log and display
    private void setLines(ArrayList<String> lines) {
        chatLog = new ArrayList<String>(lines);
        mChatScrollAdapter.setLines(lines);
        mChatView.smoothScrollToPosition(mChatScrollAdapter.getItemCount());
    }

}
