package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.classfile.Attribute;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.ClassGen;
import com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import com.sun.org.apache.bcel.internal.generic.MethodGen;
import net.contra.obfuscator.ITransformer;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.bcel.JarLoader;
import net.contra.obfuscator.util.misc.LogHandler;
import net.contra.obfuscator.util.misc.Misc;


public class AttributeObfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("AttributeObfuscator");
    private String Location = "";
    private JarLoader LoadedJar;

    public AttributeObfuscator(String loc) {
        Location = loc;
    }

    public void load() {
        LoadedJar = new JarLoader(Location);
    }

    public void transform() {
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
                cg.replaceMethod(method, mg.getMethod());
                Logger.Log("Removed Attributes/Line Numbers -> Class: " + cg.getClassName() + " Method: " + method.getName());
            }
            for (Attribute at : cg.getAttributes()) {
                cg.removeAttribute(at);
            }
        }
    }

    public String save() {
        String loc = Location.replace(".jar", Settings.FileTag + ".jar");
        LoadedJar.saveJar(loc);
        return loc;
    }
}

