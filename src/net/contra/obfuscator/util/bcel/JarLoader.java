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
                    NonClassEntries.put(entry.getName(), contents);
                }
            }
        } catch (Exception e) {
            System.out.println("Error Loading Jar! Location: " + fileLocation);
            e.printStackTrace();
        }
    }

    public String getMainClass() {
        for (String n : NonClassEntries.keySet()) {
            JarEntry destEntry = new JarEntry(n);
            byte[] bite = NonClassEntries.get(n);
            if (destEntry.getName().equals("META-INF/MANIFEST.MF")) {
                String[] man = new String(bite).split("\\r?\\n");
                for (String s : man) {
                    if (s.startsWith("Main-Class:")) {
                        //TODO: This should probably be changed, man[0] isn't always manifest-version
                        return man[0] + "\n" + s + "\n";
                    }
                }
            }
        }
        return null;
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
            for (String n : NonClassEntries.keySet()) {
                JarEntry destEntry = new JarEntry(n);
                byte[] bite = NonClassEntries.get(n);
                //Well we don't want anything besides the main-class in the manifest...
                if (destEntry.getName().equals("META-INF/MANIFEST.MF")) {
                    String newManifest = getMainClass();
                    if (newManifest != null) {
                        destEntry = new JarEntry(destEntry.getName());
                        bite = newManifest.getBytes();
                    }
                }
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
