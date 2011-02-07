package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import net.contra.obfuscator.Settings;
import net.contra.obfuscator.util.JarLoader;
import net.contra.obfuscator.util.LogHandler;
import net.contra.obfuscator.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class FieldNameObfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("FieldNameObfuscator");
    //ClassName, <OldSig, NewSig>
    private final Map<String, Map<String, String>> ChangedFields = new HashMap<String, Map<String, String>>();
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
            Map<String, String> NewFields = new HashMap<String, String>();
            if (cg.isAbstract()) continue;
            for (Field field : cg.getFields()) {
                if (field.isInterface() || field.isAbstract())
                    continue;
                FieldGen fg = new FieldGen(field, cg.getConstantPool());
                String newName = Misc.getRandomString(20);
                fg.setName(newName);
                cg.replaceField(field, fg.getField());
                NewFields.put(getBuffered(field.getName(), field.getSignature()), getBuffered(fg.getName(), fg.getSignature()));
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
                    if (handle.getInstruction() instanceof GETSTATIC) {
                        GETSTATIC get = (GETSTATIC) handle.getInstruction();
                        String clazz = get.getClassName(cg.getConstantPool());
                        String fname = get.getFieldName(cg.getConstantPool());
                        String fsig = get.getSignature(cg.getConstantPool());

                        if (!ChangedFields.containsKey(clazz)) continue;
                        Map<String, String> classFields = ChangedFields.get(clazz);
                        if (classFields.get(getBuffered(fname, fsig)) != null) {
                            String newname = classFields.get(getBuffered(fname, fsig)).split(Settings.TempBuffer)[0];
                            GETSTATIC newget = new GETSTATIC(cg.getConstantPool().addFieldref(clazz, newname, fsig));
                            handle.setInstruction(newget);
                        }
                    } else if (handle.getInstruction() instanceof GETFIELD) {
                        GETFIELD get = (GETFIELD) handle.getInstruction();
                        String clazz = get.getClassName(cg.getConstantPool());
                        String fname = get.getFieldName(cg.getConstantPool());
                        String fsig = get.getSignature(cg.getConstantPool());

                        if (!ChangedFields.containsKey(clazz)) continue;
                        Map<String, String> classFields = ChangedFields.get(clazz);
                        if (classFields.get(getBuffered(fname, fsig)) != null) {
                            String newname = classFields.get(getBuffered(fname, fsig)).split(Settings.TempBuffer)[0];
                            GETFIELD newget = new GETFIELD(cg.getConstantPool().addFieldref(clazz, newname, fsig));
                            handle.setInstruction(newget);
                        }
                    } else if (handle.getInstruction() instanceof PUTSTATIC) {
                        PUTSTATIC put = (PUTSTATIC) handle.getInstruction();
                        String clazz = put.getClassName(cg.getConstantPool());
                        String fname = put.getFieldName(cg.getConstantPool());
                        String fsig = put.getSignature(cg.getConstantPool());

                        if (!ChangedFields.containsKey(clazz)) continue;
                        Map<String, String> classFields = ChangedFields.get(clazz);
                        if (classFields.get(getBuffered(fname, fsig)) != null) {
                            String newname = classFields.get(getBuffered(fname, fsig)).split(Settings.TempBuffer)[0];
                            PUTSTATIC newput = new PUTSTATIC(cg.getConstantPool().addFieldref(clazz, newname, fsig));
                            handle.setInstruction(newput);
                        }
                    } else if (handle.getInstruction() instanceof PUTFIELD) {
                        PUTFIELD put = (PUTFIELD) handle.getInstruction();
                        String clazz = put.getClassName(cg.getConstantPool());
                        String fname = put.getFieldName(cg.getConstantPool());
                        String fsig = put.getSignature(cg.getConstantPool());

                        if (!ChangedFields.containsKey(clazz)) continue;
                        Map<String, String> classFields = ChangedFields.get(clazz);
                        if (classFields.get(getBuffered(fname, fsig)) != null) {
                            String newname = classFields.get(getBuffered(fname, fsig)).split(Settings.TempBuffer)[0];
                            PUTFIELD newput = new PUTFIELD(cg.getConstantPool().addFieldref(clazz, newname, fsig));
                            handle.setInstruction(newput);
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

    String getBuffered(String name, String sig) {
        return name + Settings.TempBuffer + sig;
    }

    public void Dump() {
        LoadedJar.Save(Location.replace(".jar", "-new.jar"));
    }
}
