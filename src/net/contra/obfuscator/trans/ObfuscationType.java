package net.contra.obfuscator.trans;


public enum ObfuscationType {
    Light(1), Normal(2), Heavy(3), Insane(4);

    private final int level;

    private ObfuscationType(int lev) {
        level = lev;
    }

    public int getLevel() {
        return level;
    }
}
