package net.contra.obfuscator.util.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IO {
    public static byte[] GetBytes(InputStream inputStream) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int len = 0;
            if (len < 0) {
                break;
            }
            try {
                len = inputStream.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (len < 0) {
                break;
            }
            bout.write(buffer, 0, len);
        }
        return bout.toByteArray();
    }
}
