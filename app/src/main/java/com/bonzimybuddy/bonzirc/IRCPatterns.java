package com.bonzimybuddy.bonzirc;

import java.util.regex.Pattern;

/**
 * Reference class for IRC regexes.
 */
public class IRCPatterns {
    private static final String letter = "[A-Za-z]";
    private static final String digit = "[\\d]";
    private static final String special = "[\\[\\]\\\\`_^{|}]"; // []\`_^{|}

    // "^(letter|special)+(letter|special|digit|-)*$"
    private static final String nickname = "^(" + letter + "|" + special + ")+"
            + "(" + letter + "|" + special + "|" + digit + "|" + "-)*$";
    private static final String servername = ".*";
    private static final String port = "^\\d+$";
    private static final String channel = "^([#+&]|![A-Za-z0-9]{5})[^,: \\00\\a\\n\\r]+$";

    public static final Pattern NICK = Pattern.compile(nickname);
    public static final Pattern SERVER = Pattern.compile(servername);
    public static final Pattern PORT = Pattern.compile(port);
    public static final Pattern CHANNEL = Pattern.compile(channel);
}
