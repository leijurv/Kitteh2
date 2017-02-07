/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author leijurv
 */
public class Kitterature {
    private Kitterature() {
    }
    public static InputStream getResourceAsStream(String n) throws IOException {
        String name = n;
        if (!name.endsWith(".k")) {
            name += ".k";
        }
        InputStream is = Kitterature.class.getResourceAsStream("/lang/" + name);
        return is;
    }
    public static byte getResource   (String name)  [] throws IOException {
        return getBytes(getResourceAsStream(name));
    }
    public static boolean resourceExists(String name) {
        try {
            return getResourceAsStream(name) != null;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    public static List<Path> listFiles() throws IOException {
        try {
            URI uri = Kitterature.class.getResource("/lang").toURI();
            List<Path> paths = new ArrayList<>();
            try (@SuppressWarnings("unused") FileSystem fileSystem = (uri.getScheme().equals("jar") ? FileSystems.newFileSystem(uri, Collections.emptyMap()) : null)) {
                Path myPath = Paths.get(uri);
                Files.walkFileTree(myPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!file.toString().endsWith(".k") || !file.toString().contains("lang")) {
                            throw new RuntimeException("Unexpected extension for file " + file);
                        }
                        String lol = file.toString().split("lang/")[1];
                        if (!lol.contains("/")) {
                            paths.add(new File(lol).toPath());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            return paths;
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
    public static byte[] getBytes(InputStream is) throws IOException {
        int size = 1024;
        byte buf[] = new byte[size];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = is.read(buf, 0, size);
        while (len != -1) {
            bos.write(buf, 0, len);
            len = is.read(buf, 0, size);
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
            return s.replace("./", "");
        }
        if (ind == 0) {
            return "../" + trimPath(s.substring(3));
        }
        int from = s.lastIndexOf('/', ind - 2);
        return trimPath(s.substring(0, from + 1) + s.substring(ind + 3));
    }
}
