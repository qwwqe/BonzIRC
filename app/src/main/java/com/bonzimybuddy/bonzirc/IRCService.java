package com.bonzimybuddy.bonzirc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Implementation of network connection service and corresponding binding. Notice that
 * NetworkBinder.getService() returns an instance of IRCService, and therefore provides the
 * caller with with direct access to the public methods of IRCService. ChatActivity uses this
 * service by such direct retrieval - as opposed to using it through the intermediary Binder.
 * <p>
 * Internally, methods of IRCService can directly write to outputStream at any time to send
 * data through the socket. When data is ready to be read from inputStream, however, the
 * ConnectionHandler thread will itself call the appropriate IRCService method - only then
 * should these methods directly access inputStream.
 * <p>
 * Notes:
 * <ul>
 * <li> Perhaps at some point factor out the binding code and connection code to their own files.</li>
 * <li> Verify that client-service communication can also be achieved through the Binder.</li>
 * </ul>
 */
public class IRCService extends Service {
    public static final String BROADCAST_NET = "broadcast_net";
    public static final String BROADCAST_IRC = "broadcast_irc";

    private final IBinder mBinder = new NetworkBinder();
    private ConnectionHandler mConnection;

    public static boolean initialized = false;

    private String nick;
    private String channel;
    public static boolean registered = false;

    private ArrayList<Intent> inputIntents = new ArrayList<Intent>();
    private ArrayList<String> outputMessages = new ArrayList<String>();

    private static int boundClients = 0;

    public static String IRC_COMMAND_PRIVMSG = "PRIVMSG";
    public static String IRC_COMMAND_PING = "PING";
    public static String IRC_COMMAND_JOIN = "JOIN";
    public static String IRC_COMMAND_PART = "PART";

    public class NetworkBinder extends Binder {
        IRCService getService() {
            return IRCService.this;
        }
    }

    /* This class deals with
     */
    private class ConnectionHandler extends Thread {
        private Socket socket;
        private final String hostname;
        private final int port;

        private BufferedReader inputStream;
        private PrintWriter outputStream;

        ConnectionHandler(String hostname, int port) {
            IRCService.this.mConnection  = this;
            this.hostname = hostname;
            this.port = port;
            start();
        }

        public void run() {
            /* TODO: error handling.
               OK. What exceptional circumstances are there?
                   - TCP/IP
                    1) initial connection failure (new Socket())
                    2) connection dropped
                   - IRC
                    3) nick in use, channel private, anything preventing the joining of said channel
                    4) quit/connection dropped (results in TCP/IP drop)
               OK. How do we want to display these to the user?
                   - TCP/IP
                    1) return to initial login screen (show toast?)
                    2) stay in chat window (print error in chat?)
                   - IRC
                    3) same as 1)
                    4) same as 2)
               OK. How and where do we want to implement these responses?
                   - TCP/IP
                    1) emit BROADCAST_FAIL and kill service
                    2) ''
                   - IRC
                    3) do not relay IRC message, instead do 1) with a bundled message
                    4) ''
               Maybe initial connection errors can be handled here (not broadcast), and all further
               errors can simply be relayed on to BroadcastListeners.
            */
            try {
                socket = new Socket(hostname, port);
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputStream = new PrintWriter(socket.getOutputStream(), true);
                Log.d("ConnectionHandler.run()", "Connected to " + hostname + ":" + String.valueOf(port));

                // register
                sendRawMessage("PASS noodlebogger\r\n"
                        + "NICK " + nick + "\r\n"
                        + "USER din don dan: bango\r\n"
                        + "JOIN " + channel + "\r\n");
                //registered = true;

                while(socket.isConnected()) {
                    // backlog of incoming messages
                    if(boundClients > 0 && !inputIntents.isEmpty()) {
                        for(Intent intent : inputIntents)
                            LocalBroadcastManager.getInstance(IRCService.this).sendBroadcast(intent);
                        inputIntents.clear();
                    }

                    if(inputStream.ready()) {
                        receiveMessage();
                    }

                    if(!outputMessages.isEmpty()) {
                        for(String line : outputMessages)
                            outputStream.write(line);

                        outputStream.flush();
                        outputMessages.clear();
                    }
                }
            } catch (Exception e) { // TODO: connection and IRC error handling...

                Log.d("connection", e.toString());
                //Intent intent = new Intent()
            }
        }

        // Helper method for message receipt and consequent broadcast.
        // Placed here for isolation from main thread.
        private void receiveMessage() {
            try {
                String line = inputStream.readLine();
                String prefix = null;
                String command;
                String params;

                String parts[];
                boolean relayMessage = false;

                Log.d("NETWORK", line);

                Intent intent = new Intent("incomingMessage");
                intent.putExtra("IRC_RAW", line);

                // do basic parsing
                parts = line.split(" ");
                if(line.charAt(0) == ':') { // prefix present?
                    prefix = parts[0].substring(1);
                    command = parts[1];
                } else
                    command = parts[0];

                //Log.d("PARSE", "PREFIX: " + prefix + ", COMMAND: " + command);

                intent.putExtra("IRC_COMMAND", command);

                if(command.equals(IRC_COMMAND_PRIVMSG)) {
                    String target;
                    String message;
                    String speaker;
                    if(prefix == null) {
                        intent.putExtra("IRC_SPEAKER", "");

                        target = parts[1];
                        message = line.substring(line.indexOf(':') + 1);
                    } else {
                        intent.putExtra("IRC_SPEAKER", prefix.substring(0, prefix.indexOf("!")));

                        target = parts[2];
                        message = line.substring(line.indexOf(':', 1) + 1);
                    }

                    intent.putExtra("IRC_TARGET", target);
                    intent.putExtra("IRC_MESSAGE", message);

                    relayMessage = true;
                } else if (command.equals(IRC_COMMAND_PING)) {
                    if(prefix == null)
                        sendRawMessage("PONG :" + parts[1].substring(1) + "\r\n");
                    else
                        sendRawMessage("PONG :" + parts[2].substring(1) + "\r\n");
                } else if (command.equals(IRC_COMMAND_JOIN)) {
                    intent.putExtra("IRC_SPEAKER", prefix.substring(0, prefix.indexOf("!")));
                    relayMessage = true;
                } else if (command.equals(IRC_COMMAND_PART)) {
                    intent.putExtra("IRC_SPEAKER", prefix.substring(0, prefix.indexOf("!")));
                    intent.putExtra("IRC_MESSAGE", parts[3].substring(1));
                    relayMessage = true;
                } else { // check for errors. TODO: everything
                    int commandCode = Integer.parseInt(command);
                    if(commandCode >= 400 && commandCode <= 599) {
                        intent.putExtra("IRC_ERROR", true);
                        relayMessage = true;
                    }
                }

                // broadcast stuff
                if(relayMessage) {
                    if(boundClients > 0)
                        LocalBroadcastManager.getInstance(IRCService.this).sendBroadcast(intent);
                    else {
                        Log.d("LALALALA", "adding intents cause clients are gone");
                        inputIntents.add(intent);
                    }
                }
            } catch (Exception e) {
                Log.d("reception", e.toString());
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boundClients += 1;
        Log.d("CLIENT_CHANGE", "boundClients increased to " + boundClients + " ( onStartCommand() )");

        if(!initialized && intent != null) {
            Bundle extras = intent.getExtras();

            nick = extras.get("nick").toString();
            channel = extras.get("channel").toString();
            String server = extras.get("server").toString();
            String port = extras.get("port").toString();
            if(nick != null && channel != null && server != null && port != null ) {
                mConnection = new ConnectionHandler(server, Integer.parseInt(port));
                initialized = true;
            }
        }

        return START_STICKY;
    }

    /* Guts relocated to onStartCommand() for the sake of service longevity/independence.
     * Provides the calling client an interface (an IBinder object) through which that
     * client may interact with this service.
     * Unsure if network threads should really be spawned here.
     */
    @Override
    public IBinder onBind (Intent intent) {
        // i have no idea why this is never called after the first bindService() call
        /*
        boundClients += 1;
        Log.d("CLIENT_CHANGE", "boundClients increased to " + boundClients);*/

        if(initialized)
            return mBinder;
        else
            return null;
    }

    /* register unbinding */
    public void deregisterClient() {
        boundClients -= 1;
        Log.d("CLIENT_CHANGE", "boundClients decreased to " + boundClients);
        if(boundClients < 0)
            boundClients = 0;

    }

    public void privateMessage(String message) {
        sendRawMessage("PRIVMSG " + channel + " :" + message + "\r\n");
    }

    public void sendRawMessage(String line) {
        outputMessages.add(line);
    }

}
