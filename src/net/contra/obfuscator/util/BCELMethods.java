package net.contra.obfuscator.util;

import com.sun.org.apache.bcel.internal.generic.*;

public class BCELMethods {


    public static boolean isFieldInvoke(Instruction ins) {
        if (ins instanceof GETSTATIC) {
            return true;
        } else if (ins instanceof PUTSTATIC) {
            return true;
        } else if (ins instanceof GETFIELD) {
            return true;
        } else if (ins instanceof PUTFIELD) {
            return true;
        } else {
            return false;
        }
    }

    public static String getFieldInvokeName(Instruction ins, ConstantPoolGen cp) {
        if (ins instanceof GETSTATIC) {
            return (((GETSTATIC) ins).getFieldName(cp));
        } else if (ins instanceof PUTSTATIC) {
            return (((PUTSTATIC) ins).getFieldName(cp));
        } else if (ins instanceof GETFIELD) {
            return (((GETFIELD) ins).getFieldName(cp));
        } else if (ins instanceof PUTFIELD) {
            return (((PUTFIELD) ins).getFieldName(cp));
        } else {
            return null;
        }
    }

    public static String getFieldInvokeClassName(Instruction ins, ConstantPoolGen cp) {
        if (ins instanceof GETSTATIC) {
            return (((GETSTATIC) ins).getClassName(cp));
        } else if (ins instanceof PUTSTATIC) {
            return (((PUTSTATIC) ins).getClassName(cp));
        } else if (ins instanceof GETFIELD) {
            return (((GETFIELD) ins).getClassName(cp));
        } else if (ins instanceof PUTFIELD) {
            return (((PUTFIELD) ins).getClassName(cp));
        } else {
            return null;
        }
    }

    public static String getFieldInvokeSignature(Instruction ins, ConstantPoolGen cp) {
        if (ins instanceof GETSTATIC) {
            return (((GETSTATIC) ins).getSignature(cp));
        } else if (ins instanceof PUTSTATIC) {
            return (((PUTSTATIC) ins).getSignature(cp));
        } else if (ins instanceof GETFIELD) {
            return (((GETFIELD) ins).getSignature(cp));
        } else if (ins instanceof PUTFIELD) {
            return (((PUTFIELD) ins).getSignature(cp));
        } else {
            return null;
        }
    }

    public static Instruction getNewFieldInvoke(Instruction ins, int index) {
        if (ins instanceof GETSTATIC) {
            return new GETSTATIC(index);
        } else if (ins instanceof PUTSTATIC) {
            return new PUTSTATIC(index);
        } else if (ins instanceof GETFIELD) {
            return new GETFIELD(index);
        } else if (ins instanceof PUTFIELD) {
            return new PUTFIELD(index);
        } else {
            return null;
        }
    }

    public static boolean isInvoke(Instruction ins) {
        if (ins instanceof INVOKESTATIC) {
            return true;
        } else if (ins instanceof INVOKEVIRTUAL) {
            return true;
        } else if (ins instanceof INVOKEINTERFACE) {
            return true;
        } else if (ins instanceof INVOKESPECIAL) {
            return true;
        } else {
            return false;
        }
    }

    public static String getInvokeMethodName(Instruction ins, ConstantPoolGen cp) {
        if (ins instanceof INVOKESTATIC) {
            INVOKESTATIC invst = (INVOKESTATIC) ins;
            return invst.getMethodName(cp);
        } else if (ins instanceof INVOKEVIRTUAL) {
            INVOKEVIRTUAL invst = (INVOKEVIRTUAL) ins;
            return invst.getMethodName(cp);
        } else if (ins instanceof INVOKEINTERFACE) {
            INVOKEINTERFACE invst = (INVOKEINTERFACE) ins;
            return invst.getMethodName(cp);
        } else if (ins instanceof INVOKESPECIAL) {
            INVOKESPECIAL invst = (INVOKESPECIAL) ins;
            return invst.getMethodName(cp);
        } else {
            return null;
        }
    }

    public static Instruction getNewInvoke(Instruction ins, int index) {
        if (ins instanceof INVOKESTATIC) {
            return new INVOKESTATIC(index);
        } else if (ins instanceof INVOKEVIRTUAL) {
            return new INVOKEVIRTUAL(index);
        } else if (ins instanceof INVOKEINTERFACE) {
            INVOKEINTERFACE newInv = ((INVOKEINTERFACE) ins);
            newInv.setIndex(index);
            return newInv;
        } else if (ins instanceof INVOKESPECIAL) {
            return new INVOKESPECIAL(index);
        } else {
            return null;
        }
    }

    public static String getInvokeSignature(Instruction ins, ConstantPoolGen cp) {
        if (ins instanceof INVOKESTATIC) {
            INVOKESTATIC invst = (INVOKESTATIC) ins;
            return invst.getSignature(cp);
        } else if (ins instanceof INVOKEVIRTUAL) {
            INVOKEVIRTUAL invst = (INVOKEVIRTUAL) ins;
            return invst.getSignature(cp);
        } else if (ins instanceof INVOKEINTERFACE) {
            INVOKEINTERFACE invst = (INVOKEINTERFACE) ins;
            return invst.getSignature(cp);
        } else if (ins instanceof INVOKESPECIAL) {
            INVOKESPECIAL invst = (INVOKESPECIAL) ins;
            return invst.getSignature(cp);
        } else {
            return null;
        }
    }

    public static String getInvokeClassName(Instruction ins, ConstantPoolGen cp) {
        if (ins instanceof INVOKESTATIC) {
            INVOKESTATIC invst = (INVOKESTATIC) ins;
            return invst.getClassName(cp);
        } else if (ins instanceof INVOKEVIRTUAL) {
            INVOKEVIRTUAL invst = (INVOKEVIRTUAL) ins;
            return invst.getClassName(cp);
        } else if (ins instanceof INVOKEINTERFACE) {
            INVOKEINTERFACE invst = (INVOKEINTERFACE) ins;
            return invst.getClassName(cp);
        } else if (ins instanceof INVOKESPECIAL) {
            INVOKESPECIAL invst = (INVOKESPECIAL) ins;
            return invst.getClassName(cp);
        } else {
            return null;
        }
    }
}
