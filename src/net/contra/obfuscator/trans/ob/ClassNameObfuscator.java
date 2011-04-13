package net.contra.obfuscator.trans.ob;

import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.ITransformer;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.bcel.BCELMethods;
import net.contra.obfuscator.util.bcel.JarLoader;
import net.contra.obfuscator.util.misc.LogHandler;
import net.contra.obfuscator.util.misc.Misc;

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

    public void load() {
        LoadedJar = new JarLoader(Location);
    }

    //TODO: NEW, FIELDS, NEWARRAY, EXCLUSIONS, NOT MAIN
    public void transform() {
        //We rename methods
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            if (cg.isAbstract()) continue; //TODO: Probably more shit we shouldn't rename
            String newName = Misc.getRandomClassName();
            byte[] manifest = LoadedJar.NonClassEntries.get("META-INF/MANIFEST.MF");
            if (manifest != null) {
                String man = new String(manifest);
                if (man.contains("Main-Class: " + cg.getClassName())) {
                    Logger.debug("Updating Manifest -> Main Class: " + cg.getClassName());
                    man = man.replace("Main-Class: " + cg.getClassName(), "Main-Class: " + newName);
                    LoadedJar.NonClassEntries.put("META-INF/MANIFEST.MF", man.getBytes());
                }
            }
            String oldName = cg.getClassName();
            cg.setClassName(newName);
            Logger.log("Obfuscating Method Names -> Class: " + oldName + " - " + cg.getClassName());
            ChangedClasses.put(oldName, cg.getClassName());
        }
        //We fix all of the method/field calls
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            for (Method method : cg.getMethods()) {
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                InstructionList list = mg.getInstructionList();
                if (list == null) continue;
                Logger.log("Fixing Method Calls -> Class: " + cg.getClassName() + " Method: " + method.getName());
                InstructionHandle[] handles = list.getInstructionHandles();
                for (InstructionHandle handle : handles) {
                    if (BCELMethods.isInvoke(handle.getInstruction())) {
                        String clazz = BCELMethods.getInvokeClassName(handle.getInstruction(), cg.getConstantPool());
                        String methname = BCELMethods.getInvokeMethodName(handle.getInstruction(), cg.getConstantPool());
                        String methsig = BCELMethods.getInvokeSignature(handle.getInstruction(), cg.getConstantPool());
                        if (!ChangedClasses.containsKey(clazz)) continue;
                        Logger.debug("Swapping Call -> Class: " + clazz + " Name: " + methname + " Sig: " + methsig);
                        String newname = ChangedClasses.get(clazz);
                        int index = cg.getConstantPool().addMethodref(newname, methname, methsig);
                        handle.setInstruction(BCELMethods.getNewInvoke(handle.getInstruction(), index));
                    } else if (BCELMethods.isFieldInvoke(handle.getInstruction())) {
                        String clazz = BCELMethods.getFieldInvokeClassName(handle.getInstruction(), cg.getConstantPool());
                        String fieldname = BCELMethods.getFieldInvokeName(handle.getInstruction(), cg.getConstantPool());
                        String fieldsig = BCELMethods.getFieldInvokeSignature(handle.getInstruction(), cg.getConstantPool());
                        if (!ChangedClasses.containsKey(clazz)) continue;
                        Logger.debug("Swapping Call -> Class: " + clazz + " Name: " + fieldname + " Sig: " + fieldsig);
                        String newname = ChangedClasses.get(clazz);
                        int index = cg.getConstantPool().addFieldref(newname, fieldname, fieldsig);
                        handle.setInstruction(BCELMethods.getNewFieldInvoke(handle.getInstruction(), index));
                    } else if (handle.getInstruction() instanceof NEW) {
                        NEW in = ((NEW) handle.getInstruction());
                        String clazz = in.getLoadClassType(cg.getConstantPool()).getClassName();
                        if (!ChangedClasses.containsKey(clazz)) continue;
                        String newname = ChangedClasses.get(clazz);
                        int index = cg.getConstantPool().addClass(newname);
                        NEW out = new NEW(index);
                        handle.setInstruction(out);
                    }
                }
                list.setPositions();
                mg.setInstructionList(list);
                mg.removeLocalVariables();
                mg.setMaxLocals();
                mg.setMaxStack();
                cg.replaceMethod(method, mg.getMethod());
            }
        }
    }

    public String save() {
        String loc = Location.replace(".jar", Settings.FILE_TAG + ".jar");
        LoadedJar.saveJar(loc);
        return loc;
    }
}
