package net.contra.obfuscator.util;

import net.contra.obfuscator.Settings;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Contra
 * Date: 2/7/11
 * Time: 8:36 AM
 */
public class Misc {
    public static String getRandomString(int length) {
        if (Settings.UseInvalid) {
            Random rand = new Random(System.currentTimeMillis());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < length; i++) {
                sb.append(new char[rand.nextInt(255)]);
            }
            return sb.toString();
        } else {
            String charset = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm";
            charset = charset + charset + charset + charset; //Ghetto but this will improve the random-ness
            Random rand = new Random(System.currentTimeMillis());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < length; i++) {
                sb.append(charset.charAt(rand.nextInt(charset.length())));
            }
            return "c" + sb.toString();
        }
    }
}
