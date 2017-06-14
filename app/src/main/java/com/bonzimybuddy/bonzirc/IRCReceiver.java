package com.bonzimybuddy.bonzirc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Receiver for broadcasts sent from {@link IRCService}.
 */
public class IRCReceiver extends BroadcastReceiver {
    private final ChatActivity activity;

    IRCReceiver (ChatActivity activity) {
        super();
        this.activity = activity;
    }

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

            if(speaker.equals(activity.nick)) { // marks a successful connection. sort of.
                activity.mChatScrollAdapter.clearLines(); // erase loading message etc
                chatLine = "You have joined " + activity.channel + ".";
            } else
                chatLine = extras.get("IRC_SPEAKER").toString() + " has joined.";
        } else if(command.equals(IRCService.IRC_COMMAND_PART))
            chatLine = extras.get("IRC_SPEAKER").toString() + " has quit: "
                    + extras.get("IRC_MESSAGE").toString();
        else
            chatLine = extras.get("IRC_RAW").toString();

        if(chatLine != null)
            activity.commitLine(chatLine);
    }
}
