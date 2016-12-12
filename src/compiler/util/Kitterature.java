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
    public static byte[] getResource(String name) throws IOException {
        if (!name.endsWith(".k")) {
            name += ".k";
        }
        InputStream is = Kitterature.class.getResourceAsStream(name);
        return getBytes(is);
    }
    public static boolean resourceExists(String name) {//TODO fix this...
        try {
            return getResource(name) != null;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
    public static List<Path> listFiles(String folder) throws IOException {
        try {
            URI uri = Kitterature.class.getResource("/" + folder).toURI();
            List<Path> paths = new ArrayList<>();
            try (FileSystem fileSystem = (uri.getScheme().equals("jar") ? FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap()) : null)) {
                Path myPath = Paths.get(uri);
                Files.walkFileTree(myPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!file.toString().endsWith(".k") || !file.toString().contains(folder)) {
                            throw new RuntimeException("Unexpected extension for file " + file);
                        }
                        String lol = file.toString().split(folder + "/")[1];
                        paths.add(new File("/" + folder + "/" + lol).toPath());
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
        int from = s.lastIndexOf('/', ind - 2);
        return trimPath(s.substring(0, from + 1) + s.substring(ind + 3));
    }
}
