package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.BCELMethods;
import net.contra.obfuscator.util.JarLoader;
import net.contra.obfuscator.util.LogHandler;
import net.contra.obfuscator.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class FieldNameObfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("FieldNameObfuscator");
    //ClassName, <OldSig, NewSig>
    private final Map<String, Map<String[], String[]>> ChangedFields = new HashMap<String, Map<String[], String[]>>();
    private String Location = "";
    private JarLoader LoadedJar;

    public FieldNameObfuscator(String loc) {
        Location = loc;
    }

    public void Load() {
        LoadedJar = new JarLoader(Location);
    }

    public void Transform() {
        //We rename methods
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            Map<String[], String[]> NewFields = new HashMap<String[], String[]>();
            if (cg.isAbstract()) continue;
            for (Field field : cg.getFields()) {
                if (field.isInterface() || field.isAbstract())
                    continue;
                FieldGen fg = new FieldGen(field, cg.getConstantPool());
                String newName = Misc.getRandomString(20);
                fg.setName(newName);
                cg.replaceField(field, fg.getField());
                NewFields.put(new String[] {field.getName(), field.getSignature()}, new String[] { fg.getName(), fg.getSignature()});
                Logger.Log("Obfuscating Field Names -> Class: " + cg.getClassName() + " Field: " + field.getName());
            }
            ChangedFields.put(cg.getClassName(), NewFields);
        }
        //We fix all of the field calls
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            for (Method method : cg.getMethods()) {
                MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                InstructionList list = mg.getInstructionList();
                if (list == null) continue;
                Logger.Log("Fixing Field Calls -> Class: " + cg.getClassName() + " Method: " + method.getName());
                InstructionHandle[] handles = list.getInstructionHandles();
                for (InstructionHandle handle : handles) {
                    if (BCELMethods.isFieldInvoke(handle.getInstruction())) {
                        String clazz = BCELMethods.getFieldInvokeClassName(handle.getInstruction(), cg.getConstantPool());
                        String fname = BCELMethods.getFieldInvokeName(handle.getInstruction(), cg.getConstantPool());
                        String fsig = BCELMethods.getFieldInvokeSignature(handle.getInstruction(), cg.getConstantPool());

                        if (!ChangedFields.containsKey(clazz)) continue;
                        Map<String[], String[]> classFields = ChangedFields.get(clazz);
                        if (classFields.get(new String[]{fname, fsig}) != null) {
                            String newname = classFields.get(new String[] {fname, fsig})[0];
                            int index = cg.getConstantPool().addFieldref(clazz, newname, fsig);
                            handle.setInstruction(BCELMethods.getNewFieldInvoke(handle.getInstruction(), index));
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

    public void Dump() {
        LoadedJar.Save(Location.replace(".jar", "-new.jar"));
    }
}
