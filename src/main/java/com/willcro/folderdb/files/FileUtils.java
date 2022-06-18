package com.willcro.folderdb.files;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Timer;

@Slf4j
public class FileUtils {

    public static String createSha1(File file) {
        var timeStart = System.currentTimeMillis();

        try (InputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            DigestInputStream is = new DigestInputStream(fis, digest);
            var buf = new byte[4096];
            while(is.read(buf, 0, 4096) != -1);

            var timeEnd = System.currentTimeMillis();
            log.info("Hash of {} took {}ms", file.getName(), (timeEnd - timeStart));

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
        byte[] data = {};
        try (InputStream in = FileUtils.class.getResourceAsStream(queryName)) {
            var os = new ByteArrayOutputStream();
            in.transferTo(os);
            var query = os.toString(StandardCharsets.UTF_8);
            connection.prepareStatement(query).execute();
        } catch (SQLException | IOException e) {
            // todo: exception handling
            throw new RuntimeException("Error occurred while running query from file", e);
        }
    }

}
