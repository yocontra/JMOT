package net.contra.obfuscator.trans.deob;

import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.Application;
import net.contra.obfuscator.ITransformer;
import net.contra.obfuscator.util.bcel.BCELMethods;
import net.contra.obfuscator.util.bcel.JarLoader;
import net.contra.obfuscator.util.misc.LogHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JShrinkDeobfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("AllatoriDeobfuscator");
    private String Location = "";
    private JarLoader LoadedJar;

    public JShrinkDeobfuscator(String loc) {
        Location = loc;
    }

    public void load() {
        LoadedJar = new JarLoader(Location);
    }

    private ClassGen getShrinkClassGen(JarLoader jr) {
        for (ClassGen cg : jr.ClassEntries.values()) {
            if (cg.getMethods().length == 3 && cg.getMethods()[1].isStatic()
                    && cg.getMethods()[1].isFinal()
                    && cg.getMethods()[1].isPublic()
                    && cg.getMethods()[1].isSynchronized()
                    && cg.getMethods()[1].getReturnType().toString().equals("java.lang.String")) {
                return cg;
            }
        }
        return null;
    }

    public void transform() {
        ClassGen shrinkClass = getShrinkClassGen(LoadedJar);
        if (shrinkClass == null) {
            Logger.error("Could not locate JShrink loader class.");
            Logger.error("This is not obfuscated with JShrink.");
            Application.close();
        } else {
            Logger.debug("JShrink Class: " + shrinkClass.getClassName());
        }
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            for (Method method : cg.getMethods()) {
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                InstructionList list = mg.getInstructionList();
                if (list == null) continue;
                Logger.debug("Stripping JShrink Calls -> Class: " + cg.getClassName() + " Method: " + method.getName());
                InstructionHandle[] handles = list.getInstructionHandles();
                for (InstructionHandle handle : handles) {
                    if (handle.getNext() == null) continue;
                    if (BCELMethods.isInteger(handle.getInstruction())
                            && handle.getNext().getInstruction() instanceof INVOKESTATIC) {
                        assert shrinkClass != null;
                        if(!BCELMethods.getInvokeClassName(handle.getNext().getInstruction(), cg.getConstantPool()).equals(shrinkClass.getClassName())) continue;
                        int storeidx = BCELMethods.getIntegerValue(handle.getInstruction());
                        StoreHandler store = new StoreHandler(LoadedJar);
                        String orig = store.getString(storeidx);
                        int stridx = cg.getConstantPool().addString(orig);
                        handle.getNext().setInstruction(new NOP()); //Remove invoke to shrink class
                        handle.setInstruction(new LDC(stridx)); //Replace old str with our new one
                        logger.debug(storeidx + " -> " + orig);
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
    }
}

class StoreHandler {
    static byte[] WSVZ;
    static String[] append = new String[256];
    static int[] close = new int[256];

    public StoreHandler(JarLoader loader) {
        try {
            InputStream localInputStream = new ByteArrayInputStream(loader.NonClassEntries.get("I/I.gif"));
            int i = localInputStream.read() << 16 | localInputStream.read() << 8 | localInputStream.read();
            WSVZ = new byte[i];
            int j = 0;
            int k = (byte) i;
            byte[] arrayOfByte = WSVZ;
            while (i != 0) {
                int m = localInputStream.read(arrayOfByte, j, i);
                if (m == -1) {
                    break;
                }
                i -= m;
                m += j;
                while (j < m) {
                    int int2 = j;
                    arrayOfByte[int2] = (byte) (arrayOfByte[int2] ^ k);
                    j++;
                }
            }
            localInputStream.reset();
            localInputStream.close();
        } catch (Exception ignored) {
        }
    }

    public String getString(int paramInt) {
        int i = paramInt & 0xFF;
        if (close[i] != paramInt) {
            close[i] = paramInt;
            if (paramInt < 0) {
                paramInt &= 65535;
            }
            String str = new String(WSVZ, paramInt, WSVZ[(paramInt - 1)] & 0xFF).intern();
            append[i] = str;
        }
        return append[i];
    }
}
