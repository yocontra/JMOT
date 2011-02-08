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
    public static String getRandomName() {
        switch (Settings.ObfuscationLevel) {
            case Light:
                return getRandomString(5, true);
            case Normal:
                return getRandomString(8, true);
            case Heavy:
                return getRandomString(5, false);
            case Insane:
                return getRandomString(10, false);
            default:
                return getRandomString(5, true);
        }
    }

    public static String getRandomClassName() {
        switch (Settings.ObfuscationLevel) {
            case Light:
                return getRandomString(8, true);
            case Normal:
                return getRandomString(10, true);
            case Heavy:
                return getRandomString(15, true);
            case Insane:
                return getRandomString(20, true);
            default:
                return getRandomString(8, true);
        }
    }

    public static String getRandomString(int length, boolean simple) {
        if (!simple) {
            Random rand = new Random(System.currentTimeMillis());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < length; i++) {
                sb.append(new char[rand.nextInt(255)]);
            }
            return sb.toString();
        } else {
            String charset = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890$_";
            Random rand = new Random(System.currentTimeMillis());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < length; i++) {
                sb.append(charset.charAt(rand.nextInt(charset.length())));
            }
            return "c" + sb.toString();
        }
    }
}
