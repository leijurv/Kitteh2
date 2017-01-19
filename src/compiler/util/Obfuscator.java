/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author leijurv
 */
public class Obfuscator {
    static final private long NANO_TIME = System.nanoTime();
    final private static MessageDigest SHA;
    static {
        MessageDigest tmp = null;
        try {
            tmp = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Obfuscator.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        SHA = tmp;
    }
    static synchronized public String obfuscate(String str) {
        if (str.equals("main") || str.equals("free") || str.equals("malloc")) {//lol
            return str;
        }
        SHA.reset();
        if (!compiler.Compiler.deterministic()) {
            str += NANO_TIME;
        }
        try {
            return bytesToHex(SHA.digest(str.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Obfuscator.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("doesn't support utf-8?????", ex);
        }
    }
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
