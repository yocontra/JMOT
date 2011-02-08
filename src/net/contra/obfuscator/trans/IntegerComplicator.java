package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.util.JarLoader;
import net.contra.obfuscator.util.LogHandler;


public class IntegerComplicator implements ITransformer {
    LogHandler Logger = new LogHandler("AttributeObfuscator");
    private String Location = "";
    private JarLoader LoadedJar;

    public IntegerComplicator(String loc) {
        Location = loc;
    }

    public void Load() {
        LoadedJar = new JarLoader(Location);
    }

    public void Transform() {
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            for (Method method : cg.getMethods()) {
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                InstructionList list = mg.getInstructionList();
                if (list == null) continue;
                Logger.Log("Complicating Constant Integers -> Class: " + cg.getClassName() + " Method: " + method.getName());
                InstructionHandle[] handles = list.getInstructionHandles();
                for (InstructionHandle handle : handles) {
                    if (handle.getInstruction() instanceof ICONST
                            && (handle.getNext().getInstruction() instanceof PUTSTATIC
                            || handle.getNext().getInstruction() instanceof PUTFIELD
                            || handle.getNext().getInstruction() instanceof ISTORE)
                            && handle.getPrev().getInstruction() instanceof ALOAD) {
                        ICONST con = (ICONST) handle.getInstruction();
                        InstructionList nlist = new InstructionList();
                        nlist.append(new ICONST(1));
                        nlist.append(new IMUL());
                        list.append(handle, nlist);
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

    public void Dump() {
        LoadedJar.Save(Location.replace(".jar", "-new.jar"));
    }
}

