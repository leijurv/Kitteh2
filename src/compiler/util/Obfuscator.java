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
 * why doesn't the obfuscator simply use an increasing counter? because its
 * called from many threads, and that would be nondeterministic. (even if the
 * deterministic flag is set, functions are parsed in separate threads, but put
 * back in the original order before assembling). instead, we can simply take a
 * deterministic hash when that's required, and a random-ish hash when
 * deterministicness isn't required
 *
 * @author leijurv
 */
public class Obfuscator {
    static final private long NANO_TIME;
    final private static MessageDigest SHA;
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
    private Obfuscator() {
    }
    static {
        NANO_TIME = System.nanoTime();
        //this is only set once the Obfuscator class is first called, which is a nondeterministic amount of time after the compiler is first started
        MessageDigest tmp;
        try {
            tmp = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Obfuscator.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        SHA = tmp;
    }
    static synchronized public String obfuscate(String str) {
        if ("main".equals(str) || "malloc".equals(str) || "free".equals(str)) {//lol
            return str;
        }
        SHA.reset();
        String s = str;
        if (!compiler.Compiler.deterministic()) {
            s += NANO_TIME;
        }
        try {
            return bytesToHex(SHA.digest(s.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Obfuscator.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("doesn't support utf-8?????", ex);
        }
    }
}
