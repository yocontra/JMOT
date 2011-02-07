package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.JarLoader;
import net.contra.obfuscator.util.LogHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MethodNameObfuscator implements ITransformer {
    LogHandler Logger = new LogHandler("MethodNameObfuscator");
    //ClassName, <OldSig, NewSig>
    Map<String, Map<String, String>> ChangedMethods = new HashMap<String, Map<String, String>>();
    String Location = "";
    JarLoader LoadedJar;

    public MethodNameObfuscator(String loc) {
        Location = loc;
    }

    public void Load() {
        LoadedJar = new JarLoader(Location);
    }

    public static String getRandomString(int length) {
        /*
        String charset = "!0123456789abcdefghijklmnopqrstuvwxyz";
        Random rand = new Random(System.currentTimeMillis());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int pos = rand.nextInt(charset.length());
            sb.append(charset.charAt(pos));
        }
        return sb.toString();  */
        Random rand = new Random(System.currentTimeMillis());
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < length; i++){
            sb.append(new char[rand.nextInt(255)]);
        }
        return sb.toString();
    }

    public void Transform() {
        //We rename methods
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            Map<String, String> NewClassMethods = new HashMap<String, String>();
            if (cg.isAbstract()) continue; //TODO: Probably more shit we shouldn't rename
            for (Method method : cg.getMethods()) {
                if (method.isInterface() || method.isAbstract() || method.getName().endsWith("init>") || method.getName().equals("main"))
                    continue; //TODO: Probably more shit we shouldn't rename
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                String newName = getRandomString(20);
                mg.setName(newName);
                cg.replaceMethod(method, mg.getMethod());
                NewClassMethods.put(getBuffered(method.getName(), method.getSignature()), getBuffered(mg.getName(), mg.getSignature()));
                Logger.Log("Obfuscating Names -> Class: " + cg.getClassName() + " Method: " + method.getName() + " <=> " + mg.getName());
            }
            ChangedMethods.put(cg.getClassName(), NewClassMethods);
        }
        //We fix all of the method calls
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            for (Method method : cg.getMethods()) {
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                InstructionList list = mg.getInstructionList();
                if (list == null) continue;
                Logger.Log("Fixing Method Calls -> Class: " + cg.getClassName() + " Method: " + method.getName());
                InstructionHandle[] handles = list.getInstructionHandles();
                for (InstructionHandle handle : handles) {
                    if (handle.getInstruction() instanceof INVOKESTATIC) {
                        INVOKESTATIC inv = (INVOKESTATIC) handle.getInstruction();
                        String clazz = inv.getClassName(cg.getConstantPool());
                        String methname = inv.getMethodName(cg.getConstantPool());
                        String methsig = inv.getSignature(cg.getConstantPool());

                        if (!ChangedMethods.containsKey(clazz)) continue;
                        Map<String, String> classmeths = ChangedMethods.get(clazz);
                        Logger.Debug("Class: " + clazz + " Name: " + methname + " Sig: " + methsig);
                        if (classmeths.get(getBuffered(methname, methsig)) != null) {
                            String newname = classmeths.get(getBuffered(methname, methsig)).split(Settings.TempBuffer)[0];
                            INVOKESTATIC newinv = new INVOKESTATIC(cg.getConstantPool().addMethodref(clazz, newname, methsig));
                            handle.setInstruction(newinv);
                        }
                    } else if (handle.getInstruction() instanceof INVOKEVIRTUAL) {
                        INVOKEVIRTUAL inv = (INVOKEVIRTUAL) handle.getInstruction();
                        String clazz = inv.getClassName(cg.getConstantPool());
                        String methname = inv.getMethodName(cg.getConstantPool());
                        String methsig = inv.getSignature(cg.getConstantPool());

                        if (!ChangedMethods.containsKey(clazz)) continue;
                        Map<String, String> classmeths = ChangedMethods.get(clazz);
                        Logger.Debug("Class: " + clazz + " Name: " + methname + " Sig: " + methsig);
                        if (classmeths.get(getBuffered(methname, methsig)) != null) {
                            String newname = classmeths.get(getBuffered(methname, methsig)).split(Settings.TempBuffer)[0];
                            INVOKEVIRTUAL newinv = new INVOKEVIRTUAL(cg.getConstantPool().addMethodref(clazz, newname, methsig));
                            handle.setInstruction(newinv);
                        }
                    }
                }
                list.setPositions();
                mg.setInstructionList(list);
                mg.setMaxLocals();
                mg.setMaxStack();
                mg.removeLineNumbers();
                cg.replaceMethod(method, mg.getMethod());
            }
        }
    }

    public String getBuffered(String name, String sig) {
        return name + Settings.TempBuffer + sig;
    }

    public void Dump() {
        LoadedJar.Save(Location.replace(".jar", "-new.jar"));
    }
}
