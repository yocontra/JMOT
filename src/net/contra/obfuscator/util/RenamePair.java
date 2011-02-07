package net.contra.obfuscator.util;

/**
 * Created by IntelliJ IDEA.
 * User: Contra
 * Date: 2/7/11
 * Time: 11:33 AM
 */
public class RenamePair {
    public String OldName;
    public String OldSignature;
    public String NewName;

    public RenamePair(String oldName, String oldSig, String newName){
        OldName = oldName;
        OldSignature = oldSig;
        NewName = newName;
    }
    public String[] GetNew(){
        return new String[] { NewName, OldSignature };
    }
    public String[] GetOld(){
        return new String[] { OldName, OldSignature };
    }
}
