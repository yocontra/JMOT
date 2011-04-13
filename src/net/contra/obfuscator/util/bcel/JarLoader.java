package net.contra.obfuscator.util.bcel;

import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.generic.ClassGen;
import net.contra.obfuscator.util.misc.IO;

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

    public final Map<String, ClassGen> ClassEntries = new HashMap<String, ClassGen>();
    public final Map<String, byte[]> NonClassEntries = new HashMap<String, byte[]>();

    public JarLoader(String fileLocation) {
        try {
            File file = new File(fileLocation);
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            if (jarFile.getManifest() != null) {
                wipeManifest(jarFile.getManifest().getMainAttributes().getValue("Main-Class"));
            }
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry == null) continue;
                InputStream entryStream = jarFile.getInputStream(entry);
                if (entry.getName().endsWith(".class")) {
                    JavaClass jc = new ClassParser(entryStream, entry.getName()).parse();
                    ClassEntries.put(jc.getClassName(), new ClassGen(jc));
                } else {
                    NonClassEntries.put(entry.getName(), IO.getBytes(entryStream));
                }
            }
        } catch (Exception e) {
            System.out.println("Error Loading Jar! Location: " + fileLocation);
            e.printStackTrace();
        }
    }

    private void wipeManifest(String main) {
        for (String n : NonClassEntries.keySet()) {
            if (n.startsWith("META-INF/")) {
                if (n.endsWith("MANIFEST.MF")) {
                    String nm = "Main-Class: " + main;
                    NonClassEntries.put(n, nm.getBytes());
                }
            } else {
                NonClassEntries.put(n, null);
            }
        }
    }

    public void saveJar(String fileName) {
        try {
            FileOutputStream os = new FileOutputStream(fileName);
            JarOutputStream jos = new JarOutputStream(os);
            for (ClassGen classIt : ClassEntries.values()) {
                jos.putNextEntry(new JarEntry(classIt.getClassName().replace('.', '/') + ".class"));
                jos.write(classIt.getJavaClass().getBytes());
                jos.closeEntry();
                jos.flush();
            }
            for (String n : NonClassEntries.keySet()) {
                JarEntry destEntry = new JarEntry(n);
                byte[] bite = NonClassEntries.get(n);
                if (bite != null) {
                    jos.putNextEntry(destEntry);
                    jos.write(bite);
                    jos.closeEntry();
                }
            }
            jos.closeEntry();
            jos.close();
        } catch (Exception e) {
            System.out.println("Error Saving Jar! Location: " + fileName);
            e.printStackTrace();
        }
    }
}
