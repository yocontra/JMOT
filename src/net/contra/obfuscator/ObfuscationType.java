package net.contra.obfuscator;


public enum ObfuscationType {
    Light(1, "Light"), Normal(2, "Normal"), Heavy(3, "Heavy"), Insane(4, "Insane");

    private final int level;
    private final String name;

    private ObfuscationType(int lev, String id) {
        level = lev;
        name = id;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }
}
