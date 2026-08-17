package github.C2F4n.common.tile.component.config;

/** 每个面的功能模式（替代旧 FaceMode），颜色用于六面染色。 */
public enum DataType {
    NONE("none", 0xAA8C8C8C),

    FLUID_IN_1("fluid_in_1", 0xFF50E69B),
    FLUID_IN_2("fluid_in_2", 0xFFFFFF33),
    FLUID_IN_3("fluid_in_3", 0xFF47CBE6),
    FLUID_OUT("fluid_out", 0xFFB33636),

    ITEM_IN("item_in", 0xFF50E69B),
    ITEM_OUT("item_out", 0xFFB33636),
    EMPTY_OUT("empty_out", 0xFFE57A2E);

    private final String name;
    private final int color;

    DataType(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public boolean isFluidMode() {
        return this == FLUID_IN_1 || this == FLUID_IN_2 || this == FLUID_IN_3 || this == FLUID_OUT;
    }

    public boolean isItemMode() {
        return this == ITEM_IN || this == ITEM_OUT || this == EMPTY_OUT;
    }

    public static DataType cycleFluid(DataType current, boolean reverse) {
        return cycle(new DataType[]{NONE, FLUID_IN_1, FLUID_IN_2, FLUID_IN_3, FLUID_OUT}, current, reverse);
    }

    public static DataType cycleItem(DataType current, boolean reverse) {
        return cycle(new DataType[]{NONE, ITEM_IN, ITEM_OUT, EMPTY_OUT}, current, reverse);
    }

    private static DataType cycle(DataType[] values, DataType current, boolean reverse) {
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) {
                index = i;
                break;
            }
        }
        index = reverse ? (index - 1 + values.length) % values.length : (index + 1) % values.length;
        return values[index];
    }
}
