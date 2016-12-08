/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author leijurv
 */
public class Kitterature {
    public static byte[] getResource(String name) throws IOException {
        String s = "";
        if (!name.endsWith(".k")) {
            name += ".k";
        }
        InputStream is = Kitterature.class.getResourceAsStream("/lang/" + name);
        return getBytes(is);
    }
    public static byte[] getBytes(InputStream is) throws IOException {
        int size = 1024;
        byte[] buf = new byte[size];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len;
        while ((len = is.read(buf, 0, size)) != -1) {
            bos.write(buf, 0, len);
        }
        buf = bos.toByteArray();
        return buf;
    }
    public static String trimPath(String s) {
        if (s == null) {
            return null;
        }
        int ind = s.indexOf("../");
        if (ind == -1) {
            return s;
        }
        if (ind == 0) {
            return "../" + trimPath(s.substring(3));
        }
        int from = s.lastIndexOf("/", ind - 2);
        return trimPath(s.substring(0, from + 1) + s.substring(ind + 3));
    }
}
