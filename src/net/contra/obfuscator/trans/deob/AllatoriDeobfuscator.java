package net.contra.obfuscator.trans.deob;


import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.Application;
import net.contra.obfuscator.ITransformer;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.bcel.BCELMethods;
import net.contra.obfuscator.util.bcel.JarLoader;
import net.contra.obfuscator.util.misc.LogHandler;

public class AllatoriDeobfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("AllatoriDeobfuscator");
    private String Location = "";
    private JarLoader LoadedJar;
    private boolean isHeavy = false;
    private boolean isLight = false;

    public AllatoriDeobfuscator(String loc) {
        Location = loc;
    }

    public void load() {
        LoadedJar = new JarLoader(Location);
    }

    private String cipher(String string) {
        int i = 85;
        char[] cs = new char[string.length()];
        int pos = cs.length - 1;
        int index = pos;
        int xor = i;
        while (pos >= 0) {
            char c = (char) (string.charAt(index) ^ xor);
            int c1_index = index;
            xor = (char) ((char) (c1_index ^ xor) & '?');
            cs[c1_index] = c;
            if (--index < 0) {
                break;
            }
            char c2 = (char) (string.charAt(index) ^ xor);
            int c2_index = index;
            xor = (char) ((char) (c2_index ^ xor) & '?');
            cs[c2_index] = c2;
            pos = --index;
        }

        return new String(cs);
    }

    private String cipherContext(String encrypted, String callingClass, String callingMethod) {
        String keyString = callingClass + callingMethod;
        int lastKeyIndex = keyString.length() - 1;
        int xor = 85;
        int keyIndex = lastKeyIndex;
        int length = encrypted.length();
        char[] cs = new char[length];
        for (int i = length - 1; i >= 0; i--) {
            if (keyIndex < 0) {
                keyIndex = lastKeyIndex;
            }
            char keyChar = keyString.charAt(keyIndex--);
            cs[i] = (char) (keyChar ^ (encrypted.charAt(i) ^ xor));
            xor = (char) (63 & (xor ^ (i ^ keyChar)));
        }
        return new String(cs);
    }

    private ClassGen getAllatoriClassGen(JarLoader jr) {
        for (ClassGen cg : jr.ClassEntries.values()) {
            if (cg.getMethods().length == 2 && cg.getMethods()[0].isStatic() && cg.getMethods()[1].isStatic()) {
                if (cg.getMethods()[0].getReturnType().toString().equals("java.lang.String")
                        && cg.getMethods()[1].getReturnType().toString().equals("java.lang.String")) {
                    return cg;
                }
            }
        }
        return null;
    }

    public void transform() {
        ClassGen hashClass = getAllatoriClassGen(LoadedJar);
        if (hashClass == null) {
            Logger.error("Could not locate Allatori cipher class.");
            Logger.error("This is not obfuscated with Allatori.");
            Application.close();
        } else {
            Logger.debug("Allatori Class ID: " + hashClass.getClassName());
        }
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            for (Method method : cg.getMethods()) {
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                InstructionList list = mg.getInstructionList();
                if (list == null) continue;
                Logger.debug("Stripping Allatori Calls -> Class: " + cg.getClassName() + " Method: " + method.getName());
                InstructionHandle[] handles = list.getInstructionHandles();
                for (InstructionHandle handle : handles) {
                    if (handle.getNext() == null) continue;
                    if (handle.getInstruction() instanceof LDC &&
                            handle.getNext().getInstruction() instanceof INVOKESTATIC) {
                        INVOKESTATIC invs = (INVOKESTATIC) handle.getNext().getInstruction();
                        assert hashClass != null;
                        if (!BCELMethods.getInvokeClassName(invs, cg.getConstantPool()).equals(hashClass.getClassName()))
                            continue;
                        if (!isLight && !isHeavy) {
                            if (!BCELMethods.getInvokeSignature(invs, cg.getConstantPool()).equals(hashClass.getMethods()[0].getSignature())) {
                                isLight = true;
                                Logger.log("Light string obfuscation detected!");
                            } else {
                                isHeavy = true;
                                Logger.log("Heavy string obfuscation detected!");
                            }
                        }
                        String original = (String) ((LDC) handle.getInstruction()).getValue(cg.getConstantPool());
                        String deciphered;
                        if (isHeavy) {
                            deciphered = cipherContext(original, cg.getClassName(), mg.getName());
                        } else {
                            deciphered = cipher(original);
                        }
                        int idx = cg.getConstantPool().addString(deciphered); //Add our new string
                        handle.getNext().setInstruction(new NOP()); //Get rid of the invoke
                        handle.setInstruction(new LDC(idx)); //Replace old LDC with new LDC
                        Logger.debug("\"" + original + "\" -> \"" + deciphered + "\"");
                    }
                }
                list.setPositions();
                mg.setInstructionList(list);
                mg.setMaxLocals();
                mg.setMaxStack();
                cg.replaceMethod(method, mg.getMethod());
            }
        }
    }

    public void save() {
        String loc = Location.replace(".jar", Settings.FILE_TAG + ".jar");
        LoadedJar.saveJar(loc);

    }
}
