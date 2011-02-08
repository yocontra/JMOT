package net.contra.obfuscator;

import net.contra.obfuscator.trans.ObfuscationType;

public class Settings {
    //Application Settings
    public static final double Version = 0.13;
    public static final boolean Debug = true;
    public static ObfuscationType ObfuscationLevel = ObfuscationType.Heavy;

    //String Obfuscation Settings
    public static final String CipherName = "hax";
    public static final String CipherArg = "s";
    public static final int CipherKey = 127;

    //Integer Complicator Settings
    public static final int Iterations = 2;
}
