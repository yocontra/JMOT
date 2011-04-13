package net.contra.obfuscator;

import net.contra.obfuscator.util.misc.LogHandler;

public interface ITransformer {

    public LogHandler logger = new LogHandler("Transformer");

    void load();

    void transform();

    void save();
}
