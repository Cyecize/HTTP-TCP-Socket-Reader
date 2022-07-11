package com.cyecize.httpreader.util;

import com.cyecize.httpreader.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

/**
 * Simple utility class for reading and writing files from/to the resource folder.
 */
public class FileUtils {
    public static boolean exist(String fileName) {
        return Constants.class.getResource(Constants.FILES_DIR + fileName) != null;
    }

    public static String probeContentType(String fileName) {
        final String[] tokens = fileName.split("\\.");
        final String extension = tokens[tokens.length - 1];

        switch (extension) {
            case "html": return "text/html";
            case "txt": return "text/plain";
            case "css": return "text/css";
            case "gif": return "image/gif";
            case "jpg": return "image/jpeg";
            case "jpeg": return "image/jpeg";
            case "js": return "text/javascript";
            case "json": return "application/json";
            case "mp4": return "video/mp4";
            case "png": return "image/png";
        }

        return "text/plain";
    }

    public static InputStream getInputStream(String fileName) {
        return Constants.class.getResourceAsStream(Constants.FILES_DIR + fileName);
    }

    public static void saveFile(String fileName, String base64) {
        final String filePath = Constants.FILES_DIR + fileName;
        final File physicalFile = new File(filePath);

        try {
            physicalFile.getParentFile().mkdirs();
            physicalFile.createNewFile();
            try (OutputStream stream = new FileOutputStream(physicalFile)) {
                if (base64.contains(",")) {
                    base64 = base64.split(",")[1];
                }

                stream.write(Base64.getMimeDecoder().decode(base64));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
