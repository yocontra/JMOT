package net.contra.obfuscator.util;

import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.generic.ClassGen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarLoader {

    public Map<String, ClassGen> ClassEntries = new HashMap<String, ClassGen>();
    public Map<JarEntry, byte[]> NonClassEntries = new HashMap<JarEntry, byte[]>();

    public JarLoader(String fileLocation) {
        try {
            File file = new File(fileLocation);
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry == null) {
                    continue;
                }
                InputStream entryStream = jarFile.getInputStream(entry);

                if (entry.getName().endsWith(".class")) {
                    JavaClass jc = new ClassParser(entryStream, entry.getName()).parse();
                    ClassGen cg = new ClassGen(jc);
                    ClassEntries.put(cg.getClassName(), cg);
                } else {
                    byte[] contents = IO.GetBytes(entryStream);
                    NonClassEntries.put(entry, contents);
                }
            }
        } catch (Exception e) {
            System.out.println("Error Loading Jar! Location: " + fileLocation);
            e.printStackTrace();
        }
    }

    public void Save(String fileName) {
        try {
            FileOutputStream os = new FileOutputStream(fileName);
            JarOutputStream jos = new JarOutputStream(os);
            for (ClassGen classIt : ClassEntries.values()) {
                jos.putNextEntry(new JarEntry(classIt.getClassName().replace('.', File.separatorChar) + ".class"));
                jos.write(classIt.getJavaClass().getBytes());
                jos.closeEntry();
                jos.flush();
            }
            for (JarEntry jbe : NonClassEntries.keySet()) {
                JarEntry destEntry = new JarEntry(jbe.getName());
                byte[] bite = NonClassEntries.get(jbe);
                jos.putNextEntry(destEntry);
                jos.write(bite);
                jos.closeEntry();
            }
            jos.closeEntry();
            jos.close();
        } catch (Exception e) {
            System.out.println("Error Saving Jar! Location: " + fileName);
            e.printStackTrace();
        }
    }
}
