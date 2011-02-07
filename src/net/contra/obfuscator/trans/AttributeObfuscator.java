package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.JarLoader;
import net.contra.obfuscator.util.LogHandler;


public class AttributeObfuscator implements ITransformer {
    LogHandler Logger = new LogHandler("StringObfuscator");
    String Location = "";
    JarLoader LoadedJar;

    public AttributeObfuscator(String loc) {
        Location = loc;
    }

    public void Load() {
        LoadedJar = new JarLoader(Location);
    }

    public void Transform() {
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            for (Method method : cg.getMethods()) {
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                mg.removeCodeAttributes();
                mg.removeAttributes();
                mg.removeLineNumbers();
                mg.removeNOPs();
                mg.setMaxLocals();
                mg.setMaxStack();
                cg.replaceMethod(method, mg.getMethod());
            }
        }
    }

    public void Dump() {
        LoadedJar.Save(Location.replace(".jar", "-new.jar"));
    }
}

