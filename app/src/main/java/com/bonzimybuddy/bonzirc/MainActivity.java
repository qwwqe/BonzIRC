package com.bonzimybuddy.bonzirc;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public int fieldsCorrect = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        EditText[] fields = {(EditText) findViewById(R.id.nick),
                (EditText) findViewById(R.id.server),
                (EditText) findViewById(R.id.port),
                (EditText) findViewById(R.id.channel) };

        // enforce syntax restraints on fields
        for (EditText field : fields) {
            final int fieldId = field.getId();
            final EditText textField = field;
            field.addTextChangedListener(new TextWatcher() {
                private final int id = fieldId;
                private final EditText field = textField;

                @Override
                public void afterTextChanged(Editable s) {
                    String pattern;
                    String text = s.toString();
                    //Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error, null);

                    switch(id) {
                        case R.id.nick:
                            pattern = "^[A-Za-z\\[\\]\\\\`_^{|}]+[A-Za-z\\[\\]\\\\`_^{|}0-9\\-]*$";
                            if(!Pattern.compile(pattern).matcher(text).matches()) {

                                field.setError("");
                            } else
                                field.setError(null);

                            break;

                        case R.id.server:
                            // Android.util.Patterns.DOMAIN_NAME is insufficient for recognizing
                            // valid irc domains, so let fail on connection instead
                            /*
                            if(!Patterns.DOMAIN_NAME.matcher(text).matches()
                                    || !Patterns.IP_ADDRESS.matcher(text).matches()) {
                                field.setError("");
                            } else
                                field.setError(null);
                                */

                            break;

                        case R.id.port:
                            pattern = "^\\d+$";
                            if(!Pattern.compile(pattern).matcher(text).matches()) {
                                field.setError("");
                            } else
                                field.setError(null);

                            break;

                        case R.id.channel:
                            pattern = "^([#+&]|![A-Za-z0-9]{5})[^,: \\00\\a\\n\\r]+$";
                            if(!Pattern.compile(pattern).matcher(text).matches()) {
                                field.setError("");
                            } else
                                field.setError(null);

                            break;

                        default:
                            break;
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int before, int count) {}
            });
        }
    }

    public void login(View view) {
        String nick = ((EditText) findViewById(R.id.nick)).getText().toString();
        String server = ((EditText) findViewById(R.id.server)).getText().toString();
        String port = ((EditText) findViewById(R.id.port)).getText().toString();
        String channel = ((EditText) findViewById(R.id.channel)).getText().toString();
        String pattern;

        // check RFC 2812 syntax validity
/*
        // nick
        pattern = "^[A-Za-z\\[\\]\\\\`_^{|}]+[A-Za-z\\[\\]\\\\`_^{|}0-9\\-]*$";
        if(!Pattern.compile(pattern).matcher(nick).matches())
            return;

        // server
        if(!Patterns.DOMAIN_NAME.matcher(server).matches()
            || !Patterns.IP_ADDRESS.matcher(server).matches())
            return;

        // port
        pattern = "^\\d+$";
        if(!Pattern.compile(pattern).matcher(port).matches())
            return;

        // channel. excludes the optional ": channelid" suffix
        pattern = "^([#+&]|![A-Za-z0-9]{5})[^,: \\00\\a\\n\\r]+$";
        if(!Pattern.compile(pattern).matcher(channel).matches())
            return;
*/
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("nick", ((EditText) findViewById(R.id.nick)).getText());
        intent.putExtra("server", ((EditText) findViewById(R.id.server)).getText());
        intent.putExtra("port", ((EditText) findViewById(R.id.port)).getText());
        intent.putExtra("channel", ((EditText) findViewById(R.id.channel)).getText());

        startActivity(intent);
    }
}
