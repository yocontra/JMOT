package net.contra.obfuscator.util;

import net.contra.obfuscator.Constants;

public class LogHandler {
    String Name = "Adopted";

    public LogHandler(String className) {
        Name = className;
    }

    public void Message(String msg) {
        System.out.println(msg);
    }

    public void Log(String msg) {
        System.out.println("[" + Name + "]" + msg);
    }

    public void Debug(String msg) {
        if (Constants.Debug) {
            System.out.println("[" + Name + "]" + "[DEBUG]" + msg);
        }
    }

    public void Error(String msg) {
        System.out.println("[" + Name + "]" + "[ERROR]" + msg);
    }
}