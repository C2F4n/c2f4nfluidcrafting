package github.C2F4n.common.tile.component.config;

/** 传输类型：流体页与物品页各一套面配置。 */
public enum TransmissionType {
    FLUID,
    ITEM;

    public DataType[] getSupportedDataTypes() {
        return this == FLUID
              ? new DataType[]{DataType.NONE, DataType.FLUID_IN_1, DataType.FLUID_IN_2, DataType.FLUID_IN_3, DataType.FLUID_OUT}
              : new DataType[]{DataType.NONE, DataType.ITEM_IN, DataType.ITEM_OUT, DataType.EMPTY_OUT};
    }
}
