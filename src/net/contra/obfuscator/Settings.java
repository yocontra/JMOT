package net.contra.obfuscator;

import net.contra.obfuscator.trans.ObfuscationType;

public class Settings {
    //Application Settings
    public static final double Version = 0.14; //Don't Touch
    public static final boolean Debug = true;
    public static final ObfuscationType ObfuscationLevel = ObfuscationType.Normal;

    //String Obfuscation Settings
    public static final String CipherName = "hax";
    public static final String CipherArg = "s";
    public static int[] CipherKeys = {}; //Don't Touch

    //Integer Complicator Settings
    public static int Iterations = 0; //Don't Touch
}
