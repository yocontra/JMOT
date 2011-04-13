package net.contra.obfuscator.util.misc;

import net.contra.obfuscator.Settings;

public class LogHandler {

    private final String Name;

    public LogHandler(String className) {
        Name = className;
    }

    public void log(String msg) {
        System.out.println("[" + Name + "]" + msg);
    }

    public void debug(String msg) {
        if (Settings.DEBUG) {
            System.out.println("[" + Name + "]" + "[DEBUG]" + msg);
        }
    }

    public void error(String msg) {
        System.out.println("[" + Name + "]" + "[ERROR]" + msg);
    }
}