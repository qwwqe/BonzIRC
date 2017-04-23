package com.bonzimybuddy.bonzirc;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.regex.Pattern;

public class LoginField extends AppCompatEditText {
    private boolean valid = false;
    private String type = "";
    private Pattern pattern = Pattern.compile(".*");
    private Context context;

    public LoginField(Context context) {
        super(context);
        this.context = context;
    }

    public LoginField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LoginField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setValidity(boolean valid) { this.valid = valid; }

    public boolean isValid() { return valid; }

    public void setPattern(Pattern pattern) { this.pattern = pattern; }

    public Pattern getPattern() { return pattern; }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LoginField, 0, 0);
        type = a.getString(R.styleable.LoginField_type);
        if(type == null)
            type = "";

        switch(type) {
            case "nick":
                pattern = IRCPatterns.NICK;
                break;
            case "server":
                pattern = IRCPatterns.SERVER;
                break;
            case "port":
                pattern = IRCPatterns.PORT;
                break;
            case "channel":
                pattern = IRCPatterns.CHANNEL;
                break;
            default:
                pattern = Pattern.compile(".*");
                break;
        }

        this.addTextChangedListener(new ValidityWatcher());
        a.recycle();
    }

    private class ValidityWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();

            Context context = getCallingContext();
            Pattern pattern = getPattern();

            if(pattern.matcher(text).matches()) {
                setValidity(true);
                LoginField.this.setCompoundDrawables(null, null, null, null);
            } else if (text.isEmpty()) {
                setValidity(false);
                LoginField.this.setCompoundDrawables(null, null, null, null);
            } else {
                Drawable errorIcon = ContextCompat.getDrawable(context, R.drawable.input_error);
                errorIcon.setBounds(0, 0, errorIcon.getIntrinsicWidth(),
                        errorIcon.getIntrinsicHeight());

                setValidity(false);
                LoginField.this.setCompoundDrawables(null, null, errorIcon, null);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private Context getCallingContext() {
        return context;
    }
}
