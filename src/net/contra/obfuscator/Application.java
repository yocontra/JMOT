package net.contra.obfuscator;

import net.contra.obfuscator.trans.Obfuscator;
import net.contra.obfuscator.util.LogHandler;

public class Application {
    static LogHandler Logger = new LogHandler("Application");

    public static void main(String[] args) {
        Logger.Log(String.format("JMO v%s by Contra", Constants.Version));
        Logger.Log("Visit RECoders.org for Info");
        if (args.length < 1) {
            Logger.Error("Please provide the proper arguments!");
            return;
        }
        Logger.Log("Beginning Obfuscation");
        try {
            Obfuscator ob = new Obfuscator(args[0]);
            ob.Logger.Log("Loading JarFile. Target: " + args[0]);
            ob.Load();
            ob.Logger.Log("Transforming Classes");
            ob.Transform();
            ob.Logger.Log("Saving JarFile");
            ob.Dump("ob");
        } catch (Exception e) {
            Logger.Error("Error Completing Obfuscation!");
            e.printStackTrace();
            return;
        }
        Logger.Log("Process Completed!");
    }
}
