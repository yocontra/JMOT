package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.ITransformer;
import net.contra.obfuscator.ObfuscationType;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.bcel.BCELMethods;
import net.contra.obfuscator.util.bcel.JarLoader;
import net.contra.obfuscator.util.misc.LogHandler;


public class IntegerBoxer implements ITransformer {
    private final LogHandler Logger = new LogHandler("AttributeObfuscator");
    private String Location = "";
    private JarLoader LoadedJar;

    public IntegerBoxer(String loc) {
        Location = loc;
    }

    public void Load() {
        LoadedJar = new JarLoader(Location);
    }

    public void Transform() {
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            MethodGen boxer = getBoxer(cg);
            INVOKESTATIC inv = new INVOKESTATIC(cg.getConstantPool().addMethodref(boxer));
            for (Method method : cg.getMethods()) {
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                InstructionList list = mg.getInstructionList();
                if (list == null) continue;
                Logger.Log("Boxing Constant Integers -> Class: " + cg.getClassName() + " Method: " + method.getName());
                InstructionHandle[] handles = list.getInstructionHandles();
                for (InstructionHandle handle : handles) {
                    if (handle.getInstruction() instanceof ICONST
                            || handle.getInstruction() instanceof BIPUSH
                            || handle.getInstruction() instanceof SIPUSH
                            || handle.getInstruction() instanceof ILOAD
                            || (handle.getInstruction() instanceof LDC && handle.getNext().getInstruction() instanceof IASTORE)) {
                        int curValue = BCELMethods.getIntegerValue(handle.getInstruction());
                        InstructionHandle nh;
                        //If it is an even int, it gets replaced with half it's value and appends
                        //an int of the key
                        if (curValue != -9001 && (curValue % 2 == 0)
                                && Settings.ObfuscationLevel.getLevel() > ObfuscationType.Normal.getLevel()) { //check if it's even.
                            int tempkey = curValue / 2;
                            Logger.Debug("Value: " + curValue + " Key: " + tempkey);
                            Instruction newIns = BCELMethods.getIntegerLoad(handle.getInstruction(), tempkey);
                            handle.setInstruction(newIns);
                            if (tempkey <= 5 && tempkey >= -1) {
                                nh = list.append(handle, new ICONST(tempkey));
                            } else {
                                nh = list.append(handle, newIns);
                            }
                        } else {
                            //Otherwise just append a 0 key and keep the number the same
                            nh = list.append(handle, new ICONST(0));
                        }
                        list.append(nh, inv);
                    }
                }
                list.setPositions();
                mg.setInstructionList(list);
                mg.setMaxLocals();
                mg.setMaxStack();
                cg.replaceMethod(method, mg.getMethod());
            }
            if (cg.containsMethod(boxer.getName(), boxer.getSignature()) == null) {
                Logger.Log("Injecting Boxer Method -> Class: " + cg.getClassName());
                cg.addMethod(boxer.getMethod());
            } else {
                Logger.Error("Boxer Method Already Exists! -> Class: " + cg.getClassName());
            }
        }
    }

    MethodGen getBoxer(ClassGen cg) {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_STATIC, Type.INT, new Type[]{Type.INT, Type.INT},
                new String[]{Settings.BoxerArg, Settings.BoxerArg + "i"}, Settings.BoxerName, cg.getClassName(), il, cg.getConstantPool());
        il.append(InstructionFactory.createLoad(Type.INT, 0));
        il.append(InstructionFactory.createLoad(Type.INT, 1));
        il.append(new IADD());
        il.append(InstructionFactory.createReturn(Type.INT));
        method.setMaxStack();
        method.setMaxLocals();
        return method;
    }

    public void Dump() {
        LoadedJar.Save(Location.replace(".jar", Settings.FileTag + ".jar"));
    }
}

