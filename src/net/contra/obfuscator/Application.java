package net.contra.obfuscator;

import net.contra.obfuscator.trans.AttributeObfuscator;
import net.contra.obfuscator.trans.ITransformer;
import net.contra.obfuscator.trans.MethodNameObfuscator;
import net.contra.obfuscator.trans.StringObfuscator;
import net.contra.obfuscator.util.LogHandler;

public class Application {
    static LogHandler Logger = new LogHandler("Application");

    public static void main(String[] args) {
        Logger.Log(String.format("JMO v%s by Contra", Settings.Version));
        Logger.Log("Visit RECoders.org for Info");
        if (args.length < 2) {
            Logger.Error("Please provide the proper arguments!");
            return;
        }
        Logger.Log("Beginning Obfuscation");
        try {
            String cmd = args[1];
            ITransformer obber;
            if(cmd.equalsIgnoreCase("string")){
                obber = new StringObfuscator(args[0]);
            } else if (cmd.equalsIgnoreCase("attribute")) {
                obber = new AttributeObfuscator(args[0]);
            } else if (cmd.equalsIgnoreCase("method-name")) {
                obber = new MethodNameObfuscator(args[0]);
            } else {
                Logger.Error("Please provide a proper transformer identifier");
                return;
            }
            obber.Logger.Log("Loading JarFile. Target: " + args[0]);
            obber.Load();
            obber.Logger.Log("Transforming Classes");
            obber.Transform();
            obber.Logger.Log("Saving JarFile");
            obber.Dump();
        } catch (Exception e) {
            Logger.Error("Error Completing Obfuscation!");
            e.printStackTrace();
            return;
        }
        Logger.Log("Process Completed!");
    }
}
