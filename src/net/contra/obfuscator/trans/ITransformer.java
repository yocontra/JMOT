package net.contra.obfuscator.trans;

import net.contra.obfuscator.util.LogHandler;

public interface ITransformer {

    LogHandler Logger = new LogHandler("Transformer");

    void Load();

    void Transform();

    void Dump();
}
