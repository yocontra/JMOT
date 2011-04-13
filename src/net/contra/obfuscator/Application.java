package net.contra.obfuscator;

import net.contra.obfuscator.trans.deob.AllatoriDeobfuscator;
import net.contra.obfuscator.trans.ob.*;
import net.contra.obfuscator.util.misc.LogHandler;

public class Application {
    private static final LogHandler Logger = new LogHandler("Application");

    public static void main(String[] args) {
        Logger.Log(String.format("JMOT v%s by Contra", Settings.VERSION));
        Logger.Log("Visit RECoders.org for Info");
        Logger.Log("Please read LICENSE.txt for licensing information.");
        if (args.length < 2) {
            Logger.Error("Please provide at least two arguments!");
            return;
        }
        Logger.Log("Running with Obfuscation Level: " + Settings.OBFUSCATION_LEVEL.getName());
        SetParameters();
        Logger.Log("Beginning Process");
        try {
            String cmd = args[1];
            ITransformer obber;
            //Obfuscation Stuff
            if (cmd.equalsIgnoreCase("all")) {
                //TODO: UNGHETTO THIS, THIS IS AWFUL
                return;
            } else if (cmd.equalsIgnoreCase("string")) {
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
            //Deobfuscation stuff
            } else if (cmd.equalsIgnoreCase("allatori")) {
                obber = new AllatoriDeobfuscator(args[0]);
            } else {
                Logger.Error("Please provide a proper transformer identifier!");
                return;
            }
            obber.logger.Log("Loading JarFile. Target: " + args[0]);
            obber.load();
            obber.logger.Log("Transforming Classes");
            obber.transform();
            obber.logger.Log("Saving JarFile");
            obber.save();
        } catch (Exception e) {
            Logger.Error("Error Completing Obfuscation!");
            e.printStackTrace();
            return;
        }
        Logger.Log("Process Completed!");
    }

    public static void SetParameters() {
        switch (Settings.OBFUSCATION_LEVEL) {
            case Light:
                Settings.CIPHER_KEYS = new int[]{127};
                Settings.ITERATIONS = 0;
                break;
            case Normal:
                Settings.CIPHER_KEYS = new int[]{81, 127};
                Settings.ITERATIONS = 1;
                break;
            case Heavy:
                Settings.CIPHER_KEYS = new int[]{85, 127, 200};
                Settings.ITERATIONS = 3;
                break;
            case Insane:
                Settings.CIPHER_KEYS = new int[]{11, 22, 33, 44, 55, 66, 77, 88};
                Settings.ITERATIONS = 15;
                break;
            default:
                Settings.CIPHER_KEYS = new int[]{127};
                Settings.ITERATIONS = 0;
                break;
        }
    }

    public static void Close(){
        System.out.println("Application is closing...");
        System.exit(1337);
    }
}
