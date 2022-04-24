package com.willcro.folderdb.files;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;

public class FileUtils {

    public static String createSha1(File file) {
        try (InputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            DigestInputStream is = new DigestInputStream(fis, digest);
            while(is.read() != -1);
            return bytesToHex(digest.digest());
        } catch (Exception ex) {
            //todo
            return "";
        }
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    private static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static void runQuery(String queryName, Connection connection) {
        try {
            var query = new String(Files.readAllBytes(Paths.get(FileUtils.class.getResource(queryName).toURI())));
            connection.prepareStatement(query).execute();
        } catch (SQLException | URISyntaxException | IOException e) {
            // todo: exception handling
            throw new RuntimeException("Error occurred while running query from file", e);
        }
    }

}
