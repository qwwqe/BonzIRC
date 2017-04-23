package com.bonzimybuddy.bonzirc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    public void login(View view) {
        LoginField nick = (LoginField) findViewById(R.id.nick);
        LoginField server = (LoginField) findViewById(R.id.server);
        LoginField port = (LoginField) findViewById(R.id.port);
        LoginField channel = (LoginField) findViewById(R.id.channel);
        if(!(nick.isValid() && server.isValid() && port.isValid() && channel.isValid()))
            return;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("nick", nick.getText());
        intent.putExtra("server", server.getText());
        intent.putExtra("port", port.getText());
        intent.putExtra("channel", channel.getText());

        startActivity(intent);
    }
}
