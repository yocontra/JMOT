package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.ClassGen;
import com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.bcel.internal.generic.MethodGen;
import net.contra.obfuscator.ITransformer;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.bcel.BCELMethods;
import net.contra.obfuscator.util.bcel.JarLoader;
import net.contra.obfuscator.util.misc.LogHandler;
import net.contra.obfuscator.util.misc.Misc;
import net.contra.obfuscator.util.misc.RenamedPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MethodNameObfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("MethodNameObfuscator");
    //ClassName, <OldSig, NewSig>
    private final Map<String, ArrayList<RenamedPair>> ChangedMethods = new HashMap<String, ArrayList<RenamedPair>>();
    private String Location = "";
    private JarLoader LoadedJar;

    public MethodNameObfuscator(String loc) {
        Location = loc;
    }

    public void Load() {
        LoadedJar = new JarLoader(Location);
    }

    public void Transform() {
        //We rename methods
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            ArrayList<RenamedPair> NewClassMethods = new ArrayList<RenamedPair>();
            if (cg.isAbstract()) continue; //TODO: Probably more shit we shouldn't rename
            for (Method method : cg.getMethods()) {
                if (method.isInterface() || method.isAbstract() || method.getName().endsWith("init>") || method.getName().equals("main"))
                    continue; //TODO: Probably more shit we shouldn't rename
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                String newName = Misc.getRandomName();
                mg.setName(newName);
                cg.replaceMethod(method, mg.getMethod());
                RenamedPair pair = new RenamedPair(method.getName(), method.getSignature(), mg.getName());
                NewClassMethods.add(pair);
                Logger.Log("Obfuscating Method Names -> Class: " + cg.getClassName() + " Method: " + method.getName());
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
                    if (BCELMethods.isInvoke(handle.getInstruction())) {
                        String clazz = BCELMethods.getInvokeClassName(handle.getInstruction(), cg.getConstantPool()).trim();
                        String methname = BCELMethods.getInvokeMethodName(handle.getInstruction(), cg.getConstantPool()).trim();
                        String methsig = BCELMethods.getInvokeSignature(handle.getInstruction(), cg.getConstantPool()).trim();

                        if (!ChangedMethods.containsKey(clazz)) continue;
                        for (RenamedPair pair : ChangedMethods.get(clazz)) {
                            if (pair.OldName.equals(methname) && pair.OldSignature.equals(methsig)) {
                                int index = cg.getConstantPool().addMethodref(clazz, pair.NewName, pair.OldSignature);
                                handle.setInstruction(BCELMethods.getNewInvoke(handle.getInstruction(), index));
                            }
                        }
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

    public String Dump() {
        String loc = Location.replace(".jar", Settings.FileTag + ".jar");
        LoadedJar.Save(loc);
        return loc;
    }
}
