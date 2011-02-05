package net.contra.obfuscator.trans;

public abstract class AbstractTransformer {

    public abstract void Load();

    public abstract void Transform();

    public abstract void Dump(String tag);
}
