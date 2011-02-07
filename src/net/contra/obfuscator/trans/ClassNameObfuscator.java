package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.classfile.Constant;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.util.BCELMethods;
import net.contra.obfuscator.util.JarLoader;
import net.contra.obfuscator.util.LogHandler;
import net.contra.obfuscator.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class ClassNameObfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("ClassNameObfuscator");
    private final Map<String, String> ChangedClasses = new HashMap<String, String>();
    private String Location = "";
    private JarLoader LoadedJar;

    public ClassNameObfuscator(String loc) {
        Location = loc;
    }

    public void Load() {
        LoadedJar = new JarLoader(Location);
    }
    //TODO: NEW, FIELDS, NEWARRAY, EXCLUSIONS, NOT MAIN
    public void Transform() {
        //We rename methods
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            Map<String, String> NewClassMethods = new HashMap<String, String>();
            if (cg.isAbstract()) continue; //TODO: Probably more shit we shouldn't rename
            String newName = Misc.getRandomString(20);
            String oldName = cg.getClassName();
            cg.setClassName(newName);
            Logger.Log("Obfuscating Method Names -> Class: " + oldName + " - " + cg.getClassName());
            ChangedClasses.put(oldName, cg.getClassName());
        }
        //We fix all of the method/field calls
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            for (Method method : cg.getMethods()) {
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                InstructionList list = mg.getInstructionList();
                if (list == null) continue;
                Logger.Log("Fixing Method Calls -> Class: " + cg.getClassName() + " Method: " + method.getName());
                InstructionHandle[] handles = list.getInstructionHandles();
                for (InstructionHandle handle : handles) {
                    if (BCELMethods.isInvoke(handle.getInstruction())) {
                        String clazz = BCELMethods.getInvokeClassName(handle.getInstruction(), cg.getConstantPool());
                        String methname = BCELMethods.getInvokeMethodName(handle.getInstruction(), cg.getConstantPool());
                        String methsig = BCELMethods.getInvokeSignature(handle.getInstruction(), cg.getConstantPool());
                        if (!ChangedClasses.containsKey(clazz)) continue;
                        Logger.Debug("Class: " + clazz + " Name: " + methname + " Sig: " + methsig);
                        String newname = ChangedClasses.get(clazz);
                        int index = cg.getConstantPool().addMethodref(clazz, newname, methsig);
                        handle.setInstruction(BCELMethods.getNewInvoke(handle.getInstruction(), index));
                    } else if(BCELMethods.isFieldInvoke(handle.getInstruction())){
                        String clazz = BCELMethods.getInvokeClassName(handle.getInstruction(), cg.getConstantPool());
                        String fieldname = BCELMethods.getInvokeMethodName(handle.getInstruction(), cg.getConstantPool());
                        String fieldsig = BCELMethods.getInvokeSignature(handle.getInstruction(), cg.getConstantPool());
                        if (!ChangedClasses.containsKey(clazz)) continue;
                        Logger.Debug("Class: " + clazz + " Name: " + fieldname + " Sig: " + fieldsig);
                        String newname = ChangedClasses.get(clazz);
                        int index = cg.getConstantPool().addFieldref(clazz, newname, fieldsig);
                        handle.setInstruction(BCELMethods.getNewFieldInvoke(handle.getInstruction(), index));
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

    public void Dump() {
        LoadedJar.Save(Location.replace(".jar", "-new.jar"));
    }
}
