package com.passjava.model.util;

public final class Base64Codec {
    private static final String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private Base64Codec() {}

    private static byte[] zeroPad(int length, byte[] bytes) {
        byte[] padded = new byte[length]; // initialized to zero by JVM
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }

    public static String encode(String string) {

        String encoded = "";
        byte[] stringArray;
        try {
            stringArray = string.getBytes("UTF-8");
        } catch (Exception ignored) {
            stringArray = string.getBytes();
        }

        int paddingCount = (3 - (stringArray.length % 3)) % 3;
        stringArray = zeroPad(stringArray.length + paddingCount, stringArray);
        for (int i = 0; i < stringArray.length; i += 3) {
            int j = ((stringArray[i] & 0xff) << 16) + ((stringArray[i + 1] & 0xff) << 8) + (stringArray[i + 2] & 0xff);
            encoded = encoded + base64code.charAt((j >> 18) & 0x3f) + base64code.charAt((j >> 12) & 0x3f) +
                    base64code.charAt((j >> 6) & 0x3f) + base64code.charAt(j & 0x3f);
        }
        return encoded.substring(0, encoded.length() - paddingCount) + "==".substring(0, paddingCount);
    }
}