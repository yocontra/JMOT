package net.contra.obfuscator;

import net.contra.obfuscator.trans.*;
import net.contra.obfuscator.util.misc.LogHandler;

public class Application {
    private static final LogHandler Logger = new LogHandler("Application");

    public static void main(String[] args) {
        Logger.Log(String.format("JMOT v%s by Contra", Settings.Version));
        Logger.Log("Visit RECoders.org for Info");
        if (args.length < 2) {
            Logger.Error("Please provide at least two arguments!");
            return;
        }
        Logger.Log("Running with Obfuscation Level: " + Settings.ObfuscationLevel.getName());
        SetParameters();
        Logger.Log("Beginning Obfuscation");
        try {
            String cmd = args[1];
            ITransformer obber;
            if (cmd.equalsIgnoreCase("string")) {
                obber = new StringObfuscator(args[0]);
            } else if (cmd.equalsIgnoreCase("attribute")) {
                obber = new AttributeObfuscator(args[0]);
            } else if (cmd.equalsIgnoreCase("class-name")) {
                obber = new ClassNameObfuscator(args[0]);
            } else if (cmd.equalsIgnoreCase("method-name")) {
                obber = new MethodNameObfuscator(args[0]);
            } else if (cmd.equalsIgnoreCase("field-name")) {
                obber = new FieldNameObfuscator(args[0]);
            } else if (cmd.equalsIgnoreCase("int-complicate")) {
                obber = new IntegerComplicator(args[0]);
            } else if (cmd.equalsIgnoreCase("int-boxer")) {
                obber = new IntegerBoxer(args[0]);
            } else {
                Logger.Error("Please provide a proper transformer identifier!");
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

    public static void SetParameters() {
        switch (Settings.ObfuscationLevel) {
            case Light:
                Settings.CipherKeys = new int[]{127};
                Settings.Iterations = 0;
                break;
            case Normal:
                Settings.CipherKeys = new int[]{81, 127};
                Settings.Iterations = 1;
                break;
            case Heavy:
                Settings.CipherKeys = new int[]{85, 127, 200};
                Settings.Iterations = 3;
                break;
            case Insane:
                Settings.CipherKeys = new int[]{1, 2, 3, 4, 5, 89, 85, 127};
                Settings.Iterations = 15;
                break;
            default:
                Settings.CipherKeys = new int[]{127};
                Settings.Iterations = 0;
                break;
        }
    }
}
