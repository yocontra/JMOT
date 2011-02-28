package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.ITransformer;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.bcel.JarLoader;
import net.contra.obfuscator.util.misc.LogHandler;


public class StringObfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("StringObfuscator");
    private String Location = "";
    private JarLoader LoadedJar;

    public StringObfuscator(String loc) {
        Location = loc;
    }

    public void load() {
        LoadedJar = new JarLoader(Location);
    }

    public void transform() {
        for (int i = 0; i < Settings.CipherKeys.length; i++) {
            for (ClassGen cg : LoadedJar.ClassEntries.values()) {
                MethodGen cryptor = getDecryptor(cg, i);
                for (Method method : cg.getMethods()) {
                    MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                    InstructionList list = mg.getInstructionList();
                    if (list == null) continue;
                    Logger.Log("Obfuscating Strings -> Class: " + cg.getClassName() + " Method: " + method.getName());
                    InstructionHandle[] handles = list.getInstructionHandles();
                    for (InstructionHandle handle : handles) {
                        if (handle.getInstruction() instanceof LDC) {
                            try {
                                String orig = ((LDC) handle.getInstruction()).getValue(cg.getConstantPool()).toString();
                                int index = cg.getConstantPool().addString(getCiphered(orig, Settings.CipherKeys[i]));
                                handle.setInstruction(new LDC(index));
                                list.append(handle, new INVOKESTATIC(cg.getConstantPool().addMethodref(cryptor)));
                            } catch (Exception e) {
                                Logger.Debug("Caught error, skipping instruction.");
                            }
                        }
                    }
                    list.setPositions();
                    mg.setInstructionList(list);
                    mg.setMaxLocals();
                    mg.setMaxStack();
                    cg.replaceMethod(method, mg.getMethod());
                }
                if (cg.containsMethod(cryptor.getName(), cryptor.getSignature()) == null) {
                    Logger.Log("Injecting Cipher Method -> Class: " + cg.getClassName());
                    cg.addMethod(cryptor.getMethod());
                } else {
                    Logger.Error("Cipher Method Already Exists! -> Class: " + cg.getClassName());
                }
            }
        }
    }

    public String save() {
        String loc = Location.replace(".jar", Settings.FileTag + ".jar");
        LoadedJar.saveJar(loc);
        return loc;
    }

    String getCiphered(String input, int key) {
        char[] inputChars = input.toCharArray();
        for (int i = 0; i < inputChars.length; i++) {
            inputChars[i] = (char) (inputChars[i] ^ key);
        }
        return new String(inputChars);
    }

    MethodGen getDecryptor(ClassGen cg, int i) {
        InstructionList il = new InstructionList();
        InstructionFactory fa = new InstructionFactory(cg);
        il.append(new ALOAD(0));
        il.append(fa.createInvoke("java.lang.String", "toCharArray", new ArrayType(Type.CHAR, 1), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        il.append(new ASTORE(2));
        il.append(new ICONST(0));
        il.append(new ISTORE(3));
        il.append(new ILOAD(3));
        il.append(new ALOAD(2));
        il.append(new ARRAYLENGTH());
        il.append(new IF_ICMPGE(il.getInstructionHandles()[0]));//Placeholder, to be replaced
        il.append(new ALOAD(2));
        il.append(new ILOAD(3));
        il.append(new ALOAD(2));
        il.append(new ILOAD(3));
        il.append(new CALOAD());
        il.append(new BIPUSH((byte) Settings.CipherKeys[i]));
        il.append(new IXOR());
        il.append(new I2C());
        il.append(new CASTORE());
        il.append(new IINC(3, 1));
        il.append(new GOTO(il.getInstructionHandles()[5]));
        il.append(fa.createNew(ObjectType.STRING));
        il.append(new DUP());
        il.append(new ALOAD(2));
        il.append(fa.createInvoke("java.lang.String", "<init>", Type.VOID, new Type[]{new ArrayType(Type.CHAR, 1)}, Constants.INVOKESPECIAL));
        il.append(new ARETURN());
        il.getInstructionHandles()[8].setInstruction(new IF_ICMPGE(il.getInstructionHandles()[20]));
        il.setPositions();

        MethodGen mg = new MethodGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC, Type.STRING, new Type[]{Type.STRING},
                new String[]{Settings.CipherArg}, Settings.CipherName + i, cg.getClassName(), il, cg.getConstantPool());
        mg.setMaxLocals();
        mg.setMaxStack();
        return mg;
    }
}
