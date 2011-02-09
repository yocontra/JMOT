package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.classfile.Attribute;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.ClassGen;
import com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import com.sun.org.apache.bcel.internal.generic.MethodGen;
import net.contra.obfuscator.ITransformer;
import net.contra.obfuscator.util.JarLoader;
import net.contra.obfuscator.util.LogHandler;
import net.contra.obfuscator.util.Misc;


public class AttributeObfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("AttributeObfuscator");
    private String Location = "";
    private JarLoader LoadedJar;

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
                for (LocalVariableGen g : mg.getLocalVariables()) {
                    g.setName(Misc.getRandomName());
                }
                for (int i = 0; i < mg.getArgumentNames().length; i++) {
                    mg.setArgumentName(i, Misc.getRandomName());
                }
                for (Attribute at : cg.getAttributes()) {
                    cg.removeAttribute(at);
                }
                cg.replaceMethod(method, mg.getMethod());
                Logger.Log("Removed Attributes/Line Numbers -> Class: " + cg.getClassName() + " Method: " + method.getName());
            }
        }
    }

    public void Dump() {
        LoadedJar.Save(Location.replace(".jar", "-new.jar"));
    }
}

