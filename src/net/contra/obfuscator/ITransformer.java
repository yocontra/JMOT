package net.contra.obfuscator;

import net.contra.obfuscator.util.misc.LogHandler;

public interface ITransformer {

    public LogHandler Logger = new LogHandler("Transformer");

    void Load();

    void Transform();

    String Dump();
}
