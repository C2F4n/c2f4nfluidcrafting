package github.C2F4n.block.entity;

public enum MiningMode {
    KEEP_NBT("keep_nbt"),
    DROP_ITEMS("drop_items");

    private final String name;

    MiningMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MiningMode byIndex(int index) {
        MiningMode[] values = values();
        return index >= 0 && index < values.length ? values[index] : KEEP_NBT;
    }
}
