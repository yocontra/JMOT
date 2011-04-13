package net.contra.obfuscator.util.misc;


public class RenamedPair {
    public final String OldName;
    public final String OldSignature;
    public final String NewName;

    public RenamedPair(String oldName, String oldSig, String newName) {
        OldName = oldName;
        OldSignature = oldSig;
        NewName = newName;
    }
}
