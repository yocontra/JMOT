package net.contra.obfuscator.trans;

/**
 * Created by IntelliJ IDEA.
 * User: Contra
 * Date: 2/4/11
 * Time: 8:05 PM
 */
public abstract class AbstractTransformer {

    public abstract void Load();

    public abstract void Transform();

    public abstract void Dump(String tag);
}
