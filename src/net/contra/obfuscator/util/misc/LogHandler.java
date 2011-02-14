package net.contra.obfuscator.util.misc;

import net.contra.obfuscator.Settings;

public class LogHandler {
    private String Name = "Adopted";

    public LogHandler(String className) {
        Name = className;
    }

    public void Log(String msg) {
        System.out.println("[" + Name + "]" + msg);
    }

    public void Debug(String msg) {
        if (Settings.Debug) {
            System.out.println("[" + Name + "]" + "[DEBUG]" + msg);
        }
    }

    public void Error(String msg) {
        System.out.println("[" + Name + "]" + "[ERROR]" + msg);
    }
}