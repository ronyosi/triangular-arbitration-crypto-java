package com.webhopper.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static java.nio.file.Files.readAllBytes;

public class FileUtils {

    public static String openFile(String path) throws IOException {
        final File file = new File(path);

        if(!file.exists()) {
            return null;
        }

        return new String(readAllBytes(file.toPath()));
    }

    public static void writeFile(String content, String pathName) {
        Path path = Paths.get(pathName);
        byte[] strToBytes = content.getBytes();

        try {
            Files.write(path, strToBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
