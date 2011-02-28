package net.contra.obfuscator.trans;

import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
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

public class FieldNameObfuscator implements ITransformer {
    private final LogHandler Logger = new LogHandler("FieldNameObfuscator");
    //ClassName, <OldSig, NewSig>
    private final Map<String, ArrayList<RenamedPair>> ChangedFields = new HashMap<String, ArrayList<RenamedPair>>();
    private String Location = "";
    private JarLoader LoadedJar;

    public FieldNameObfuscator(String loc) {
        Location = loc;
    }

    public void load() {
        LoadedJar = new JarLoader(Location);
    }

    public void transform() {
        //We rename methods
        for (ClassGen cg : LoadedJar.ClassEntries.values()) {
            ArrayList<RenamedPair> NewFields = new ArrayList<RenamedPair>();
            if (cg.isAbstract()) continue;
            for (Field field : cg.getFields()) {
                if (field.isInterface() || field.isAbstract())
                    continue;
                FieldGen fg = new FieldGen(field, cg.getConstantPool());
                String newName = Misc.getRandomName();
                fg.setName(newName);
                cg.replaceField(field, fg.getField());
                RenamedPair newPair = new RenamedPair(field.getName(), field.getSignature(), fg.getName());
                NewFields.add(newPair);
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
                        String clazz = BCELMethods.getFieldInvokeClassName(handle.getInstruction(), cg.getConstantPool()).trim().replace(" ", "");
                        String fname = BCELMethods.getFieldInvokeName(handle.getInstruction(), cg.getConstantPool()).trim().replace(" ", "");
                        String fsig = BCELMethods.getFieldInvokeSignature(handle.getInstruction(), cg.getConstantPool()).trim().replace(" ", "");

                        if (!ChangedFields.containsKey(clazz)) continue;
                        for (RenamedPair pair : ChangedFields.get(clazz)) {
                            if (pair.OldName.equals(fname) && pair.OldSignature.equals(fsig)) {
                                int index = cg.getConstantPool().addFieldref(clazz, pair.NewName, fsig);
                                handle.setInstruction(BCELMethods.getNewFieldInvoke(handle.getInstruction(), index));
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

    public String save() {
        String loc = Location.replace(".jar", Settings.FileTag + ".jar");
        LoadedJar.saveJar(loc);
        return loc;
    }
}
