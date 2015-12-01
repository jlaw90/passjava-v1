package com.passjava.model.util;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class HTMLEncoder {
    private static String htmlSpecialChars = "<>&";
    private static String[] escaped = {"&lt;", "&gt;", "&amp;"};

    public static String escape(String s, String charset) {
        Charset cs = Charset.forName(charset);
        CharsetEncoder ce = cs.newEncoder();

        StringBuilder sb = new StringBuilder();
        char[] data = s.toCharArray();

        int idx;
        for(char c: data) {

            // Special HTML character, return escaped representation
            if((idx = htmlSpecialChars.indexOf(c)) != -1) {
                sb.append(escaped[idx]);
                continue;
            }

            // Charset contains this character so we can place it
            if(ce.canEncode(c)) {
                sb.append(c);
                continue;
            }

            // Charset doesn't contain this character, encode...
            sb.append("&#").append((int) c).append(";");
        }

        return sb.toString();
    }
}